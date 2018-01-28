package com.dizzl.android.CalendarCloner;

import android.provider.CalendarContract.Events;

public class AccessLevels extends SelectList {
	public static final int ACCESS_CONFIDENTIAL = Events.ACCESS_CONFIDENTIAL;
	public static final int ACCESS_DEFAULT = Events.ACCESS_DEFAULT;
	public static final int ACCESS_PRIVATE = Events.ACCESS_PRIVATE;
	public static final int ACCESS_PUBLIC = Events.ACCESS_PUBLIC;

	public static final int[] mKeys = new int[] { ACCESS_DEFAULT, ACCESS_PRIVATE, ACCESS_CONFIDENTIAL, ACCESS_PUBLIC };

	public static final String[] mNames = new String[] { ClonerApp.translate(R.string.access_level_default),
			ClonerApp.translate(R.string.access_level_private),
			ClonerApp.translate(R.string.access_level_confidential), ClonerApp.translate(R.string.access_level_public) };

	public AccessLevels(boolean allSelected) {
		super(allSelected);
	}

	public AccessLevels clone() {
		AccessLevels other = new AccessLevels(false);
		other.decode(this.toString());
		return other;
	}

	@Override
	protected void init() {
		this.init(mKeys, mNames);
	}
}
