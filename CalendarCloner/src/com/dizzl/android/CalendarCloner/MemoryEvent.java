package com.dizzl.android.CalendarCloner;

import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Events;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class MemoryEvent implements Event {
	private long mCalendarId;
	private String mTitle, mLocation, mDescription;
	private DateTime mStartTime;
	private DateTime mEndTime;
	private final DateTimeZone mCalendarTimeZone;
	private DateTimeZone mTimeZone;
	private int mAccessLevel = Events.ACCESS_DEFAULT;
	private int mAvailability = Events.AVAILABILITY_BUSY;
	private int mAvailabilitySamsung = Device.Samsung.AVAILABILITY_BUSY;
	private String mUniqueId;

	public MemoryEvent(DateTimeZone calendarTimeZone) {
		mCalendarTimeZone = calendarTimeZone;
	}

	@Override
	public long getId() {
		return 0;
	}

	@Override
	public long getCalendarId() {
		return mCalendarId;
	}

	@Override
	public boolean isDeleted() {
		return false;
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public String getOrganizer() {
		return null;
	}

	@Override
	public String getTitle() {
		return mTitle;
	}

	@Override
	public String getLocation() {
		return mLocation;
	}

	@Override
	public String getDescription() {
		return mDescription;
	}

	@Override
	public DateTime getStartTime() {
		if (mStartTime == null) {
			mStartTime = new DateTime();
		}
		return mStartTime;
	}

	@Override
	public DateTime getEndTime() {
		if (mEndTime == null) {
			mEndTime = new DateTime();
		}
		return mEndTime;
	}

	@Override
	public String getDuration() {
		return null;
	}

	@Override
	public boolean isAllDay() {
		return false;
	}

	@Override
	public DateTimeZone getStartTimeZone(boolean allowNull) {
		if (mTimeZone == null && !allowNull) {
			return DateTimeZone.getDefault();
		}
		return mTimeZone;
	}

	@Override
	public DateTimeZone getEndTimeZone() {
		return mTimeZone;
	}

	@Override
	public String getRecurrenceRule() {
		return null;
	}

	@Override
	public String getRecurrenceDate() {
		return null;
	}

	@Override
	public String getRecurrenceExRule() {
		return null;
	}

	@Override
	public String getRecurrenceExDate() {
		return null;
	}

	@Override
	public DateTime getLastDate() {
		return null;
	}

	@Override
	public long getOriginalID() {
		return 0;
	}

	@Override
	public boolean isOriginalAllDay() {
		return false;
	}

	@Override
	public DateTime getOriginalInstanceTime() {
		return null;
	}

	@Override
	public int getAccessLevel() {
		return mAccessLevel;
	}

	@Override
	public int getAvailability() {
		return mAvailability;
	}

	@Override
	public int getAvailabilitySamsung() {
		return mAvailabilitySamsung;
	}

	@Override
	public boolean hasAvailabilitySamsung() {
		return true;
	}

	@Override
	public int getStatus() {
		return Events.STATUS_CONFIRMED;
	}

	@Override
	public boolean hasStatus() {
		return false;
	}

	@Override
	public int getSelfAttendeeStatus() {
		return Attendees.ATTENDEE_STATUS_ACCEPTED;
	}

	@Override
	public String getUniqueId() {
		return mUniqueId;
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
	public boolean isSingleEvent() {
		return true;
	}

	@Override
	public Period getPeriod() {
		return new Period(this.getStartTime(), this.getEndTime());
	}

	@Override
	public DateTimeZone getCalendarTimeZone() {
		return mCalendarTimeZone;
	}

	public void setCalendarId(long calendarId) {
		mCalendarId = calendarId;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public void setLocation(String location) {
		mLocation = location;
	}

	public void setDescription(String description) {
		mDescription = description;
	}

	public void setStartTime(DateTime startTime) {
		mStartTime = startTime;
	}

	public void setEndTime(DateTime endTime) {
		mEndTime = endTime;
	}

	public void setTimeZone(DateTimeZone timeZone) {
		mTimeZone = timeZone;
	}

	public void setAccessLevel(int accessLevel) {
		mAccessLevel = accessLevel;
	}

	public void setAvailability(int availability) {
		mAvailability = availability;
	}

	public void setAvailabilitySamsung(int availabilitySamsung) {
		mAvailabilitySamsung = availabilitySamsung;
	}

	public void setUniqueId(String uniqueId) {
		mUniqueId = uniqueId;
	}
}
