package com.dizzl.android.CalendarClonerFree;

import java.util.List;
import java.util.Map;

import android.content.ContentValues;

public class Processor {
	private ClonerLog mLog;
	private LogLines mLogLines = null;
	private String mLogPrefix;
	private String mOriginalLogPrefix;

	public class InitResult {
		private final boolean mSuccess;
		private final String mErrorMessage;
		private final int mUpdateCount;

		public InitResult(boolean success, String errorMessage, int updateCount) {
			mSuccess = success;
			mErrorMessage = errorMessage;
			mUpdateCount = updateCount;
		}

		public boolean isSuccess() {
			return mSuccess;
		}

		public String getErrorMessage() {
			return mErrorMessage;
		}

		public int getUpdateCount() {
			return mUpdateCount;
		}
	}

	public InitResult init(ClonerLog log) {
		return new InitResult(true, "", 0);
	}

	protected void initLogLines(ClonerLog log, String logPrefix) {
		mLog = log;
		this.startNewLogLines(logPrefix);
		mOriginalLogPrefix = logPrefix;
	}

	protected void startNewLogLines(String logPrefix) {
		mLogLines = mLog.createLogLines();
		mLogPrefix = logPrefix;
	}

	protected void useLogLines(LogLines logLines) {
		mLog = null;
		mLogLines = logLines;
		mLogPrefix = null;
		mOriginalLogPrefix = null;
	}

	protected LogLines getLogLines() {
		return mLogLines;
	}

	protected String getOriginalLogPrefix() {
		return mOriginalLogPrefix;
	}

	protected void log(int level, String message) {
		if (mLogLines != null) {
			mLogLines.log(level, null, message);
		}
	}

	protected void log(int level, String message, String value) {
		if (mLogLines != null) {
			mLogLines.log(level, null, message, value);
		}
	}

	protected void logEvent(Event event) {
		if (mLogLines != null) {
			mLogLines.logEvent(event);
		}
	}

	protected void logEvent(Event event, ContentValues delta) {
		if (mLogLines != null) {
			mLogLines.logEvent(event, delta);
		}
	}

	protected void logEvents(Event event, Event clone, ContentValues delta) {
		if (mLogLines != null) {
			mLogLines.logEvents(event, clone, delta);
		}
	}

	protected void logAttendees(List<DbAttendee> eventAttendees, List<DbAttendee> cloneAttendees,
			DbCalendar srcCalendar, DbCalendar dstCalendar, AttendeeDeltas deltas, String dummyEmailDomain) {
		if (mLogLines != null) {
			mLogLines.logAttendees(eventAttendees, cloneAttendees, srcCalendar, dstCalendar, deltas, dummyEmailDomain);
		}
	}

	protected void logReminders(int level, List<DbReminder> eventReminders, List<DbReminder> cloneReminders,
			Map<Long, Long> mappedReminders) {
		if (mLogLines != null) {
			mLogLines.logReminders(level, eventReminders, cloneReminders, mappedReminders);
		}
	}

	protected void logSummary(int level, String message, Event event) {
		this.logSummary(level, message, event, null);
	}

	protected void logSummary(int level, String message, Event event, String additionalInfo) {
		String title = Utilities.dateTimeToString(event.getStartTime()) + " - " + event.getTitle();
		if (mLog != null && mLog.getLogType() == ClonerLog.TYPE_EXTENDED && additionalInfo != null && mLogLines != null) {
			mLogLines.log(ClonerLog.LOG_INFO, "", additionalInfo);
		}
		this.logSummary(level, message, title);
	}

	protected void logSummary(int level, String message, String title) {
		if (mLogLines != null && level < mLogLines.getMaxLevel()) {
			level = mLogLines.getMaxLevel();
		}
		mLog.log(mLog.createLogLine(level, mLogPrefix, message, title), mLogLines);
		mLogLines = null;
	}
}
