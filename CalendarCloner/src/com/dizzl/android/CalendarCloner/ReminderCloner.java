package com.dizzl.android.CalendarCloner;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.provider.CalendarContract.Reminders;

public class ReminderCloner extends Processor {
	int mMaxReminders;
	Rule mRule;
	RemindersTable mRemindersTable;

	public static class ReminderCloneContext {
		protected LogLines logLines = null;
		protected Map<Long, Long> mappedReminders = new HashMap<Long, Long>();
		protected Map<Long, ContentValues> reminderDeltas = new HashMap<Long, ContentValues>();
	}

	public ReminderCloner(Rule rule, RemindersTable table, int maxReminders) {
		mRule = rule;
		mRemindersTable = table;
		mMaxReminders = maxReminders;
	}

	private boolean compareReminders(DbReminder eventReminder, DbReminder cloneReminder) {
		// Return true if both reminders are similar
		return eventReminder.getMethod() == cloneReminder.getMethod()
				&& eventReminder.getMinutes() == cloneReminder.getMinutes();
	}

	private void prepareSrcReminderList(List<DbReminder> reminders) {
		// If we do not need to clone original reminders, clear the list
		if (!mRule.getCloneReminders()) {
			reminders.clear();
		}
		// If the rule specifies a custom reminder, add it here
		if (mRule.getCustomReminder()) {
			DbReminder reminder = new DbReminder(mRemindersTable, mRule.getCustomReminderMethod(),
					mRule.getCustomReminderMinutes());
			reminders.add(reminder);
		}
		// Remove duplicate reminders (get filtered out in sync most of the
		// times anyway)
		for (int i = 0; i < reminders.size() - 1; i++) {
			int j = i + 1;
			while (j < reminders.size()) {
				if (compareReminders(reminders.get(i), reminders.get(j))) {
					reminders.remove(j);
				} else {
					j++;
				}
			}
		}
		// Trim the list of reminders down to the maximum number
		while (reminders.size() > mMaxReminders) {
			reminders.remove(reminders.size() - 1);
		}
	}

	private void cloneReminderFields(DbReminder reminder, ContentValues delta) {
		delta.put(Reminders.METHOD, reminder.getMethod());
		delta.put(Reminders.MINUTES, reminder.getMinutes());
	}

	public boolean process(Event event, long cloneId, ReminderCloneContext reminderCloneContext) {
		this.useLogLines(reminderCloneContext.logLines);
		boolean updated = false;

		// Load the event's and clone's reminders
		List<DbReminder> eventReminders = DbReminder.getReminders(mRemindersTable, event.getId());
		List<DbReminder> cloneReminders = DbReminder.getReminders(mRemindersTable, cloneId);
		// Calculate the cloned list of reminders
		this.prepareSrcReminderList(eventReminders);

		// Try to find exact matches between event reminders and clone reminders
		List<DbReminder> unclonedReminders = new LinkedList<DbReminder>();
		for (DbReminder eventReminder : eventReminders) {
			// Find an exact match in the list of cloned reminders
			boolean matchFound = false;
			for (DbReminder cloneReminder : cloneReminders) {
				if (compareReminders(eventReminder, cloneReminder)) {
					matchFound = true;
					cloneReminders.remove(cloneReminder);
					// Register mapping from event reminder to clone reminder
					reminderCloneContext.mappedReminders.put(eventReminder.getId(), cloneReminder.getId());
					break;
				}
			}
			// Move the reminder to a list of reminders to (re)clone
			if (!matchFound) {
				unclonedReminders.add(eventReminder);
			}
		}

		// Clone all remaining reminders
		for (DbReminder unclonedReminder : unclonedReminders) {
			long cloneReminderId = 0;
			// If we have reminder entries left on the clonedReminder list,
			// reuse those to clone this reminder
			if (cloneReminders.size() > 0) {
				DbReminder cloneReminder = cloneReminders.get(0);
				cloneReminderId = cloneReminder.getId();
				cloneReminders.remove(cloneReminder);
			}

			ContentValues delta = new ContentValues();
			this.cloneReminderFields(unclonedReminder, delta);
			if (cloneReminderId > 0) {
				mRemindersTable.update(cloneReminderId, delta);
			} else {
				delta.put(Reminders.EVENT_ID, cloneId);
				cloneReminderId = mRemindersTable.insert(delta);
			}
			// Register mapping from event reminder to clone reminder
			reminderCloneContext.mappedReminders.put(unclonedReminder.getId(), cloneReminderId);
			// Register clone reminder delta
			reminderCloneContext.reminderDeltas.put(cloneReminderId, delta);
			updated = true;
		}

		// Remove all remaining reminders from the cloned event
		for (DbReminder cloneReminder : cloneReminders) {
			this.log(ClonerLog.LOG_UPDATE, ClonerApp.translate(R.string.cloner_log_reminder_deleted));
			mRemindersTable.delete(cloneReminder.getId());
			updated = true;
		}

		return updated;
	}
}
