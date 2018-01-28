package com.dizzl.android.CalendarCloner;

public abstract class LogLineFactory {
	public abstract LogLine createLogLine(int level, String logPrefix, String col0);
	public abstract LogLine createLogLine(int level, String logPrefix, String col0, String col1);
	public abstract LogLine createLogLine(int level, String logPrefix, String col0, String col1, String col2);
}
