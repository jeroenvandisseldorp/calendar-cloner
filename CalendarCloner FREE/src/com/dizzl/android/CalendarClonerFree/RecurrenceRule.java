package com.dizzl.android.CalendarClonerFree;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class RecurrenceRule {
	public static Map<String, String> parseRule(String rule) {
		if (rule == null || rule.contentEquals("")) {
			return null;
		}
		HashMap<String, String> result = new HashMap<String, String>();
		String[] parts = rule.split(";");
		for (int index = 0; index < parts.length; index++) {
			int pos = parts[index].indexOf("=");
			if (pos > 0) {
				String key = parts[index].substring(0, pos);
				String value = parts[index].substring(pos + 1, parts[index].length());
				result.put(key, value);
			}
		}
		return result;
	}

	public static boolean compareRules(String rule1, String rule2) {
		// Parse the rules
		Map<String, String> map1 = RecurrenceRule.parseRule(rule1);
		Map<String, String> map2 = RecurrenceRule.parseRule(rule2);
		// Perform first quick checking
		if ((map1 == null && map2 == null)) {
			return true;
		}
		if (map1 == null || map2 == null) {
			return false;
		}
		// Determine which is the larger map
		Map<String, String> biggest = (map1.size() > map2.size() ? map1 : map2);
		Map<String, String> smallest = (map1.size() > map2.size() ? map2 : map1);
		// Compare elements from both maps
		for (Entry<String, String> element : biggest.entrySet()) {
			String key = element.getKey();
			if (smallest.containsKey(key)) {
				String value = element.getValue();
				if (!value.contentEquals(smallest.get(key))) {
					return false;
				}
			} else {
				// Accept difference on WKST element
				if (!key.contentEquals("WKST")) {
					return false;
				}
			}
		}
		return true;
	}
}
