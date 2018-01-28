package com.dizzl.android.CalendarCloner;

import android.provider.CalendarContract.Attendees;

public class AttendeesTable extends ClonerTable {
	// CalendarContract columns
	public Column _ID = new Column(Attendees._ID);
	public Column EVENT_ID = new Column(Attendees.EVENT_ID);
	public Column ATTENDEE_NAME = new Column(Attendees.ATTENDEE_NAME);
	public Column ATTENDEE_EMAIL = new Column(Attendees.ATTENDEE_EMAIL);
	public Column ATTENDEE_RELATIONSHIP = new Column(Attendees.ATTENDEE_RELATIONSHIP);
	public Column ATTENDEE_STATUS = new Column(Attendees.ATTENDEE_STATUS);
	public Column ATTENDEE_TYPE = new Column(Attendees.ATTENDEE_TYPE);

	public AttendeesTable(ClonerDb db) {
		super(db, Attendees.CONTENT_URI);
		// Add CalendarContract columns
		this.addColumn(_ID);
		this.addColumn(EVENT_ID);
		this.addColumn(ATTENDEE_NAME);
		this.addColumn(ATTENDEE_EMAIL);
		this.addColumn(ATTENDEE_RELATIONSHIP);
		this.addColumn(ATTENDEE_STATUS);
		this.addColumn(ATTENDEE_TYPE);
	}
}
