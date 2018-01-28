package com.dizzl.android.CalendarClonerFree;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class ProxyEvent implements Event {
    private final Event mSource;

    public ProxyEvent(Event source) {
        mSource = source;
    }

    public Event getSource() {
        return mSource;
    }

    @Override
    public String toString() {
        String result = this.getClass().getCanonicalName() + ":\n";
        result += "Title: " + this.getTitle() + "\n";
        result += "Location: " + this.getLocation() + "\n";
        result += "Status: " + this.getStatus() + "\n";
        return result;
    }

    @Override
    public long getId() {
        return mSource.getId();
    }

    @Override
    public long getCalendarId() {
        return mSource.getCalendarId();
    }

    @Override
    public boolean isDeleted() {
        return mSource.isDeleted();
    }

    @Override
    public boolean isDirty() {
        return mSource.isDirty();
    }

    @Override
    public String getOrganizer() {
        return mSource.getOrganizer();
    }

    @Override
    public String getTitle() {
        return mSource.getTitle();
    }

    @Override
    public String getLocation() {
        return mSource.getLocation();
    }

    @Override
    public String getDescription() {
        return mSource.getDescription();
    }

    @Override
    public DateTime getStartTime() {
        return mSource.getStartTime();
    }

    @Override
    public DateTime getEndTime() {
        return mSource.getEndTime();
    }

    @Override
    public String getDuration() {
        return mSource.getDuration();
    }

    @Override
    public boolean isAllDay() {
        return mSource.isAllDay();
    }

    @Override
    public DateTimeZone getStartTimeZone(boolean allowNull) {
        return mSource.getStartTimeZone(allowNull);
    }

    @Override
    public DateTimeZone getEndTimeZone() {
        return mSource.getEndTimeZone();
    }

    @Override
    public String getRecurrenceRule() {
        return mSource.getRecurrenceRule();
    }

    @Override
    public String getRecurrenceDate() {
        return mSource.getRecurrenceDate();
    }

    @Override
    public String getRecurrenceExRule() {
        return mSource.getRecurrenceExRule();
    }

    @Override
    public String getRecurrenceExDate() {
        return mSource.getRecurrenceExDate();
    }

    @Override
    public DateTime getLastDate() {
        return mSource.getLastDate();
    }

    @Override
    public long getOriginalID() {
        return mSource.getOriginalID();
    }

    @Override
    public boolean isOriginalAllDay() {
        return mSource.isOriginalAllDay();
    }

    @Override
    public DateTime getOriginalInstanceTime() {
        return mSource.getOriginalInstanceTime();
    }

    @Override
    public int getAccessLevel() {
        return mSource.getAccessLevel();
    }

    @Override
    public int getAvailability() {
        return mSource.getAvailability();
    }

    @Override
    public int getAvailabilitySamsung() {
        return mSource.getAvailabilitySamsung();
    }

    @Override
    public boolean hasAvailabilitySamsung() {
        return mSource.hasAvailabilitySamsung();
    }

    @Override
    public int getStatus() {
        return mSource.getStatus();
    }

    @Override
    public boolean hasStatus() {
        return mSource.hasStatus();
    }

    @Override
    public int getSelfAttendeeStatus() {
        return mSource.getSelfAttendeeStatus();
    }

    @Override
    public String getUniqueId() {
        return mSource.getUniqueId();
    }

    @Override
    public boolean isRecurringEvent() {
        return mSource.isRecurringEvent();
    }

    @Override
    public boolean isRecurringEventException() {
        return mSource.isRecurringEventException();
    }

    @Override
    public boolean isSingleEvent() {
        return mSource.isSingleEvent();
    }

    @Override
    public Period getPeriod() {
        return mSource.getPeriod();
    }

    @Override
    public DateTimeZone getCalendarTimeZone() {
        return mSource.getCalendarTimeZone();
    }
}
