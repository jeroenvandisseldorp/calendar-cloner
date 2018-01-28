package com.dizzl.android.CalendarCloner;

import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;

import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Events;

public class ClonedEvent extends ProxyEvent {
	private final static long ONE_MINUTE = 60 * 1000;
	private final Rule mRule;
	private final AttendeesTable mAttendeesTable = new AttendeesTable(ClonerApp.getDb(true));

	public ClonedEvent(Event source, Rule rule) {
		super(source);
		mRule = rule;
	}

	@Override
	public String getTitle() {
		// Clone the event's title
		if (!mRule.getCloneTitle()) {
			return mRule.getCustomTitle();
		}
		return super.getTitle();
	}

	@Override
	public String getLocation() {
		// Clone the event's location
		if (!mRule.getCloneLocation()) {
			return mRule.getCustomLocation();
		}
		return super.getLocation();
	}

	protected String getRelationshipsString(List<DbAttendee> attendees, int relationship, String indent) {
		String result = "";
		final int MAXNAMES = 15;
		int maxNames = MAXNAMES;

		Iterator<DbAttendee> iterator = attendees.iterator();
		while (iterator.hasNext() && maxNames-- > 0) {
			DbAttendee att = iterator.next();
			if (att.getRelationship() == relationship) {
				if (att.getName() != null) {
					result += indent + att.getName() + " <" + att.getEmail() + ">\n";
				} else {
					result += indent + att.getEmail() + "\n";
				}
			}
		}
		int remaining = 0;
		while (iterator.hasNext()) {
			DbAttendee att = iterator.next();
			if (att.getRelationship() == relationship) {
				remaining++;
			}
		}
		if (remaining > 0) {
			result += indent + ClonerApp.translate(R.string.clonedevent_andmore, new String[] { "" + remaining })
					+ "\n";
		}
		return result;
	}

	protected String headerString(String header, String content) {
		if (!content.contentEquals("")) {
			return header + "\n" + content;
		}
		return "";
	}

	protected String getAttendeesString(long eventId) {
		List<DbAttendee> attendees = DbAttendee.getByEvent(mAttendeesTable, eventId);
		String result = headerString(ClonerApp.translate(R.string.relationship_organizer) + ":",
				getRelationshipsString(attendees, Attendees.RELATIONSHIP_ORGANIZER, "  "));
		result += headerString(
				"",
				headerString(ClonerApp.translate(R.string.relationship_speakers) + ":",
						getRelationshipsString(attendees, Attendees.RELATIONSHIP_SPEAKER, "  ")));
		result += headerString(
				"",
				headerString(ClonerApp.translate(R.string.relationship_performers) + ":",
						getRelationshipsString(attendees, Attendees.RELATIONSHIP_PERFORMER, "  ")));
		result += headerString(
				"",
				headerString(ClonerApp.translate(R.string.relationship_attendees) + ":",
						getRelationshipsString(attendees, Attendees.RELATIONSHIP_ATTENDEE, "  ")));
		return result;
	}

	public String markEventDescription(String descr, Event event) {
		return EventMarker.markEventDescription(descr, EventMarker.TYPE_CLONE, mRule.getHash(), event.getUniqueId());
	}

	@Override
	public String getDescription() {
		// Clone the event's description and append the CloneOf marker
		String description = Utilities.emptyWhenNull(super.getDescription());
		description = EventMarker.neutralizeEventDescription(description);

		if (mRule.getCloneDescription()) {
			// Remove leading returns
			while (description.length() > 0 && description.substring(0, 1).contentEquals("\n")) {
				description = description.substring(1);
			}
			// Include attendee details in paid version
			if (ClonerVersion.shouldAddAttendees() && mRule.getAttendeesAsText()) {
				// Include descriptive attendee information
				String attendeeInfo = getAttendeesString(this.getId());
				if (!attendeeInfo.contentEquals("")) {
					description = "\n" + description;
				}
				description = attendeeInfo + description;
			}
			description = this.markEventDescription(description, this);
		} else {
			description = this.markEventDescription(mRule.getCustomDescription(), this);
		}

		return description;
	}

	@Override
	public DateTime getStartTime() {
		// If not an all-day event, reserve proper time before the event
		DateTime startTime = super.getStartTime();
		if (!this.isAllDay()) {
			startTime = startTime.minus(mRule.getReserveBefore() * ONE_MINUTE);
		}
		return startTime;
	}

	@Override
	public DateTime getEndTime() {
		// If not an all-day event, reserve proper time after the event
		DateTime endTime = super.getEndTime();
		if (!this.isAllDay()) {
			endTime = endTime.plus(mRule.getReserveAfter() * ONE_MINUTE);
		}
		return endTime;
	}

	@Override
	public String getDuration() {
		// If not an all-day event, reserve proper time extra for the
		// duration
		String duration = super.getDuration();
		if (!this.isAllDay()) {
			long extra = (mRule.getReserveBefore() + mRule.getReserveAfter()) * ONE_MINUTE;
			if (extra > 0) {
				long dur = Duration.parseDuration(duration);
				return Duration.encodeDuration(dur + extra);
			}
		}
		return duration;
	}

	@Override
	public DateTime getOriginalInstanceTime() {
		// If not an all-day event, reserve proper time before the event
		DateTime originalInstanceTime = super.getOriginalInstanceTime();
		if (!this.isAllDay()) {
			originalInstanceTime = originalInstanceTime.minus(mRule.getReserveBefore() * ONE_MINUTE);
		}
		return originalInstanceTime;
	}

	@Override
	public int getAccessLevel() {
		switch (mRule.getCustomAccessLevel()) {
		case Rule.CUSTOM_ACCESS_LEVEL_DEFAULT:
			return Events.ACCESS_DEFAULT;
		case Rule.CUSTOM_ACCESS_LEVEL_PRIVATE:
			return Events.ACCESS_PRIVATE;
		case Rule.CUSTOM_ACCESS_LEVEL_CONFIDENTIAL:
			return Events.ACCESS_CONFIDENTIAL;
		case Rule.CUSTOM_ACCESS_LEVEL_PUBLIC:
			return Events.ACCESS_PUBLIC;
		}
		return super.getAccessLevel();
	}

	@Override
	public int getAvailability() {
		switch (mRule.getCustomAvailability()) {
		case Rule.CUSTOM_AVAILABILITY_BUSY:
			return Events.AVAILABILITY_BUSY;
		case Rule.CUSTOM_AVAILABILITY_FREE:
			return Events.AVAILABILITY_FREE;
		}
		return super.getAvailability();
	}

	@Override
	public int getAvailabilitySamsung() {
		switch (mRule.getCustomAvailability()) {
		case Rule.CUSTOM_AVAILABILITY_BUSY:
			return Device.Samsung.AVAILABILITY_BUSY;
		case Rule.CUSTOM_AVAILABILITY_FREE:
			return Device.Samsung.AVAILABILITY_FREE;
		case Rule.CUSTOM_AVAILABILITY_OUT_OF_OFFICE:
			return Device.Samsung.AVAILABILITY_OUT_OF_OFFICE;
		}
		return super.getAvailabilitySamsung();
	}

	@Override
	public boolean hasAvailabilitySamsung() {
		switch (mRule.getCustomAvailability()) {
		case Rule.CUSTOM_AVAILABILITY_BUSY:
		case Rule.CUSTOM_AVAILABILITY_FREE:
		case Rule.CUSTOM_AVAILABILITY_OUT_OF_OFFICE:
			return true;
		}
		return super.hasAvailabilitySamsung();
	}
}
