package com.dizzl.android.CalendarClonerFree;

import java.util.HashMap;
import java.util.Map;

public class SettingsMap {
	private HashMap<String, String> mMap = new HashMap<String, String>();

	public boolean contains(String key) {
		return mMap.containsKey(key);
	}

	public void clear() {
		mMap.clear();
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		String value = mMap.get(key);
		if (value == null) {
			return defaultValue;
		}
		if (value.contentEquals("true")) {
			return true;
		}
		if (value.contentEquals("false")) {
			return false;
		}
		return defaultValue;
	}

	public int getInt(String key, int defaultValue) {
		String value = mMap.get(key);
		if (value == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value);
		} catch (Exception e) {
			// Eat
		}
		return defaultValue;
	}

	public long getLong(String key, long defaultValue) {
		String value = mMap.get(key);
		if (value == null) {
			return defaultValue;
		}
		try {
			return Long.parseLong(value);
		} catch (Exception e) {
			// Eat
		}
		return defaultValue;
	}

	public String getString(String key, String defaultValue) {
		String result = mMap.get(key);
		if (result == null) {
			return defaultValue;
		}
		return result;
	}

	public void put(String key, boolean value) {
		this.put(key, value ? "true" : "false");
	}

	public void put(String key, int value) {
		this.put(key, "" + value);
	}

	public void put(String key, long value) {
		this.put(key, "" + value);
	}

	public void put(String key, String value) {
		mMap.put(key, value);
	}

	public Map<String, String> getAll() {
		return mMap;
	}

	public void remove(String key) {
		mMap.remove(key);
	}
}
