package com.dizzl.android.CalendarClonerFree;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.CalendarContract.Attendees;

import java.util.LinkedList;
import java.util.List;

public class DbAttendee implements Attendee {
	private DbObject mObject;
	private AttendeesTable mTable;

	// Attendee fields
	private DbObject.LongField mId, mEventId;
	private DbObject.StringField mName, mEmail;
	private DbObject.IntegerField mRelationship, mStatus, mType;

	public static DbAttendee getByEmail(AttendeesTable table, long eventId, String attendeeEmail) {
		DbAttendee result = null;
		Cursor cur = table.query("((" + table.EVENT_ID.getName() + "=? AND " + table.ATTENDEE_EMAIL.getName() + "=?))",
				new String[] { "" + eventId, attendeeEmail }, table.ATTENDEE_NAME.getName() + " ASC");
		if (cur != null) {
			try {
				if (cur.moveToNext()) {
					result = new DbAttendee(table, new DbObject(cur));
					result.loadAll();
				}
			} finally {
				cur.close();
			}
		}
		return result;
	}

	public static List<DbAttendee> getByEvent(AttendeesTable table, long eventId, AttendeeDeltas deltas) {
		List<DbAttendee> attendees = new LinkedList<DbAttendee>();
		Cursor cur = table.query("((" + table.EVENT_ID.getName() + "=?))", new String[] { "" + eventId },
				table.ATTENDEE_NAME.getName() + " ASC, " + table.ATTENDEE_EMAIL.getName() + " ASC");
		if (cur != null) {
			try {
				while (cur.moveToNext()) {
					String name = cur.getString(table.ATTENDEE_NAME.getColumnIndex());
					String email = cur.getString(table.ATTENDEE_EMAIL.getColumnIndex());
					ContentValues delta = deltas != null ? deltas.get(name, email) : null;
					DbObject obj = delta != null ? new DbObjectWithDelta(cur, delta) : new DbObject(cur);
					DbAttendee attendee = new DbAttendee(table, obj);
					attendee.loadAll();
					attendees.add(attendee);
				}
			} finally {
				cur.close();
			}
		}
		return attendees;
	}

	public static List<DbAttendee> getByEvent(AttendeesTable table, long eventId) {
		return DbAttendee.getByEvent(table, eventId, null);
	}
	
	public static AttendeeMap getByEventHashed(AttendeesTable table, long eventId) {
		AttendeeMap attendees = new AttendeeMap();
		Cursor cur = table.query("((" + table.EVENT_ID.getName() + "=?))", new String[] { "" + eventId },
				table._ID.getName() + " DESC");
		if (cur != null) {
			try {
				while (cur.moveToNext()) {
					DbAttendee attendee = new DbAttendee(table, new DbObject(cur));
					attendee.loadAll();
					attendees.put(attendee.getName(), attendee.getEmail(), attendee);
				}
			} finally {
				cur.close();
			}
		}
		return attendees;
	}

	public static DbAttendee getById(AttendeesTable table, long attendeeId, ContentValues delta) {
		Cursor cur = table.getById(attendeeId);
		if (cur != null) {
			try {
				if (cur.moveToNext()) {
					DbObject obj = delta != null ? new DbObjectWithDelta(cur, delta) : new DbObject(cur);
					DbAttendee result = new DbAttendee(table, obj);
					result.loadAll();
					return result;
				}
			} finally {
				cur.close();
			}
		}
		
		if (delta != null) {
			return new DbAttendee(table, new DbObjectWithDelta(null, delta));
		}

		return null;
	}

	public static DbAttendee getById(AttendeesTable table, long attendeeId) {
		return DbAttendee.getById(table, attendeeId, null);
	}
	
	public static boolean attendeeStatusIsAResponse(int status) {
		return (status != Attendees.ATTENDEE_STATUS_NONE && status != Attendees.ATTENDEE_STATUS_INVITED);
	}

	public DbAttendee(AttendeesTable table, DbObject obj) {
		mTable = table;
		mObject = obj;
	}

	@Override
	public String toString() {
		String result = "";
		if (mName != null) {
			result = Utilities.appendToString(result, mName.isLoaded, mName.value);
		}
		if (mEmail != null) {
			result = Utilities.appendToString(result, mEmail.isLoaded, mEmail.value);
		}
		if (mRelationship != null) {
			result = Utilities.appendToString(result, mRelationship.isLoaded,
					new AttendeeRelationships(true).getKeyName(mRelationship.value));
		}
		if (mType != null) {
			result = Utilities.appendToString(result, mType.isLoaded, new AttendeeTypes(true).getKeyName(mType.value));
		}
		if (mStatus != null) {
			result = Utilities.appendToString(result, mStatus.isLoaded,
					new AttendeeStatuses(true).getKeyName(mStatus.value));
		}
		return result;
	}

	public void loadAll() {
        try {
            this.getId();
            this.getName();
            this.getEmail();
            this.getRelationship();
            this.getType();
            this.getStatus();
        } finally {
            // Release cursor
            mObject.releaseCursor();
        }
	}

	public long getId() {
		if (mId == null) {
			mId = new DbObject.LongField(mTable._ID);
		}
		return mObject.loadField(mId);
	}

	public long getEventId() {
		if (mEventId == null) {
			mEventId = new DbObject.LongField(mTable.EVENT_ID);
		}
		return mObject.loadField(mEventId);
	}

	public String getName() {
		if (mName == null) {
			mName = new DbObject.StringField(mTable.ATTENDEE_NAME);
		}
		return mObject.loadField(mName);
	}

	public String getEmail() {
		if (mEmail == null) {
			mEmail = new DbObject.StringField(mTable.ATTENDEE_EMAIL);
		}
		return mObject.loadField(mEmail);
	}

	public int getRelationship() {
		if (mRelationship == null) {
			mRelationship = new DbObject.IntegerField(mTable.ATTENDEE_RELATIONSHIP);
		}
		return mObject.loadField(mRelationship);
	}

	public int getType() {
		if (mType == null) {
			mType = new DbObject.IntegerField(mTable.ATTENDEE_TYPE);
		}
		return mObject.loadField(mType);
	}

	public int getStatus() {
		if (mStatus == null) {
			mStatus = new DbObject.IntegerField(mTable.ATTENDEE_STATUS);
		}
		return mObject.loadField(mStatus);
	}
}
