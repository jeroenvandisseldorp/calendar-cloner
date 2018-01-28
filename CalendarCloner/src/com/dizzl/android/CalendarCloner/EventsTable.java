package com.dizzl.android.CalendarCloner;

import android.net.Uri;
import android.provider.CalendarContract.Events;

public class EventsTable extends ClonerTable {
	// CalendarContract columns
	public Column _ID = new Column(Events._ID);
	public Column CALENDAR_ID = new Column(Events.CALENDAR_ID);
	public Column _SYNC_ID = new Column(Events._SYNC_ID);
	public Column LAST_SYNCED = new Column(Events.LAST_SYNCED);
	public Column DELETED = new Column(Events.DELETED);
	public Column DIRTY = new Column(Events.DIRTY);
	public Column ACCOUNT_TYPE = new Column(Events.ACCOUNT_TYPE);
	public Column SYNC_DATA2 = new Column(Events.SYNC_DATA2);
	public Column UID_2445 = new Column("uid2445");

	public Column TITLE = new Column(Events.TITLE);
	public Column EVENT_LOCATION = new Column(Events.EVENT_LOCATION);
	public Column DESCRIPTION = new Column(Events.DESCRIPTION);
	public Column ORGANIZER = new Column(Events.ORGANIZER);
	public Column DTSTART = new Column(Events.DTSTART);
	public Column DTEND = new Column(Events.DTEND);
	public Column DURATION = new Column(Events.DURATION);
	public Column ALL_DAY = new Column(Events.ALL_DAY);
	public Column EVENT_TIMEZONE = new Column(Events.EVENT_TIMEZONE);
	public Column EVENT_END_TIMEZONE = new Column(Events.EVENT_END_TIMEZONE);
	public Column RRULE = new Column(Events.RRULE);
	public Column RDATE = new Column(Events.RDATE);
	public Column EXRULE = new Column(Events.EXRULE);
	public Column EXDATE = new Column(Events.EXDATE);
	public Column LAST_DATE = new Column(Events.LAST_DATE);
	public Column ORIGINAL_ID = new Column(Events.ORIGINAL_ID);
	public Column ORIGINAL_ALL_DAY = new Column(Events.ORIGINAL_ALL_DAY);
	public Column ORIGINAL_INSTANCE_TIME = new Column(Events.ORIGINAL_INSTANCE_TIME);
	public Column ACCESS_LEVEL = new Column(Events.ACCESS_LEVEL);
	public Column AVAILABILITY = new Column(Events.AVAILABILITY);
	public Column AVAILABILITY_SAMSUNG = new Column(Device.Samsung.AVAILABILITY);
	public Column STATUS = new Column(Events.STATUS);
	public Column SELF_ATTENDEE_STATUS = new Column(Events.SELF_ATTENDEE_STATUS);

	// HTC special columns
	public Column HTC_ICAL_GUID = new Column("iCalGUID");

	public EventsTable(ClonerDb db) {
		super(db, Events.CONTENT_URI);
		this.initColumns();
	}

	public EventsTable(ClonerDb db, Uri uri) {
		super(db, uri);
		this.initColumns();
	}

	private void initColumns() {
		// Add CalendarContract columns
		this.addColumn(_ID);
		this.addColumn(CALENDAR_ID);
		this.addColumn(_SYNC_ID);
		this.addColumn(LAST_SYNCED);
		this.addColumn(DELETED);
		this.addColumn(DIRTY);
		this.addColumn(ACCOUNT_TYPE);
		this.addColumn(SYNC_DATA2);
		this.addColumn(UID_2445);

		this.addColumn(TITLE);
		this.addColumn(EVENT_LOCATION);
		this.addColumn(DESCRIPTION);
		this.addColumn(ORGANIZER);
		this.addColumn(DTSTART);
		this.addColumn(DTEND);
		this.addColumn(DURATION);
		this.addColumn(ALL_DAY);
		this.addColumn(EVENT_TIMEZONE);
		this.addColumn(EVENT_END_TIMEZONE);
		this.addColumn(RRULE);
		this.addColumn(RDATE);
		this.addColumn(EXRULE);
		this.addColumn(EXDATE);
		this.addColumn(LAST_DATE);
		this.addColumn(ORIGINAL_ID);
		this.addColumn(ORIGINAL_ALL_DAY);
		this.addColumn(ORIGINAL_INSTANCE_TIME);
		this.addColumn(ACCESS_LEVEL);
		this.addColumn(AVAILABILITY);
		this.addColumn(AVAILABILITY_SAMSUNG);
		this.addColumn(STATUS);
		this.addColumn(SELF_ATTENDEE_STATUS);

		// Special columns
		this.addColumn(HTC_ICAL_GUID);
	}
}
