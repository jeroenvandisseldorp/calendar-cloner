package com.dizzl.android.CalendarCloner;

import android.provider.CalendarContract.Attendees;

public class ClonedAttendeeOther extends ProxyAttendee {
	private final boolean mDummyEmail;
	private final String mDummyEmailDomain;

	public ClonedAttendeeOther(Attendee source, boolean dummyEmail, String dummyEmailDomain) {
		super(source);
		mDummyEmail = dummyEmail;
		mDummyEmailDomain = dummyEmailDomain;
	}

	@Override
	public String getEmail() {
		if (mDummyEmail) {
			return EmailObfuscator.generateDummyEmailFrom(super.getEmail(), mDummyEmailDomain);
		}
		return super.getEmail();
	}

	@Override
	public int getRelationship() {
		int relationship = super.getRelationship();
		if (relationship == Attendees.RELATIONSHIP_ORGANIZER) {
			relationship = Attendees.RELATIONSHIP_ATTENDEE;
		}
		return relationship;
	}

	@Override
	public int getStatus() {
		int status = super.getStatus();
		if (status == Attendees.ATTENDEE_STATUS_NONE) {
			status = Attendees.ATTENDEE_STATUS_INVITED;
		}
		return status;
	}
}
