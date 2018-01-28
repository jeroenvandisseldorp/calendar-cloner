package com.dizzl.android.CalendarCloner;

import android.content.ContentValues;
import android.provider.CalendarContract.Events;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class EventDiffer extends Differ {
	private static final long ONE_DAY = 24 * 3600 * 1000;
	private static final DateTimeZone UTC_ZONE = DateTimeZone.forID("UTC");
	private long mDefaultDuration;
	protected EventsTable mEventsTable = new EventsTable(ClonerApp.getDb(true));
	private final DateTimeZone calendarTimeZone;

	public EventDiffer(DateTimeZone calendarTimeZone) {
		this.calendarTimeZone = calendarTimeZone;
	}

	public void setDefaultDuration(long defaultDuration) {
		mDefaultDuration = defaultDuration;
	}

	private String toLocalMillis(DateTime time) {
		return "" + time.withZone(calendarTimeZone).getMillis();
	}

	private String toUTCMillis(DateTime time) {
		return ""
				+ new DateTime(time.year().get(), time.monthOfYear().get(), time.dayOfMonth().get(), 0, 0, UTC_ZONE)
						.getMillis();
	}

	protected void compareStartTime(Event event, Event clone, ContentValues delta) {
		// Sometimes calendar sync sets start time and end time of a canceled
		// meeting to the time of the first occurence. This kills certain syncs
		// though, for example Google sync deletes such events. The code below
		// prevents that.
		if (event.isRecurringEventException() && event.getStatus() == Events.STATUS_CANCELED) {
			// Only set the clone's start time if not initialized yet
			if (clone.getId() == 0) {
				DateTime startTime = event.getOriginalInstanceTime();
				this.compareField(mEventsTable.DTSTART, toLocalMillis(startTime), toLocalMillis(clone.getStartTime()),
						true, delta);
			}
			return;
		}

		if (!event.isAllDay()) {
			this.compareField(mEventsTable.DTSTART, toLocalMillis(event.getStartTime()),
					toLocalMillis(clone.getStartTime()), clone.getId() == 0, delta);
		} else {
			this.compareField(mEventsTable.DTSTART, toUTCMillis(event.getStartTime()),
					toUTCMillis(clone.getStartTime()), clone.getId() == 0, delta);
		}
	}

	protected void compareEndTime(Event event, Event clone, ContentValues delta) {
		if (event.isRecurringEventException() && event.getStatus() == Events.STATUS_CANCELED) {
			// Only set the clone's start time if not initialized yet
			if (clone.getEndTime().year().get() < 1980) {
				// For some reason Google sync does not accept all end times for
				// canceled exceptions of a recurring
				// event. It does seem to accept the end time when set to
				// originalStart plus its parent duration (ie.
				// the original slot), so that's what we simulate here.
				DateTime endTime = event.getOriginalInstanceTime().plus(mDefaultDuration);
				this.compareField(mEventsTable.DTEND, toLocalMillis(endTime), toLocalMillis(clone.getEndTime()), true,
						delta);
			}

			return;
		}

		if (!event.isAllDay()) {
			this.compareField(mEventsTable.DTEND, toLocalMillis(event.getEndTime()), toLocalMillis(clone.getEndTime()),
					clone.getId() == 0, delta);
		} else {
			this.compareField(mEventsTable.DTEND, toUTCMillis(event.getEndTime()), toUTCMillis(clone.getEndTime()),
					clone.getId() == 0, delta);
		}
	}

	protected void compareTimeZone(ClonerTable.Column field, DateTimeZone tzEvent, DateTimeZone tzClone,
			boolean mandatory, ContentValues delta) {
		if (tzEvent == null || tzClone == null || !tzEvent.equals(tzClone)) {
			this.compareField(field, tzEvent != null ? tzEvent.getID() : "", tzClone != null ? tzClone.getID() : "",
					mandatory, delta);
		}
	}

	protected void compareAvailability(Event event, Event clone, ContentValues delta) {
		// Availability is tricky, because on Samsung devices with Exchange
		// calendars the availability status is stored
		// in a non-standard field. To compensate for this case, we have to
		// check both source and destination event and
		// copy respective fields properly.
		if (ClonerApp.getDevice().supportsAvailabilitySamsung() && event.hasAvailabilitySamsung()) {
			// Since we are the organizer of the clone, we can not mark ourself
			// as TENTATIVE
			int eventAvailability = event.getAvailabilitySamsung();
			if (eventAvailability == Device.Samsung.AVAILABILITY_TENTATIVE) {
				eventAvailability = Device.Samsung.AVAILABILITY_BUSY;
			}
			// Copy Samsung field
			this.compareField(mEventsTable.AVAILABILITY_SAMSUNG, "" + eventAvailability,
					"" + clone.getAvailabilitySamsung(), clone.getId() == 0, delta);
			// Convert Samsung availability to regular availability
			this.compareField(mEventsTable.AVAILABILITY, "" + Device.Samsung.toRegularAvailability(eventAvailability),
					"" + clone.getAvailability(), clone.getId() == 0, delta);
		} else {
			// Since we are the organizer of the clone, we can not mark ourself
			// as TENTATIVE
			int eventAvailability = event.getAvailability();
			if (eventAvailability != Events.AVAILABILITY_FREE) {
				eventAvailability = Events.AVAILABILITY_BUSY;
			}
			// Copy regular availability field
			this.compareField(mEventsTable.AVAILABILITY, "" + eventAvailability, "" + clone.getAvailability(),
					clone.getId() == 0, delta);
			// Check if we need to set the Samsung availabilty field
			if (clone.hasAvailabilitySamsung()) {
				// Convert from regular availability to Samsung availability
				this.compareField(mEventsTable.AVAILABILITY_SAMSUNG,
						"" + Device.Samsung.fromRegularAvailability(eventAvailability),
						"" + clone.getAvailabilitySamsung(), clone.getId() == 0, delta);
			}
		}
	}

	protected boolean compareInts(int[] a, int[] b) {
		if ((a == null && b == null) || (a == null && b.length > 0) || (b == null && a.length > 0)
				|| (a.length == 0 && b.length == 0)) {
			return true;
		}
		if (a.length != b.length) {
			return false;
		}
		for (int i = 0; i < a.length; i++) {
			if (a[i] != b[i]) {
				return false;
			}
		}
		return true;
	}

	protected void compareStatus(Event event, Event clone, ContentValues delta) {
		// Compensate for Exchange appointments, which set eventStatus to null
		if (event.hasStatus()) {
			int eventStatus = event.getStatus() != Events.STATUS_CANCELED ? Events.STATUS_CONFIRMED
					: Events.STATUS_CANCELED;
			this.compareField(mEventsTable.STATUS, "" + eventStatus, "" + clone.getStatus(), !clone.hasStatus(), delta);
		}
	}

	protected void compareDuration(Event event, Event clone, ContentValues delta) {
		// For recurring events, calculate the duration in seconds and then
		// transpose to the destination calendar's
		// preferred units

		// Parse the event's duration
		String eventDuration = event.getDuration();
		if (eventDuration == null && event.isAllDay()) {
			eventDuration = Duration.encodeDuration(ONE_DAY);
		}
		long newDuration = Duration.parseDuration(eventDuration);
		// Parse the clone's duration
		long oldDuration = Duration.parseDuration(clone.getDuration());

		// Only copy if they are semantically different or clone needs to be
		// initialized
		if (newDuration != oldDuration || clone.getDuration() == null) {
			this.compareField(mEventsTable.DURATION, Duration.encodeDuration(newDuration), null, true, delta);
		}
	}

	protected void compareRecurrenceRule(ClonerTable.Column field, String eventRule, String cloneRule,
			ContentValues delta) {
		// Perform semantic comparison to allow for server rrule rewrite
		if (!RecurrenceRule.compareRules(eventRule, cloneRule)) {
			// If not equal, copy syntactically
			this.compareField(field, eventRule, cloneRule, true, delta);
		}
	}

	public void compare(Event event, Event clone, ContentValues delta) {
		// Compare regular event fields
		this.compareStatus(event, clone, delta);
		this.compareField(mEventsTable.TITLE, event.getTitle(), clone.getTitle(), clone.getId() == 0, delta);
		this.compareField(mEventsTable.EVENT_LOCATION, event.getLocation(), clone.getLocation(), clone.getId() == 0,
				delta);
		this.compareField(mEventsTable.DESCRIPTION, event.getDescription(), clone.getDescription(), clone.getId() == 0,
				delta);
		this.compareStartTime(event, clone, delta);
		this.compareField(mEventsTable.ALL_DAY, event.isAllDay() ? "1" : "0", clone.isAllDay() ? "1" : "0",
				clone.getId() == 0, delta);
		this.compareTimeZone(mEventsTable.EVENT_TIMEZONE, event.getStartTimeZone(true), clone.getStartTimeZone(true), true,
				delta);
		this.compareTimeZone(mEventsTable.EVENT_END_TIMEZONE, event.getEndTimeZone(), clone.getEndTimeZone(), false,
				delta);
		this.compareField(mEventsTable.ACCESS_LEVEL, "" + event.getAccessLevel(), "" + clone.getAccessLevel(),
				clone.getId() == 0, delta);
		this.compareAvailability(event, clone, delta);

		if (event.isRecurringEvent()) {
			// Compare recurring event fields
			this.compareDuration(event, clone, delta);
			this.compareRecurrenceRule(mEventsTable.RRULE, event.getRecurrenceRule(), clone.getRecurrenceRule(), delta);
			this.compareField(mEventsTable.RDATE, event.getRecurrenceDate(), clone.getRecurrenceDate(), false, delta);
			this.compareRecurrenceRule(mEventsTable.EXRULE, event.getRecurrenceExRule(), clone.getRecurrenceExRule(),
					delta);
			this.compareField(mEventsTable.EXDATE, event.getRecurrenceExDate(), clone.getRecurrenceExDate(), false,
					delta);
		} else if (event.isRecurringEventException()) {
			// Compare recurring event exception fields
			this.compareEndTime(event, clone, delta);
			this.compareField(mEventsTable.ORIGINAL_ALL_DAY, event.isOriginalAllDay() ? "1" : "0",
					clone.isOriginalAllDay() ? "1" : "0", clone.getId() == 0, delta);
			this.compareField(mEventsTable.ORIGINAL_INSTANCE_TIME, "" + event.getOriginalInstanceTime(),
					"" + clone.getOriginalInstanceTime(), clone.getId() == 0, delta);
		} else {
			// Compare regular event fields
			this.compareEndTime(event, clone, delta);
		}
	}

	protected boolean eventWasRescheduled(Event event, Event clone) {
		// Check if start/end times have changed (required user accept)
		ContentValues delta = new ContentValues();
		this.compareStartTime(event, clone, delta);
		if (!event.isRecurringEvent()) {
			this.compareEndTime(event, clone, delta);
		} else {
			// Should never occur since superclass returns false for all changed
			// recurring events
			this.compareDuration(event, clone, delta);
		}

		return delta.size() > 0;
	}
}
