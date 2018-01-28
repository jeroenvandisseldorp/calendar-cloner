package com.dizzl.android.CalendarCloner;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Instances;

public class InstancesTable extends EventsTable {
	public Column EVENT_ID = new Column(Instances.EVENT_ID);
	public Column BEGIN = new Column(Instances.BEGIN);
	public Column END = new Column(Instances.END);
	public Column END_DAY = new Column(Instances.END_DAY);
	public Column END_MINUTE = new Column(Instances.END_MINUTE);
	public Column START_DAY = new Column(Instances.START_DAY);
	public Column START_MINUTE = new Column(Instances.START_MINUTE);

	public InstancesTable(ClonerDb db) {
		super(db, Instances.CONTENT_URI);
		this.initColumns();
	}

	private void initColumns() {
		// Add CalendarContract columns
		this.addColumn(EVENT_ID);
		this.addColumn(BEGIN);
		this.addColumn(END);
		this.addColumn(END_DAY);
		this.addColumn(END_MINUTE);
		this.addColumn(START_DAY);
		this.addColumn(START_MINUTE);
	}

	public Cursor queryByDay(long calendarId, long begin, long end) {
		Uri.Builder eventsUriBuilder = Instances.CONTENT_URI.buildUpon();
		ContentUris.appendId(eventsUriBuilder, begin);
		ContentUris.appendId(eventsUriBuilder, end);
		Uri eventsUri = eventsUriBuilder.build();
		return this.rawQuery(eventsUri, Instances.CALENDAR_ID + " =?", new String[] { "" + calendarId },
				Instances.DTSTART + " ASC");
	}
}
