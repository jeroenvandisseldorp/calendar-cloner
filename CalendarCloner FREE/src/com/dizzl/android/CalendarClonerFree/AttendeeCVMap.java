package com.dizzl.android.CalendarClonerFree;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;

public class AttendeeCVMap {
	private Map<String, ContentValues> mMap = new HashMap<String, ContentValues>();

	public ContentValues get(String name, String email) {
		return mMap.get(AttendeeId.map(0, name, email));
	}

	public void put(String name, String email, ContentValues values) {
		mMap.put(AttendeeId.map(0, name, email), values);
	}
}
