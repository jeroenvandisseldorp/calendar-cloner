package com.dizzl.android.CalendarClonerFree;

import android.provider.CalendarContract.Reminders;

public class ReminderMethods extends SelectList {
	public static final int REMINDER_METHOD_ALERT = Reminders.METHOD_ALERT;
	public static final int REMINDER_METHOD_DEFAULT = Reminders.METHOD_DEFAULT;
	public static final int REMINDER_METHOD_EMAIL = Reminders.METHOD_EMAIL;
	public static final int REMINDER_METHOD_SMS = Reminders.METHOD_SMS;

	public static final int[] mKeys = new int[] { REMINDER_METHOD_ALERT, REMINDER_METHOD_DEFAULT,
			REMINDER_METHOD_EMAIL, REMINDER_METHOD_SMS };

	public static final String[] mNames = new String[] { ClonerApp.translate(R.string.reminder_method_alert),
			ClonerApp.translate(R.string.reminder_method_default), ClonerApp.translate(R.string.reminder_method_email),
			ClonerApp.translate(R.string.reminder_method_sms) };

	public ReminderMethods(boolean allSelected) {
		super(allSelected);
	}

	public ReminderMethods clone() {
		ReminderMethods other = new ReminderMethods(false);
		other.decode(this.toString());
		return other;
	}

	@Override
	protected void init() {
		this.init(mKeys, mNames);
	}
}
