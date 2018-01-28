package com.dizzl.android.CalendarClonerFree;

import android.database.Cursor;

public class CursorDumper {
	static ClonerLog mLog = new LogLogcat("CursorDumper", ClonerLog.TYPE_EXTENDED, ClonerLog.LOG_INFO);

	public static String dump(Cursor cur) {
		String log = "";
		for (int index = 0; index < cur.getColumnCount(); index++) {
			String line = cur.getColumnName(index) + ": " + cur.getString(index) + "\n";
			mLog.info(line);
			log += line;
		}
		String separator = "________\n";
		mLog.info(separator);
		log += separator;
		return log;
	}
}
