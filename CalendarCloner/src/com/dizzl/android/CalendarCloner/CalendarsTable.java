package com.dizzl.android.CalendarCloner;

import android.provider.CalendarContract.Calendars;

public class CalendarsTable extends ClonerTable {
	// CalendarContract columns
	public Column _ID = new Column(Calendars._ID);
	public Column NAME = new Column(Calendars.NAME);
	public Column CALENDAR_DISPLAY_NAME = new Column(Calendars.CALENDAR_DISPLAY_NAME);
	public Column CALENDAR_TIME_ZONE = new Column(Calendars.CALENDAR_TIME_ZONE);
	public Column ACCOUNT_NAME = new Column(Calendars.ACCOUNT_NAME);
	public Column ACCOUNT_TYPE = new Column(Calendars.ACCOUNT_TYPE);
	public Column OWNER_ACCOUNT = new Column(Calendars.OWNER_ACCOUNT);
	public Column CALENDAR_ACCESS_LEVEL = new Column(Calendars.CALENDAR_ACCESS_LEVEL);
	public Column SYNC_EVENTS = new Column(Calendars.SYNC_EVENTS);
	public Column VISIBLE = new Column(Calendars.VISIBLE);
	public Column CAN_ORGANIZER_RESPOND = new Column(Calendars.CAN_ORGANIZER_RESPOND);
	public Column ALLOWED_REMINDERS = new Column(Calendars.ALLOWED_REMINDERS);
	public Column MAX_REMINDERS = new Column(Calendars.MAX_REMINDERS);
	public Column CAL_SYNC1 = new Column(Calendars.CAL_SYNC1);

	public CalendarsTable(ClonerDb db) {
		super(db, Calendars.CONTENT_URI);
		// Add CalendarContract columns
		this.addColumn(_ID);
		this.addColumn(NAME);
		this.addColumn(CALENDAR_DISPLAY_NAME);
		this.addColumn(CALENDAR_TIME_ZONE);
		this.addColumn(ACCOUNT_NAME);
		this.addColumn(ACCOUNT_TYPE);
		this.addColumn(OWNER_ACCOUNT);
		this.addColumn(CALENDAR_ACCESS_LEVEL);
		this.addColumn(SYNC_EVENTS);
		this.addColumn(VISIBLE);
		this.addColumn(CAN_ORGANIZER_RESPOND);
		this.addColumn(ALLOWED_REMINDERS);
		this.addColumn(MAX_REMINDERS);
		this.addColumn(CAL_SYNC1);
	}
}
