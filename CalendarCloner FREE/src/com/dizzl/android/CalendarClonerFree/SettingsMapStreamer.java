package com.dizzl.android.CalendarClonerFree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.preference.PreferenceManager;

public class SettingsMapStreamer {
	public static synchronized SettingsMap loadFromSharedPrefs(Context context) {
		SettingsMap result = new SettingsMap();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
			result.put(entry.getKey(), entry.getValue().toString());
		}

		return result;
	}

	public static synchronized SettingsMap loadFromFile(String filename) {
		try {
			SettingsMap result = new SettingsMap();
			File file = new File(Environment.getExternalStorageDirectory(), filename);

			FileReader fr = new FileReader(file);
			BufferedReader buffer = new BufferedReader(fr);
			String line = buffer.readLine();
			while (line != null) {
				int pos = line.indexOf("=");
				if (pos >= 0) {
					String key = line.substring(0, pos);
					String value = line.substring(pos + 1, line.length());
					result.put(key, value);
				}
				line = buffer.readLine();
			}
			buffer.close();
			return result;
		} catch (Exception e) {
			return null;
		}
	}

	public static synchronized boolean saveToSharedPrefs(Context context, SettingsMap map) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		try {
			Editor edit = prefs.edit();
			for (Map.Entry<String, String> entry : map.getAll().entrySet()) {
				edit.putString(entry.getKey(), entry.getValue());
			}
			edit.commit();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static synchronized boolean saveToFile(String filename, SettingsMap map) {
		try {
			File file = new File(Environment.getExternalStorageDirectory(), filename);
			FileWriter fw = new FileWriter(file);
			PrintWriter pw = new PrintWriter(fw);

			Map<String, String> mapping = map.getAll();
			String[] keys = mapping.keySet().toArray(new String[] { "" });
			for (int i = 0; i < keys.length - 1; i++) {
				for (int j = i + 1; j < keys.length; j++) {
					if (keys[j].compareTo(keys[i]) < 0) {
						String temp = keys[i];
						keys[i] = keys[j];
						keys[j] = temp;
					}
				}
			}

			for (String key : keys) {
				pw.println(key + "=" + mapping.get(key));
			}

			pw.close();
			fw.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
