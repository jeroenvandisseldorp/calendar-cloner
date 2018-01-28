package com.dizzl.android.CalendarCloner;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class ClonerDb {
	// Application behavior settings
	private static final ClonerLog mLog = new LogLogcat("ClonerDb", ClonerLog.TYPE_EXTENDED, ClonerLog.LOG_WARNING);
	private ContentResolver mCr;
	private boolean mIsReadOnly;

	public ClonerDb(ContentResolver cr, boolean readOnly) {
		mCr = cr;
		mIsReadOnly = readOnly;
	}

	public boolean isReadOnly() {
		return mIsReadOnly;
	}

	public int delete(Uri uri, String where, String[] selectionArgs) {
		mLog.debug(ClonerApp.translate(R.string.db_deleted) + " " + uri.toString());
		if (!mIsReadOnly) {
			try {
				return mCr.delete(uri, where, selectionArgs);
			} catch (Exception e) {
				mLog.stacktrace(e);
			}
		}
		return 0;
	}

	public Uri insert(Uri uri, ContentValues values) {
		if (!mIsReadOnly) {
			try {
				Uri insertUri = mCr.insert(uri, values);
				mLog.debug(ClonerApp.translate(R.string.db_inserted) + " " + insertUri.toString());
				return insertUri;
			} catch (Exception e) {
				mLog.stacktrace(e);
			}
		} else {
			mLog.debug("  " + values.toString());
		}
		return null;
	}

	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		try {
			return mCr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			mLog.stacktrace(e);
		}
		return null;
	}

	public int update(Uri uri, ContentValues values, String where, String[] selectionArgs) {
		mLog.debug(ClonerApp.translate(R.string.db_updated) + " " + uri.toString());
		if (!mIsReadOnly) {
			try {
				return mCr.update(uri, values, where, selectionArgs);
			} catch (Exception e) {
				mLog.stacktrace(e);
			}
		} else {
			mLog.debug("  " + values.toString());
		}
		return 1;
	}
}
