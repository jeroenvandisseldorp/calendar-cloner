package com.dizzl.android.CalendarCloner;

public class LogLineFactoryNull extends LogLineFactory {
	@Override
	public LogLine createLogLine(int level, String logPrefix, String col0) {
		return null;
	}

	@Override
	public LogLine createLogLine(int level, String logPrefix, String col0, String col1) {
		return null;
	}

	@Override
	public LogLine createLogLine(int level, String logPrefix, String col0, String col1, String col2) {
		return null;
	}
}
