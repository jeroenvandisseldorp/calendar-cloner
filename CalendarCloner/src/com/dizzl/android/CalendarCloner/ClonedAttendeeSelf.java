package com.dizzl.android.CalendarCloner;

import android.provider.CalendarContract.Attendees;

public class ClonedAttendeeSelf extends ProxyAttendee {
	private String mSelfDisplayName;
	private String mSelfEmail;
	private boolean mSelfOrganizer;
	private boolean mCloneAttendeeStatus;
	private boolean mResetStatus;

	public ClonedAttendeeSelf(Attendee source, String selfDisplayName, String selfEmail, boolean selfOrganizer,
			boolean cloneAttendeeStatus, boolean resetStatus) {
		super(source);
		mSelfDisplayName = selfDisplayName;
		mSelfEmail = selfEmail;
		mSelfOrganizer = selfOrganizer;
		mCloneAttendeeStatus = cloneAttendeeStatus;
		mResetStatus = resetStatus;
	}

	@Override
	public String getName() {
		if (mSelfDisplayName == null || mSelfDisplayName.contentEquals("")) {
			return ClonerApp.translate(R.string.msg_you);
		}
		return mSelfDisplayName;
	}

	@Override
	public String getEmail() {
		return mSelfEmail;
	}

	@Override
	public int getRelationship() {
		return Attendees.RELATIONSHIP_ORGANIZER;
	}

	@Override
	public int getStatus() {
		int attendeeStatus = mCloneAttendeeStatus ? super.getStatus() : Attendees.ATTENDEE_STATUS_ACCEPTED;

		if (!mSelfOrganizer) {
			// Convert NONE to INVITED
			if (attendeeStatus == Attendees.ATTENDEE_STATUS_NONE) {
				attendeeStatus = Attendees.ATTENDEE_STATUS_INVITED;
			}
			// Reset status (e.g. when times of the original event changed)
			if (mResetStatus) {
				attendeeStatus = Attendees.ATTENDEE_STATUS_INVITED;
			}
		} else {
			// Convert NONE to ACCEPTED
			if (attendeeStatus == Attendees.ATTENDEE_STATUS_NONE) {
				attendeeStatus = Attendees.ATTENDEE_STATUS_ACCEPTED;
			}
			// Reset status (e.g. when times of the original event changed)
			if (mResetStatus) {
				attendeeStatus = Attendees.ATTENDEE_STATUS_ACCEPTED;
			}
		}
		return attendeeStatus;
	}
}
