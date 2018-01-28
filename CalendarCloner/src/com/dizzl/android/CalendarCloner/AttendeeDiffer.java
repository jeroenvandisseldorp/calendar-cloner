package com.dizzl.android.CalendarCloner;

import android.content.ContentValues;

public class AttendeeDiffer extends Differ {
	private AttendeesTable mAttendeeTable = new AttendeesTable(ClonerApp.getDb(true));

	public void compare(Attendee att, Attendee clone,
			boolean resetStatus, boolean cloneStatusReverse,
			ContentValues delta) {
		if (clone.getId() == 0) {
			// Compare regular event fields
			this.compareField(mAttendeeTable.ATTENDEE_NAME, att.getName(),
					clone.getName(), true, delta);
			this.compareField(mAttendeeTable.ATTENDEE_EMAIL, att.getEmail(),
					clone.getEmail(), true, delta);
			this.compareField(mAttendeeTable.ATTENDEE_RELATIONSHIP,
					"" + att.getRelationship(), "" + clone.getRelationship(),
					true, delta);
			this.compareField(mAttendeeTable.ATTENDEE_TYPE, "" + att.getType(),
					"" + clone.getType(), true, delta);
		}
		if (clone.getId() == 0
				|| resetStatus
				|| (!cloneStatusReverse && att.getStatus() != clone.getStatus())
				|| (DbAttendee.attendeeStatusIsAResponse(att.getStatus()) && !DbAttendee
						.attendeeStatusIsAResponse(clone.getStatus()))) {
			this.compareField(mAttendeeTable.ATTENDEE_STATUS,
					"" + att.getStatus(), "" + clone.getStatus(),
					clone.getId() == 0, delta);
		}
	}
}
