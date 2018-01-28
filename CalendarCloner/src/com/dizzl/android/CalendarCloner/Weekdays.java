package com.dizzl.android.CalendarCloner;

import java.util.Calendar;

public class Weekdays extends SelectList {
	public static final int MONDAY = Calendar.MONDAY;
	public static final int TUESDAY = Calendar.TUESDAY;
	public static final int WEDNESDAY = Calendar.WEDNESDAY;
	public static final int THURSDAY = Calendar.THURSDAY;
	public static final int FRIDAY = Calendar.FRIDAY;
	public static final int SATURDAY = Calendar.SATURDAY;
	public static final int SUNDAY = Calendar.SUNDAY;

	public static final int[] mKeys = new int[] { MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY };
	public static final String[] mNames = new String[] { ClonerApp.translate(R.string.weekday_monday),
			ClonerApp.translate(R.string.weekday_tuesday), ClonerApp.translate(R.string.weekday_wednesday),
			ClonerApp.translate(R.string.weekday_thursday), ClonerApp.translate(R.string.weekday_friday),
			ClonerApp.translate(R.string.weekday_saturday), ClonerApp.translate(R.string.weekday_sunday) };

	public Weekdays(boolean sunday, boolean monday, boolean tuesday, boolean wednesday, boolean thursday,
			boolean friday, boolean saturday) {
		super();
		this.selectByKey(SUNDAY, sunday);
		this.selectByKey(MONDAY, monday);
		this.selectByKey(TUESDAY, tuesday);
		this.selectByKey(WEDNESDAY, wednesday);
		this.selectByKey(THURSDAY, thursday);
		this.selectByKey(FRIDAY, friday);
		this.selectByKey(SATURDAY, saturday);
	}

	public Weekdays(boolean allSelected) {
		super(allSelected);
	}

	public Weekdays clone() {
		Weekdays other = new Weekdays(false);
		other.decode(this.toString());
		return other;
	}

	@Override
	protected void init() {
		this.init(mKeys, mNames);
	}

	@Override
	protected void decode(String encoded) {
		if (encoded == null) {
			encoded = "";
		}

		if (encoded.length() == 7) {
			int[] keys = new int[] { SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY };
			for (int index = 0; index < 7; index++) {
				this.selectByKey(keys[index], encoded.substring(index, index + 1).contentEquals("1"));
			}
		} else {
			super.decode(encoded);
		}
	}
}
