package com.dizzl.android.CalendarClonerFree;

public class AttendeeId {
	public static String map(long id, String name, String email) {
		if (email != null && !email.contentEquals("")) {
			return "Email: "+email;
		}
		if (name != null && !name.contentEquals("")) {
			return "Name: "+name;
		}
		return "Id: "+id;
	}

	public static String map(String name, String email) {
		return map(0, name, email);
	}

	public static String map(String email) {
		return map(0, "", email);
	}
}
