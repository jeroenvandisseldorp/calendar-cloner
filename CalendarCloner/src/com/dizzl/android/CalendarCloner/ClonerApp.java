package com.dizzl.android.CalendarCloner;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

public class ClonerApp extends Application {
	// Debug constants
	private static boolean mDebug = true;
	public static final boolean PROFILE = false;

	// Create a handler to handle messages to this thread
	private static Handler mHandler = new Handler();
	// Cloner thread variable
	private static ClonerThread mClonerThread = null;
	
	// Databases
	private ClonerDb mReadOnlyDb = null;
	private ClonerDb mReadWriteDb = null;

	// Event registration
	private HashMap<ClonerStateRunnable, Handler> mOnTimerSignalHandlers = new HashMap<ClonerStateRunnable, Handler>();
	private long mClonerStateOrResyncTime = ClonerStateRunnable.CLONER_NOT_RUNNING;

	// Parameter passing variables
	private HashMap<String, Object> mParams = new HashMap<String, Object>();

	// The singleton app variable
	private static ClonerApp mApp = null;
	// The time at which the app was created
	private long mTimeCreated = 0;
	// This app's settings
	private Settings mSettings = null;

	// Variables for error notification
	private int mFailCount = 0;
	private static final int ID_SYNC_FAILED = 13;

	// Device variable
	private Device mDevice;

	@Override
	public void onCreate() {
		super.onCreate();
		mApp = this;
		mTimeCreated = System.currentTimeMillis();
		mReadOnlyDb = 	new ClonerDb(this.getContentResolver(), true);
		mReadWriteDb = new ClonerDb(this.getContentResolver(), false);
		mDevice = new Device();
		
		SettingsMap map = SettingsMapStreamer.loadFromSharedPrefs(this);
		mSettings = new Settings();
		mSettings.loadfromMap(map);

		mSettings.registerSettingsChangeHandler(new Runnable() {
			@Override
			public void run() {
				saveSettingsToSharedPrefs();
			}
		}, false);

		// Automatically save preferences when the cloner stops, effectively
		// saving all rules' source calendar hashes
		ClonerApp.registerClonerStateRunnable(new ClonerStateRunnable() {
			@Override
			public void run(long clonerStateOrResyncTime) {
				if (clonerStateOrResyncTime != ClonerStateRunnable.CLONER_RUNNING) {
					saveSettingsToSharedPrefs();
				}
			}
		}, false);

		// Make sure the background instance is running!
		Intent serviceIntent = new Intent(this, ClonerService.class);
		this.startService(serviceIntent);
	}

	private void saveSettingsToSharedPrefs() {
		// Store settings in settings map
		final SettingsMap map = new SettingsMap();
		mSettings.saveToMap(map);
		// Save settings to SharedPreferences in the background
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				SettingsMapStreamer.saveToSharedPrefs(ClonerApp.this, map);
				return null;
			}
		}.execute();
	}

	public static ClonerApp getInstance() {
		return mApp;
	}

	public static long getRunTime() {
		return System.currentTimeMillis() - mApp.mTimeCreated;
	}

	public static Settings getSettings() {
		if (mApp != null) {
			return mApp.mSettings;
		}
		return null;
	}

	public static String translate(int resourceId) {
		if (mApp != null) {
			return mApp.getString(resourceId);
		}
		return "RES: " + resourceId;
	}

	public static String translate(int resourceId, String[] replacements) {
		String result = ClonerApp.translate(resourceId);
		for (int index = 1; index <= replacements.length; index++) {
			if (replacements[index - 1] != null) {
				result = result.replace("{" + index + "}", replacements[index - 1]);
			} else {
				result = result.replace("{" + index + "}", "");
			}
		}
		return result;
	}

	public static void toast(String message) {
		if (mApp != null) {
			Toast.makeText(mApp, message, Toast.LENGTH_SHORT).show();
		}
	}

	public static void toggleDebugMode() {
		mDebug = !mDebug;
	}

	public static boolean isDebugMode() {
		return mDebug;
	}

	public static void scheduleWakeup(long delay, Handler handler, Runnable runnable) {
		if (mApp != null) {
			AlarmReceiver.setCallback(handler, runnable);
			AlarmManager mgr = (AlarmManager) mApp.getSystemService(Context.ALARM_SERVICE);
			Intent i = new Intent(mApp, AlarmReceiver.class);
			PendingIntent pi = PendingIntent.getBroadcast(mApp, 0, i, 0);

			mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delay, pi);
		}
	}

	public static void registerClonerStateRunnable(ClonerStateRunnable runnable, boolean runImmediate) {
		if (mApp != null) {
			mApp.mOnTimerSignalHandlers.put(runnable, new Handler());
			if (runImmediate) {
				runnable.run(mApp.mClonerStateOrResyncTime);
			}
		}
	}

	public static void unregisterClonerStateRunnable(ClonerStateRunnable runnable) {
		if (mApp != null) {
			mApp.mOnTimerSignalHandlers.remove(runnable);
		}
	}

	public static void notifyClonerStateChange(long clonerStateOrResyncTime) {
		if (mApp != null) {
			mApp.mClonerStateOrResyncTime = clonerStateOrResyncTime;

			// Call all registered listeners
			Set<Entry<ClonerStateRunnable, Handler>> entries = mApp.mOnTimerSignalHandlers.entrySet();
			for (Entry<ClonerStateRunnable, Handler> entry : entries) {
				final ClonerStateRunnable csr = entry.getKey();
				entry.getValue().post(new Runnable() {
					@Override
					public void run() {
						csr.run(mApp.mClonerStateOrResyncTime);
					}
				});
			}
		}
	}

	public static void decreaseFailCount() {
		if (mApp != null) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					doDecreaseFailCount();
				}
			});
		}
	}

	private static void doDecreaseFailCount() {
		if (mApp != null && mApp.mFailCount > 0) {
			mApp.mFailCount--;
			if (mApp.mFailCount == 0) {
				NotificationManager notificationManager = (NotificationManager) mApp
						.getSystemService(Context.NOTIFICATION_SERVICE);
				// mId allows you to update the notification later on.
				notificationManager.cancel(ID_SYNC_FAILED);
			}
		}
	}

	public static void increaseFailCount() {
		if (mApp != null) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					doIncreaseFailCount();
				}
			});
		}
	}

	private static void doIncreaseFailCount() {
		if (mApp.mFailCount == 0) {
			ClonerApp.displayFailNotification();
		}
		mApp.mFailCount++;
	}

	private static void displayFailNotification() {
		Bitmap logo = Bitmap.createScaledBitmap(
				BitmapFactory.decodeResource(mApp.getResources(), R.drawable.ic_calendarcloner), 192, 192, false);
		Notification.Builder builder = new Notification.Builder(mApp).setSmallIcon(R.drawable.status_fail)
				.setLargeIcon(logo).setContentTitle("Cloning process failed")
				.setContentText("See rule history for more details");
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(mApp, RulesActivity.class);

		// The stack builder object will contain an artificial back stack for
		// the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(mApp);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(RulesActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

		builder.setContentIntent(resultPendingIntent);
		NotificationManager notificationManager = (NotificationManager) mApp
				.getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		notificationManager.notify(ID_SYNC_FAILED, builder.getNotification());
	}

	public static void startClonerThread() {
		if (mClonerThread == null) {
			// Start a new thread for cloning
			mClonerThread = new ClonerThread(mHandler, new Runnable() {
				@Override
				public void run() {
					onStopClonerThread();
				}
			});
			mClonerThread.start();
		}
	}

	private static void onStopClonerThread() {
		if (mClonerThread != null) {
			mClonerThread = null;
		}
	}

	public static void resync(String source) {
		if (mClonerThread != null) {
			mClonerThread.resync(source, true, true);
		}
	}

	public static void setParameter(String name, Object value) {
		if (mApp != null) {
			mApp.mParams.put(name, value);
		}
	}

	public static Object getParameter(String name) {
		if (mApp != null) {
			return mApp.mParams.get(name);
		}
		return null;
	}

	public static String getVersion() {
		if (mApp != null) {
			try {
				return mApp.getPackageManager().getPackageInfo(mApp.getPackageName(), 0).versionName;
			} catch (Exception e) {
			}
		}
		return "";
	}

	public static Device getDevice() {
		if (mApp != null) {
			return mApp.mDevice;
		}
		return null;
	}

	public static ClonerDb getDb(boolean readOnly) {
		if (mApp != null) {
			if (readOnly) {
				return mApp.mReadOnlyDb;
			}
			return mApp.mReadWriteDb;
		}
		return null;
	}
}
