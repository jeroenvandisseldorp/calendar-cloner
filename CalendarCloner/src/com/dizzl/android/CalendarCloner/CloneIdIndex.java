package com.dizzl.android.CalendarCloner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CloneIdIndex {
	private Set<Long> mCloneIds = new HashSet<Long>();
	private Map<String, Long> mEventHashes = new HashMap<String, Long>();

	public void add(Event event, long cloneId) {
		if (cloneId != 0) {
			mCloneIds.add(cloneId);
			String eventHash = EventMarker.getEventHash(event.getUniqueId());
			mEventHashes.put(eventHash, cloneId);
		}
	}

	public boolean containsId(long id) {
		return mCloneIds.contains(id);
	}

	public boolean containsHash(String eventHash) {
		return mEventHashes.containsKey(eventHash);
	}

	public long getCloneId(String eventHash) {
		return mEventHashes.get(eventHash);
	}
}
