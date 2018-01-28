package com.dizzl.android.CalendarCloner;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.provider.CalendarContract.Calendars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressLint("UseSparseArrays")
public class CalendarLoader {
	private static Map<Long, DbCalendar> mCalendars = new HashMap<Long, DbCalendar>();
	private static Map<String, CalendarInfo> mCalendarInfoByRef = new HashMap<String, CalendarInfo>();
	private static CalendarsTable mCalendarsTable = new CalendarsTable(ClonerApp.getDb(true));
	private static long mLastReloadTime = 0;

	public static final int CALENDAR_LOADED = 0;
	public static final int CALENDAR_NOT_FOUND = 1;
	public static final int CALENDAR_NOT_SET_UP = 2;
	public static final int CALENDAR_REF_AMBIGUOUS = 3;

	public static class CalendarInfo {
		private DbCalendar mCalendar = null;
		private int mError = CALENDAR_LOADED;

		public DbCalendar getCalendar() {
			return mCalendar;
		}

		public int getError() {
			return mError;
		}
	}

	private static final Map<Long, DbCalendar> loadAll() {
		Map<Long, DbCalendar> calendars = new HashMap<Long, DbCalendar>();
		Cursor cur = mCalendarsTable.query(null, null, Calendars.CALENDAR_DISPLAY_NAME + " ASC, "
				+ Calendars.ACCOUNT_NAME + " ASC, " + Calendars.ACCOUNT_TYPE + " ASC");
		if (cur != null) {
			try {
				while (cur.moveToNext()) {
					DbCalendar cal = new DbCalendar(mCalendarsTable, new DbObject(cur));
                    cal.loadAll();
                    calendars.put(cal.getId(), cal);
                }
			} finally {
				cur.close();
			}
		}
		return calendars;
	}

	private static void reloadIfNecessary() {
		if (System.currentTimeMillis() - mLastReloadTime > 60 * 1000) {
			mCalendars = loadAll();
			mCalendarInfoByRef.clear();

			for (Entry<Long, DbCalendar> entry : mCalendars.entrySet()) {
				DbCalendar cal = entry.getValue();
				if (!mCalendarInfoByRef.containsKey(cal.getRef())) {
					CalendarInfo info = new CalendarInfo();
					info.mCalendar = cal;
					mCalendarInfoByRef.put(cal.getRef(), info);
				} else {
					CalendarInfo info = mCalendarInfoByRef.get(cal.getRef());
					info.mCalendar = null;
					info.mError = CALENDAR_REF_AMBIGUOUS;
				}
			}
			mLastReloadTime = System.currentTimeMillis();
		}
	}

	public static CalendarInfo getCalendarByRef(String ref) {
		if (ref != null && !ref.contentEquals("")) {
			reloadIfNecessary();
			if (mCalendarInfoByRef.containsKey(ref)) {
				return mCalendarInfoByRef.get(ref);
			}

			CalendarInfo info = new CalendarInfo();
			info.mError = CALENDAR_NOT_FOUND;
			return info;
		}

		CalendarInfo info = new CalendarInfo();
		info.mError = CALENDAR_NOT_SET_UP;
		return info;
	}

	public static DbCalendar getCalendar(long calendarId) {
		return mCalendars.get(calendarId);
	}

	public static String getCalendarNameOrErrorMessage(long calendarId) {
		reloadIfNecessary();
		if (mCalendars.containsKey(calendarId)) {
			return mCalendars.get(calendarId).getDisplayName();
		}
		return ClonerApp.translate(R.string.error_calendar_x_not_found,
				new String[] { ClonerApp.translate(R.string.calendar_generic) });
	}

	public static String getErrorString(int error, String calendar) {
		switch (error) {
		case CALENDAR_NOT_FOUND:
			return ClonerApp.translate(R.string.error_calendar_x_not_found, new String[] { calendar });
		case CALENDAR_NOT_SET_UP:
			return ClonerApp.translate(R.string.error_calendar_x_not_set, new String[] { calendar });
		case CALENDAR_REF_AMBIGUOUS:
			return ClonerApp.translate(R.string.error_duplicate_calendar_name, new String[] { calendar });
		}
		return "";
	}

	public static List<String> getValidRefs() {
		reloadIfNecessary();
		List<String> refs = new ArrayList<String>();
		for (Entry<Long, DbCalendar> entry : mCalendars.entrySet()) {
			DbCalendar cal = entry.getValue();
			CalendarInfo info = mCalendarInfoByRef.get(cal.getRef());
			if (info.mCalendar != null) {
				refs.add(cal.getRef());
			}
		}
		return refs;
	}

	public static String guessCalendarRefByUri(String uri) {
		reloadIfNecessary();
		for (Entry<Long, DbCalendar> entry : mCalendars.entrySet()) {
			DbCalendar cal = entry.getValue();
			if (mCalendarInfoByRef.get(cal.getRef()).mCalendar == cal) {
				String ref = cal.getRef();
				if (ref.contentEquals(uri)) {
					return cal.getRef();
				}
			}
		}
		return "";
	}
}
