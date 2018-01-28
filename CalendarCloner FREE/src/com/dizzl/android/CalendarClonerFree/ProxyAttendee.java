package com.dizzl.android.CalendarClonerFree;

public class ProxyAttendee implements Attendee {
	private Attendee mSource;

	public ProxyAttendee(Attendee source) {
		mSource = source;
	}

	public String toString() {
		String result = this.getClass().getCanonicalName() + ":\n";
		result += "Name: " + this.getName() + "\n";
		result += "Email: " + this.getEmail() + "\n";
		result += "Status: " + this.getStatus() + "\n";
		return result;
	}

	@Override
	public long getId() {
		return mSource.getId();
	}

	@Override
	public String getName() {
		return mSource.getName();
	}

	@Override
	public String getEmail() {
		return mSource.getEmail();
	}

	@Override
	public int getRelationship() {
		return mSource.getRelationship();
	}

	@Override
	public int getType() {
		return mSource.getType();
	}

	@Override
	public int getStatus() {
		return mSource.getStatus();
	}
}
