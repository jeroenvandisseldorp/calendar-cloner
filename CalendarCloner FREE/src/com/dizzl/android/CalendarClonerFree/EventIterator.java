package com.dizzl.android.CalendarClonerFree;

import java.util.HashSet;
import java.util.Set;

import com.dizzl.android.CalendarClonerFree.EventProcessor.EventResult;

import android.database.Cursor;
import android.provider.CalendarContract.Events;

public abstract class EventIterator {
	protected static final String FAKE_EVENT_TITLE = "Fake event to work around a calendar issue.";
	private Set<String> mSrcServerIds = new HashSet<String>();

	private ClonerLog mLog;

	public class Result {
		public static final int STATUS_SUCCESS = 0;
		public static final int STATUS_NOT_COMPLETED = 1;
		public static final int STATUS_FAIL = 2;

		private int mStatus;
		private String mSummary;

		public Result(int status, String summary) {
			mStatus = status;
			mSummary = summary;
		}

		public int getStatus() {
			return mStatus;
		}

		public String getSummary() {
			return mSummary;
		}
	}

	public EventIterator(ClonerLog log) {
		mLog = log;
	}

	protected boolean isRealEvent(DbEvent event) {
		// Don't clone events that are marked as fake (administrative events
		// used during sync)
		if (event.isLastSynced()) {
			return false;
		}

		String syncId = event.getSyncId();
		if (event.getSyncId() != null && syncId.matches(".*fakeevent")) {
			return false;
		}

		String title = event.getTitle();
		if (title != null && title.contentEquals(FAKE_EVENT_TITLE)) {
			return false;
		}

		// Always ignore single canceled events with null titles (trouble in
		// Google calendars)
		if (event.getStatus() == Events.STATUS_CANCELED) {
			if (event.isSingleEvent() && (event.getTitle() == null || event.getTitle().contentEquals(""))) {
				return false;
			}
		}

		return true;
	}

	protected void assertServerEventIdUniqueness(DbEvent event) throws Exception {
		// The calendar cloning works under the assumption that all serverIds
		// are unique (per calendar). Here we check
		// if that assumption remains true. If not, we skip this event.
		String uniqueId = event.getUniqueId();
		if (uniqueId.contentEquals("")) {
			event.loadAll();
			throw new Exception(ClonerApp.translate(R.string.error_no_unique_id) + ": " + event.getTitle());
		}
		if (mSrcServerIds.contains(uniqueId)) {
			// Error in assumption that all server ids are unique (per calendar)
			throw new Exception(ClonerApp.translate(R.string.error_duplicate_server_id) + ": " + event.getTitle()
					+ ", Account type: " + event.getAccountType() + ", UniqueID: " + uniqueId + ", ID: "
					+ event.getId());
		}
		mSrcServerIds.add(uniqueId);
	}

	abstract protected Cursor doQuery(long sourceCalendarId);

	abstract protected DbEvent getEvent(Cursor cur);

	abstract protected EventsTable getTable();

	public Result execute(EventProcessor processor) throws Exception {
		boolean completed = true;

		// Perform the actual cloning
		mLog.log(
				mLog.createLogLine(ClonerLog.LOG_INFO, null, ClonerApp.translate(R.string.cloner_process_initializing)),
				null);
		Processor.InitResult initResult = processor.init(mLog);
		if (!initResult.isSuccess()) {
			return new Result(Result.STATUS_FAIL, initResult.getErrorMessage());
		}

		// Count the number of events processed and updates
		int updateCount = initResult.getUpdateCount();
		// Iterate through all the non-recurring events from the source calendar
		Cursor cur = this.doQuery(processor.getSrcCalendarId());
		if (cur == null) {
			return new Result(Result.STATUS_FAIL, ClonerApp.translate(R.string.error_calendar_x_not_accessible,
					new String[] { ClonerApp.translate(R.string.calendar_source) }));
		}
		try {
			mLog.log(
					mLog.createLogLine(
							ClonerLog.LOG_INFO,
							null,
							ClonerApp.translate(R.string.msg_found_x_events_in_source_calendar,
									new String[] { "" + cur.getCount() })), null);

			int eventNr = 0;
			while (cur.moveToNext() && ClonerApp.getSettings().isClonerEnabled()) {
				eventNr++;
				DbEvent event = this.getEvent(cur);

				if (!event.getUniqueId().contentEquals("") && this.isRealEvent(event) && !event.isDeleted()
						&& !event.isRecurringEventException()) {
					// Make sure all server event ids are unique
					this.assertServerEventIdUniqueness(event);

					try {
						// Process the event
						EventResult result = processor.process(event, mLog, "" + eventNr);
						completed &= result.completed;
						if (result.updateCount > 0) {
							// Count as one logical update for the user
							updateCount++;
						}
					} catch (Exception e) {
						// eat and progress to next event
						mLog.stacktrace(e);
					}
				}
			}
		} finally {
			cur.close();
		}

		mLog.log(
				mLog.createLogLine(
						ClonerLog.LOG_INFO,
						null,
						ClonerApp.translate(R.string.cloner_state_finished_after_x_events, new String[] { ""
								+ updateCount })), null);

		// Now that all events were cloned, check if there are events that need
		// to be deleted because the source event
		// is gone
		if (ClonerApp.getSettings().isClonerEnabled()) {
			EventResult roundupResult = processor.roundup(mLog);
			completed &= roundupResult.completed;
			updateCount += roundupResult.updateCount;
		}

		// If cloning was aborted by user (by disabling cloning in main
		// settings), exit here
		if (!ClonerApp.getSettings().isClonerEnabled()) {
			return new Result(Result.STATUS_NOT_COMPLETED, ClonerApp.translate(R.string.cloner_state_interrupted)
					+ ": " + Utilities.getNowString());
		}

		mLog.log(mLog.createLogLine(ClonerLog.LOG_INFO, null, ClonerApp.translate(R.string.cloner_log_done)), null);

		// If limit was reached, exit with state signaling that
		if (!completed) {
			if (updateCount > 0) {
				return new Result(Result.STATUS_NOT_COMPLETED, ClonerApp.translate(
						R.string.cloner_process_updated_x_events, new String[] { "" + updateCount })
						+ ": "
						+ Utilities.getNowString());
			}
			return new Result(Result.STATUS_NOT_COMPLETED, ClonerApp.translate(R.string.cloner_state_unchanged) + ": "
					+ Utilities.getNowString());
		}

		// Exit with proper description, based on number of updates
		if (updateCount > 0) {
			return new Result(Result.STATUS_SUCCESS, ClonerApp.translate(R.string.cloner_process_updated_x_events,
					new String[] { "" + updateCount }) + ": " + Utilities.getNowString());
		}
		return new Result(Result.STATUS_SUCCESS, ClonerApp.translate(R.string.cloner_state_unchanged) + ": "
				+ Utilities.getNowString());
	}
}
