package com.dizzl.android.CalendarClonerFree;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class CalendarChangedReceiver extends BroadcastReceiver {
	private static Handler mHandler = null;
	private static Runnable mRunnable = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (mHandler != null && mRunnable != null) {
			mHandler.post(mRunnable);
		}
	}

	public static void setCallback(Handler handler, Runnable runnable) {
		mHandler = handler;
		mRunnable = runnable;
	}
}
