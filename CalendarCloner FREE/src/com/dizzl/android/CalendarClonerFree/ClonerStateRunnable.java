package com.dizzl.android.CalendarClonerFree;

public abstract class ClonerStateRunnable {
	// Both constants are defined as negative numbers to not conflict with possible resync times
	public static final long CLONER_RUNNING = -1;
	public static final long CLONER_NOT_RUNNING = -2;

	public abstract void run(long clonerStateOrResyncTime);
}
