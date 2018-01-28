package com.dizzl.android.CalendarClonerFree;

import android.content.ContentValues;

public class Differ {
	protected void compareField(ClonerTable.Column field, String newValue, String oldValue, boolean mandatoryField,
			ContentValues delta) {
		if (newValue != null) {
			newValue = newValue.trim();
		} else {
			newValue = "";
		}
		if (oldValue != null) {
			oldValue = oldValue.trim();
		} else {
			oldValue = "";
		}

		// If mandatory field or the new value differs from the old value
		if (mandatoryField || !newValue.contentEquals(oldValue)) {
			// Add field to the delta
			delta.put(field.getName(), newValue);
		}
	}
}
