package com.dizzl.android.CalendarClonerFree;

import android.os.Debug;

public class RuleExecutor {
	public class Result {
		public static final int STATUS_SUCCESS = 0;
		public static final int STATUS_NOT_COMPLETED = 1;
		public static final int STATUS_FAIL = 2;

		private final int mStatus;
		private final String mSummary;

		public Result(int status, String summary) {
			mStatus = status;
			mSummary = summary;
		}

		public int getStatus() {
			return mStatus;
		}

		public String getSummary() {
			return mSummary;
		}
	}

	private Result run(Rule rule) throws Exception {
		EventProcessor processor = null;
		EventIterator iterator = null;
		if (rule.getMethod() == Rule.METHOD_CLONE) {
			processor = new EventCloner(rule);
			iterator = new DbEventIterator(rule.getLog());
		}
		if (rule.getMethod() == Rule.METHOD_MOVE) {
			processor = new EventMover(rule);
			iterator = new DbEventIterator(rule.getLog());
		}
		if (rule.getMethod() == Rule.METHOD_AGGREGATE) {
			EventAggregator aggregator = new EventAggregator(rule);
			processor = aggregator;
			iterator = new DbInstanceIterator(aggregator.getPeriod(), rule.getLog());
		}
		if (processor == null) {
			return new Result(Result.STATUS_FAIL, "Unknown method");
		}

		// Log if rule is in readonly mode
		if (rule.isReadOnly()) {
			rule.getLog().log(
					rule.getLog().createLogLine(ClonerLog.LOG_INFO, "",
							ClonerApp.translate(R.string.cloner_log_executing_readonly_rule)), null);
		}

		DbEventIterator.Result result = iterator.execute(processor);
		int status = rule.getLog().getMaxLevel() != ClonerLog.LOG_ERROR ? result.getStatus() : ClonerLog.LOG_ERROR;
		switch (status) {
		case DbEventIterator.Result.STATUS_SUCCESS:
			// Update the source calendar hash in the rule
			rule.clearSrcCalendarHash();
			return new Result(Result.STATUS_SUCCESS, result.getSummary());
		case DbEventIterator.Result.STATUS_NOT_COMPLETED:
			return new Result(Result.STATUS_NOT_COMPLETED, result.getSummary());
		default:
			return new Result(Result.STATUS_FAIL, result.getSummary());
		}
	}

	public Result execute(Rule rule) {
		try {
			if (ClonerVersion.isExpired()) {
				return new Result(Result.STATUS_FAIL, ClonerApp.translate(R.string.msg_app_expired));
			}
			if (ClonerApp.PROFILE) {
				Debug.startMethodTracing("ClonerRule " + rule.getName(), 64 * 1024 * 1024);
				// Debug.startAllocCounting();
			}
			return this.run(rule);
		} catch (IllegalStateException e) {
			// Weird stuff happening here. Probably the query is open too long
			// while syncing, so we decide here to leave for now and proceed
			// next iteration
			ClonerLog log = rule.getLog();
			log.stacktrace(e);
			log.log(log.createLogLine(ClonerLog.LOG_ERROR, null,
					ClonerApp.translate(R.string.cloner_state_partially_cloned_info)), null);
			return new Result(Result.STATUS_NOT_COMPLETED, ClonerApp.translate(R.string.cloner_state_partially_cloned)
					+ ": " + Utilities.getNowString());
		} catch (Exception e) {
			rule.getLog().stacktrace(e);
			return new Result(Result.STATUS_FAIL, ClonerApp.translate(R.string.log_info_see_rulelog));
		} finally {
			if (ClonerApp.PROFILE) {
				// Debug.stopAllocCounting();
				try {
					Debug.dumpHprofData("/storage/sdcard0/ClonerRule hprof " + rule.getName());
				} catch (Exception e) {
					rule.getLog().stacktrace(e);
				}
				Debug.stopMethodTracing();
			}
		}
	}
}
