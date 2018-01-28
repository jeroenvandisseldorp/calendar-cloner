package com.dizzl.android.CalendarClonerFree;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.annotation.SuppressLint;

public class Utilities {
	// Set infinity to approximately 500 years in milliseconds
	public static long INFINITY_MILLIS = 500L * 365 * 24 * 60 * 60 * 1000;
	public static DateTime INFINITY = new DateTime(INFINITY_MILLIS);

	// Last date formatter
	private static DateTimeFormatter mFormatter = null;
	private static String mLastFormat = null;

	public static String emptyWhenNull(String value) {
		return value != null ? value : "";
	}

	@SuppressLint("SimpleDateFormat")
	public static String dateTimeToString(String format, DateTime time) {
		// Check to see if we need to get a new formatter
		if (mLastFormat == null || mLastFormat != format || mFormatter == null) {
			// Create a DateFormatter object for displaying date in specified
			// format.
			mFormatter = DateTimeFormat.forPattern(format);
			mLastFormat = format;
		}
		return mFormatter.print(time);
	}

	public static String dateTimeToString(DateTime time) {
		return dateTimeToString(ClonerApp.translate(R.string.dateformat), time);
	}

	public static String dateTimeToTimeString(DateTime time) {
		return dateTimeToString("yyyyMMdd", time) + "T" + dateTimeToString("HHmmssSSS", time);
	}

	public static String getNowString() {
		// Finally, return in text what we've just done
		return dateTimeToString(new DateTime());
	}

	public static int getSelectionFromArray(int[] possibleValues, int value, int defaultValue) {
		for (int index = 0; index < possibleValues.length; index++) {
			if (value == possibleValues[index]) {
				return value;
			}
		}
		return defaultValue;
	}

	public static long getSelectionFromArray(long[] possibleValues, long value, long defaultValue) {
		for (int index = 0; index < possibleValues.length; index++) {
			if (value == possibleValues[index]) {
				return value;
			}
		}
		return defaultValue;
	}

	public static String appendToString(String prefix, boolean condition, String postfix) {
		if (condition) {
			if (prefix != null && !prefix.contentEquals("")) {
				prefix += ", ";
			}
			return prefix + postfix;
		}
		return prefix;
	}
}
