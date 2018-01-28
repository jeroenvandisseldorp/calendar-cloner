package com.dizzl.android.CalendarCloner;

import android.provider.CalendarContract.Attendees;

public class AttendeeRelationships extends SelectList {
	public static final int ATTENDEE_RELATIONSHIP_ATTENDEE = Attendees.RELATIONSHIP_ATTENDEE;
	public static final int ATTENDEE_RELATIONSHIP_NONE = Attendees.RELATIONSHIP_NONE;
	public static final int ATTENDEE_RELATIONSHIP_ORGANIZER = Attendees.RELATIONSHIP_ORGANIZER;
	public static final int ATTENDEE_RELATIONSHIP_PERFORMER = Attendees.RELATIONSHIP_PERFORMER;
	public static final int ATTENDEE_RELATIONSHIP_SPEAKER = Attendees.RELATIONSHIP_SPEAKER;

	public static final int[] mKeys = new int[] { ATTENDEE_RELATIONSHIP_ATTENDEE, ATTENDEE_RELATIONSHIP_NONE,
			ATTENDEE_RELATIONSHIP_ORGANIZER, ATTENDEE_RELATIONSHIP_PERFORMER, ATTENDEE_RELATIONSHIP_SPEAKER };

	public static final String[] mNames = new String[] { ClonerApp.translate(R.string.attendee_relationship_attendee),
			ClonerApp.translate(R.string.attendee_relationship_none),
			ClonerApp.translate(R.string.attendee_relationship_organizer),
			ClonerApp.translate(R.string.attendee_relationship_performer),
			ClonerApp.translate(R.string.attendee_relationship_speaker) };

	public AttendeeRelationships(boolean allSelected) {
		super(allSelected);
	}

	public AttendeeRelationships clone() {
		AttendeeRelationships other = new AttendeeRelationships(false);
		other.decode(this.toString());
		return other;
	}

	@Override
	protected void init() {
		this.init(mKeys, mNames);
	}
}
