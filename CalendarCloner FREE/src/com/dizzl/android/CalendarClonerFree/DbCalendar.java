package com.dizzl.android.CalendarClonerFree;

import org.joda.time.DateTimeZone;

import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;

import com.dizzl.android.CalendarClonerFree.DbObject.StringField;

public class DbCalendar {
	private final DbObject mObject;
	private final CalendarsTable mTable;

	// Calendar fields
	private DbObject.LongField mId;
	private DbObject.StringField mName, mDisplayName;
	private DateTimeZone mTimeZone;
	private DbObject.StringField mAccountName, mAccountType, mOwnerAccount;
	private DbObject.IntegerField mAccessLevel;
	private DbObject.BooleanField mIsSynchronized, mIsVisible;
	private DbObject.BooleanField mCanOrganizerRespond;
	private ReminderMethods mAllowedReminders;
	private DbObject.IntegerField mMaxReminders;
	private DbObject.StringField mCalSync1;

	public DbCalendar(CalendarsTable table, DbObject obj) {
		mTable = table;
		mObject = obj;
	}

	public void loadAll() {
        try {
            this.getId();
            this.getName();
            this.getDisplayName();
            this.getTimeZone();
            this.getAccountName();
            this.getAccountType();
            this.getOwnerAccount();
            this.getAccessLevel();
            this.isSynchronized();
            this.isVisible();
            this.getCanOrganizerRespond();
            this.getAllowedReminders();
            this.getMaxReminders();
            this.getCalSync1();
        } finally {
            // Release cursor
            mObject.releaseCursor();
        }
	}

	public long getId() {
		if (mId == null) {
			mId = new DbObject.LongField(mTable._ID);
		}
		return mObject.loadField(mId);
	}

	public String getName() {
		if (mName == null) {
			mName = new DbObject.StringField(mTable.NAME);
		}
		return mObject.loadField(mName);
	}

	public String getDisplayName() {
		if (mDisplayName == null) {
			mDisplayName = new DbObject.StringField(mTable.CALENDAR_DISPLAY_NAME);
		}
		return mObject.loadField(mDisplayName);
	}

	public DateTimeZone getTimeZone() {
		if (mTimeZone == null) {
			StringField tzField = new DbObject.StringField(mTable.CALENDAR_TIME_ZONE);
			String tz = mObject.loadField(tzField);
            try {
                mTimeZone = DateTimeZone.forID(tz);
            } catch (IllegalArgumentException e) {
                // Set to default when timezone not found
                mTimeZone = DateTimeZone.getDefault();
            }
		}

		return mTimeZone;
	}

	public String getAccountName() {
		if (mAccountName == null) {
			mAccountName = new DbObject.StringField(mTable.ACCOUNT_NAME);
		}
		return mObject.loadField(mAccountName);
	}

	public String getAccountType() {
		if (mAccountType == null) {
			mAccountType = new DbObject.StringField(mTable.ACCOUNT_TYPE);
		}
		return mObject.loadField(mAccountType);
	}

	public String getOwnerAccount() {
		if (mOwnerAccount == null) {
			mOwnerAccount = new DbObject.StringField(mTable.OWNER_ACCOUNT);
		}
		return mObject.loadField(mOwnerAccount);
	}

	public int getAccessLevel() {
		if (mAccessLevel == null) {
			mAccessLevel = new DbObject.IntegerField(mTable.CALENDAR_ACCESS_LEVEL);
		}
		return mObject.loadField(mAccessLevel);
	}

	public boolean isLocal() {
		return this.getAccountType().contentEquals(CalendarContract.ACCOUNT_TYPE_LOCAL);
	}

	public boolean isSynchronized() {
		if (mIsSynchronized == null) {
			mIsSynchronized = new DbObject.BooleanField(mTable.SYNC_EVENTS);
		}
		return mObject.loadField(mIsSynchronized);
	}

	public boolean isVisible() {
		if (mIsVisible == null) {
			mIsVisible = new DbObject.BooleanField(mTable.VISIBLE);
		}
		return mObject.loadField(mIsVisible);
	}

	public boolean getCanOrganizerRespond() {
		if (mCanOrganizerRespond == null) {
			mCanOrganizerRespond = new DbObject.BooleanField(mTable.CAN_ORGANIZER_RESPOND);
		}
		return mObject.loadField(mCanOrganizerRespond);
	}

	public ReminderMethods getAllowedReminders() {
		if (mAllowedReminders == null) {
			mAllowedReminders = new ReminderMethods(false);
			DbObject.StringField field = new DbObject.StringField(mTable.ALLOWED_REMINDERS);
			String codedAllowedReminders = mObject.loadField(field);
			String[] ars = codedAllowedReminders.split(",");
			for (int index = 0; index < ars.length; index++) {
				try {
					int key = Integer.parseInt(ars[index]);
					mAllowedReminders.selectByKey(key, true);
				} catch (NumberFormatException e) {
				}
			}
		}
		return mAllowedReminders;
	}

	public int getMaxReminders() {
		if (mMaxReminders == null) {
			mMaxReminders = new DbObject.IntegerField(mTable.MAX_REMINDERS);
		}
		return mObject.loadField(mMaxReminders);
	}

	public String getCalSync1() {
		if (mCalSync1 == null) {
			mCalSync1 = new DbObject.StringField(mTable.CAL_SYNC1);
		}
		return mObject.loadField(mCalSync1);
	}

	public boolean isReadable() {
		int accessLevel = this.getAccessLevel();
		return (accessLevel != Calendars.CAL_ACCESS_NONE && accessLevel != Calendars.CAL_ACCESS_OVERRIDE);
	}

	public boolean isWriteable() {
		if (this.isLocal()) {
			return true;
		}

		int accessLevel = this.getAccessLevel();
		return (accessLevel == Calendars.CAL_ACCESS_CONTRIBUTOR || accessLevel == Calendars.CAL_ACCESS_EDITOR
				|| accessLevel == Calendars.CAL_ACCESS_OWNER || accessLevel == Calendars.CAL_ACCESS_ROOT);
	}

	public String getRef() {
		return this.getAccountName() + "/" + this.getDisplayName();
	}

	public boolean canAccess(boolean needWriteAccess) {
		if (!this.isLocal() && !this.isSynchronized()) {
			return false;
		}
		if (!needWriteAccess) {
			return this.isReadable();
		}
		return this.isWriteable();
	}
}
