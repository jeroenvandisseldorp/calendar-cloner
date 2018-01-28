package com.dizzl.android.CalendarClonerFree;

import android.provider.CalendarContract.Attendees;

public class MemoryAttendee implements Attendee {
	private String mName = "";
	private String mEmail = "";
	private int mRelationship = Attendees.RELATIONSHIP_NONE;
	private int mType = Attendees.TYPE_NONE;
	private int mStatus = Attendees.ATTENDEE_STATUS_NONE;

	public long getId() {
		return 0;
	}

	public String getName() {
		return mName;
	}

	public String getEmail() {
		return mEmail;
	}

	public int getRelationship() {
		return mRelationship;
	}

	public int getType() {
		return mType;
	}

	public int getStatus() {
		return mStatus;
	}

	public void setName(String name) {
		mName = name;
	}

	public void setEmail(String email) {
		mEmail = email;
	}

	public void setRelationship(int relationship) {
		mRelationship = relationship;
	}

	public void setType(int type) {
		mType = type;
	}

	public void setStatus(int status) {
		mStatus = status;
	}
}
