package com.dizzl.android.CalendarCloner;

public class LogLine {
	private String[] mColumns;
	private int mColumnCount = 0;
	private int mLogLevel;
	private String mLogPrefix;

	public LogLine(boolean dummy, int level, String logPrefix, String col0) {
		mLogLevel = level;
		mLogPrefix = logPrefix;
		mColumnCount = 1;
		mColumns = new String[mColumnCount];
		mColumns[0] = col0;
	}

	public LogLine(boolean dummy, int level, String logPrefix, String col0, String col1) {
		mLogLevel = level;
		mLogPrefix = logPrefix;
		mColumnCount = 2;
		mColumns = new String[mColumnCount];
		mColumns[0] = col0;
		mColumns[1] = col1;
	}

	public LogLine(boolean dummy, int level, String logPrefix, String col0, String col1, String col2) {
		mLogLevel = level;
		mLogPrefix = logPrefix;
		mColumnCount = 3;
		mColumns = new String[mColumnCount];
		mColumns[0] = col0;
		mColumns[1] = col1;
		mColumns[2] = col2;
	}

	public int getLevel() {
		return mLogLevel;
	}

	public String getLogPrefix() {
		return mLogPrefix;
	}

	public int getColumnCount() {
		return mColumnCount;
	}

	public String getColumn(int index) {
		return mColumns[index];
	}
}
