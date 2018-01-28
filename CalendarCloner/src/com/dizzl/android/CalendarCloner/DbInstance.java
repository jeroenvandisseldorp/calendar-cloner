package com.dizzl.android.CalendarCloner;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class DbInstance extends DbEvent {
	private final DbObject mObject;
	private final InstancesTable mTable;
	private static final DateTimeZone UTC_ZONE = DateTimeZone.forID("UTC");

	// Instance fields
	private DbObject.LongField mBeginLong, mEndLong;
	private DateTime mBegin, mEnd;
	private DbObject.IntegerField mStartDay, mStartMinute, mEndDay, mEndMinute;
	private DbObject.LongField mEventId;

	public DbInstance(InstancesTable table, DbObject obj) {
		super(table, obj);
		mTable = table;
		mObject = obj;
	}

	@Override
	public void loadAll() {
        try {
            this.getBegin();
            this.getEnd();
            this.getStartDay();
            this.getStartMinute();
            this.getEndDay();
            this.getEndMinute();
            this.getEventId();
        } finally {
            // Load super and release cursor
            super.loadAll();
        }
	}

	public DateTime getBegin() {
		if (mBegin == null) {
			mBeginLong = new DbObject.LongField(mTable.BEGIN);
			long beginLong = mObject.loadField(mBeginLong);

			if (this.isAllDay()) {
				// Convert to start of day in the calendar's time zone
				DateTime date = new DateTime(beginLong, UTC_ZONE);
				mBegin = new DateTime(date.year().get(), date.monthOfYear().get(), date.dayOfMonth().get(), 0, 0,
						getCalendarTimeZone());
			} else {
				mBegin = new DateTime(beginLong, getStartTimeZone(false));
			}
		}

		return mBegin;
	}

	public DateTime getEnd() {
		if (mEnd == null) {
			if (this.isAllDay()) {
				mEnd = getBegin().plusDays(1);
			} else {
				mEndLong = new DbObject.LongField(mTable.END);
				mEnd = new DateTime(mObject.loadField(mEndLong), getEndTimeZone() != null ? getEndTimeZone()
						: getStartTimeZone(false));
			}
		}

		return mEnd;
	}

	public int getStartDay() {
		if (mStartDay == null) {
			mStartDay = new DbObject.IntegerField(mTable.START_DAY);
		}
		return mObject.loadField(mStartDay);
	}

	public int getStartMinute() {
		if (mStartMinute == null) {
			mStartMinute = new DbObject.IntegerField(mTable.START_MINUTE);
		}
		return mObject.loadField(mStartMinute);
	}

	public int getEndDay() {
		if (mEndDay == null) {
			mEndDay = new DbObject.IntegerField(mTable.END_DAY);
		}
		return mObject.loadField(mEndDay);
	}

	public int getEndMinute() {
		if (mEndMinute == null) {
			mEndMinute = new DbObject.IntegerField(mTable.END_MINUTE);
		}
		return mObject.loadField(mEndMinute);
	}

	public long getEventId() {
		if (mEventId == null) {
			mEventId = new DbObject.LongField(mTable.EVENT_ID);
		}
		return mObject.loadField(mEventId);
	}

	@Override
	public DateTime getStartTime() {
		return this.getBegin();
	}

	@Override
	public DateTime getEndTime() {
		return this.getEnd();
	}

	@Override
	public boolean isRecurringEvent() {
		return false;
	}

	@Override
	public boolean isRecurringEventException() {
		return false;
	}

	@Override
	public String getUniqueId() {
		if (super.isRecurringEvent()) {
			// Generate unique ids for each instance of a recurring event
			return super.getUniqueId() + "@" + Utilities.dateTimeToTimeString(this.getBegin());
		}
		return super.getUniqueId();
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
