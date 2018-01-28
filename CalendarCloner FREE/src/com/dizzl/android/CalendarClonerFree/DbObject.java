package com.dizzl.android.CalendarClonerFree;

import android.database.Cursor;

public class DbObject {
	private Cursor mCur;

	protected static class Field {
		ClonerTable.Column column;
		boolean isLoaded = false;

		public Field(ClonerTable.Column col) {
			column = col;
		}
	}

	protected static class BooleanField extends Field {
		boolean value;

		public BooleanField(ClonerTable.Column column) {
			super(column);
		}
	}

	protected static class IntegerField extends Field {
		int value;

		public IntegerField(ClonerTable.Column column) {
			super(column);
		}
	}

	protected static class IntegerFieldWithNull extends IntegerField {
		boolean isNull;

		public IntegerFieldWithNull(ClonerTable.Column column) {
			super(column);
		}
	}

	protected static class LongField extends Field {
		long value;

		public LongField(ClonerTable.Column column) {
			super(column);
		}
	}

	protected static class StringField extends Field {
		String value;

		public StringField(ClonerTable.Column column) {
			super(column);
		}
	}

	public DbObject(Cursor cur) {
		mCur = cur;
	}

	public boolean hasCursor() {
		return mCur != null;
	}

	public boolean hasColumn(ClonerTable.Column col) {
		if (this.hasCursor()) {
			return col.isPresent();
		}
		return false;
	}

	public boolean hasFilledColumn(ClonerTable.Column col) {
		if (this.hasColumn(col)) {
			return mCur.getString(col.getColumnIndex()) != null;
		}
		return false;
	}

	public void releaseCursor() {
		mCur = null;
	}

	public boolean loadField(BooleanField field) {
		if (!field.isLoaded) {
			if (this.hasCursor() && field.column.isPresent()) {
				final int columnIndex = field.column.getColumnIndex();
				try {
					field.value = mCur.getInt(columnIndex) != 0;
				} catch (NumberFormatException e) {
					field.value = false;
				}
				field.isLoaded = true;
			} else {
				field.value = false;
			}
		}
		return field.value;
	}

	public int loadField(IntegerField field) {
		if (!field.isLoaded) {
			if (this.hasCursor() && field.column.isPresent()) {
				final int columnIndex = field.column.getColumnIndex();
				try {
					field.value = mCur.getInt(columnIndex);
				} catch (NumberFormatException e) {
					field.value = 0;
				}
				field.isLoaded = true;
			} else {
				field.value = 0;
			}
		}
		return field.value;
	}

	public int loadField(IntegerFieldWithNull field) {
		if (!field.isLoaded) {
			if (this.hasCursor() && field.column.isPresent()) {
				final int columnIndex = field.column.getColumnIndex();
				field.isNull = mCur.isNull(columnIndex);
				if (!field.isNull) {
					try {
						field.value = mCur.getInt(columnIndex);
					} catch (NumberFormatException e) {
						field.value = 0;
					}
				} else {
					field.value = 0;
				}
				field.isLoaded = true;
			} else {
				field.value = 0;
			}
		}
		return field.value;
	}

	public long loadField(LongField field) {
		if (!field.isLoaded) {
			if (this.hasCursor() && field.column.isPresent()) {
				final int columnIndex = field.column.getColumnIndex();
				try {
					field.value = mCur.getLong(columnIndex);
				} catch (Exception e) {
					field.value = 0;
				}
				field.isLoaded = true;
			} else {
				field.value = 0;
			}
		}
		return field.value;
	}

	public String loadField(StringField field) {
		if (!field.isLoaded) {
			if (this.hasCursor() && field.column.isPresent()) {
				String value = mCur.getString(field.column.getColumnIndex());
				field.value = value;
				field.isLoaded = true;
			} else {
				field.value = null;
			}
		}
		return field.value;
	}

	public int getInt(ClonerTable.Column col) {
		if (mCur != null) {
			if (col.isPresent()) {
				return mCur.getInt(col.getColumnIndex());
			}
		}
		return 0;
	}

	public long getLong(ClonerTable.Column col) {
		if (mCur != null) {
			if (col.isPresent()) {
				return mCur.getLong(col.getColumnIndex());
			}
		}
		return 0;
	}

	public String getString(ClonerTable.Column col) {
		if (mCur != null) {
			if (col.isPresent()) {
				return mCur.getString(col.getColumnIndex());
			}
		}
		return null;
	}
}
