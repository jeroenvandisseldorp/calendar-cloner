package com.dizzl.android.CalendarCloner;

import java.util.ArrayList;

public class LogMemory extends ClonerLog {
	private ArrayList<LogLines> mLineSets = new ArrayList<LogLines>();
	private ArrayList<LogLine> mSummaries = new ArrayList<LogLine>();

	public LogMemory(String title, int logType) {
		super(title, logType, new LogLineFactoryMemory());
	}

	@Override
	public void log(LogLine summary, LogLines lines) {
		super.log(summary, lines);
		// Log to memory
		mSummaries.add(summary);
		if (this.getLogType() == ClonerLog.TYPE_EXTENDED
				|| (summary != null && summary.getLevel() == ClonerLog.LOG_ERROR)
				|| (lines != null && lines.getMaxLevel() == ClonerLog.LOG_ERROR)) {
			mLineSets.add(lines);
		} else {
			mLineSets.add(null);
		}
	}

	public int size() {
		return mSummaries.size();
	}

	public void getLineSets(LogLines[] lineSets) {
		mLineSets.toArray(lineSets);
	}

	public void getSummaries(LogLine[] summaries) {
		mSummaries.toArray(summaries);
	}
}
