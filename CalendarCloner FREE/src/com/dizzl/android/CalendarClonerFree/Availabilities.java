package com.dizzl.android.CalendarClonerFree;

import android.provider.CalendarContract.Events;

public class Availabilities extends SelectList {
	public static final int AVAILABILITY_FREE = Events.AVAILABILITY_FREE;
	public static final int AVAILABILITY_BUSY = Events.AVAILABILITY_BUSY;
	public static final int AVAILABILITY_TENTATIVE = 2;
	public static final int AVAILABILITY_OUT_OF_OFFICE = 3;

	private static final int[] mKeys = new int[] { AVAILABILITY_FREE, AVAILABILITY_TENTATIVE, AVAILABILITY_BUSY };
	private static final int[] mKeysSamsung = new int[] { AVAILABILITY_FREE, AVAILABILITY_TENTATIVE, AVAILABILITY_BUSY,
			AVAILABILITY_OUT_OF_OFFICE };

	private static final String[] mNames = new String[] { ClonerApp.translate(R.string.availability_free),
			ClonerApp.translate(R.string.availability_tentative), ClonerApp.translate(R.string.availability_busy), };
	private static final String[] mNamesSamsung = new String[] { ClonerApp.translate(R.string.availability_free),
			ClonerApp.translate(R.string.availability_tentative), ClonerApp.translate(R.string.availability_busy),
			ClonerApp.translate(R.string.availability_out_of_office) };

	public Availabilities(boolean allSelected) {
		super(allSelected);
	}

	public Availabilities clone() {
		Availabilities other = new Availabilities(false);
		other.decode(this.toString());
		return other;
	}

	@Override
	protected void init() {
		if (ClonerApp.getDevice().supportsAvailabilitySamsung()) {
			this.init(mKeysSamsung, mNamesSamsung);
		} else {
			this.init(mKeys, mNames);
		}
	}
}
