package com.dizzl.android.CalendarCloner;

import android.content.ContentValues;
import android.database.Cursor;

public class DbObjectWithDelta extends DbObject {
	ContentValues mDelta;

	public DbObjectWithDelta(Cursor cur, ContentValues delta) {
		super(cur);
		mDelta = delta;
	}

	@Override
	public boolean loadField(BooleanField field) {
		if (mDelta != null && mDelta.containsKey(field.column.getName())) {
			field.isLoaded = true;
			field.value = mDelta.getAsInteger(field.column.getName()) != 0;
			return field.value;
		}
		return super.loadField(field);
	}

	public int loadField(IntegerField field) {
		if (mDelta != null && mDelta.containsKey(field.column.getName())) {
			field.isLoaded = true;
			field.value = mDelta.getAsInteger(field.column.getName());
			return field.value;
		}
		return super.loadField(field);
	}

	public int loadField(IntegerFieldWithNull field) {
		if (mDelta != null && mDelta.containsKey(field.column.getName())) {
			field.isLoaded = true;
			field.isNull = false;
			field.value = mDelta.getAsInteger(field.column.getName());
			return field.value;
		}
		return super.loadField(field);
	}

	public long loadField(LongField field) {
		if (mDelta != null && mDelta.containsKey(field.column.getName())) {
			field.isLoaded = true;
			field.value = mDelta.getAsLong(field.column.getName());
			return field.value;
		}
		return super.loadField(field);
	}

	public String loadField(StringField field) {
		if (mDelta != null && mDelta.containsKey(field.column.getName())) {
			field.isLoaded = true;
			field.value = mDelta.getAsString(field.column.getName());
			return field.value;
		}
		return super.loadField(field);
	}
}
