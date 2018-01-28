package com.dizzl.android.CalendarCloner;

import java.util.ArrayList;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class ClonerTable {
	public class Column {
		private String mName;
		private int mIndex = -1;
		private boolean mPresent = false;

		public Column(String name) {
			mName = name;
		}

		public String getName() {
			return mName;
		}

		public int getColumnIndex() {
			return mIndex;
		}

		public boolean isPresent() {
			return mPresent;
		}
	}

	private ClonerDb mDb;
	private Uri mUri;
	protected String[] mProjection;
	private ArrayList<Column> mColumns = new ArrayList<Column>();
	private boolean mColumnsDirty = true;
	private boolean mReadOnly = false;

	public ClonerTable(ClonerDb db, Uri uri) {
		mDb = db;
		if (db == null) {
			Log.e("ClonerTable",  this.getClass().getCanonicalName());
			try {
				throw new Exception();
			} catch (Exception e) {
				new LogLogcat("title", ClonerLog.TYPE_EXTENDED, ClonerLog.LOG_INFO).stacktrace(e);
			}
		}
		mUri = uri;
		mProjection = null;
	}

	public ClonerDb getDb() {
		return mDb;
	}

	protected void addColumn(Column col) {
		mColumns.add(col);
		mColumnsDirty = true;
	}

	public void setProjection(Column[] cols) {
		if (cols != null) {
			mProjection = new String[cols.length];
			for (int index = 0; index < cols.length; index++) {
				mProjection[index] = cols[index].mName;
			}
		} else {
			mProjection = null;
		}
		mColumnsDirty = true;
	}

	public void setReadOnly(boolean readOnly) {
		mReadOnly = readOnly;
	}

	protected Cursor getById(long id) {
		Uri uri = ContentUris.withAppendedId(mUri, id);
		Cursor cur = mDb.query(uri, mProjection, null, null, null);
		this.indexColumnsFromCursor(cur);
		return cur;
	}

	protected void indexColumnsFromCursor(Cursor cur) {
		if (cur != null) {
			if (mColumnsDirty) {
				for (int index = 0; index < mColumns.size(); index++) {
					Column col = mColumns.get(index);
					col.mIndex = cur.getColumnIndex(col.mName);
					col.mPresent = col.mIndex >= 0;
				}
				mColumnsDirty = false;
			}
		}
	}

	public Cursor query(String selection, String[] selectionArgs, String sortOrder) {
		return this.rawQuery(mUri, selection, selectionArgs, sortOrder);
	}

	public Cursor rawQuery(Uri uri, String selection, String[] selectionArgs, String sortOrder) {
		Cursor cur = mDb.query(uri, mProjection, selection, selectionArgs, sortOrder);
		if (cur != null) {
			this.indexColumnsFromCursor(cur);
			return new ClonerCursor(cur, "Query " + uri.toString() + " WHERE " + selection + " ARGS "
					+ (selectionArgs != null ? selectionArgs.toString() : "null") + " SORTBY " + sortOrder);
		}
		return null;
	}

	public CursorLoader getLoader(Context context, String selection, String[] selectionArgs, String sortOrder) {
		return new CursorLoader(context, mUri, mProjection, selection, selectionArgs, sortOrder) {
			@Override
			public void deliverResult(Cursor cur) {
				indexColumnsFromCursor(cur);
				super.deliverResult(cur);
			}
		};
	}

	protected boolean supportsColumn(Column col) {
		boolean result = false;
		Cursor cur = this.getById(0);
		try {
			result = col.isPresent();
		} finally {
			cur.close();
		}
		return result;
	}

	public long insert(ContentValues values) {
		if (!mReadOnly) {
			Uri uri = mDb.insert(mUri, values);
			if (uri != null && uri != Uri.EMPTY) {
				return ContentUris.parseId(uri);
			}
		}
		return 0;
	}

	public int update(long id, ContentValues values) {
		if (!mReadOnly) {
			final Uri uri = ContentUris.withAppendedId(mUri, id);
			return mDb.update(uri, values, null, null);
		}
		return 1;
	}

	public int delete(long id) {
		if (!mReadOnly) {
			final Uri uri = ContentUris.withAppendedId(mUri, id);
			return mDb.delete(uri, null, null);
		}
		return 1;
	}
}
