package com.dizzl.android.CalendarClonerFree;

import android.content.ContentValues;
import android.database.Cursor;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.LinkedList;
import java.util.List;

public class DbEvent implements Event {
	private DbObject mObject = null;
	private final EventsTable mTable;
	private static final DateTimeZone UTC_ZONE = DateTimeZone.forID("UTC");

	// Event meta fields
	private DbObject.LongField mId, mCalendarId;
	private DbObject.StringField mSyncId;
	private DbObject.BooleanField mLastSynced, mDeleted, mDirty;
	private DbObject.StringField mAccountType;
	private DbObject.StringField mSyncData2;
	private DbObject.StringField mUid2445;

	// Event fields
	private DbObject.StringField mOrganizer;
	private DbObject.StringField mTitle, mLocation, mDescription;
	private DbObject.LongField mStartTimeLong, mEndTimeLong;
	private DateTime mStartTime, mEndTime;
	private DbObject.StringField mDuration;
	private DbObject.BooleanField mAllDay;
	private DbObject.StringField mStartTimeZoneStr, mEndTimeZoneStr;
	private DateTimeZone mStartTimeZone, mEndTimeZone;
	private DbObject.StringField mRecurrenceRule, mRecurrenceDate;
	private DbObject.StringField mRecurrenceExRule, mRecurrenceExDate;
	private DbObject.LongField mLastDateLong;
	private DateTime mLastDate;
	private DbObject.LongField mOriginalID;
	private DbObject.BooleanField mOriginalAllDay;
	private DbObject.LongField mOriginalInstanceTimeLong;
	private DateTime mOriginalInstanceTime;
	private DbObject.IntegerField mAccessLevel;
	private DbObject.IntegerFieldWithNull mAvailability, mAvailabilitySamsung;
	private DbObject.IntegerFieldWithNull mStatus;
	private DbObject.IntegerField mSelfAttendeeStatus;

	// HTC fields
	private DbObject.StringField mHtcIcalGuid;

	public static List<Event> getAll(EventsTable table) {
		List<Event> events = new LinkedList<Event>();
		Cursor cur = table.query(null, null, null);
		if (cur != null) {
			try {
				while (cur.moveToNext()) {
					DbEvent event = new DbEvent(table, new DbObject(cur));
					event.loadAll();
					events.add(event);
				}
			} finally {
				cur.close();
			}
		}
		return events;
	}

	public static Event get(EventsTable table, long id, ContentValues delta) {
		Cursor cur = table.getById(id);
		if (cur != null) {
			try {
				if (cur.moveToNext()) {
					DbObject obj = delta != null ? new DbObjectWithDelta(cur, delta) : new DbObject(cur);
					DbEvent result = new DbEvent(table, obj);
					result.loadAll();
					return result;
				}
			} finally {
				cur.close();
			}
		}

		if (delta != null) {
			return new DbEvent(table, new DbObjectWithDelta(null, delta));
		}

		return null;
	}

	public static Event get(EventsTable table, long id) {
		return DbEvent.get(table, id, null);
	}

	public DbEvent(EventsTable table, DbObject obj) {
		mTable = table;
		mObject = obj;
	}

	public DbEvent(EventsTable table, DbCalendar parent) {
		mTable = table;
		mObject = new DbObject(null);
		mAccountType = new DbObject.StringField(mTable.ACCOUNT_TYPE);
		mAccountType.isLoaded = true;
		mAccountType.value = parent.getAccountType();
	}

	@Override
	public String toString() {
		String result = "";
		if (mId.isLoaded) {
			result += "Id: " + mId.value + "\n";
		}
		if (mTitle.isLoaded) {
			result += "Title: " + mTitle.value + "\n";
		}
		if (mStartTimeLong.isLoaded) {
			result += "Start: " + mStartTimeLong.value + "\n";
		}
		if (mLocation.isLoaded) {
			result += "Location: " + mLocation.value + "\n";
		}
		return result;
	}

	public void loadAll() {
        try {
            this.getId();
            this.getCalendarId();
            this.getSyncId();
            this.isLastSynced();
            this.isDeleted();
            this.isDirty();
            this.getAccountType();
            this.getSyncData2();
            this.getUid2445();

            this.getOrganizer();
            this.getTitle();
            this.getLocation();
            this.getDescription();
            this.getStartTime();
            this.getEndTime();
            this.getDuration();
            this.isAllDay();
            this.getStartTimeZone(true);
            this.getEndTimeZone();
            this.getRecurrenceRule();
            this.getRecurrenceDate();
            this.getRecurrenceExRule();
            this.getRecurrenceExDate();
            this.getLastDate();
            this.getOriginalID();
            this.isOriginalAllDay();
            this.getOriginalInstanceTime();
            this.getAccessLevel();
            this.getAvailability();
            this.getAvailabilitySamsung();
            this.getStatus();
            this.getSelfAttendeeStatus();

            this.getHtcIcalGuid();
        } finally {
            // Release cursor
            mObject.releaseCursor();
        }
	}

	@Override
	public long getId() {
		if (mId == null) {
			mId = new DbObject.LongField(mTable._ID);
		}
		return mObject.loadField(mId);
	}

	@Override
	public long getCalendarId() {
		if (mCalendarId == null) {
			mCalendarId = new DbObject.LongField(mTable.CALENDAR_ID);
		}
		return mObject.loadField(mCalendarId);
	}

	public String getSyncId() {
		if (mSyncId == null) {
			mSyncId = new DbObject.StringField(mTable._SYNC_ID);
		}
		return mObject.loadField(mSyncId);
	}

	public boolean isLastSynced() {
		if (mLastSynced == null) {
			mLastSynced = new DbObject.BooleanField(mTable.LAST_SYNCED);
		}
		return mObject.loadField(mLastSynced);
	}

	@Override
	public boolean isDeleted() {
		if (mDeleted == null) {
			mDeleted = new DbObject.BooleanField(mTable.DELETED);
		}
		return mObject.loadField(mDeleted);
	}

	@Override
	public boolean isDirty() {
		if (mDirty == null) {
			mDirty = new DbObject.BooleanField(mTable.DIRTY);
		}
		return mObject.loadField(mDirty);
	}

	public String getAccountType() {
		if (mAccountType == null) {
			mAccountType = new DbObject.StringField(mTable.ACCOUNT_TYPE);
		}
		return mObject.loadField(mAccountType);
	}

	public String getSyncData2() {
		if (mSyncData2 == null) {
			mSyncData2 = new DbObject.StringField(mTable.SYNC_DATA2);
		}
		return mObject.loadField(mSyncData2);
	}

	public String getUid2445() {
		if (mUid2445 == null) {
			mUid2445 = new DbObject.StringField(mTable.UID_2445);
		}
		return mObject.loadField(mUid2445);
	}

	@Override
	public String getOrganizer() {
		if (mOrganizer == null) {
			mOrganizer = new DbObject.StringField(mTable.ORGANIZER);
		}
		return mObject.loadField(mOrganizer);
	}

	@Override
	public String getTitle() {
		if (mTitle == null) {
			mTitle = new DbObject.StringField(mTable.TITLE);
		}
		return mObject.loadField(mTitle);
	}

	@Override
	public String getLocation() {
		if (mLocation == null) {
			mLocation = new DbObject.StringField(mTable.EVENT_LOCATION);
		}
		return mObject.loadField(mLocation);
	}

	@Override
	public String getDescription() {
		if (mDescription == null) {
			mDescription = new DbObject.StringField(mTable.DESCRIPTION);
		}
		return mObject.loadField(mDescription);
	}

	@Override
	public DateTime getStartTime() {
		if (mStartTime == null) {
			mStartTimeLong = new DbObject.LongField(mTable.DTSTART);
			long startTimeLong = mObject.loadField(mStartTimeLong);

			if (this.isAllDay()) {
				// Convert to start of day in the calendar's time zone
				DateTime date = new DateTime(startTimeLong, UTC_ZONE);
				mStartTime = new DateTime(date.year().get(), date.monthOfYear().get(), date.dayOfMonth().get(), 0, 0,
						getCalendarTimeZone());
			} else {
				mStartTime = new DateTime(startTimeLong, getStartTimeZone(false));
			}
		}

		return mStartTime;
	}

	@Override
	public DateTime getEndTime() {
		if (mEndTime == null) {
			mEndTimeLong = new DbObject.LongField(mTable.DTEND);
			long endTimeLong = mObject.loadField(mEndTimeLong);

			if (this.isAllDay()) {
				DateTime date = new DateTime(endTimeLong, UTC_ZONE);
				mEndTime = new DateTime(date.year().get(), date.monthOfYear().get(), date.dayOfMonth().get(), 0, 0,
						getCalendarTimeZone());
			} else {
				mEndTime = new DateTime(endTimeLong, getEndTimeZone() != null ? getEndTimeZone() : getStartTimeZone(false));
			}
		}

		return mEndTime;
	}

	@Override
	public String getDuration() {
		if (mDuration == null) {
			mDuration = new DbObject.StringField(mTable.DURATION);
		}
		return mObject.loadField(mDuration);
	}

	@Override
	public boolean isAllDay() {
		if (mAllDay == null) {
			mAllDay = new DbObject.BooleanField(mTable.ALL_DAY);
		}
		return mObject.loadField(mAllDay);
	}

	@Override
	public DateTimeZone getStartTimeZone(boolean allowNull) {
		if (mStartTimeZone == null) {
			if (mStartTimeZoneStr == null) {
				mStartTimeZoneStr = new DbObject.StringField(mTable.EVENT_TIMEZONE);
			}
			String startTZ = mObject.loadField(mStartTimeZoneStr);

			if (startTZ != null && !startTZ.contentEquals("")) {
				mStartTimeZone = DateTimeZone.forID(startTZ);
			}
		}

		if (mStartTimeZone == null && !allowNull) {
			return DateTimeZone.getDefault();
		}

		return mStartTimeZone;
	}

	@Override
	public DateTimeZone getEndTimeZone() {
		if (mEndTimeZone == null && mEndTimeZoneStr == null) {
			mEndTimeZoneStr = new DbObject.StringField(mTable.EVENT_END_TIMEZONE);
			String endTZ = mObject.loadField(mEndTimeZoneStr);

			if (endTZ != null && !endTZ.contentEquals("")) {
				mEndTimeZone = DateTimeZone.forID(endTZ);
			}
		}

		return mEndTimeZone;
	}

	@Override
	public String getRecurrenceRule() {
		if (mRecurrenceRule == null) {
			mRecurrenceRule = new DbObject.StringField(mTable.RRULE);
		}
		return mObject.loadField(mRecurrenceRule);
	}

	@Override
	public String getRecurrenceDate() {
		if (mRecurrenceDate == null) {
			mRecurrenceDate = new DbObject.StringField(mTable.RDATE);
		}
		return mObject.loadField(mRecurrenceDate);
	}

	@Override
	public String getRecurrenceExRule() {
		if (mRecurrenceExRule == null) {
			mRecurrenceExRule = new DbObject.StringField(mTable.EXRULE);
		}
		return mObject.loadField(mRecurrenceExRule);
	}

	@Override
	public String getRecurrenceExDate() {
		if (mRecurrenceExDate == null) {
			mRecurrenceExDate = new DbObject.StringField(mTable.EXDATE);
		}
		return mObject.loadField(mRecurrenceExDate);
	}

	@Override
	public DateTime getLastDate() {
		if (mLastDate == null) {
			if (mLastDateLong == null) {
				mLastDateLong = new DbObject.LongField(mTable.LAST_DATE);
			}
			mLastDate = new DateTime(mObject.loadField(mLastDateLong), getEndTimeZone() != null ? getEndTimeZone()
					: getStartTimeZone(false));
		}

		return mLastDate;
	}

	@Override
	public long getOriginalID() {
		if (mOriginalID == null) {
			mOriginalID = new DbObject.LongField(mTable.ORIGINAL_ID);
		}
		return mObject.loadField(mOriginalID);
	}

	@Override
	public boolean isOriginalAllDay() {
		if (mOriginalAllDay == null) {
			mOriginalAllDay = new DbObject.BooleanField(mTable.ORIGINAL_ALL_DAY);
		}
		return mObject.loadField(mOriginalAllDay);
	}

	@Override
	public DateTime getOriginalInstanceTime() {
		if (mOriginalInstanceTime == null) {
			mOriginalInstanceTimeLong = new DbObject.LongField(mTable.ORIGINAL_INSTANCE_TIME);
			long oitLong = mObject.loadField(mOriginalInstanceTimeLong);

			mOriginalInstanceTime = new DateTime(oitLong, getStartTimeZone(false));
		}

		return mOriginalInstanceTime;
	}

	@Override
	public int getAccessLevel() {
		if (mAccessLevel == null) {
			mAccessLevel = new DbObject.IntegerField(mTable.ACCESS_LEVEL);
		}
		return mObject.loadField(mAccessLevel);
	}

	@Override
	public int getAvailability() {
		if (mAvailability == null) {
			mAvailability = new DbObject.IntegerFieldWithNull(mTable.AVAILABILITY);
		}
		return mObject.loadField(mAvailability);
	}

	@Override
	public int getAvailabilitySamsung() {
		if (mAvailabilitySamsung == null) {
			mAvailabilitySamsung = new DbObject.IntegerFieldWithNull(mTable.AVAILABILITY_SAMSUNG);
		}
		return mObject.loadField(mAvailabilitySamsung);
	}

	@Override
	public boolean hasAvailabilitySamsung() {
		if (!Device.Samsung.supportsAvailabilitySamsung(this.getAccountType())) {
			return false;
		}
		this.getAvailabilitySamsung();
		return mAvailabilitySamsung.isLoaded && !mAvailabilitySamsung.isNull && this.getId() > 0;
	}

	@Override
	public int getStatus() {
		if (mStatus == null) {
			mStatus = new DbObject.IntegerFieldWithNull(mTable.STATUS);
		}
		return mObject.loadField(mStatus);
	}

	@Override
	public boolean hasStatus() {
		this.getStatus();
		return mStatus.isLoaded && !mStatus.isNull && this.getId() > 0;
	}

	@Override
	public int getSelfAttendeeStatus() {
		if (mSelfAttendeeStatus == null) {
			mSelfAttendeeStatus = new DbObject.IntegerField(mTable.SELF_ATTENDEE_STATUS);
		}
		return mObject.loadField(mSelfAttendeeStatus);
	}

	public String getHtcIcalGuid() {
		if (mHtcIcalGuid == null) {
			mHtcIcalGuid = new DbObject.StringField(mTable.HTC_ICAL_GUID);
		}
		return mObject.loadField(mHtcIcalGuid);
	}

	@Override
	public String getUniqueId() {
		if (this.isDeleted()) {
			return "";
		}

		// Try to get reasonably static ids for events across devices/OSes
		String accountType = this.getAccountType();
		if (accountType != null && accountType.contentEquals("com.google")) {
			String possibleId = this.getSyncId();
			if (possibleId != null) {
				return possibleId;
			}
			return "";
		}
		if (accountType != null && accountType.contentEquals("com.android.exchange")) {
			String possibleId = this.getSyncData2();
			if (possibleId != null && possibleId.length() > 2) {
				return possibleId;
			}
			possibleId = this.getSyncId();
			if (possibleId != null) {
				return possibleId;
			}
			return "";
		}

		// Try HTC fields, but not for recurring exceptions (same id as parent)
		if (!this.isRecurringEventException()) {
			String possibleId = this.getHtcIcalGuid();
			if (possibleId != null) {
				return possibleId;
			}
		}

		// Try Uid2445 field (Jelly Bean MR1+)
		String possibleId = this.getUid2445();
		if (possibleId != null) {
			return possibleId;
		}

		if (this.getId() > 0) {
			return "" + this.getId();
		}
		return "";
	}

	@Override
	public boolean isRecurringEvent() {
		return this.getDuration() != null;
	}

	@Override
	public boolean isRecurringEventException() {
		return this.getOriginalID() != 0;
	}

	@Override
	public boolean isSingleEvent() {
		return !this.isRecurringEvent() && !this.isRecurringEventException();
	}

	@Override
	public Period getPeriod() {
		if (!this.isRecurringEvent()) {
			// Simplest case: event period is from start to end
			return new Period(this.getStartTime(), this.getEndTime());
		}

		if (this.getLastDate() != null) {
			// Return period between start time and last date
			return new Period(this.getStartTime(), this.getLastDate());
		}

		return new Period(this.getStartTime(), Utilities.INFINITY);
	}

	@Override
	public DateTimeZone getCalendarTimeZone() {
		DbCalendar cal = CalendarLoader.getCalendar(this.getCalendarId());
		return cal != null ? cal.getTimeZone() : null;
	}
}
