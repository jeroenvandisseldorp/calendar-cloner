package com.dizzl.android.CalendarCloner;

import java.util.ArrayList;

public class CloneRemover extends EventRemover {

	public void init(Rule rule, String question) {
		String calRef = rule.getDstCalendarRef();
		String selection;
		ArrayList<String> args = new ArrayList<String>();
		// Default: remove all clones from the destination calendar
		selection = EventMarker.buildDbSelect(EventMarker.TYPE_CLONE, rule.getHash(), null, args);

		super.init(calRef, selection, args, rule.getName(), question);
	}
}
