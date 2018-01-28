package com.dizzl.android.CalendarClonerFree;

public class LogSplitter extends LogMemory {
	private LogLogcat mLogcat;

	public LogSplitter(String title, int logType, int fromLevel) {
		super(title, logType);
		mLogcat = new LogLogcat(title, logType, fromLevel);
	}

	@Override
	public void log(LogLine summary, LogLines lines) {
		mLogcat.log(summary, lines);
		super.log(summary, lines);
	}
}
