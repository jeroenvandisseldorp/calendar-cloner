package com.dizzl.android.CalendarClonerFree;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ClonerService extends Service {
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		ClonerApp.startClonerThread();
		return START_STICKY;
	}
}
