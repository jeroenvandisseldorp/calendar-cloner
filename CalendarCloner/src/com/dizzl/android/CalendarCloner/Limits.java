package com.dizzl.android.CalendarCloner;

import android.util.SparseArray;

public class Limits {
	public static final int TYPE_EVENT = 0;
	public static final int TYPE_ATTENDEE = 1;
	public static final int EVENT_LIMITS[] = { 50, 100, 250, 500, 1000, 2000, 4000, 10000, 0 };
	public static final int ATTENDEE_LIMITS[] = { 25, 50, 100, 150, 200, 300, 500, 1000, 0 };

	private static int mAttendeeLimit;
	private static int mEventLimit;

	private static long mCurrentTime = 0;

	// The registration for all modifications
	public static class TypeCounter {
		private int mCount = 0;
		private boolean mLimitReached = false;
		private long mStartTime = 0;

		public int getCount() {
			return mCount;
		}

		public boolean getLimitReached() {
			return mLimitReached;
		}

		public long getStartTime() {
			return mStartTime;
		}

		public void setCount(int count) {
			mCount = count;
		}

		public void setStartTime(long startTime) {
			mStartTime = startTime;
		}
	}

	private static SparseArray<TypeCounter> mTypeCounters = new SparseArray<TypeCounter>();

	public static boolean canModify(int type) {
		return Limits.canModify(type, 1);
	}

	public static boolean canModify(int type, int number) {
		// Find the relevant counter
		TypeCounter counter = Limits.getTypeCounter(type);

		// Initialize the counter time if not set
		if (counter.mStartTime == 0) {
			counter.mStartTime = mCurrentTime;
		}

		// If more than an hour has passed, reset the counter
		long inBetween = mCurrentTime - counter.mStartTime;
		if (inBetween > 60 * 60 * 1000) {
			counter.mCount = 0;
			counter.mLimitReached = false;
			counter.mStartTime = mCurrentTime;
		}

		int limit = Limits.getTypeLimit(type);
		if (limit == 0 || counter.mCount <= limit - number) {
			counter.mCount += number;
			return true;
		}

		counter.mLimitReached = true;
		return false;
	}

	public static TypeCounter getTypeCounter(int type) {
		TypeCounter counter = mTypeCounters.get(type);
		if (counter != null) {
			return counter;
		}

		// Create a new counter and add it to the array
		counter = new TypeCounter();
		mTypeCounters.put(type, counter);
		return counter;
	}

	public static int getTypeLimit(int type) {
		int limit = 0;
		switch (type) {
		case TYPE_EVENT:
			limit = mEventLimit;
			break;
		case TYPE_ATTENDEE:
			limit = mAttendeeLimit;
			break;
		}
		return limit;
	}

	public static void setTypeCounter(int type, TypeCounter counter) {
		mTypeCounters.put(type, counter);
	}

	public static boolean setTypeLimit(int type, int limit) {
		switch (type) {
		case TYPE_EVENT:
			int mOldEventLimit = mEventLimit;
			if (ClonerVersion.IS_PAID_VERSION()) {
				mEventLimit = Utilities.getSelectionFromArray(EVENT_LIMITS, limit, EVENT_LIMITS[0]);
			} else {
				mEventLimit = EVENT_LIMITS[0];
			}
			return mEventLimit != mOldEventLimit;
		case TYPE_ATTENDEE:
			int mOldAttendeeLimit = mAttendeeLimit;
			if (ClonerVersion.IS_PAID_VERSION()) {
				mAttendeeLimit = Utilities.getSelectionFromArray(ATTENDEE_LIMITS, limit, ATTENDEE_LIMITS[0]);
			} else {
				mAttendeeLimit = ATTENDEE_LIMITS[0];
			}
			return mAttendeeLimit != mOldAttendeeLimit;
		}
		return false;
	}

	public static void startRun() {
		mCurrentTime = System.currentTimeMillis();
	}
}
