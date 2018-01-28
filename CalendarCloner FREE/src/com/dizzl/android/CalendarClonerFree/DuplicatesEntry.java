package com.dizzl.android.CalendarClonerFree;

import java.util.ArrayList;

import org.joda.time.DateTime;

public class DuplicatesEntry {
	public static class Entry {
		String uniqueId;
		long eventId;
		DateTime startTime;
		String title;
		long calendarId;
		String location;
		boolean isRecurring;
		boolean isException;
		boolean isDeleted;
	}

	public ArrayList<Entry> events = new ArrayList<Entry>();
}
