package com.dizzl.android.CalendarClonerFree;

import android.provider.CalendarContract.Events;

public class EventStatuses extends SelectList {
	public static final int STATUS_TENTATIVE = Events.STATUS_TENTATIVE;
	public static final int STATUS_CONFIRMED = Events.STATUS_CONFIRMED;
	public static final int STATUS_CANCELED = Events.STATUS_CANCELED;

	public static final int[] mKeys = new int[] { STATUS_TENTATIVE, STATUS_CONFIRMED, STATUS_CANCELED };
	public static final String[] mNames = new String[] { ClonerApp.translate(R.string.event_status_tentative),
			ClonerApp.translate(R.string.event_status_confirmed), ClonerApp.translate(R.string.event_status_canceled) };

	public EventStatuses(boolean allSelected) {
		super(allSelected);
	}

	public EventStatuses clone() {
		EventStatuses other = new EventStatuses(false);
		other.decode(this.toString());
		return other;
	}

	@Override
	protected void init() {
		this.init(mKeys, mNames);
	}
}
