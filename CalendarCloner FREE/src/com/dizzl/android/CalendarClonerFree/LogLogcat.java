package com.dizzl.android.CalendarClonerFree;

import android.util.Log;

public class LogLogcat extends ClonerLog {
	private int mFromLevel;

	public LogLogcat(String title, int logType, int fromLevel) {
		super(title, logType, new LogLineFactoryMemory());
		mFromLevel = fromLevel;
	}

	private void logToLogcat(LogLine line) {
		if (line.getLevel() >= mFromLevel) {
			String logMessage = "";
			for (int index = 0; index < line.getColumnCount(); index++) {
				logMessage += " | " + line.getColumn(index);
			}
			if (!this.getTitle().contentEquals("")) {
				logMessage += ": " + this.getTitle();
			}
			switch (line.getLevel()) {
			case ClonerLog.LOG_WARNING:
				Log.w("Cloner", logMessage);
				break;
			case ClonerLog.LOG_UPDATE:
				Log.i("Cloner", logMessage);
				break;
			case ClonerLog.LOG_ERROR:
				Log.e("Cloner", logMessage);
				break;
			default:
				Log.d("Cloner", logMessage);
			}
		}
	}

	@Override
	public void log(LogLine summary, LogLines lines) {
		super.log(summary, lines);
		// Log to logcat
		if (summary != null) {
			this.logToLogcat(summary);
		}
		if (lines != null) {
			if (this.getLogType() == ClonerLog.TYPE_EXTENDED
					|| (summary != null && summary.getLevel() == ClonerLog.LOG_ERROR)
					|| lines.getMaxLevel() == ClonerLog.LOG_ERROR) {
				for (LogLine line : lines.getLines()) {
					this.logToLogcat(line);
				}
			}
		}
	}

}
