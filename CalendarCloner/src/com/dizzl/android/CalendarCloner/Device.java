package com.dizzl.android.CalendarCloner;

public class Device {
	public static class Samsung {
		public static final String AVAILABILITY = "availabilityStatus";
		public static final int AVAILABILITY_FREE = 0;
		public static final int AVAILABILITY_TENTATIVE = 1;
		public static final int AVAILABILITY_BUSY = 2;
		public static final int AVAILABILITY_OUT_OF_OFFICE = 3;

		public static boolean supportsAvailabilitySamsung(String accountType) {
			if (!ClonerApp.getDevice().supportsAvailabilitySamsung()) {
				return false;
			}
			if (accountType != null && accountType.contentEquals("com.android.exchange")) {
				return true;
			}
			return false;
		}

		public static int fromRegularAvailability(int availability) {
			switch (availability) {
			case Availabilities.AVAILABILITY_FREE:
				return AVAILABILITY_FREE;
			case Availabilities.AVAILABILITY_TENTATIVE:
				return AVAILABILITY_TENTATIVE;
			case Availabilities.AVAILABILITY_BUSY:
			default:
				return AVAILABILITY_BUSY;
			}
		}

		public static int toRegularAvailability(int availability) {
			switch (availability) {
			case AVAILABILITY_FREE:
				return Availabilities.AVAILABILITY_FREE;
			case AVAILABILITY_TENTATIVE:
				return Availabilities.AVAILABILITY_TENTATIVE;
			case AVAILABILITY_BUSY:
			case AVAILABILITY_OUT_OF_OFFICE:
			default:
				return Availabilities.AVAILABILITY_BUSY;
			}
		}
	}

	private boolean mSupportsAvailabilitySamsung;

	public Device() {
		EventsTable table = new EventsTable(ClonerApp.getDb(true));
		mSupportsAvailabilitySamsung = table.supportsColumn(table.AVAILABILITY_SAMSUNG);
	}

	public boolean supportsAvailabilitySamsung() {
		return mSupportsAvailabilitySamsung;
	}
}
