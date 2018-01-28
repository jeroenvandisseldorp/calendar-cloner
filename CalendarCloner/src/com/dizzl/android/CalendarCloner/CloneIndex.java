package com.dizzl.android.CalendarCloner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

import android.database.Cursor;

public class CloneIndex {
	private final EventsTable mEventsTable;
	private final HashMap<String, Event> mIndex = new HashMap<String, Event>();
	private String mRuleHash;
	private String mRuleBackupHash;

	public class IndexResult {
		private final boolean mSuccess;
		private final String mErrorMessage;
		private final int mUpdateCount;

		public IndexResult(boolean success, String errorMessage, int updateCount) {
			mSuccess = success;
			mErrorMessage = errorMessage;
			mUpdateCount = updateCount;
		}

		public boolean isSuccess() {
			return mSuccess;
		}

		public String getErrorMessage() {
			return mErrorMessage;
		}

		public int getUpdateCount() {
			return mUpdateCount;
		}
	}

	public CloneIndex(EventsTable eventsTable) {
		mEventsTable = eventsTable;
	}

	protected boolean matchRuleHash(String ruleHash) {
		// Event marked by rule id
		if (mRuleHash.contentEquals(ruleHash)) {
			return true;
		}

		// Event marked by source calendar ref
		if (mRuleBackupHash != null && !mRuleBackupHash.contentEquals("") && mRuleBackupHash.contentEquals(ruleHash)) {
			return true;
		}

		// The rulehash did not stem from the rule
		return false;
	}

	protected String parseEventHash(Event clone) {
		EventMarker.Marker marker = EventMarker.parseCloneEventHash(clone);
		if (marker != null && this.matchRuleHash(marker.ruleHash)) {
			return marker.eventHash;
		}
		return null;
	}

	public IndexResult index(long calendarId, String ruleHash, String ruleBackupHash, ClonerLog log) {
		mRuleHash = ruleHash;
		mRuleBackupHash = ruleBackupHash;
		// Set up an events table for projection of just id and description
		EventsTable clonesTable = new EventsTable(ClonerApp.getDb(true));
		// Keep track of events to delete (duplicate clones)
		LinkedList<Event> deleteClones = new LinkedList<Event>();
		// Add title for verbose logging in rule log screen
		clonesTable.setProjection(new ClonerTable.Column[] { clonesTable._ID, clonesTable.CALENDAR_ID,
				clonesTable.DELETED, clonesTable.DESCRIPTION, clonesTable.ALL_DAY, clonesTable.DTSTART,
				clonesTable.EVENT_TIMEZONE, clonesTable.TITLE });
		// Query for all events in the destination calendar
		Cursor cloneCur = clonesTable.query("((" + clonesTable.CALENDAR_ID.getName() + "=?))", new String[] { ""
				+ calendarId }, null);

		if (cloneCur == null) {
			return new IndexResult(false, ClonerApp.translate(R.string.error_calendar_x_not_accessible,
					new String[] { ClonerApp.translate(R.string.calendar_destination) }), 0);
		}
		try {
			while (cloneCur.moveToNext()) {
				DbEvent clone = new DbEvent(clonesTable, new DbObject(cloneCur));
				if (!clone.isDeleted()) {
					String eventHash = this.parseEventHash(clone);
					if (eventHash != null) {
						// Load existing clone's values from the cursor
						clone.loadAll();
						if (!mIndex.containsKey(eventHash)) {
							// Add to list of existing clones for quick
							// lookup during cloning process
							mIndex.put(eventHash, clone);
						} else {
							// If we reach this point, we found a duplicate
							// "CloneOf:" string. We delete the clone
							// immediately since it can interfere with the sync
							// process.
							deleteClones.add(clone);
						}
					}
				}
			}
		} finally {
			cloneCur.close();
		}

		// Delete all duplicate clones (without interfering with a cursor)
		int eventNr = 0;
		int updateCount = 0;
		for (Event clone : deleteClones) {
			if (Limits.canModify(Limits.TYPE_EVENT)) {
				if (clone.isRecurringEventException()) {
					mEventsTable.delete(clone.getOriginalID());
				}
				mEventsTable.delete(clone.getId());
				String title = Utilities.dateTimeToString(clone.getStartTime()) + " - " + clone.getTitle();
				log.log(log.createLogLine(ClonerLog.LOG_UPDATE, "" + ++eventNr,
						ClonerApp.translate(R.string.cloner_log_deleted), title), null);
				updateCount++;
			}
		}

		return new IndexResult(true, "", updateCount);
	}

	public boolean containsClone(String eventHash) {
		return mIndex.containsKey(eventHash);
	}

	public Event getCloneOf(Event event) {
		final String eventHash = EventMarker.getEventHash(event.getUniqueId());
		if (mIndex.containsKey(eventHash)) {
			return DbEvent.get(mEventsTable, mIndex.get(eventHash).getId());
		}
		return null;
	}

	public Set<Event> getAllClones() {
		Set<Event> result = new HashSet<Event>();
		Set<Entry<String, Event>> clones = mIndex.entrySet();
		for (Entry<String, Event> entry : clones) {
			result.add(entry.getValue());
		}
		return result;
	}

	public void removeClone(String eventHash) {
		mIndex.remove(eventHash);
	}
}
