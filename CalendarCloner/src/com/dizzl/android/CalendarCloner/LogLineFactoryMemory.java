package com.dizzl.android.CalendarCloner;

public class LogLineFactoryMemory extends LogLineFactory {
	@Override
	public LogLine createLogLine(int level, String logPrefix, String col0) {
		return new LogLine(false, level, logPrefix, col0);
	}

	@Override
	public LogLine createLogLine(int level, String logPrefix, String col0, String col1) {
		return new LogLine(false, level, logPrefix, col0, col1);
	}

	@Override
	public LogLine createLogLine(int level, String logPrefix, String col0, String col1, String col2) {
		return new LogLine(false, level, logPrefix, col0, col1, col2);
	}
}
