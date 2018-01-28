package com.dizzl.android.CalendarCloner;

import java.util.HashSet;
import java.util.Set;

import android.content.ContentValues;
import android.provider.CalendarContract.Attendees;

public class AttendeeCloner extends Processor {
	private final AttendeesTable mAttendeesTable;
	private final Rule mRule;
	private final DbCalendar mDstCalendar;
	private final DbCalendar mSrcCalendar;
	private final AttendeeDiffer mDiffer = new AttendeeDiffer();

	protected class AttendeeCloneResult {
		int updateCount = 0;
		boolean completed = true;
		protected AttendeeDeltas deltas = new AttendeeDeltas();
	}

	protected static class AttendeeCloneContext {
		protected LogLines logLines = null;
		protected Set<String> processedAttendees = new HashSet<String>();
	}

	public AttendeeCloner(Rule rule, DbCalendar srcCalendar, DbCalendar dstCalendar, AttendeesTable table) {
		mRule = rule;
		mSrcCalendar = srcCalendar;
		mDstCalendar = dstCalendar;
		mAttendeesTable = table;
	}

	private String mapSourceAttendeeEmail(Attendee attendee, boolean self, boolean customAttendee) {
		String email = attendee.getEmail() != null ? attendee.getEmail() : "";
		if (self) {
			email = mDstCalendar.getOwnerAccount();
		} else if (mRule.getUseDummyEmailAddresses() && !customAttendee) {
			email = EmailObfuscator.generateDummyEmailFrom(email, mRule.getDummyEmailDomain());
		}
		return email;
	}

	private boolean isSourceSelf(String email) {
		return email != null ? mSrcCalendar.getOwnerAccount().contentEquals(email) : false;
	}

	private boolean isDestinationSelf(String email) {
		return email != null ? mDstCalendar.getOwnerAccount().contentEquals(email) : false;
	}

	private void prepareSrcAttendeeList(AttendeeMap attendees) {
		// Find SELF in the source list of attendees
		Attendee self = attendees.get(mRule.getSelfAttendeeName(), mSrcCalendar.getOwnerAccount());

		// If we clone the attendee list
		if (mRule.getCloneAttendees()) {
			// Make sure SELF is on the attendee list
			if (self == null) {
				// Add SELF to list of source attendees
				MemoryAttendee selfAtt = new MemoryAttendee();
				selfAtt.setName(mRule.getSelfAttendeeName());
				selfAtt.setEmail(mSrcCalendar.getOwnerAccount());
				selfAtt.setRelationship(Attendees.RELATIONSHIP_ATTENDEE);
				selfAtt.setType(Attendees.TYPE_REQUIRED);
				attendees.put(selfAtt.getName(), selfAtt.getEmail(), selfAtt);
			}
		} else {
			// Clear the list to clone
			attendees.clear();
		}
	}

	private void diffAttendee(Attendee attendee, Attendee clone, boolean selfOrganizer, boolean customAttendee,
			boolean resetCloneSelfAttendeeStatus, ContentValues delta) {
		delta.clear();
		// Find out if we're diffing SELF
		final boolean self = this.isSourceSelf(attendee.getEmail());

		if (self) {
			mDiffer.compare(
					new ClonedAttendeeSelf(attendee, mDstCalendar.getDisplayName(), mDstCalendar.getOwnerAccount(),
							selfOrganizer, mRule.getCloneSelfAttendeeStatus(), resetCloneSelfAttendeeStatus), clone,
					resetCloneSelfAttendeeStatus, mRule.getCloneSelfAttendeeStatusReverse(), delta);
		} else {
			mDiffer.compare(new ClonedAttendeeOther(attendee, !customAttendee && mRule.getUseDummyEmailAddresses(),
					mRule.getDummyEmailDomain()), clone, false, mRule.getCloneSelfAttendeeStatusReverse(), delta);
		}
	}

	private boolean processedAttendeeBefore(String name, String email, AttendeeCloneContext attendeeCloneContext) {
		// This method ensures that we don't overcount attendees when adhering
		// to invite limits
		boolean result = false;
		if (attendeeCloneContext != null && attendeeCloneContext.processedAttendees != null) {
			String attId = AttendeeId.map(0, name, email);
			if (attendeeCloneContext.processedAttendees.contains(attId)) {
				// We've seen this attendee before
				result = true;
			} else {
				// We haven't seen it before, so add it here
				attendeeCloneContext.processedAttendees.add(attId);
			}
		}

		return result;
	}

	private boolean canModifyAttendee(String name, String email, boolean self, AttendeeCloneContext atc) {
		boolean result = self || this.processedAttendeeBefore(name, email, atc)
				|| Limits.canModify(Limits.TYPE_ATTENDEE);
		if (!result) {
			this.log(ClonerLog.LOG_WARNING, ClonerApp.translate(R.string.cloner_log_attendee_limit_reached));
		}
		return result;
	}

	public AttendeeCloneResult process(Event event, long cloneId, boolean resetClonedSelfAttendeeStatus,
			AttendeeCloneContext attendeeCloneContext) {
		this.useLogLines(attendeeCloneContext.logLines);
		AttendeeCloneResult result = new AttendeeCloneResult();

		// Load the event's attendees
		AttendeeMap eventAttendees = DbAttendee.getByEventHashed(mAttendeesTable, event.getId());
		// Load the clone's attendees
		AttendeeMap cloneAttendees = DbAttendee.getByEventHashed(mAttendeesTable, cloneId);
		boolean selfOrganizer = event.getOrganizer() != null ? event.getOrganizer().contentEquals(
				mSrcCalendar.getOwnerAccount()) : true;

		// Calculate the cloned list of attendees
		this.prepareSrcAttendeeList(eventAttendees);

		// Try to find matches between event attendees and clone attendees in
		// terms of name/email
		for (Attendee eventAttendee : eventAttendees.getSet()) {
			final boolean self = this.isSourceSelf(eventAttendee.getEmail());
			// Clone the attendee email
			String cloneAttendeeEmail = this.mapSourceAttendeeEmail(eventAttendee, self, false);

			// Lookup the clone attendee
			final Attendee cloneAttendee = cloneAttendees.get(eventAttendee.getName(), cloneAttendeeEmail);
			if (cloneAttendee != null) {
				ContentValues delta = new ContentValues();
				this.diffAttendee(eventAttendee, cloneAttendee, selfOrganizer, false, resetClonedSelfAttendeeStatus,
						delta);
				if (delta.size() > 0) {
					// Check to see if we can update within limits
					if (!this.canModifyAttendee(cloneAttendee.getName(), cloneAttendee.getEmail(), self,
							attendeeCloneContext)) {
						result.completed = false;
						return result;
					}

					// Update the attendee
					mAttendeesTable.update(cloneAttendee.getId(), delta);
					result.updateCount++;
					result.deltas.put(cloneAttendee.getName(), cloneAttendee.getEmail(), delta);
					log(ClonerLog.LOG_UPDATE,
							ClonerApp.translate(R.string.cloner_log_attendee_updated,
									new String[] { cloneAttendee.getName(), cloneAttendee.getEmail() }));
				}
				cloneAttendees.remove(eventAttendee.getName(), cloneAttendeeEmail);
			} else {
				// Check to see if we can insert within limits
				if (!this.canModifyAttendee(eventAttendee.getName(), cloneAttendeeEmail, self, attendeeCloneContext)) {
					result.completed = false;
					return result;
				}

				// We didn't find a match, so insert a new attendee
				ContentValues delta = new ContentValues();
				this.diffAttendee(eventAttendee, new MemoryAttendee(), selfOrganizer, false,
						resetClonedSelfAttendeeStatus, delta);
				delta.put(Attendees.EVENT_ID, cloneId);
				mAttendeesTable.insert(delta);
				result.updateCount++;
				result.deltas.put(delta.getAsString(Attendees.ATTENDEE_NAME),
						delta.getAsString(Attendees.ATTENDEE_EMAIL), delta);
				int resourceId = self ? R.string.cloner_log_attendee_added_self
						: mRule.getUseDummyEmailAddresses() ? R.string.cloner_log_attendee_added_dummy
								: R.string.cloner_log_attendee_added;
				log(ClonerLog.LOG_UPDATE,
						ClonerApp.translate(resourceId, new String[] { delta.getAsString(Attendees.ATTENDEE_NAME),
								delta.getAsString(Attendees.ATTENDEE_EMAIL) }));
			}
		}

		// If the rule specifies a custom attendee, add it here
		if (mRule.getCustomAttendee()) {
			Attendee customAttendee = cloneAttendees.get(mRule.getCustomAttendeeName(), mRule.getCustomAttendeeEmail());
			if (customAttendee == null) {
				// Check to see if we can insert within limits
				if (!this.canModifyAttendee(mRule.getCustomAttendeeName(), mRule.getCustomAttendeeEmail(), false,
						attendeeCloneContext)) {
					result.completed = false;
					return result;
				}

				MemoryAttendee att = new MemoryAttendee();
				att.setName(mRule.getCustomAttendeeName());
				att.setEmail(mRule.getCustomAttendeeEmail());
				att.setRelationship(Attendees.RELATIONSHIP_ATTENDEE);
				att.setType(Attendees.TYPE_REQUIRED);
				att.setStatus(Attendees.ATTENDEE_STATUS_INVITED);
				customAttendee = att;

				ContentValues delta = new ContentValues();
				this.diffAttendee(customAttendee, new MemoryAttendee(), selfOrganizer, true,
						resetClonedSelfAttendeeStatus, delta);
				delta.put(Attendees.EVENT_ID, cloneId);
				mAttendeesTable.insert(delta);
				result.updateCount++;
				result.deltas.put(customAttendee.getName(), customAttendee.getEmail(), delta);
				log(ClonerLog.LOG_UPDATE,
						ClonerApp.translate(R.string.cloner_log_attendee_added_extra,
								new String[] { mRule.getCustomAttendeeName(), mRule.getCustomAttendeeEmail() }));
			} else {
				cloneAttendees.remove(mRule.getCustomAttendeeName(), mRule.getCustomAttendeeEmail());
			}
		}

		// Remove all remaining attendees from the cloned event
		for (final Attendee cloneAttendee : cloneAttendees.getSet()) {
			final boolean self = this.isDestinationSelf(cloneAttendee.getEmail());
			if (!self) {
				// Check to see if we can delete within limits
				if (!this.canModifyAttendee(cloneAttendee.getName(), cloneAttendee.getEmail(), false,
						attendeeCloneContext)) {
					result.completed = false;
					return result;
				}

				mAttendeesTable.delete(cloneAttendee.getId());
				log(ClonerLog.LOG_UPDATE,
						ClonerApp.translate(R.string.cloner_log_attendee_removed,
								new String[] { cloneAttendee.getName(), cloneAttendee.getEmail() }));
				result.updateCount++;
			} else {
				// Don't remove self if neutral (added automatically by most
				// calendars)
				if (cloneAttendee.getStatus() != Attendees.ATTENDEE_STATUS_NONE
						&& cloneAttendee.getStatus() != Attendees.ATTENDEE_STATUS_ACCEPTED) {
					if (mAttendeesTable.delete(cloneAttendee.getId()) != 1) {
						result.completed = false;
						return result;
					}
					log(ClonerLog.LOG_UPDATE,
							ClonerApp.translate(R.string.cloner_log_attendee_removed,
									new String[] { cloneAttendee.getName(), cloneAttendee.getEmail() }));
					result.updateCount++;
				}
			}
		}

		return result;
	}
}
