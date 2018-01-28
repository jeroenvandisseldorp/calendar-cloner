package com.dizzl.android.CalendarClonerFree;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract.Events;

public class DuplicatesLoader extends AsyncLoader<DuplicatesEntry[]> {
	private boolean mCancelled = false;
	private String mSelection;
	private String[] mSelectionArgs;

	public DuplicatesLoader(Context context, String selection, String[] selectionArgs) {
		super(context);
		mSelection = selection;
		mSelectionArgs = selectionArgs;
	}

	private String getUniqueId(Event event) {
		return "" + event.getCalendarId() + ":" + event.getUniqueId();
	}

	@Override
	public DuplicatesEntry[] loadInBackground() {
		mCancelled = false;
		Set<String> uniqueIds = new HashSet<String>();
		Set<String> duplicateIds = new HashSet<String>();
		HashMap<String, DuplicatesEntry> duplicates = new HashMap<String, DuplicatesEntry>();

		EventsTable table = new EventsTable(ClonerApp.getDb(true));
		Cursor cur = table.query(mSelection, mSelectionArgs, Events.DTSTART + " ASC");
		if (cur != null) {
			try {
				while (cur.moveToNext()) {
					if (mCancelled) {
						return null;
					}

					Event event = new DbEvent(table, new DbObject(cur));
					// Skip deleted or dirty events
					if (!event.isDeleted() && !event.isDirty()) {
						// Only evaluate normal events (no clones or forwards)
						if (EventMarker.getEventType(event) == EventMarker.TYPE_NORMAL) {
							String uniqueId = this.getUniqueId(event);
							if (uniqueIds.contains(uniqueId)) {
								duplicateIds.add(uniqueId);
							} else {
								uniqueIds.add(uniqueId);
							}
						}
					}
				}
			} finally {
				cur.close();
			}
		}

		cur = table.query(mSelection, mSelectionArgs, Events.DTSTART + " ASC");
		if (cur != null) {
			try {
				while (cur.moveToNext()) {
					if (mCancelled) {
						return null;
					}

					Event event = new DbEvent(table, new DbObject(cur));
					if (!event.isDeleted() && !event.isDirty()) {
						if (EventMarker.getEventType(event) == EventMarker.TYPE_NORMAL) {
							String uniqueId = this.getUniqueId(event);
							if (duplicateIds.contains(uniqueId)) {
								DuplicatesEntry.Entry entry = new DuplicatesEntry.Entry();
								entry.eventId = event.getId();
								entry.startTime = event.getStartTime();
								entry.title = event.getTitle();
								entry.uniqueId = event.getUniqueId();
								entry.location = event.getLocation();
								entry.calendarId = event.getCalendarId();
								entry.isRecurring = event.isRecurringEvent();
								entry.isException = event.isRecurringEventException();
								entry.isDeleted = event.isDeleted();

								DuplicatesEntry duplicate = null;
								if (!duplicates.containsKey(uniqueId)) {
									duplicate = new DuplicatesEntry();
									duplicates.put(uniqueId, duplicate);
								} else {
									duplicate = duplicates.get(uniqueId);
								}
								duplicate.events.add(entry);
							}
						}
					}
				}
			} finally {
				cur.close();
			}
		}

		// Clean up
		if (mCancelled) {
			return null;
		}

		// Now put all duplicate entries into a sorted array
		DuplicatesEntry[] dups = new DuplicatesEntry[duplicates.size()];
		int index = 0;
		for (DuplicatesEntry entry : duplicates.values()) {
			if (mCancelled) {
				return null;
			}
			dups[index++] = entry;
		}
		return dups;
	}

	@Override
	public boolean cancelLoad() {
		mCancelled = true;
		return super.cancelLoad();
	}
}
