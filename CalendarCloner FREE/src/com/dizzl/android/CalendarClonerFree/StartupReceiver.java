package com.dizzl.android.CalendarClonerFree;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartupReceiver extends BroadcastReceiver {
	private static boolean mStartupReceived = false;

	public static boolean startupReceived() {
		return mStartupReceived;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		mStartupReceived = true;
		Intent serviceIntent = new Intent(context, ClonerService.class);
		context.startService(serviceIntent);
	}

}
