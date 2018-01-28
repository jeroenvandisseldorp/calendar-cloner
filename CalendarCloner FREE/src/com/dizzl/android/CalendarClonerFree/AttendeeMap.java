package com.dizzl.android.CalendarClonerFree;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class AttendeeMap {
	private Map<String, Attendee> mMap = new HashMap<String, Attendee>();

	public void clear() {
		mMap.clear();
	}

	public Attendee get(String name, String email) {
		return mMap.get(AttendeeId.map(0, name, email));
	}

	public Set<Attendee> getSet() {
		Set<Attendee> result = new HashSet<Attendee>();
		for (Entry<String, Attendee> entry : mMap.entrySet()) {
			result.add(entry.getValue());
		}
		return result;
	}

	public void put(String name, String email, Attendee att) {
		mMap.put(AttendeeId.map(name, email), att);
	}

	public void remove(String name, String email) {
		mMap.remove(AttendeeId.map(name, email));
	}
}
