package com.dizzl.android.CalendarCloner;

import android.database.Cursor;
import android.provider.CalendarContract.Events;

public class DbEventIterator extends EventIterator {
	private EventsTable mEventsTable = new EventsTable(ClonerApp.getDb(true));

	public DbEventIterator(ClonerLog log) {
		super(log);
	}

	@Override
	protected Cursor doQuery(long sourceCalendarId) {
		return mEventsTable.query("((" + Events.CALENDAR_ID + "=? AND " + Events.ORIGINAL_ID + " ISNULL))",
				new String[] { "" + sourceCalendarId }, Events.DTSTART + " ASC");
	}

	@Override
	protected DbEvent getEvent(Cursor cur) {
		return new DbEvent(mEventsTable, new DbObject(cur));
	}

	@Override
	protected EventsTable getTable() {
		return mEventsTable;
	}
}
