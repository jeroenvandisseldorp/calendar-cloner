package com.dizzl.android.CalendarCloner;

import android.provider.CalendarContract.Attendees;

public class AttendeeStatuses extends SelectList {
	public static final int ATTENDEE_STATUS_ACCEPTED = Attendees.ATTENDEE_STATUS_ACCEPTED;
	public static final int ATTENDEE_STATUS_DECLINED = Attendees.ATTENDEE_STATUS_DECLINED;
	public static final int ATTENDEE_STATUS_INVITED = Attendees.ATTENDEE_STATUS_INVITED;
	public static final int ATTENDEE_STATUS_NONE = Attendees.ATTENDEE_STATUS_NONE;
	public static final int ATTENDEE_STATUS_TENTATIVE = Attendees.ATTENDEE_STATUS_TENTATIVE;

	public static final int[] mKeys = new int[] { ATTENDEE_STATUS_NONE, ATTENDEE_STATUS_INVITED,
			ATTENDEE_STATUS_TENTATIVE, ATTENDEE_STATUS_ACCEPTED, ATTENDEE_STATUS_DECLINED };

	public static final String[] mNames = new String[] { ClonerApp.translate(R.string.attendee_status_none),
			ClonerApp.translate(R.string.attendee_status_invited),
			ClonerApp.translate(R.string.attendee_status_tentative),
			ClonerApp.translate(R.string.attendee_status_accepted),
			ClonerApp.translate(R.string.attendee_status_declined) };

	public AttendeeStatuses(boolean allSelected) {
		super(allSelected);
	}

	public AttendeeStatuses clone() {
		AttendeeStatuses other = new AttendeeStatuses(false);
		other.decode(this.toString());
		return other;
	}

	@Override
	protected void init() {
		this.init(mKeys, mNames);
	}
}
