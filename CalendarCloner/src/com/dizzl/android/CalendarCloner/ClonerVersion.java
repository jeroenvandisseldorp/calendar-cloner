package com.dizzl.android.CalendarCloner;

public class ClonerVersion {
	// Free version limitations
	private static int FREE_VERSION_MAX_RULES = 2;

	// Expiration constants
	private static final boolean mExpires = false;
	private static final long TIMESTAMP_2013 = 1356994800000L;
	private static final long TIMESTAMP_1_MONTH = (long) (30.42 * 24 * 3600 * 1000);
	private static final long mExpirationTime = TIMESTAMP_2013 + TIMESTAMP_1_MONTH * (12 + 5);

	public static String PAID_VERSION_PACKAGENAME() {
		return "com" + "." + "dizzl" + "." + "android" + "." + "CalendarCloner";
	}

	// Version functions
	public static boolean IS_PAID_VERSION() {
		// Break up string to prevent renaming by cc_free
		String packageName = ClonerApp.class.getPackage().getName();
		return packageName.contentEquals(PAID_VERSION_PACKAGENAME());
	}

	public static boolean IS_FREE_VERSION() {
		return !IS_PAID_VERSION();
	}

	public static String paidVersionName() {
		return "Calendar" + " Cloner";
	}

	public static String freeVersionName() {
		return "Calendar" + " Cloner" + " FREE";
	}

	public static String thisVersionName() {
		if (IS_PAID_VERSION()) {
			return paidVersionName();
		}
		return freeVersionName();
	}

	public static void msgPaidVersionOnly() {
		ClonerApp.toast(ClonerApp.translate(R.string.msg_paid_version_only));
	}

	private static boolean inPaidVersionOnly(boolean enabled) {
		if (!ClonerVersion.IS_PAID_VERSION() && enabled) {
			msgPaidVersionOnly();
			enabled = false;
		}
		return enabled;
	}

	public static boolean isExpired() {
		long currentTime = System.currentTimeMillis();
		if (currentTime > mExpirationTime) {
			return mExpires;
		}
		return false;
	}

	public static int setNumRules(int numRules) {
		if (!ClonerVersion.IS_PAID_VERSION() && numRules > FREE_VERSION_MAX_RULES) {
			return FREE_VERSION_MAX_RULES;
		}
		return numRules;
	}

	public static int setRuleMethod(int method) {
		if (!ClonerVersion.IS_PAID_VERSION() && method != Rule.METHOD_CLONE) {
			msgPaidVersionOnly();
			return Rule.METHOD_CLONE;
		}
		return method;
	}

	public static boolean setUseEventFilters(boolean enabled) {
		return inPaidVersionOnly(enabled);
	}

	public static boolean setIncludeClones(boolean enabled) {
		return inPaidVersionOnly(enabled);
	}

	public static boolean setCloneSelfAttendeeStatus(boolean enabled) {
		return inPaidVersionOnly(enabled);
	}

	public static int setCustomAccessLevel(int customAccessLevel) {
		if (customAccessLevel != Rule.CUSTOM_ACCESS_LEVEL_SOURCE) {
			if (!inPaidVersionOnly(true)) {
				return Rule.CUSTOM_ACCESS_LEVEL_SOURCE;
			}
		}
		return customAccessLevel;
	}

	public static int setCustomAvailability(int customAvailability) {
		if (customAvailability != Rule.CUSTOM_AVAILABILITY_SOURCE) {
			if (!inPaidVersionOnly(true)) {
				return Rule.CUSTOM_AVAILABILITY_SOURCE;
			}
		}
		return customAvailability;
	}

	public static boolean setCloneAttendees(boolean enabled) {
		return inPaidVersionOnly(enabled);
	}

	public static boolean setAttendeesAsText(boolean enabled) {
		return inPaidVersionOnly(enabled);
	}

	public static boolean setCustomAttendee(boolean enabled) {
		return inPaidVersionOnly(enabled);
	}

	public static boolean setCloneReminders(boolean enabled) {
		return inPaidVersionOnly(enabled);
	}

	public static boolean setCustomReminder(boolean enabled) {
		return inPaidVersionOnly(enabled);
	}

	public static boolean setRetainClonesOutsideSourceEventWindow(boolean enabled) {
		return inPaidVersionOnly(enabled);
	}

	public static boolean shouldAddAttendees() {
		return ClonerVersion.IS_PAID_VERSION();
	}

	public static boolean shouldResyncAfterEachInterval() {
		return ClonerVersion.IS_FREE_VERSION();
	}
}
