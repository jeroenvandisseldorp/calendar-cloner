package com.dizzl.android.CalendarCloner;

import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import android.content.ContentValues;
import android.provider.CalendarContract.Events;

import com.dizzl.android.CalendarCloner.CalendarLoader.CalendarInfo;

public class EventAggregator extends EventProcessor {
	private final EventDiffer mEventDiffer;
	private final boolean mInvertPeriods;
	private final Period mPeriod;
	private Periods mPeriods = new Periods();

	private final EventsTable mEventsTable;
	private final CloneIndex mExistingClones;
	private final CloneIdIndex mValidClones = new CloneIdIndex();

	public EventAggregator(Rule rule) {
		super(rule);
		mEventsTable = new EventsTable(ClonerApp.getDb(rule.isReadOnly()));
		mExistingClones = new CloneIndex(mEventsTable);

		CalendarInfo info = CalendarLoader.getCalendarByRef(mRule.getDstCalendarRef());
		DateTimeZone tz = info.getCalendar() != null ? info.getCalendar().getTimeZone() : null;
		DateTime startOfToday = new DateTime(tz).withTime(0, 0, 0, 0);
		DateTime start = startOfToday.minus(mRule.getSyncPeriodBefore()).withTime(0, 0, 0, 0);
		DateTime end = startOfToday.plus(mRule.getSyncPeriodAfter()).plusDays(1).withTime(0, 0, 0, 0);
		mPeriod = new Period(start, end);
		mInvertPeriods = false;

		mEventDiffer = new EventDiffer(tz);
	}

	public Period getPeriod() {
		return mPeriod.clone();
	}

	@Override
	protected EventResult processSingleEvent(Event event) {
		EventResult result = super.processSingleEvent(event);
		if (result.completed) {
			Period eventPeriod = event.getPeriod();
			mPeriods.merge(new Period(eventPeriod.getStart().minusMinutes(mRule.getReserveBefore()), eventPeriod
					.getEnd().plusMinutes(mRule.getReserveAfter())));
			this.logSummary(ClonerLog.LOG_INFO, ClonerApp.translate(R.string.cloner_log_processed), event);
		}

		return result;
	}

	@Override
	public InitResult init(ClonerLog log) {
		InitResult result = super.init(log);
		if (!result.isSuccess()) {
			return result;
		}

		// Prepare index for clone matching
		CloneIndex.IndexResult indexResult = mExistingClones.index(mDstCalendar.getId(), mRule.getHash(),
				mRule.getSrcCalendarHash(), log);
		if (!indexResult.isSuccess()) {
			return new Processor.InitResult(false, indexResult.getErrorMessage(), result.getUpdateCount()
					+ indexResult.getUpdateCount());
		}

		return new Processor.InitResult(true, "", result.getUpdateCount() + indexResult.getUpdateCount());
	}

	private void invertPeriods() {
		Periods neg = new Periods();
		neg.merge(mPeriod);
		for (Period p : mPeriods.getPeriods()) {
			neg.subtract(p);
		}
		mPeriods = neg;
	}

	private boolean insertAggregateEvent(Period p, ClonerLog log, String logPrefix) {
		boolean updated = false;
		// Initialize the logging context
		this.initLogLines(log, logPrefix);

		// Create the in-memory event
		MemoryEvent event = new MemoryEvent(mDstCalendar.getTimeZone());
		event.setUniqueId("Aggr@" + Utilities.dateTimeToTimeString(p.getStart()));
		event.setStartTime(p.getStart());
		event.setEndTime(p.getEnd());
		event.setTimeZone(mDstCalendar.getTimeZone());
		// event.setTimezone("UTC");
		event.setTitle(mRule.getCustomTitle());
		event.setLocation(mRule.getCustomLocation());
		event.setDescription(EventMarker.markEventDescription(mRule.getCustomDescription(), EventMarker.TYPE_CLONE,
				mRule.getHash(), event.getUniqueId()));

		// Set proper access level
		switch (mRule.getCustomAccessLevel()) {
		case Rule.CUSTOM_ACCESS_LEVEL_PRIVATE:
			event.setAccessLevel(Events.ACCESS_PRIVATE);
			break;
		case Rule.CUSTOM_ACCESS_LEVEL_CONFIDENTIAL:
			event.setAccessLevel(Events.ACCESS_CONFIDENTIAL);
			break;
		case Rule.CUSTOM_ACCESS_LEVEL_PUBLIC:
			event.setAccessLevel(Events.ACCESS_PUBLIC);
			break;
		default:
			event.setAccessLevel(Events.ACCESS_DEFAULT);
		}

		// Set both regular and Samsung's proprietary availability
		switch (mRule.getCustomAvailability()) {
		case Rule.CUSTOM_AVAILABILITY_FREE:
			event.setAvailability(Events.AVAILABILITY_FREE);
			event.setAvailabilitySamsung(Device.Samsung.AVAILABILITY_FREE);
			break;
		case Rule.CUSTOM_AVAILABILITY_OUT_OF_OFFICE:
			event.setAvailability(Events.AVAILABILITY_BUSY);
			event.setAvailabilitySamsung(Device.Samsung.AVAILABILITY_OUT_OF_OFFICE);
			break;
		case Rule.CUSTOM_AVAILABILITY_BUSY:
		default:
			event.setAvailability(Events.AVAILABILITY_BUSY);
			event.setAvailabilitySamsung(Device.Samsung.AVAILABILITY_BUSY);
			break;
		}

		// Look up an existing clone
		Event clone = mExistingClones.getCloneOf(event);
		if (clone == null) {
			clone = new DbEvent(mEventsTable, mDstCalendar);
		}
		long cloneId = clone.getId();
		ContentValues delta = new ContentValues();
		mEventDiffer.compare(event, clone, delta);
		if (delta.size() > 0) {
			if (clone.getId() == 0) {
				delta.put(mEventsTable.CALENDAR_ID.getName(), mDstCalendar.getId());
				cloneId = mEventsTable.insert(delta);
				if (cloneId > 0) {
					Event logClone = mEventsTable.getDb().isReadOnly() ? DbEvent.get(mEventsTable, cloneId, delta)
							: DbEvent.get(mEventsTable, cloneId);
					this.logEvent(logClone, delta);
				}
				this.logSummary(ClonerLog.LOG_UPDATE, ClonerApp.translate(R.string.cloner_log_inserted), event);
			} else {
				mEventsTable.update(clone.getId(), delta);
				Event logClone = mEventsTable.getDb().isReadOnly() ? DbEvent.get(mEventsTable, cloneId, delta)
						: DbEvent.get(mEventsTable, cloneId);
				this.logEvent(logClone, delta);
				this.logSummary(ClonerLog.LOG_UPDATE, ClonerApp.translate(R.string.cloner_log_updated), event);
			}
			updated = true;
		} else {
			this.logSummary(ClonerLog.LOG_INFO, ClonerApp.translate(R.string.cloner_state_unchanged), event);
		}
		mValidClones.add(event, cloneId);
		return updated;
	}

	@Override
	public EventResult roundup(ClonerLog log) {
		log.log(log.createLogLine(ClonerLog.LOG_INFO, null,
				ClonerApp.translate(R.string.cloner_process_generating_aggregate_events)), null);

		EventResult result = new EventResult();
		if (mInvertPeriods) {
			this.invertPeriods();
		}

		int eventNr = 0;
		for (Period eventPeriod : mPeriods.getPeriods()) {
			if (!eventPeriod.isNull()) {
				DateTime startOfDay = eventPeriod.getStart().withZone(mDstCalendar.getTimeZone()).withTime(0, 0, 0, 0);
				DateTime startOfEvent = eventPeriod.getStart();

				// For multiple day periods, create events that run to midnight
				while (eventPeriod.getEnd().isAfter(startOfDay.plusDays(1))) {
					if (this.insertAggregateEvent(new Period(startOfEvent, startOfDay.plusDays(1)), log, "" + ++eventNr)) {
						result.updateCount++;
					}
					startOfDay = startOfDay.plusDays(1);
					startOfEvent = startOfDay;
				}

				// Create the aggregate event for the last day of the period
				if (this.insertAggregateEvent(new Period(startOfEvent, eventPeriod.getEnd()), log, "" + ++eventNr)) {
					result.updateCount++;
				}
			}
		}

		Set<Long> cloneIds = this.getRemainingCloneIds();

		for (long cloneId : cloneIds) {
			eventNr++;
			Event clone = DbEvent.get(mEventsTable, cloneId);
			if (clone != null) {
				if (!clone.isDeleted()) {
					if (this.shouldDeleteClone(clone)) {
						this.initLogLines(log, "" + eventNr);
						// Check to see if we can delete within limits
						if (!Limits.canModify(Limits.TYPE_EVENT)) {
							this.log(ClonerLog.LOG_WARNING,
									ClonerApp.translate(R.string.cloner_log_event_limit_reached));
							result.completed = false;
							return result;
						}

						// The event must be deleted, because it represents
						// a clone of an already deleted event from this
						// calendar
						mEventsTable.delete(clone.getId());
						this.logSummary(ClonerLog.LOG_UPDATE, ClonerApp.translate(R.string.cloner_log_deleted), clone);
						result.updateCount++;
					} else {
						// The event is a valid clone
						this.logSummary(ClonerLog.LOG_INFO, ClonerApp.translate(R.string.cloner_log_skipped), clone,
								ClonerApp.translate(R.string.cloner_log_valid_clone));
					}
				} else {
					// The event is already being deleted
					this.logSummary(ClonerLog.LOG_INFO, ClonerApp.translate(R.string.cloner_log_skipped), clone,
							ClonerApp.translate(R.string.cloner_log_marked_deleted));
				}
			}
		}

		return result;
	}

	public Set<Long> getRemainingCloneIds() {
		// Return the list of unprocessed clones
		Set<Long> result = new HashSet<Long>();
		Set<Event> clones = mExistingClones.getAllClones();
		for (Event clone : clones) {
			if (!mValidClones.containsId(clone.getId())) {
				result.add(clone.getId());
			}
		}
		return result;
	}

	private boolean shouldDeleteClone(Event clone) {
		// Check if clone is a valid one, if not delete
		EventMarker.Marker marker = EventMarker.parseCloneEventHash(clone);
		if (marker != null) {
			if (mValidClones.containsHash(marker.eventHash)) {
				Long validCloneId = mValidClones.getCloneId(marker.eventHash);
				if (clone.getId() != validCloneId) {
					// Probably an overriden clone, so delete it
					return true;
				}
			}
		} else {
			// Should never reach here (invalidly marked as clone)
			return true;
		}

		// Valid clone that we don't need to retain
		return true;
	}
}
