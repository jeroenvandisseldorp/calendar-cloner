package com.dizzl.android.CalendarClonerFree;

import java.util.List;

import android.content.ContentValues;
import android.provider.CalendarContract.Events;

public class EventMover extends EventCloner {
	public EventMover(Rule rule) {
		super(rule);
	}

	protected boolean neutralizeEventType(Event event) {
		if (event != null) {
			// Remove PARTIAL_MOVE_HEADER
			ContentValues delta = new ContentValues();
			delta.put(Events.DESCRIPTION, EventMarker.neutralizeEventDescription(event.getDescription()));
			mEventsTable.update(event.getId(), delta);
			return true;
		}

		return false;
	}

	@Override
	protected void logCloneSummary(Event event, PartialCloneResult cloneResult, EventResult result) {
		if (!cloneResult.completed || !Limits.canModify(Limits.TYPE_EVENT)) {
			// Needs more time in the next iteration
			cloneResult.completed = false;
			// Log the appropriate message
			switch (cloneResult.result) {
			case RESULT_EVENT_INSERTED:
			case RESULT_EVENT_UPDATED:
				this.logSummary(ClonerLog.LOG_UPDATE, ClonerApp.translate(R.string.cloner_log_moved_partially), event);
			default:
				super.logCloneSummary(event, cloneResult, result);
			}
			return;
		}

		// The move was fully finished and we reserved 1 more event modification. Actual modifications are done in
		// processSingleEvent and processRecurringEvent.

		// Log the appropriate message
		switch (cloneResult.result) {
		case RESULT_EVENT_INSERTED:
		case RESULT_EVENT_UPDATED:
			this.logSummary(ClonerLog.LOG_UPDATE, ClonerApp.translate(R.string.cloner_log_moved), event);
			return;
		default:
			super.logCloneSummary(event, cloneResult, result);
		}
	}

	@Override
	protected EventResult processSingleEvent(Event event) {
		CloneResult result = (CloneResult) super.processSingleEvent(event);
		if (result.completed) {
			// Clone was successfully created and we reserved two more modifications. So now all we have to do is remove
			// the PARTIAL_MOVE_HEADER from the description and delete the source event.
			Event clone = DbEvent.get(mEventsTable, result.cloneId);
			if (clone != null) {
				if (this.neutralizeEventType(clone)) {
					// Delete original event
					mEventsTable.delete(event.getId());
					// Increase the update counter
					result.updateCount++;
				} else {
					result.completed = false;
				}
			} else {
				result.completed = false;
			}
		}

		return result;
	}

	@Override
	protected EventResult processRecurringEvent(Event event) {
		CloneResult result = (CloneResult) super.processRecurringEvent(event);
		if (result.completed) {
			// Clone was successfully created and we reserved one more modification for the main event and for each
			// exception. So now all we have to do is remove the PARTIAL_MOVE_HEADER from the events' descriptions and
			// delete the source event.
			Event clone = DbEvent.get(mEventsTable, result.cloneId);
			if (clone != null) {
				// Get the list of cloned exceptions
				List<Event> exceptions = this.getExceptions(clone);
				// Neutralize all exceptions one by one
				for (Event exception : exceptions) {
					if (result.completed == true && this.neutralizeEventType(exception)) {
						// Increase the update counter
						result.updateCount++;
					} else {
						result.completed = false;
					}
				}

				// Exceptions were all neutralized, so now neutralize the main event
				if (result.completed == true && this.neutralizeEventType(clone)) {
					// Delete original event
					mEventsTable.delete(event.getId());
					// Increase the update counter
					result.updateCount++;
				}
			} else {
				result.completed = false;
			}
		}

		return result;
	}

	@Override
	public InitResult init(ClonerLog log) {
		InitResult result = super.init(log);
		if (!result.isSuccess()) {
			return result;
		}

		// Check for source calendar write access
		if (!mSrcCalendar.isWriteable()) {
			return new InitResult(false, ClonerApp.translate(R.string.error_calendar_x_not_writeable,
					new String[] { ClonerApp.translate(R.string.calendar_source) }), result.getUpdateCount());
		}
		return new InitResult(true, "", result.getUpdateCount());
	}
}
