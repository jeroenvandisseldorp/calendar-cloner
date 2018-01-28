package com.dizzl.android.CalendarClonerFree;

import android.content.ContentValues;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Events;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class LogLines {
    private static final String NULL = "null";
    private final List<LogLine> mLines = new ArrayList<LogLine>();
    private final LogLineFactory mLogLineFactory;
    private int mMaxLevel = 0;

    public LogLines(LogLineFactory logLineFactory) {
        mLogLineFactory = logLineFactory;
    }

    public boolean empty() {
        return mLines.size() == 0;
    }

    public List<LogLine> getLines() {
        return mLines;
    }

    public int getMaxLevel() {
        return mMaxLevel;
    }

    private String timeToString(DateTime time) {
        return Utilities.dateTimeToString(time) + " (" + time.getMillis() + ")";
    }

    private void add(LogLine line) {
        mLines.add(line);
        if (line != null && line.getLevel() > mMaxLevel) {
            mMaxLevel = line.getLevel();
        }
    }

    public void addEmptyLine() {
        this.add(mLogLineFactory.createLogLine(ClonerLog.LOG_INFO, null, "", "", ""));
    }

    public boolean isUsed() {
        return mLines.size() > 0;
    }

    public void log(int level, String logPrefix, String message) {
        this.add(mLogLineFactory.createLogLine(level, logPrefix, message));
    }

    public void log(int level, String logPrefix, String message, String eventTitle) {
        int size = 10;
        if (message != null) {
            size += message.length();
        }
        if (eventTitle != null) {
            size += eventTitle.length();
        }
        StringBuffer buf = new StringBuffer(size);
        buf.append(" ");
        if (message != null) {
            buf.append(message);
        }
        if (eventTitle != null) {
            buf.append(": ");
            buf.append(eventTitle);
        }
        this.add(mLogLineFactory.createLogLine(level, logPrefix, buf.toString()));
    }

    public int getLevel(String key, ContentValues delta) {
        if (delta != null && delta.containsKey(key)) {
            return ClonerLog.LOG_UPDATE;
        }
        return ClonerLog.LOG_INFO;
    }

    public int getLevel(String key, ContentValues srcDelta, ContentValues dstDelta) {
        if (srcDelta != null && srcDelta.containsKey(key)) {
            return ClonerLog.LOG_UPDATE;
        }
        if (dstDelta != null && dstDelta.containsKey(key)) {
            return ClonerLog.LOG_UPDATE;
        }
        return ClonerLog.LOG_INFO;
    }

    private String getAvailabilityDescription(Event event) {
        if (event.hasAvailabilitySamsung()) {
            int availability = event.getAvailabilitySamsung();
            return new AvailabilitiesSamsung(true).getKeyName(availability) + " (" + availability + "S)";
        }
        return new Availabilities(true).getKeyNameAndValue(event.getAvailability());
    }

    public void logEvent(Event event, ContentValues changedFields) {
        String logPrefix = null;

        if (event == null) {
            this.add(mLogLineFactory.createLogLine(ClonerLog.LOG_WARNING, logPrefix, "Logging null event"));
            return;
        }

        this.add(mLogLineFactory.createLogLine(this.getLevel(Events._ID, changedFields), logPrefix, "Id",
                "" + event.getId()));
        if (event instanceof DbEvent) {
            this.add(mLogLineFactory.createLogLine(this.getLevel(Events._SYNC_ID, changedFields), logPrefix, "Sync id",
                    ((DbEvent) event).getSyncId()));
        }
        this.add(mLogLineFactory.createLogLine(ClonerLog.LOG_INFO, logPrefix, "CC UID", event.getUniqueId()));
        this.add(mLogLineFactory.createLogLine(ClonerLog.LOG_INFO, logPrefix, "Event hash",
                EventMarker.getEventHash(event.getUniqueId())));
        this.add(mLogLineFactory.createLogLine(this.getLevel(Events.DELETED, changedFields), logPrefix, "Deleted",
                event.isDeleted() ? "Yes" : "No"));
        this.addEmptyLine();

        this.add(mLogLineFactory.createLogLine(ClonerLog.LOG_INFO, logPrefix, "Calendar",
                CalendarLoader.getCalendarNameOrErrorMessage(event.getCalendarId()) + " (Id: " + event.getCalendarId()
                        + ")"));
        this.add(mLogLineFactory.createLogLine(this.getLevel(Events.TITLE, changedFields), logPrefix, "Title",
                event.getTitle()));
        this.add(mLogLineFactory.createLogLine(this.getLevel(Events.EVENT_LOCATION, changedFields), logPrefix,
                "Location", event.getLocation()));
        this.add(mLogLineFactory.createLogLine(this.getLevel(Events.DTSTART, changedFields), logPrefix, "StartTime",
                this.timeToString(event.getStartTime())));
        // if (!event.isAllDay()) {
        if (!event.isRecurringEvent()) {
            this.add(mLogLineFactory.createLogLine(this.getLevel(Events.DTEND, changedFields), logPrefix, "End time",
                    this.timeToString(event.getEndTime())));
        } else {
            this.add(mLogLineFactory.createLogLine(this.getLevel(Events.DURATION, changedFields), logPrefix,
                    "Duration", event.getDuration()));
        }
        this.add(mLogLineFactory.createLogLine(this.getLevel(Events.ALL_DAY, changedFields), logPrefix, "All day",
                event.isAllDay() ? "Yes" : "No"));

        this.add(mLogLineFactory.createLogLine(this.getLevel(Events.ORGANIZER, changedFields), logPrefix, "Organizer",
                event.getOrganizer()));
        this.add(mLogLineFactory.createLogLine(this.getLevel(Events.DESCRIPTION, changedFields), logPrefix,
                "Description", event.getDescription()));

        if (event.isRecurringEvent()) {
            // Clone recurring event fields
            this.add(mLogLineFactory.createLogLine(this.getLevel(Events.RRULE, changedFields), logPrefix, "RRule",
                    event.getRecurrenceRule()));
            this.add(mLogLineFactory.createLogLine(this.getLevel(Events.RDATE, changedFields), logPrefix, "RDate",
                    event.getRecurrenceDate()));
            this.add(mLogLineFactory.createLogLine(this.getLevel(Events.EXRULE, changedFields), logPrefix, "ExRule",
                    event.getRecurrenceExRule()));
            this.add(mLogLineFactory.createLogLine(this.getLevel(Events.EXDATE, changedFields), logPrefix, "ExDate",
                    event.getRecurrenceExDate()));
            this.add(mLogLineFactory.createLogLine(
                    this.getLevel(Events.LAST_DATE, changedFields),
                    logPrefix,
                    "LastDate",
                    event.getLastDate() != null ? this.timeToString(event.getLastDate()) : ClonerApp
                            .translate(R.string.msg_infinite) + " (0)"));
        }
        if (event.isRecurringEventException()) {
            // Log recurring event exception fields
            this.add(mLogLineFactory.createLogLine(this.getLevel(Events.ORIGINAL_ID, changedFields), logPrefix,
                    "Original ID", "" + event.getOriginalID()));
            this.add(mLogLineFactory.createLogLine(this.getLevel(Events.ORIGINAL_ALL_DAY, changedFields), logPrefix,
                    "Original all day", event.isOriginalAllDay() ? "Yes" : "No"));
            this.add(mLogLineFactory.createLogLine(this.getLevel(Events.ORIGINAL_INSTANCE_TIME, changedFields),
                    logPrefix, "Original instance time", this.timeToString(event.getOriginalInstanceTime())));
        }
        this.add(mLogLineFactory.createLogLine(this.getLevel(Events.EVENT_TIMEZONE, changedFields), logPrefix,
                "Start Timezone", event.getStartTimeZone(true) != null ? event.getStartTimeZone(false).toString() : NULL));
        this.add(mLogLineFactory.createLogLine(this.getLevel(Events.EVENT_END_TIMEZONE, changedFields), logPrefix,
                "End timezone", event.getEndTimeZone() != null ? event.getEndTimeZone().toString() : NULL));
        this.add(mLogLineFactory.createLogLine(this.getLevel(Events.ACCESS_LEVEL, changedFields), logPrefix,
                "Access level", new AccessLevels(true).getKeyNameAndValue(event.getAccessLevel())));
        this.add(mLogLineFactory.createLogLine(this.getLevel(Events.AVAILABILITY, changedFields), logPrefix,
                "Availability", this.getAvailabilityDescription(event)));
        this.add(mLogLineFactory.createLogLine(this.getLevel(Events.STATUS, changedFields), logPrefix, "Status",
                new EventStatuses(true).getKeyNameAndValue(event.getStatus())));
        this.add(mLogLineFactory.createLogLine(this.getLevel(Events.SELF_ATTENDEE_STATUS, changedFields), logPrefix,
                "SelfAttendeeStatus", new AttendeeStatuses(true).getKeyNameAndValue(event.getSelfAttendeeStatus())));
    }

    public void logEvent(Event event) {
        this.logEvent(event, null);
    }

    public void logEvents(Event src, Event dst, ContentValues changedFields) {
        if (src == null) {
            this.logEvent(dst, changedFields);
            return;
        }
        if (dst == null) {
            this.logEvent(src, changedFields);
            return;
        }

        String logPrefix = null;
        this.add(mLogLineFactory.createLogLine(this.getLevel(Events._ID, changedFields), logPrefix, "Id",
                "" + src.getId(), "" + dst.getId()));
        if (src instanceof DbEvent && dst instanceof DbEvent) {
            this.add(mLogLineFactory.createLogLine(this.getLevel(Events._SYNC_ID, changedFields), logPrefix, "Sync id",
                    ((DbEvent) src).getSyncId(), ((DbEvent) dst).getSyncId()));
        }
        this.add(mLogLineFactory.createLogLine(ClonerLog.LOG_INFO, logPrefix, "CC UID", src.getUniqueId(),
                dst.getUniqueId()));
        this.add(mLogLineFactory.createLogLine(ClonerLog.LOG_INFO, logPrefix, "Event hash",
                EventMarker.getEventHash(src.getUniqueId()), EventMarker.getEventHash(dst.getUniqueId())));
        this.add(mLogLineFactory.createLogLine(this.getLevel(Events.DELETED, changedFields), logPrefix, "Deleted",
                src.isDeleted() ? "Yes" : "No", dst.isDeleted() ? "Yes" : "No"));
        this.add(mLogLineFactory.createLogLine(ClonerLog.LOG_INFO, logPrefix, "", "", ""));

        this.add(mLogLineFactory.createLogLine(ClonerLog.LOG_INFO, logPrefix, "Calendar",
                CalendarLoader.getCalendarNameOrErrorMessage(src.getCalendarId()) + " (Id: " + src.getCalendarId()
                        + ")",
                CalendarLoader.getCalendarNameOrErrorMessage(dst.getCalendarId()) + " (Id: " + dst.getCalendarId()
                        + ")"));
        this.add(mLogLineFactory.createLogLine(this.getLevel(Events.TITLE, changedFields), logPrefix, "Title",
                src.getTitle(), dst.getTitle()));
        this.add(mLogLineFactory.createLogLine(this.getLevel(Events.EVENT_LOCATION, changedFields), logPrefix,
                "Location", src.getLocation(), dst.getLocation()));
        this.add(mLogLineFactory.createLogLine(this.getLevel(Events.DTSTART, changedFields), logPrefix, "StartTime",
                this.timeToString(src.getStartTime()), this.timeToString(dst.getStartTime())));
        if (!src.isRecurringEvent() || !dst.isRecurringEvent()) {
            this.add(mLogLineFactory.createLogLine(this.getLevel(Events.DTEND, changedFields), logPrefix, "End time",
                    !src.isRecurringEvent() ? this.timeToString(src.getEndTime()) : "",
                    !dst.isRecurringEvent() ? this.timeToString(dst.getEndTime()) : ""));
        }
        if (src.isRecurringEvent() || dst.isRecurringEvent()) {
            this.add(mLogLineFactory.createLogLine(this.getLevel(Events.DURATION, changedFields), logPrefix,
                    "Duration", src.isRecurringEvent() ? src.getDuration() : "",
                    dst.isRecurringEvent() ? dst.getDuration() : ""));
        }
        this.add(mLogLineFactory.createLogLine(this.getLevel(Events.ALL_DAY, changedFields), logPrefix, "All day",
                src.isAllDay() ? "Yes" : "No", dst.isAllDay() ? "Yes" : "No"));

        this.add(mLogLineFactory.createLogLine(this.getLevel(Events.ORGANIZER, changedFields), logPrefix, "Organizer",
                src.getOrganizer(), dst.getOrganizer()));
        this.add(mLogLineFactory.createLogLine(this.getLevel(Events.DESCRIPTION, changedFields), logPrefix,
                "Description", src.getDescription(), dst.getDescription()));

        if (src.isRecurringEvent() || dst.isRecurringEvent()) {
            // Clone recurring event fields
            this.add(mLogLineFactory.createLogLine(this.getLevel(Events.RRULE, changedFields), logPrefix, "RRule",
                    src.isRecurringEvent() ? src.getRecurrenceRule() : "",
                    dst.isRecurringEvent() ? dst.getRecurrenceRule() : ""));
            this.add(mLogLineFactory.createLogLine(this.getLevel(Events.RDATE, changedFields), logPrefix, "RDate",
                    src.isRecurringEvent() ? src.getRecurrenceDate() : "",
                    dst.isRecurringEvent() ? dst.getRecurrenceDate() : ""));
            this.add(mLogLineFactory.createLogLine(this.getLevel(Events.EXRULE, changedFields), logPrefix, "ExRule",
                    src.isRecurringEvent() ? src.getRecurrenceExRule() : "",
                    dst.isRecurringEvent() ? dst.getRecurrenceExRule() : ""));
            this.add(mLogLineFactory.createLogLine(this.getLevel(Events.EXDATE, changedFields), logPrefix, "ExDate",
                    src.isRecurringEvent() ? src.getRecurrenceExDate() : "",
                    dst.isRecurringEvent() ? dst.getRecurrenceExDate() : ""));
            this.add(mLogLineFactory.createLogLine(this.getLevel(Events.LAST_DATE, changedFields), logPrefix,
                    "LastDate",
                    src.isRecurringEvent() ? (src.getLastDate() != null ? this.timeToString(src.getLastDate())
                            : ClonerApp.translate(R.string.msg_infinite) + " (0)") : "",
                    dst.isRecurringEvent() ? (dst.getLastDate() != null ? this.timeToString(dst.getLastDate())
                            : ClonerApp.translate(R.string.msg_infinite) + " (0)") : ""));
        }
        if (src.isRecurringEventException() || dst.isRecurringEventException()) {
            // Log recurring event exception fields
            this.add(mLogLineFactory.createLogLine(this.getLevel(Events.ORIGINAL_ID, changedFields), logPrefix,
                    "Original ID", src.isRecurringEventException() ? "" + src.getOriginalID() : "",
                    dst.isRecurringEventException() ? "" + dst.getOriginalID() : ""));
            this.add(mLogLineFactory.createLogLine(this.getLevel(Events.ORIGINAL_ALL_DAY, changedFields), logPrefix,
                    "Original all day", src.isRecurringEventException() ? (src.isOriginalAllDay() ? "Yes" : "No") : "",
                    dst.isRecurringEventException() ? (dst.isOriginalAllDay() ? "Yes" : "No") : ""));
            this.add(mLogLineFactory.createLogLine(this.getLevel(Events.ORIGINAL_INSTANCE_TIME, changedFields),
                    logPrefix, "Original instance time",
                    src.isRecurringEventException() ? this.timeToString(src.getOriginalInstanceTime()) : "",
                    dst.isRecurringEventException() ? this.timeToString(dst.getOriginalInstanceTime()) : ""));
        }
        this.add(mLogLineFactory.createLogLine(this.getLevel(Events.EVENT_TIMEZONE, changedFields), logPrefix,
                "Start Timezone",
                src.getStartTimeZone(true) != null ? src.getStartTimeZone(false).toString() : NULL,
                dst.getStartTimeZone(true) != null ? dst.getStartTimeZone(false).toString() : NULL));
        this.add(mLogLineFactory.createLogLine(this.getLevel(Events.EVENT_END_TIMEZONE, changedFields), logPrefix,
                "End timezone", src.getEndTimeZone() != null ? src.getEndTimeZone().toString() : NULL,
                dst.getEndTimeZone() != null ? dst.getEndTimeZone().toString() : NULL));
        this.add(mLogLineFactory.createLogLine(this.getLevel(Events.ACCESS_LEVEL, changedFields), logPrefix,
                "Access level", new AccessLevels(true).getKeyNameAndValue(src.getAccessLevel()),
                new AccessLevels(true).getKeyNameAndValue(dst.getAccessLevel())));
        this.add(mLogLineFactory.createLogLine(this.getLevel(dst.hasAvailabilitySamsung() ? Device.Samsung.AVAILABILITY
                        : Events.AVAILABILITY, changedFields), logPrefix, "Availability", this.getAvailabilityDescription(src),
                this.getAvailabilityDescription(dst)));
        this.add(mLogLineFactory.createLogLine(this.getLevel(Events.STATUS, changedFields), logPrefix, "Status",
                new EventStatuses(true).getKeyNameAndValue(src.getStatus()),
                new EventStatuses(true).getKeyNameAndValue(dst.getStatus())));
        this.add(mLogLineFactory.createLogLine(this.getLevel(Events.SELF_ATTENDEE_STATUS, changedFields), logPrefix,
                "SelfAttendeeStatus", new AttendeeStatuses(true).getKeyNameAndValue(src.getSelfAttendeeStatus()),
                new AttendeeStatuses(true).getKeyNameAndValue(dst.getSelfAttendeeStatus())));
    }

    public void logAttendees(int level, List<DbAttendee> attendees) {
        String logPrefix = null;
        for (int index = 0; index < attendees.size(); index++) {
            if (index > 0) {
                this.addEmptyLine();
            }
            DbAttendee att = attendees.get(index);
            this.add(mLogLineFactory.createLogLine(level, logPrefix, "Attendee " + (index + 1), "" + att.getId()));
            this.add(mLogLineFactory.createLogLine(level, logPrefix, "Name", att.getName()));
            this.add(mLogLineFactory.createLogLine(level, logPrefix, "Email", att.getEmail()));
            this.add(mLogLineFactory.createLogLine(level, logPrefix, "Relationship",
                    new AttendeeRelationships(true).getKeyNameAndValue(att.getRelationship())));
            this.add(mLogLineFactory.createLogLine(level, logPrefix, "Type",
                    new AttendeeTypes(true).getKeyNameAndValue(att.getType())));
            this.add(mLogLineFactory.createLogLine(level, logPrefix, "Status",
                    new AttendeeStatuses(true).getKeyNameAndValue(att.getStatus())));
        }
    }

    private void logAttendees(int index, DbAttendee eventAttendee, DbAttendee cloneAttendee, ContentValues srcDelta,
                              ContentValues dstDelta) {
        String logPrefix = null;
        this.add(mLogLineFactory.createLogLine(this.getLevel(Attendees._ID, srcDelta, dstDelta), logPrefix, "Attendee "
                + (index), eventAttendee != null ? "" + eventAttendee.getId() : "", cloneAttendee != null ? ""
                + cloneAttendee.getId() : ""));
        this.add(mLogLineFactory.createLogLine(this.getLevel(Attendees.ATTENDEE_NAME, srcDelta, dstDelta), logPrefix,
                "Name", eventAttendee != null ? eventAttendee.getName() : "",
                cloneAttendee != null ? cloneAttendee.getName() : ""));
        this.add(mLogLineFactory.createLogLine(this.getLevel(Attendees.ATTENDEE_EMAIL, srcDelta, dstDelta), logPrefix,
                "Email", eventAttendee != null ? eventAttendee.getEmail() : "",
                cloneAttendee != null ? cloneAttendee.getEmail() : ""));
        this.add(mLogLineFactory.createLogLine(
                this.getLevel(Attendees.ATTENDEE_RELATIONSHIP, srcDelta, dstDelta),
                logPrefix,
                "Relationship",
                eventAttendee != null ? new AttendeeRelationships(true).getKeyNameAndValue(eventAttendee
                        .getRelationship()) : "",
                cloneAttendee != null ? new AttendeeRelationships(true).getKeyNameAndValue(cloneAttendee
                        .getRelationship()) : ""));
        this.add(mLogLineFactory.createLogLine(this.getLevel(Attendees.ATTENDEE_TYPE, srcDelta, dstDelta), logPrefix,
                "Type", eventAttendee != null ? new AttendeeTypes(true).getKeyNameAndValue(eventAttendee.getType())
                        : "",
                cloneAttendee != null ? new AttendeeTypes(true).getKeyNameAndValue(cloneAttendee.getType()) : ""));
        this.add(mLogLineFactory.createLogLine(this.getLevel(Attendees.ATTENDEE_STATUS, srcDelta, dstDelta), logPrefix,
                "Status",
                eventAttendee != null ? new AttendeeStatuses(true).getKeyNameAndValue(eventAttendee.getStatus()) : "",
                cloneAttendee != null ? new AttendeeStatuses(true).getKeyNameAndValue(cloneAttendee.getStatus()) : ""));
    }

    public void logAttendees(List<DbAttendee> eventAttendees, List<DbAttendee> cloneAttendees, DbCalendar srcCalendar,
                             DbCalendar dstCalendar, AttendeeDeltas deltas, String dummyEmailDomain) {
        HashMap<String, DbAttendee> clones = new HashMap<String, DbAttendee>();
        if (cloneAttendees != null) {
            for (DbAttendee clone : cloneAttendees) {
                clones.put(AttendeeId.map(clone.getName(), clone.getEmail()), clone);
            }
        }

        int index = 0;
        for (DbAttendee eventAttendee : eventAttendees) {
            if (index > 0) {
                this.addEmptyLine();
            }
            // Look up the cloned attendee
            DbAttendee clone;
            // If SELF then look up destination account
            if (srcCalendar != null
                    && dstCalendar != null
                    && AttendeeId.map(srcCalendar.getOwnerAccount()).contentEquals(
                    AttendeeId.map(eventAttendee.getEmail()))) {
                clone = clones.get(AttendeeId.map(dstCalendar.getOwnerAccount()));
            } else {
                // Look up by source attendee
                clone = clones.get(AttendeeId.map(eventAttendee.getName(), eventAttendee.getEmail()));
            }
            // If still not found
            if (clone == null) {
                // Look up by dummy email address
                clone = clones.get(AttendeeId.map(eventAttendee.getName(),
                        EmailObfuscator.generateDummyEmailFrom(eventAttendee.getEmail(), dummyEmailDomain)));
            }
            this.logAttendees(
                    ++index,
                    eventAttendee,
                    clone,
                    eventAttendee != null && deltas != null ? deltas.get(eventAttendee.getName(),
                            eventAttendee.getEmail()) : null,
                    clone != null && deltas != null ? deltas.get(clone.getName(), clone.getEmail()) : null);
            if (clone != null) {
                clones.remove(AttendeeId.map(clone.getName(), clone.getEmail()));
            }
        }

        // Log remaining cloned attendees
        for (Entry<String, DbAttendee> cloneEntry : clones.entrySet()) {
            final DbAttendee clone = cloneEntry.getValue();
            this.logAttendees(++index, null, clone, null,
                    deltas != null ? deltas.get(clone.getName(), clone.getEmail()) : null);
        }
    }

    public void logReminders(int level, List<DbReminder> eventReminders, List<DbReminder> cloneReminders,
                             Map<Long, Long> mappedReminders) {
        String logPrefix = null;
        int index = 0;
        for (DbReminder eventReminder : eventReminders) {
            if (index++ > 0) {
                this.addEmptyLine();
            }

            if (cloneReminders != null && mappedReminders != null) {
                DbReminder cloneReminder = null;

                long cloneReminderId = mappedReminders.containsKey(eventReminder.getId()) ? mappedReminders
                        .get(eventReminder.getId()) : 0;
                for (DbReminder cr : cloneReminders) {
                    if (cr.getId() == cloneReminderId) {
                        cloneReminder = cr;
                    }
                }

                if (cloneReminder != null) {
                    this.add(mLogLineFactory.createLogLine(
                            level,
                            logPrefix,
                            "Reminder " + (index + 1),
                            eventReminder != null ? new ReminderMethods(true).getKeyNameAndValue(eventReminder
                                    .getMethod())
                                    + " ("
                                    + ClonerApp.translate(R.string.msg_n_minutes,
                                    new String[]{"" + eventReminder.getMinutes()}) + ")" : "",
                            cloneReminder != null ? new ReminderMethods(true).getKeyNameAndValue(cloneReminder
                                    .getMethod())
                                    + " ("
                                    + ClonerApp.translate(R.string.msg_n_minutes,
                                    new String[]{"" + cloneReminder.getMinutes()}) + ")" : ""));
                }
            } else {
                this.add(mLogLineFactory.createLogLine(
                        level,
                        logPrefix,
                        "Reminder " + (index + 1),
                        eventReminder != null ? new ReminderMethods(true).getKeyNameAndValue(eventReminder.getMethod())
                                + " ("
                                + ClonerApp.translate(R.string.msg_n_minutes,
                                new String[]{"" + eventReminder.getMinutes()}) + ")" : ""));
            }
        }
    }
}
