package com.dizzl.android.CalendarClonerFree;

public class ClonerLog {
	public static final int TYPE_SUMMARY = 0;
	public static final int TYPE_EXTENDED = 1;

	private LogLineFactory mLogLineFactory;
	private int mLogType;
	private int mMaxLevel = LOG_INFO;
	private String mTitle;

	public static final int LOG_INFO = 0;
	public static final int LOG_WARNING = 1;
	public static final int LOG_UPDATE = 2;
	public static final int LOG_ERROR = 3;
	public static final int NUM_LOG_LEVELS = 4;

	public ClonerLog(String title, int logType, LogLineFactory logLineFactory) {
		mTitle = title;
		mLogType = logType;
		mLogLineFactory = logLineFactory;
	}

	public String getTitle() {
		return mTitle;
	}

	public int getLogType() {
		return mLogType;
	}

	public LogLineFactory getLogLineFactory() {
		return mLogLineFactory;
	}

	public int getMaxLevel() {
		return mMaxLevel;
	}

	public LogLine createLogLine(int level, String logPrefix, String col0) {
		return mLogLineFactory.createLogLine(level, logPrefix, col0);
	}

	public LogLine createLogLine(int level, String logPrefix, String col0, String col1) {
		return mLogLineFactory.createLogLine(level, logPrefix, col0, col1);
	}

	public LogLine createLogLine(int level, String logPrefix, String col0, String col1, String col2) {
		return mLogLineFactory.createLogLine(level, logPrefix, col0, col1, col2);
	}

	public LogLines createLogLines() {
		return new LogLines(mLogLineFactory);
	}

	public void log(LogLine summary, LogLines lines) {
		// Keep track of max log level
		if (summary != null && summary.getLevel() > mMaxLevel) {
			mMaxLevel = summary.getLevel();
		}
		if (lines != null && lines.getMaxLevel() > mMaxLevel) {
			mMaxLevel = lines.getMaxLevel();
		}
	}

	public void debug(String message) {
		LogLine line = mLogLineFactory.createLogLine(ClonerLog.LOG_INFO, null, message);
		this.log(line, null);
	}

	public void error(String message) {
		LogLine line = mLogLineFactory.createLogLine(ClonerLog.LOG_ERROR, null, message);
		this.log(line, null);
	}

	public void stacktrace(Throwable e) {
		StackTraceElement[] stack = e.getStackTrace();
		LogLines logLines = this.createLogLines();
		LogLine summary = mLogLineFactory.createLogLine(
				ClonerLog.LOG_ERROR,
				null,
				ClonerApp.translate(R.string.log_exception) + ": " + e.getClass().getCanonicalName() + "\n"
						+ ClonerApp.translate(R.string.log_message) + ": " + e.getMessage() + "\n");
		if (logLines != null) {
			logLines.log(ClonerLog.LOG_ERROR, null, ClonerApp.translate(R.string.log_stacktrace) + ":\n");
			for (int index = 0; index < stack.length; index++) {
				logLines.log(ClonerLog.LOG_ERROR, null,
						"  " + stack[index].getMethodName() + " " + ClonerApp.translate(R.string.log_exception_at)
								+ " " + stack[index].getFileName() + ":" + stack[index].getLineNumber() + "\n");
			}
		}
		this.log(summary, logLines);
	}

	public void info(String message) {
		LogLine line = mLogLineFactory.createLogLine(ClonerLog.LOG_UPDATE, null, message);
		this.log(line, null);
	}

	public void warning(String message) {
		LogLine line = mLogLineFactory.createLogLine(ClonerLog.LOG_WARNING, null, message);
		this.log(line, null);
	}
}
