package com.dizzl.android.CalendarCloner;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public interface Event {
	public long getId();
	public long getCalendarId();
	public boolean isDeleted();
	public boolean isDirty();
	public String getOrganizer();
	public String getTitle();
	public String getLocation();
	public String getDescription();
	public DateTime getStartTime();
	public DateTime getEndTime();
	public String getDuration();
	public boolean isAllDay();
	public DateTimeZone getStartTimeZone(boolean allowNull);
	public DateTimeZone getEndTimeZone();
	public String getRecurrenceRule();
	public String getRecurrenceDate();
	public String getRecurrenceExRule();
	public String getRecurrenceExDate();
	public DateTime getLastDate();
	public long getOriginalID();
	public boolean isOriginalAllDay();
	public DateTime getOriginalInstanceTime();
	public int getAccessLevel();
	public int getAvailability();
	public int getAvailabilitySamsung();
	public boolean hasAvailabilitySamsung();
	public int getStatus();
	public boolean hasStatus();
	public int getSelfAttendeeStatus();
	public String getUniqueId();
	public boolean isRecurringEvent();
	public boolean isRecurringEventException();
	public boolean isSingleEvent();
	public Period getPeriod();
	public DateTimeZone getCalendarTimeZone();
}
