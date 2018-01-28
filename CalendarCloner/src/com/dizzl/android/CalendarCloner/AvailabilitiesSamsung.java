package com.dizzl.android.CalendarCloner;

public class AvailabilitiesSamsung extends SelectList {
	public static final int AVAILABILITY_FREE = Device.Samsung.AVAILABILITY_FREE;
	public static final int AVAILABILITY_BUSY = Device.Samsung.AVAILABILITY_BUSY;
	public static final int AVAILABILITY_TENTATIVE = Device.Samsung.AVAILABILITY_TENTATIVE;
	public static final int AVAILABILITY_OUT_OF_OFFICE = Device.Samsung.AVAILABILITY_OUT_OF_OFFICE;

	public static final int[] mKeys = new int[] { AVAILABILITY_FREE, AVAILABILITY_TENTATIVE, AVAILABILITY_BUSY,
			AVAILABILITY_OUT_OF_OFFICE };

	public static final String[] mNames = new String[] { ClonerApp.translate(R.string.availability_free),
			ClonerApp.translate(R.string.availability_tentative), ClonerApp.translate(R.string.availability_busy),
			ClonerApp.translate(R.string.availability_out_of_office) };

	public AvailabilitiesSamsung(boolean allSelected) {
		super(allSelected);
	}

	public Availabilities clone() {
		Availabilities other = new Availabilities(false);
		other.decode(this.toString());
		return other;
	}

	@Override
	protected void init() {
		this.init(mKeys, mNames);
	}
}
