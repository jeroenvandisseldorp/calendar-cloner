package com.dizzl.android.CalendarCloner;

import android.provider.CalendarContract.Attendees;

public class AttendeeTypes extends SelectList {
	public static final int ATTENDEE_TYPE_NONE = Attendees.TYPE_NONE;
	public static final int ATTENDEE_TYPE_OPTIONAL = Attendees.TYPE_OPTIONAL;
	public static final int ATTENDEE_TYPE_REQUIRED = Attendees.TYPE_REQUIRED;

	public static final int[] mKeys = new int[] { ATTENDEE_TYPE_NONE, ATTENDEE_TYPE_OPTIONAL, ATTENDEE_TYPE_REQUIRED };

	public static final String[] mNames = new String[] { ClonerApp.translate(R.string.attendee_type_none),
			ClonerApp.translate(R.string.attendee_type_optional), ClonerApp.translate(R.string.attendee_type_required) };

	public AttendeeTypes(boolean allSelected) {
		super(allSelected);
	}

	public AttendeeTypes clone() {
		AttendeeTypes other = new AttendeeTypes(false);
		other.decode(this.toString());
		return other;
	}

	@Override
	protected void init() {
		this.init(mKeys, mNames);
	}
}
