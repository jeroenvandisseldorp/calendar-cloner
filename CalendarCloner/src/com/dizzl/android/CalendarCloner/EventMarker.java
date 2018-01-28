package com.dizzl.android.CalendarCloner;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.provider.CalendarContract.Events;

public class EventMarker {
	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_CLONE = 1;

	private static final String MERGE_CHAR = "/";
	private static final String CLONE_HEADER = "CloneOf: ";
	private static final String FORWARD_HEADER = "ForwardOf: ";

	private static final int DESCRIPTION_MAX_LENGTH = 8180;
	private static final String DESCRIPTION_PREFIX = "\n~~~~\n";

	public static class Marker {
		public String ruleHash;
		public String eventHash;
	}

	public static final String HASH_PATTERN = "([0-9a-f]{32,})";
	private static final Pattern mClonePattern = Pattern.compile(CLONE_HEADER + HASH_PATTERN + "\\/" + HASH_PATTERN);

	private static String buildEventTypeParam(String[] headers, String ruleHash, String eventHash, List<String> args) {
		String result = "";
		for (String header : headers) {
			if (ruleHash != null) {
				if (!result.contentEquals("")) {
					result += " OR ";
				}
				result += Events.DESCRIPTION + " LIKE ?";
				String arg = "%" + header + ruleHash + "/";
				if (eventHash != null) {
					arg += eventHash;
				}
				arg += "%";
				args.add(arg);
			} else {
				if (!result.contentEquals("")) {
					result += " OR ";
				}
				result += Events.DESCRIPTION + " LIKE ?";
				String arg = "%" + header + "%/";
				if (eventHash != null) {
					arg += eventHash;
				}
				arg += "%";
				args.add(arg);
			}
		}
		return result;
	}

	public static String buildDbSelect(int type, String ruleHash, String eventHash, List<String> args) {
		switch (type) {
		case TYPE_NORMAL:
			return "(" + Events.DESCRIPTION + " ISNULL OR (NOT "
					+ EventMarker.buildDbSelect(TYPE_CLONE, ruleHash, eventHash, args) + "))";
		case TYPE_CLONE:
			return "("
					+ EventMarker.buildEventTypeParam(new String[] { CLONE_HEADER, FORWARD_HEADER }, ruleHash,
							eventHash, args) + ")";
		}
		return "";
	}

	public static int getEventType(Event event) {
		if (parseCloneEventHash(event) != null) {
			return TYPE_CLONE;
		}
		return TYPE_NORMAL;
	}

	public static String getEventHash(String eventUniqueId) {
		return Hasher.hash(eventUniqueId);
	}

	protected static String getCloneEventHash(String ruleHash, String eventUniqueId) {
		// Calculate the unique hash to put in the clone's description
		return CLONE_HEADER + ruleHash + MERGE_CHAR + getEventHash(eventUniqueId);
	}

	private static int[] locateEventHash(String descr, Pattern pattern) {
		if (descr != null && !descr.contentEquals("")) {
			Matcher m = pattern.matcher(descr);
			if (m.find()) {
				return new int[] { m.start(), m.end() };
			}
		}
		return null;
	}

	public static String markEventDescription(String descr, int type, String ruleHash, String eventUniqueId) {
		String marker = "";
		switch (type) {
		case TYPE_CLONE:
			marker = EventMarker.getCloneEventHash(ruleHash, eventUniqueId);
			break;
		}
		if (!descr.contentEquals("")) {
			marker = DESCRIPTION_PREFIX + marker;
		}
		if (descr.length() + marker.length() > DESCRIPTION_MAX_LENGTH) {
			descr = descr.substring(0, DESCRIPTION_MAX_LENGTH - marker.length());
		}
		return descr + marker;
	}

	private static String neutralizePattern(String descr, Pattern pattern) {
		int[] pos = EventMarker.locateEventHash(descr, pattern);
		while (pos != null) {
			// Remove the prefix if found
			if (pos[0] >= DESCRIPTION_PREFIX.length()
					&& descr.substring(pos[0] - DESCRIPTION_PREFIX.length(), pos[0]).contentEquals(DESCRIPTION_PREFIX)) {
				pos[0] -= DESCRIPTION_PREFIX.length();
			}
			descr = descr.substring(0, pos[0]) + descr.substring(pos[1], descr.length());
			pos = EventMarker.locateEventHash(descr, pattern);
		}
		return descr;
	}

	public static String neutralizeEventDescription(String descr) {
		if (descr != null) {
			descr = EventMarker.neutralizePattern(descr, mClonePattern);
		}
		return descr;
	}

	private static Marker parseEventHash(String descr, Pattern pattern) {
		if (descr != null && !descr.contentEquals("")) {
			Matcher m = pattern.matcher(descr);
			if (m.find()) {
				Marker result = new Marker();
				result.ruleHash = m.group(1);
				result.eventHash = m.group(2);
				return result;
			}
		}
		return null;
	}

	public static Marker parseCloneEventHash(Event event) {
		Marker m = EventMarker.parseEventHash(event.getDescription(), mClonePattern);
		return m;
	}
}
