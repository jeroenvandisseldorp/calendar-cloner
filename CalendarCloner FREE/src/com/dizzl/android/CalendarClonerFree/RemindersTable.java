package com.dizzl.android.CalendarClonerFree;

import android.provider.CalendarContract.Reminders;

public class RemindersTable extends ClonerTable {
	// CalendarContract columns
	public Column _ID = new Column(Reminders._ID);
	public Column EVENT_ID = new Column(Reminders.EVENT_ID);
	public Column METHOD = new Column(Reminders.METHOD);
	public Column MINUTES = new Column(Reminders.MINUTES);

	public RemindersTable(ClonerDb db) {
		super(db, Reminders.CONTENT_URI);
		// Add CalendarContract columns
		this.addColumn(_ID);
		this.addColumn(EVENT_ID);
		this.addColumn(METHOD);
		this.addColumn(MINUTES);
	}
}
