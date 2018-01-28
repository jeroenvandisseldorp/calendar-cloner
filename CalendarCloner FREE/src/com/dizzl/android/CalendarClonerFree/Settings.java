package com.dizzl.android.CalendarClonerFree;

import java.util.HashMap;
import java.util.Map.Entry;

import android.os.Handler;
import android.provider.CalendarContract.Reminders;

public class Settings {
	private static String RULE_PREFIX = "rule";
	private final HashMap<Runnable, Handler> mOnChangeRunnables = new HashMap<Runnable, Handler>();
	private final HashMap<Runnable, Handler> mOnRuleStatusChangeRunnables = new HashMap<Runnable, Handler>();

	private final CalendarsTable mCalendarsTable = new CalendarsTable(ClonerApp.getDb(true));
	private boolean mClonerEnabled = false;
	private int mClonerTimeWait = 120;
	private int mLogType = ClonerLog.TYPE_SUMMARY;
	private boolean mLogToLogcat = false;
	private boolean mLogToMemory = true;
	private Rule[] mRules = null;

	public void registerRuleStatusChangeHandler(Runnable runnable, boolean runImmediate) {
		mOnRuleStatusChangeRunnables.put(runnable, new Handler());
		if (runImmediate) {
			// Run once
			runnable.run();
		}
	}

	public void registerSettingsChangeHandler(Runnable runnable, boolean runImmediate) {
		mOnChangeRunnables.put(runnable, new Handler());
		if (runImmediate) {
			// Run once
			runnable.run();
		}
	}

	public void unregisterRuleStatusChangeHandler(Runnable runnable) {
		mOnRuleStatusChangeRunnables.remove(runnable);
	}

	public void unregisterSettingsChangeHandler(Runnable runnable) {
		mOnChangeRunnables.remove(runnable);
	}

	private void notifySettingsChange(boolean markAllDirty) {
		if (markAllDirty) {
			for (int index = 0; index < mRules.length; index++) {
				mRules[index].markDirty();
			}
		}
		for (Entry<Runnable, Handler> entry : mOnChangeRunnables.entrySet()) {
			entry.getValue().post(entry.getKey());
		}
	}

	public void notifyRuleStatusChange() {
		for (Entry<Runnable, Handler> entry : mOnRuleStatusChangeRunnables.entrySet()) {
			entry.getValue().post(entry.getKey());
		}
	}

	public static Rule createNewRule() {
		Rule rule = new Rule();
		rule.setAccessLevels(new AccessLevels(true));
		rule.setAttendeeStatuses(new AttendeeStatuses(true));
		rule.setAvailabilities(new Availabilities(true));
		rule.setEventStatuses(new EventStatuses(true));
		rule.setWeekdays(new Weekdays(true));
		return rule;
	}

	private String getCalendarRef(SettingsMap map, int index, String refKey, String uriKey) {
		String ref = map.getString(ruleKey(index, refKey), "");
		if (ref.contentEquals("")) {
			String uri = map.getString(ruleKey(index, uriKey), "");
			ref = CalendarLoader.guessCalendarRefByUri(uri);
		}
		return ref;
	}

	private void loadLimitFromMap(SettingsMap map, int type, int defaultLimit) {
		// Load the type limit
		int limit = map.getInt("limit." + type, defaultLimit);
		Limits.setTypeLimit(type, limit);

		// Load the type counter
		int count = map.getInt("limit." + type + ".count", 0);
		long startTime = map.getLong("limit." + type + ".startTime", 0);
		Limits.TypeCounter counter = new Limits.TypeCounter();
		counter.setCount(count);
		counter.setStartTime(startTime);
		Limits.setTypeCounter(type, counter);
	}

	private void migrateRuleSettings(SettingsMap map, int index, int fromVersion) {
		// Migration fromVersion to fromVersion+1
		if (fromVersion == 1) {
			// Field name migration
			map.put(ruleKey(index, "customTitle"), map.getString(ruleKey(index, "cloneTitle"), ""));
			map.put(ruleKey(index, "customLocation"), map.getString(ruleKey(index, "cloneLocation"), ""));
			map.put(ruleKey(index, "customDescription"), "");

			// Move away from detail settings to content settings
			int detail = map.contains(ruleKey(index, "detail")) ? map.getInt(ruleKey(index, "detail"),
					Rule.DETAIL_ALLDETAILS) : map.getInt(ruleKey(index, "action"), Rule.DETAIL_ALLDETAILS);
			map.put(ruleKey(index, "cloneTitle"), detail != Rule.DETAIL_FREEBUSY);
			map.put(ruleKey(index, "cloneLocation"), detail != Rule.DETAIL_FREEBUSY);
			map.put(ruleKey(index, "cloneDescription"), detail == Rule.DETAIL_ALLDETAILS);

			map.put(ruleKey(index, "cloneSelfAttendeeStatus"), map.getBoolean(ruleKey(index, "cloneStatus"), false));
			map.put(ruleKey(index, "cloneSelfAttendeeStatusReverse"),
					map.getBoolean(ruleKey(index, "cloneStatusReverse"), false));
		}
	}

	public synchronized void loadfromMap(SettingsMap map) {
		mClonerEnabled = map.getBoolean("clonerEnabled", false);
		mClonerTimeWait = map.getInt("clonerTimeWait", 120);
		this.loadLimitFromMap(map, Limits.TYPE_EVENT, 50);
		this.loadLimitFromMap(map, Limits.TYPE_ATTENDEE, 25);
		mLogToLogcat = map.getBoolean("clonerLogToLogcat", false);
		mLogToMemory = map.getBoolean("clonerLogToMemory", true);
		mLogType = map.getInt("clonerLogType", ClonerLog.TYPE_SUMMARY);

		// Load all rules from settings
		int numRules = ClonerVersion.setNumRules(map.getInt("numRules", 0));
		mRules = new Rule[numRules];
		for (int index = 0; index < numRules; index++) {
			// Load basic rule settings
			int version = map.getInt(ruleKey(index, "version"), 1);
			for (int fromVersion = version; fromVersion < Rule.CURRENT_RULE_VERSION; fromVersion++) {
				this.migrateRuleSettings(map, index, fromVersion);
			}
			String id = map.getString(ruleKey(index, "id"), "");
			boolean enabled = map.getBoolean(ruleKey(index, "enabled"), true);
			String name = map.getString(ruleKey(index, "name"), "");
			int method = map.getInt(ruleKey(index, "method"), Rule.METHOD_CLONE);
			String srcCalendarRef = this.getCalendarRef(map, index, "srcCalendar", "srcCalendarUri");
			String dstCalendarRef = this.getCalendarRef(map, index, "dstCalendar", "dstCalendarUri");
			String fwdCalendarRef = this.getCalendarRef(map, index, "fwdCalendar", "fwdCalendarUri");
			String srcCalendarHash = map.getString(ruleKey(index, "srcCalendarHash"), "");
			boolean readOnly = map.getBoolean(ruleKey(index, "readOnly"), false);

			// Event selection settings
			long eventPeriodBefore = map.getLong(ruleKey(index, "syncWindowBefore"), 365L * 24 * 3600 * 1000);
			long eventPeriodAfter = map.getLong(ruleKey(index, "syncWindowAfter"), 365L * 24 * 3600 * 1000);
			boolean includeClones = map.getBoolean(ruleKey(index, "includeClones"), false);
			boolean useEventFilters = false;
			int eventTypeFilter = map.getInt(ruleKey(index, "eventTypeFilter"), Rule.EVENT_TYPE_ALL);
			String titleMustContain = map.getString(ruleKey(index, "titleMustContain"), "");
			String titleMustNotContain = map.getString(ruleKey(index, "titleMustNotContain"), "");
			String locationMustContain = map.getString(ruleKey(index, "locationMustContain"), "");
			String locationMustNotContain = map.getString(ruleKey(index, "locationMustNotContain"), "");
			String descriptionMustContain = map.getString(ruleKey(index, "descriptionMustContain"), "");
			String descriptionMustNotContain = map.getString(ruleKey(index, "descriptionMustNotContain"), "");

			AccessLevels accessLevels = new AccessLevels(true);
			AttendeeStatuses attendeeStatuses = new AttendeeStatuses(true);
			Availabilities availabilities = new Availabilities(true);
			EventStatuses eventStatuses = new EventStatuses(true);
			Weekdays weekdays = new Weekdays(true);

			accessLevels.decode(map.getString(ruleKey(index, "accessLevels"), accessLevels.toString()));

			if (map.contains(ruleKey(index, "ignoreDeclined"))
					&& !map.getBoolean(ruleKey(index, "ignoreDeclined"), true)) {
				useEventFilters = true;
				attendeeStatuses.selectByKey(AttendeeStatuses.ATTENDEE_STATUS_DECLINED, true);
			}

			if (map.contains(ruleKey(index, "weekdays"))) {
				weekdays.decode(map.getString(ruleKey(index, "weekdays"), ""));
				boolean allSelected = true;
				for (int i = 0; i < weekdays.getCount(); i++) {
					allSelected &= weekdays.isIndexSelected(i);
				}
				if (!allSelected) {
					// Mark custom selection, will be overwritten below if
					// disabled
					useEventFilters = true;
				}
			}

			if (map.contains(ruleKey(index, "useEventFilters")) || map.contains(ruleKey(index, "useEventSelection"))
					|| map.contains(ruleKey(index, "customEventSelection"))) {
				useEventFilters = map.getBoolean(
						ruleKey(index, "useEventFilters"),
						map.getBoolean(ruleKey(index, "useEventSelection"),
								map.getBoolean(ruleKey(index, "customEventSelection"), useEventFilters)));

				attendeeStatuses.decode(map.getString(ruleKey(index, "attendeeStatuses"), attendeeStatuses.toString()));
				availabilities.decode(map.getString(ruleKey(index, "availabilities"), availabilities.toString()));
				eventStatuses.decode(map.getString(ruleKey(index, "eventStatuses"), eventStatuses.toString()));
			}

			// Content settings
			boolean cloneTitle = map.getBoolean(ruleKey(index, "cloneTitle"), true);
			String customTitle = map.getString(ruleKey(index, "customTitle"), "");
			boolean cloneLocation = map.getBoolean(ruleKey(index, "cloneLocation"), true);
			String customLocation = map.getString(ruleKey(index, "customLocation"), "");
			boolean cloneDescription = map.getBoolean(ruleKey(index, "cloneDescription"), true);
			String customDescription = map.getString(ruleKey(index, "customDescription"), "");
			boolean cloneSelfAttendeeStatus = map.getBoolean(ruleKey(index, "cloneSelfAttendeeStatus"), false);
			boolean cloneSelfAttendeeStatusReverse = map.getBoolean(ruleKey(index, "cloneSelfAttendeeStatusReverse"),
					false);
			int reserveBefore = map.getInt(ruleKey(index, "reserveBefore"), 0);
			int reserveAfter = map.getInt(ruleKey(index, "reserveAfter"), 0);
			int customAccessLevel = map.getInt(ruleKey(index, "customAccessLevel"), Rule.CUSTOM_ACCESS_LEVEL_SOURCE);
			int customAvailability = map.getInt(ruleKey(index, "customAvailability"), Rule.CUSTOM_AVAILABILITY_SOURCE);

			// Attendee settings
			boolean cloneAttendees = map.getBoolean(ruleKey(index, "cloneAttendees"), false);
			boolean useDummyEmailAddresses = map.getBoolean(ruleKey(index, "useDummyEmailAddresses"), false);
			String dummyEmailDomain = map.getString(ruleKey(index, "dummyEmailDomain"), Rule.DEFAULT_DUMMY_DOMAIN);
			String selfAttendeeName = map.getString(ruleKey(index, "selfAttendeeName"), "");
			boolean attendeesAsText = map.getBoolean(ruleKey(index, "attendeesAsText"), false);
			boolean customAttendee = map.getBoolean(ruleKey(index, "customAttendee"), false);
			String customAttendeeName = map.getString(ruleKey(index, "customAttendeeName"), "");
			String customAttendeeEmail = map.getString(ruleKey(index, "customAttendeeEmail"), "");

			// Reminder settings
			boolean cloneReminders = map.getBoolean(ruleKey(index, "cloneReminders"), false);
			boolean customReminder = map.getBoolean(ruleKey(index, "customReminder"), false);
			int customReminderMethod = map.getInt(ruleKey(index, "customReminderMethod"), Reminders.METHOD_DEFAULT);
			int customReminderMinutes = map.getInt(ruleKey(index, "customReminderMinutes"), Reminders.MINUTES_DEFAULT);

			// Additional options
			boolean retainClonesOutsideSourceEventWindow = map.getBoolean(
					ruleKey(index, "retainClonesOutsideSourceEventWindow"), false);

			// Advanced options
			int hashMethod = map.getInt(ruleKey(index, "hashMethod"), Rule.HASH_METHOD_SOURCE_CALENDAR);
			String hash = map.getString(ruleKey(index, "hash"), "");

			// Convert FORWARD to CLONE rules
			if (method == Rule.METHOD_LEGACY_FORWARD) {
				enabled = false;
				method = Rule.METHOD_CLONE;
				DbCalendar fwdCal = CalendarLoader.getCalendarByRef(fwdCalendarRef).getCalendar();
				if (fwdCal != null) {
					customAttendee = true;
					customAttendeeName = fwdCal.getDisplayName();
					customAttendeeEmail = fwdCal.getOwnerAccount();
				}
			}

			if (version < Rule.CURRENT_RULE_VERSION) {
				// DO VERSION CONVERSION HERE
			}

			// Create the rule
			mRules[index] = new Rule(mCalendarsTable, id, enabled, name, method, srcCalendarRef, dstCalendarRef,
					srcCalendarHash, readOnly, eventPeriodBefore, eventPeriodAfter, includeClones, useEventFilters,
					eventTypeFilter, titleMustContain, titleMustNotContain, locationMustContain,
					locationMustNotContain, descriptionMustContain, descriptionMustNotContain, accessLevels,
					attendeeStatuses, availabilities, eventStatuses, weekdays, cloneTitle, customTitle, cloneLocation,
					customLocation, cloneDescription, customDescription, cloneSelfAttendeeStatus,
					cloneSelfAttendeeStatusReverse, selfAttendeeName, reserveBefore, reserveAfter, customAccessLevel,
					customAvailability, cloneAttendees, useDummyEmailAddresses, dummyEmailDomain, attendeesAsText,
					customAttendee, customAttendeeName, customAttendeeEmail, cloneReminders, customReminder,
					customReminderMethod, customReminderMinutes, retainClonesOutsideSourceEventWindow, hashMethod, hash);
		}
	}

	private void saveLimitToMap(SettingsMap map, int type) {
		// Save the type limit
		map.put("limit." + type, Limits.getTypeLimit(type));

		// Save the type counter
		Limits.TypeCounter counter = Limits.getTypeCounter(type);
		map.put("limit." + type + ".count", counter.getCount());
		map.put("limit." + type + ".startTime", counter.getStartTime());
	}

	public synchronized void saveToMap(SettingsMap map) {
		map.put("clonerEnabled", mClonerEnabled);
		map.put("clonerTimeWait", mClonerTimeWait);
		this.saveLimitToMap(map, Limits.TYPE_EVENT);
		this.saveLimitToMap(map, Limits.TYPE_ATTENDEE);
		map.put("clonerLogToLogcat", mLogToLogcat);
		map.put("clonerLogToMemory", mLogToMemory);
		map.put("clonerLogType", mLogType);

		// Save all rules to settings
		int numRules = ClonerVersion.setNumRules(mRules.length);
		for (int index = 0; index < numRules; index++) {
			Rule rule = mRules[index];
			// Basic rule settings
			map.put(ruleKey(index, "id"), rule.getId());
			map.put(ruleKey(index, "version"), rule.getVersion());
			map.put(ruleKey(index, "enabled"), rule.isEnabled());
			map.put(ruleKey(index, "name"), rule.getName());
			map.put(ruleKey(index, "method"), rule.getMethod());
			map.put(ruleKey(index, "srcCalendar"), rule.getSrcCalendarRef());
			map.put(ruleKey(index, "dstCalendar"), rule.getDstCalendarRef());
			map.put(ruleKey(index, "srcCalendarHash"), rule.getSrcCalendarHash());
			map.put(ruleKey(index, "readOnly"), rule.isReadOnly());

			// Event filters
			map.put(ruleKey(index, "syncWindowBefore"), rule.getSyncPeriodBefore());
			map.put(ruleKey(index, "syncWindowAfter"), rule.getSyncPeriodAfter());
			map.put(ruleKey(index, "includeClones"), rule.getIncludeClones());
			map.put(ruleKey(index, "useEventFilters"), rule.useEventFilters());
			map.put(ruleKey(index, "eventTypeFilter"), rule.getEventTypeFilter());
			map.put(ruleKey(index, "titleMustContain"), rule.getTitleMustContain());
			map.put(ruleKey(index, "titleMustNotContain"), rule.getTitleMustNotContain());
			map.put(ruleKey(index, "locationMustContain"), rule.getLocationMustContain());
			map.put(ruleKey(index, "locationMustNotContain"), rule.getLocationMustNotContain());
			map.put(ruleKey(index, "descriptionMustContain"), rule.getDescriptionMustContain());
			map.put(ruleKey(index, "descriptionMustNotContain"), rule.getDescriptionMustNotContain());

			map.put(ruleKey(index, "accessLevels"), rule.getAccessLevels().toString());
			map.put(ruleKey(index, "attendeeStatuses"), rule.getAttendeeStatuses().toString());
			map.put(ruleKey(index, "availabilities"), rule.getAvailabilities().toString());
			map.put(ruleKey(index, "eventStatuses"), rule.getEventStatuses().toString());
			map.put(ruleKey(index, "weekdays"), rule.getWeekdays().toString());

			// Content settings
			map.put(ruleKey(index, "cloneTitle"), rule.getCloneTitle());
			map.put(ruleKey(index, "customTitle"), rule.getCustomTitle());
			map.put(ruleKey(index, "cloneLocation"), rule.getCloneLocation());
			map.put(ruleKey(index, "customLocation"), rule.getCustomLocation());
			map.put(ruleKey(index, "cloneDescription"), rule.getCloneDescription());
			map.put(ruleKey(index, "customDescription"), rule.getCustomDescription());
			map.put(ruleKey(index, "reserveBefore"), rule.getReserveBefore());
			map.put(ruleKey(index, "reserveAfter"), rule.getReserveAfter());
			map.put(ruleKey(index, "customAvailability"), rule.getCustomAvailability());
			map.put(ruleKey(index, "customAccessLevel"), rule.getCustomAccessLevel());
			map.put(ruleKey(index, "cloneSelfAttendeeStatus"), rule.getCloneSelfAttendeeStatus());
			map.put(ruleKey(index, "cloneSelfAttendeeStatusReverse"), rule.getCloneSelfAttendeeStatusReverse());
			map.put(ruleKey(index, "selfAttendeeName"), rule.getSelfAttendeeName());

			// Attendee options
			map.put(ruleKey(index, "cloneAttendees"), rule.getCloneAttendees());
			map.put(ruleKey(index, "useDummyEmailAddresses"), rule.getUseDummyEmailAddresses());
			map.put(ruleKey(index, "dummyEmailDomain"), rule.getDummyEmailDomain());
			map.put(ruleKey(index, "attendeesAsText"), rule.getAttendeesAsText());
			map.put(ruleKey(index, "customAttendee"), rule.getCustomAttendee());
			map.put(ruleKey(index, "customAttendeeName"), rule.getCustomAttendeeName());
			map.put(ruleKey(index, "customAttendeeEmail"), rule.getCustomAttendeeEmail());

			// Reminder options
			map.put(ruleKey(index, "cloneReminders"), rule.getCloneReminders());
			map.put(ruleKey(index, "customReminder"), rule.getCustomReminder());
			map.put(ruleKey(index, "customReminderMethod"), rule.getCustomReminderMethod());
			map.put(ruleKey(index, "customReminderMinutes"), rule.getCustomReminderMinutes());

			// Additional options
			map.put(ruleKey(index, "retainClonesOutsideSourceEventWindow"),
					rule.getRetainClonesOutsideSourceEventWindow());

			// Advanced options
			map.put(ruleKey(index, "hashMethod"), rule.getHashMethod());
			map.put(ruleKey(index, "hash"), rule.getHash());

			// Legacy key removal
			map.remove(ruleKey(index, "action"));
			map.remove(ruleKey(index, "ignoreDeclined"));
			map.remove(ruleKey(index, "srcCalendarUri"));
			map.remove(ruleKey(index, "dstCalendarUri"));
			map.remove(ruleKey(index, "fwdCalendarUri"));
			map.remove(ruleKey(index, "fwdCalendar"));
			map.remove(ruleKey(index, "useEventSelection"));

			// Version 1 key removal
			map.remove(ruleKey(index, "detail"));
			map.remove(ruleKey(index, "cloneStatus"));
			map.remove(ruleKey(index, "cloneStatusReverse"));
		}
		map.put("numRules", numRules);
	}

	public int getClonerTimeWait() {
		return mClonerTimeWait;
	}

	public int getLogType() {
		return mLogType;
	}

	public boolean getLogToLogcat() {
		return mLogToLogcat;
	}

	public boolean getLogToMemory() {
		return mLogToMemory;
	}

	public int getNumberOfRules() {
		return mRules.length;
	}

	public Rule getRule(int index) {
		if (index >= 0 && index < mRules.length) {
			return mRules[index];
		}
		return null;
	}

	public Rule getRuleById(String id) {
		for (Rule rule : mRules) {
			if (rule.getId().contentEquals(id)) {
				return rule;
			}
		}
		return null;
	}

	public boolean isClonerEnabled() {
		return mClonerEnabled;
	}

	private String ruleKey(int index, String key) {
		return RULE_PREFIX + index + "." + key;
	}

	public synchronized void setClonerEnabled(boolean enabled) {
		mClonerEnabled = enabled;
		this.notifySettingsChange(true);
	}

	public void setClonerTimeWait(int seconds) {
		mClonerTimeWait = seconds;
		this.notifySettingsChange(true);
	}

	public synchronized void setRules(Rule[] rules, boolean markAllDirty) {
		int numRules = ClonerVersion.setNumRules(rules.length);

		mRules = new Rule[numRules];
		for (int index = 0; index < numRules; index++) {
			mRules[index] = rules[index];
		}
		this.notifySettingsChange(markAllDirty);
	}

	public void executeRule(int index) {
		if (index < mRules.length) {
			mRules[index].markDirty();
			this.notifySettingsChange(false);
		}
	}

	public boolean setTypeLimit(int type, int limit) {
		if (Limits.setTypeLimit(type, limit)) {
			this.notifySettingsChange(true);
			return true;
		}
		return false;
	}

	public void setLogType(int logtype) {
		mLogType = logtype;
		this.notifySettingsChange(false);
	}

	public void setLogToLogcat(boolean enabled) {
		mLogToLogcat = enabled;
		this.notifySettingsChange(false);
	}

	public void setLogToMemory(boolean enabled) {
		mLogToMemory = enabled;
		this.notifySettingsChange(false);
	}
}
