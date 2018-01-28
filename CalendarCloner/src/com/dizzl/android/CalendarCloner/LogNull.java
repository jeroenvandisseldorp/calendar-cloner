package com.dizzl.android.CalendarCloner;

public class LogNull extends ClonerLog {
	public LogNull(String title) {
		super(title, ClonerLog.TYPE_SUMMARY, new LogLineFactoryNull());
	}

	@Override
	public void log(LogLine summary, LogLines lines) {
	}
}
