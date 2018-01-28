package com.dizzl.android.CalendarCloner;

import android.database.Cursor;

public class DbInstanceIterator extends EventIterator {
	private final InstancesTable mInstancesTable = new InstancesTable(ClonerApp.getDb(true));
	private final Period mPeriod;

	public DbInstanceIterator(Period period, ClonerLog log) {
		super(log);
		mPeriod = period;
	}

	@Override
	protected Cursor doQuery(long sourceCalendarId) {
		return mInstancesTable.queryByDay(sourceCalendarId, mPeriod.getStart().getMillis(), mPeriod.getEnd()
				.getMillis());
	}

	@Override
	protected DbEvent getEvent(Cursor cur) {
		return new DbInstance(mInstancesTable, new DbObject(cur));
	}

	@Override
	protected EventsTable getTable() {
		return mInstancesTable;
	}
}
