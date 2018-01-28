package com.dizzl.android.CalendarClonerFree;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DbReminder {
	private DbObject mObject;
	private RemindersTable mTable;

	// Reminder fields
	private long mId, mEventId;
	private int mMethod, mMinutes;

	// Reminder field indicators
	private boolean mIdLoaded = false;
	private boolean mEventIdLoaded = false;
	private boolean mMethodLoaded = false;
	private boolean mMinutesLoaded = false;

	public static List<DbReminder> getReminders(RemindersTable table, long eventId, Map<Long, ContentValues> deltas) {
		List<DbReminder> reminders = new LinkedList<DbReminder>();
		Cursor cur = table.query("((" + table.EVENT_ID.getName() + "=?))", new String[] { "" + eventId },
				table._ID.getName() + " ASC");
		if (cur != null) {
			try {
				while (cur.moveToNext()) {
					ContentValues delta = deltas != null ? deltas.get(cur.getLong(table._ID.getColumnIndex())) : null;
					DbObject obj = delta != null ? new DbObjectWithDelta(cur, delta) : new DbObject(cur);
					DbReminder reminder = new DbReminder(table, obj);
					reminder.loadAll();
					reminders.add(reminder);
				}
			} finally {
				cur.close();
			}
		}
		return reminders;
	}

	public static List<DbReminder> getReminders(RemindersTable table, long eventId) {
		return DbReminder.getReminders(table, eventId, null);
	}
	
	public DbReminder(RemindersTable table, DbObject obj) {
		mTable = table;
		mObject = obj;
	}

	public DbReminder(RemindersTable table, int method, int minutes) {
		mTable = table;
		mObject = new DbObject(null);
		mId = 0;
		mIdLoaded = true;
		mEventId = 0;
		mEventIdLoaded = true;
		mMethod = method;
		mMethodLoaded = true;
		mMinutes = minutes;
		mMinutesLoaded = true;
	}

	public void loadAll() {
        try {
            this.getId();
            this.getEventId();
            this.getMethod();
            this.getMinutes();
        } finally {
            // Release cursor
            mObject.releaseCursor();
        }
	}

	public long getId() {
		if (!mIdLoaded) {
			mId = mObject.getLong(mTable._ID);
			mIdLoaded = true;
		}
		return mId;
	}

	public long getEventId() {
		if (!mEventIdLoaded) {
			mEventId = mObject.getLong(mTable.EVENT_ID);
			mEventIdLoaded = true;
		}
		return mEventId;
	}

	public int getMethod() {
		if (!mMethodLoaded) {
			mMethod = mObject.getInt(mTable.METHOD);
			mMethodLoaded = true;
		}
		return mMethod;
	}

	public int getMinutes() {
		if (!mMinutesLoaded) {
			mMinutes = mObject.getInt(mTable.MINUTES);
			mMinutesLoaded = true;
		}
		return mMinutes;
	}
}
