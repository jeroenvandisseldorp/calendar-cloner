package com.dizzl.android.CalendarClonerFree;

import android.database.Cursor;
import android.database.CursorWrapper;

public class ClonerCursor extends CursorWrapper {
	private static ClonerLog mLog = new LogLogcat("ClonerCursor", ClonerLog.TYPE_EXTENDED, ClonerLog.LOG_WARNING);
	private boolean mCloseCalled = false;
	private String mName;

	public ClonerCursor(Cursor cur, String name) {
		super(cur);
		mName = name;
	}

	@Override
	public void close() {
		super.close();
		mCloseCalled = true;
	}

	@Override
	protected void finalize() {
		// Copy log as local variable since finalize may clean it up
		ClonerLog log = mLog;
		if (!mCloseCalled) {
			log.warning(ClonerApp.translate(R.string.log_cursor_not_closed) + ": " + mName);
		}
		try {
			super.finalize();
		} catch (Throwable e) {
			log.stacktrace(e);
		}
	}

	@Override
	public String toString() {
		return mName;
	}
}
