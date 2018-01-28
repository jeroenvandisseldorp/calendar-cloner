package com.dizzl.android.CalendarCloner;

import java.util.LinkedList;
import java.util.List;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

public class ClonerThread extends Thread {
	private static final long ONE_MINUTE = 60 * 1000;
	private static ClonerLog mLog = new LogLogcat("ClonerThread", ClonerLog.TYPE_EXTENDED, ClonerLog.LOG_INFO);

	private boolean mEnabled = false;
	private long mLastResyncTime = 0;
	private List<Rule> mRules = new LinkedList<Rule>();
	private boolean mRulesChanged = false;
	private boolean mSyncAgainAfterTaskQueue = false;
	private boolean mSyncAgainForce = false;
	private boolean mSyncAgainAll = false;
	private Boolean mTaskExecuting = false;
	private List<ClonerTask> mTasks = new LinkedList<ClonerTask>();
	private Runnable mLastTimedRunnable = null;
	private long mTimeWait = 120;

	private Handler mHandler = null;
	private Handler mParentHandler = null;
	private Runnable mOnExit = null;
	private static Looper mThreadLooper = null;

	private class ReloadConfigRunnable implements Runnable {
		@Override
		public void run() {
			Settings settings = ClonerApp.getSettings();

			if (settings.isClonerEnabled()) {
				// Reload the TimeWait amount
				mTimeWait = settings.getClonerTimeWait();
				// Reload the rules from configuration
				mRules.clear();
				for (int index = 0; index < settings.getNumberOfRules(); index++) {
					mRules.add(settings.getRule(index));
				}
				mRulesChanged = true;

				// Check to see if we are (re)enabling
				if (!mEnabled) {
					// Set enabled
					mEnabled = true;
					// Force resync on all rules
					requestResync("Service enabled", true, true);
				} else {
					// Force resync on dirty rules
					requestResync("Config reload", true, false);
				}
			} else {
				if (mEnabled) {
					// Set disabled
					mEnabled = false;
				}
			}
		}
	}

	public ClonerThread(Handler parentHandler, Runnable onExit) {
		mParentHandler = parentHandler;
		mOnExit = onExit;
	}

	public synchronized void initLooper() {
		if (ClonerThread.mThreadLooper == null) {
			ClonerThread.mThreadLooper = Looper.myLooper();
		}
	}

	public synchronized void endLooper() {
		if (ClonerThread.mThreadLooper != null) {
			ClonerThread.mThreadLooper.quit();
			ClonerThread.mThreadLooper = null;
		}
	}

	@Override
	public void run() {
		// Background thread runs at lower priority
		this.setPriority(MIN_PRIORITY);
		Looper.prepare();

		// Register stuff
		mHandler = new Handler();

		// Register callback for calendar changes
		CalendarChangedReceiver.setCallback(mHandler, new Runnable() {
			@Override
			public void run() {
				// Resync request on all rules
				requestResync("CalendarProvider", false, true);
			}
		});

		// Load the preferences and start if enabled
		ReloadConfigRunnable rcr = new ReloadConfigRunnable();
		Settings settings = ClonerApp.getSettings();
		settings.registerSettingsChangeHandler(rcr, true);

		this.initLooper();
		Looper.loop(); // loop until "quit()" is called.

		// Unregister the change handler
		settings.unregisterSettingsChangeHandler(rcr);
		// Remove the callback for calendar changes
		CalendarChangedReceiver.setCallback(null, null);
		// Signal parent that we've stopped execution
		mParentHandler.post(mOnExit);
	}

	private void setWakeupTimer(long delay) {
		// Set the timed runnable now
		mLastTimedRunnable = new Runnable() {
			@Override
			public void run() {
				if (this == mLastTimedRunnable) {
					mLastTimedRunnable = null;
					// Resync request on all rules
					requestResync("TimedRunnable", false, true);
				}
			}
		};
		ClonerApp.scheduleWakeup(delay, mHandler, mLastTimedRunnable);
		mLog.info(ClonerApp.translate(R.string.log_timer_set, new String[] { "" + delay }));
		ClonerApp.notifyClonerStateChange(System.currentTimeMillis() + delay);
	}

	private void requestResync(String source, boolean forceRun, boolean forceAll) {
		mLog.info(ClonerApp.translate(R.string.log_cloner_resync_request_source, new String[] { source }));

		// Only process if cloner is enabled
		if (!mEnabled) {
			return;
		}

		// If a timer has been set and no force is applied, then exit straight
		// away
		if (mLastTimedRunnable != null && !forceRun) {
			return;
		}

		// If the device just booted up
		if (StartupReceiver.startupReceived()) {
			// Delay the initial run for one minute to allow the device to start
			// normally (prevent boot hog)
			long runTime = ClonerApp.getRunTime();
			if (runTime < ONE_MINUTE) {
				this.setWakeupTimer(ONE_MINUTE - runTime);
				return;
			}
		}

		// If a task is already executing, then mark for resync and exit
		if (mTaskExecuting) {
			// If rules have changed, don't bother executing the old rules
			if (mRulesChanged) {
				while (mTasks.size() > 1) {
					mTasks.remove(1);
				}
				mRulesChanged = false;
			}
			mSyncAgainAfterTaskQueue = true;
			mSyncAgainForce |= forceRun;
			return;
		}

		// If no force, then calculate delay timer interval
		if (!forceRun) {
			// If we ran too recently then set a timer and wait some more
			long now = System.currentTimeMillis();
			long delay = (mTimeWait * 1000) - (now - mLastResyncTime);

			if (delay > 0) {
				this.setWakeupTimer(delay);
				return;
			}
		}

		// If a timed resync is pending in the queue, disable it by setting
		// mLastTimedRunnable to null (disables executing requestResync in its
		// run() method
		mLastTimedRunnable = null;

		// Resync process starts here
		ClonerApp.notifyClonerStateChange(ClonerStateRunnable.CLONER_RUNNING);

		// Initialize current time for cloner limits
		Limits.startRun();

		// If we need to execute all rules, mark them dirty here
		if (forceAll) {
			for (int index = 0; index < mRules.size(); index++) {
				mRules.get(index).markDirty();
			}
		}

		mTasks.clear();
		// Copy all dirty rules into a queue of cloner tasks
		for (int index = 0; index < mRules.size(); index++) {
			Rule r = mRules.get(index);
			if (r.isDirty()) {
				mTasks.add(new ClonerTask(r));
			}
		}

		// No rule changed as of last task queuing
		mRulesChanged = false;

		mLog.info(ClonerApp.translate(R.string.log_processing_task_queue));
		processTaskQueue();
	}

	private void onResyncDone() {
		mLastResyncTime = System.currentTimeMillis();
		ClonerApp.notifyClonerStateChange(ClonerStateRunnable.CLONER_NOT_RUNNING);

		// In the FREE_VERSION we sync every timer interval. In the PAID_VERSION we allow quiet times by listening to
		// calendar changes. This makes battery saving an added feature of the paid version.
		if (ClonerVersion.shouldResyncAfterEachInterval()) {
			mSyncAgainAfterTaskQueue = true;
		}

		// If we received a signal during resync, we immediately request a resync here
		if (mSyncAgainAfterTaskQueue) {
			mSyncAgainAfterTaskQueue = false;
			boolean forceRun = mSyncAgainForce;
			boolean forceAll = mSyncAgainAll;
			mSyncAgainForce = false;
			mSyncAgainAll = false;
			this.requestResync("SyncAgain flag", forceRun, forceAll);
		}
	}

	private void processTaskQueue() {
		// Always check to see if cloning is enabled
		if (mEnabled) {
			// Process the next task
			if (!mTaskExecuting && mTasks.size() > 0) {
				mTaskExecuting = true;
				mTasks.get(0).execute(new Void[] {});
			}
		} else {
			// Clear task queue if cloning is disabled
			mTasks.clear();
		}
	}

	private void onTaskFinished(final Boolean completed) {
		try {
			if (!completed) {
				// Resync request on this rule
				mTasks.get(0).mRule.markDirty();
				this.requestResync("Not completed", false, false);
			}
			mTasks.remove(0);
			mTaskExecuting = false;
			processTaskQueue();
			if (!mTaskExecuting) {
				this.onResyncDone();
			}
		} catch (Exception e) {
			mLog.stacktrace(e);
		}
	}

	class ClonerTask extends AsyncTask<Void, Void, Boolean> {
		private Rule mRule;

		public ClonerTask(Rule rule) {
			// Clone the rule itself to prevent users altering it while cloning
			mRule = rule;
		}

		@Override
		protected Boolean doInBackground(Void... arg0) {
			boolean completed = true;
			Settings settings = ClonerApp.getSettings();
			int lockSecret = mRule.tryLock();
			if (lockSecret != 0) {
				if (mRule.isEnabled()) {
					mRule.startExecution(ClonerApp.translate(R.string.cloner_state_syncing) + "...");
					settings.notifyRuleStatusChange();

					final ClonerLog log;
					if (settings.getLogToLogcat() && settings.getLogToMemory()) {
						log = new LogSplitter(mRule.getName(), settings.getLogType(), ClonerLog.LOG_INFO);
					} else if (settings.getLogToLogcat()) {
						log = new LogLogcat(mRule.getName(), settings.getLogType(), ClonerLog.LOG_INFO);
					} else if (settings.getLogToMemory()) {
						log = new LogMemory(mRule.getName(), settings.getLogType());
					} else {
						log = new LogNull(mRule.getName());
					}

					mRule.setLog(log);
					try {
						// If rule failed before, decrease fail count
						if (mRule.hasExecuted() && mRule.getStatus() == RuleExecutor.Result.STATUS_FAIL) {
							ClonerApp.decreaseFailCount();
						}
						// Clone here
						RuleExecutor executor = new RuleExecutor();
						RuleExecutor.Result result = executor.execute(mRule);
						mRule.finishExecution(
						// RuleExecutor.Result.STATUS_FAIL,
								result.getStatus(), result.getSummary());
						if (mRule.getStatus() == RuleExecutor.Result.STATUS_NOT_COMPLETED) {
							completed = false;
						}
						if (mRule.getStatus() == RuleExecutor.Result.STATUS_FAIL) {
							ClonerApp.increaseFailCount();
						}
					} catch (Exception e) {
						mRule.getLog().stacktrace(e);
						mRule.finishExecution(RuleExecutor.Result.STATUS_FAIL,
								ClonerApp.translate(R.string.log_exception) + ": " + e.toString());
					}
					settings.notifyRuleStatusChange();
				}
				mRule.tryRelease(lockSecret);
			} else {
				mRule.finishExecution(RuleExecutor.Result.STATUS_FAIL, ClonerApp.translate(R.string.cloner_log_skipped));
				settings.notifyRuleStatusChange();
			}
			return completed;
		}

		@Override
		protected void onPostExecute(final Boolean completed) {
			// Make sure onTaskFinished is executed in the parent thread (necessary since JELLY BEAN and later force
			// execution of onPostExecute in main thread)
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					onTaskFinished(completed);
				}
			});
		}
	}

	public void resync(final String source, final boolean forceRun, final boolean forceAll) {
		// Can be called from outside the thread, so schedule a message to be delivered in the execution thread
		if (mHandler != null) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					// Simulate a rule change so resync starts from the top
					mRulesChanged = true;
					requestResync(source, forceRun, forceAll);
				}
			});
		}
	}
}
