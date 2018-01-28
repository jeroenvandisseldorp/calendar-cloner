package com.dizzl.android.CalendarCloner;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract.Events;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class BrowseActivity extends Activity implements LoaderCallbacks<Cursor> {
	List<String> mCalendarRefs = CalendarLoader.getValidRefs();
	CalendarsTable mCalendarsTable = new CalendarsTable(ClonerApp.getDb(true));
	EventsTable mEventsTable = new EventsTable(ClonerApp.getDb(true));

	CursorAdapter mAdapter;
	long mRowCount = 0;

	Spinner mSearchCloneSourceView;

	static final int SEARCH_ALL_EVENTS = 0;
	static final int SEARCH_ONLY_NORMAL = 1;
	static final int SEARCH_ONLY_CLONES = 2;

	long mSearchCalendarId = -1;
	String mSearchFilter = "";
	int mSearchEventType = SEARCH_ALL_EVENTS;
	Rule mSearchCloneSourceRule = null;

	String mDuplicatesSelection = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.overridePendingTransition(R.anim.animation_enter_right, R.anim.animation_leave_left);
		setContentView(R.layout.browseactivity_layout);
		this.getActionBar().setDisplayHomeAsUpEnabled(true);

		final EditText searchFilterView = (EditText) findViewById(R.id.searchKey);
		final Spinner searchCalendarView = (Spinner) findViewById(R.id.searchCalendar);
		final Spinner searchEventTypeView = (Spinner) findViewById(R.id.searchEventType);
		mSearchCloneSourceView = (Spinner) findViewById(R.id.searchCloneSourceCalendar);

		Bundle params = this.getIntent().getExtras();
		if (params == null || !params.containsKey("duplicatesSelection")) {
			this.getActionBar().setTitle(R.string.browse_activity_title);

			// Prepare the calendar adapter
			String[] calNames = new String[mCalendarRefs.size() + 1];
			calNames[0] = ClonerApp.translate(R.string.browse_all_calendars);
			for (int index = 0; index < mCalendarRefs.size(); index++) {
				CalendarLoader.CalendarInfo info = CalendarLoader.getCalendarByRef(mCalendarRefs.get(index));
				if (info.getCalendar() != null) {
					calNames[index + 1] = info.getCalendar().getDisplayName();
				} else {
					calNames[index + 1] = "###";
				}
			}
			ArrayAdapter<String> calendarAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
					calNames);
			calendarAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
			searchCalendarView.setAdapter(calendarAdapter);
			searchCalendarView.setSelection(0);
			searchCalendarView.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> view, View item, int position, long id) {
					if (position > 0) {
						CalendarLoader.CalendarInfo info = CalendarLoader.getCalendarByRef(mCalendarRefs
								.get(position - 1));
						if (info.getCalendar() != null) {
							mSearchCalendarId = info.getCalendar().getId();
						} else {
							mSearchCalendarId = -1;
						}
					} else {
						mSearchCalendarId = -1;
					}
					updateQuery();
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					mSearchCalendarId = -1;
					updateQuery();
				}
			});

			// Prepare the search filter
			searchFilterView.addTextChangedListener(new TextWatcher() {
				@Override
				public void afterTextChanged(Editable arg0) {
					mSearchFilter = arg0.toString();
					updateQuery();
				}

				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				}

				@Override
				public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				}
			});

			// Prepare the event type filter
			String[] eventTypes = new String[] { ClonerApp.translate(R.string.browse_type_all_events),
					ClonerApp.translate(R.string.browse_type_originals),
					ClonerApp.translate(R.string.browse_type_clones_only) };
			ArrayAdapter<String> eventTypeAdapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item, eventTypes);
			eventTypeAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
			searchEventTypeView.setAdapter(eventTypeAdapter);
			searchEventTypeView.setSelection(0);
			searchEventTypeView.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> view, View item, int position, long id) {
					mSearchEventType = position;
					updateQuery();
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					mSearchCalendarId = -1;
					updateQuery();
				}
			});

			Button clear = (Button) findViewById(R.id.clearSearchKey);
			clear.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					searchFilterView.setText("");
				}
			});

			final Settings settings = ClonerApp.getSettings();
			String[] ruleNames = new String[settings.getNumberOfRules() + 1];
			ruleNames[0] = ClonerApp.translate(R.string.browse_all_rules);
			for (int index = 0; index < settings.getNumberOfRules(); index++) {
				ruleNames[index + 1] = settings.getRule(index).getName();
			}
			// Prepare the source rule filter
			ArrayAdapter<String> ruleAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
					ruleNames);
			ruleAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
			mSearchCloneSourceView.setAdapter(ruleAdapter);
			mSearchCloneSourceView.setSelection(0);
			mSearchCloneSourceView.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> view, View item, int position, long id) {
					if (position > 0) {
						mSearchCloneSourceRule = settings.getRule(position - 1);
					} else {
						mSearchCloneSourceRule = null;
					}
					updateQuery();
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					mSearchCloneSourceRule = null;
					updateQuery();
				}
			});
		} else {
			// Initialize for the viewing of duplicate events
			this.getActionBar().setTitle(R.string.duplicates_view_title);
			mDuplicatesSelection = params.getString("duplicatesSelection");
			View filters = findViewById(R.id.search_filters);
			filters.setVisibility(View.GONE);
		}

		final LayoutInflater inflater = LayoutInflater.from(this);
		ListView lv = (ListView) findViewById(R.id.eventList);
		mAdapter = new CursorAdapter(this, null, 0) {
			@Override
			public void bindView(View view, Context context, Cursor cur) {
				ImageView recurringIcon = (ImageView) view.findViewById(R.id.eventRecurring);
				ImageView exceptionIcon = (ImageView) view.findViewById(R.id.eventException);
				ImageView singleIcon = (ImageView) view.findViewById(R.id.eventSingle);
				ImageView trashcanIcon = (ImageView) view.findViewById(R.id.trashcan);

				TextView title = (TextView) view.findViewById(R.id.title);
				TextView subline1 = (TextView) view.findViewById(R.id.subline1);
				TextView subline2 = (TextView) view.findViewById(R.id.subline2);
				Event event = new DbEvent(mEventsTable, new DbObject(cur));

				recurringIcon.setVisibility(event.isRecurringEvent() ? View.VISIBLE : View.GONE);
				exceptionIcon.setVisibility(event.isRecurringEventException() ? View.VISIBLE : View.GONE);
				singleIcon.setVisibility(!event.isRecurringEvent() && !event.isRecurringEventException() ? View.VISIBLE
						: View.GONE);
				trashcanIcon.setVisibility(event.isDeleted() ? View.VISIBLE : View.GONE);

				String calendarName = CalendarLoader.getCalendarNameOrErrorMessage(event.getCalendarId());
				title.setText(event.getTitle());
				String originalStartTime = event.isRecurringEventException() ? "  (from: "
						+ Utilities.dateTimeToString(event.getOriginalInstanceTime()) + ")" : "";
				subline1.setText(event.getId() + "  " + calendarName + "  "
						+ Utilities.dateTimeToString(event.getStartTime()) + originalStartTime);
				if (event.getLocation() != null) {
					subline2.setText(event.getLocation());
				} else {
					subline2.setText("");
				}
			}

			@Override
			public View newView(Context context, Cursor cur, ViewGroup parent) {
				return inflater.inflate(R.layout.eventrow_layout, parent, false);
			}
		};

		lv.setAdapter(mAdapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> view, View item, int position, long id) {
				Event event = DbEvent.get(mEventsTable, id);
				if (event != null) {
					ClonerLog log = new LogMemory(event.getTitle(), ClonerLog.TYPE_EXTENDED);
					logEvent(log, event, event.getTitle());

					List<Event> clones = getClonesOf(event);
					if (clones.size() > 0) {
						// Add empty line
						log.log(log.createLogLine(ClonerLog.LOG_INFO, null, ""), null);
					}
					for (Event clone : clones) {
						String ruleHash = null;
						EventMarker.Marker marker = EventMarker.parseCloneEventHash(clone);
						if (marker != null) {
							ruleHash = marker.ruleHash;
						}
						String ruleName = ruleHash != null ? findRule(ruleHash, clone.getCalendarId()) : "";
						if (!ruleName.contentEquals("")) {
							ruleName = " (" + ruleName + ")";
						}
						logEvent(log, clone, ClonerApp.translate(R.string.cloner_log_cloned_event) + ruleName + ": "
								+ clone.getTitle());
					}

					ClonerApp.setParameter("log", log);
					Intent i = new Intent(BrowseActivity.this, LogActivity.class);
					i.putExtra("expand", true);
					i.putExtra("uselevel", false);
					i.putExtra("title", ClonerApp.translate(R.string.browse_event_details));
					i.putExtra("displayHeader", false);
					startActivity(i);
				}
			}
		});
		lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
				final ListView.AdapterContextMenuInfo info = (ListView.AdapterContextMenuInfo) menuInfo;

				MenuItem mi = menu.add(ClonerApp.translate(R.string.browse_delete_event));
				mi.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						long eventId = info.id;
						getContentResolver().delete(Events.CONTENT_URI, "((" + Events._ID + "=?))",
								new String[] { "" + eventId });
						updateQuery();
						return false;
					}
				});
			}
		});

		getLoaderManager().initLoader(0, null, this);
	}

	private List<Event> getClonesOf(Event event) {
		List<Event> result = new LinkedList<Event>();
		List<String> args = new LinkedList<String>();
		String selection = EventMarker.buildDbSelect(EventMarker.TYPE_CLONE, null, Hasher.hash(event.getUniqueId()),
				args);
		Cursor cur = mEventsTable.query(selection, args.toArray(new String[args.size()]), null);
		if (cur != null) {
			try {
				while (cur.moveToNext()) {
					DbEvent clone = new DbEvent(mEventsTable, new DbObject(cur));
					clone.loadAll();
					result.add(clone);
				}
			} finally {
				cur.close();
			}
		}
		return result;
	}

	private String findRule(String ruleHash, long calendarId) {
		String result = "";
		Settings settings = ClonerApp.getSettings();
		for (int index = 0; index < settings.getNumberOfRules(); index++) {
			Rule rule = settings.getRule(index);
			CalendarLoader.CalendarInfo info = CalendarLoader.getCalendarByRef(rule.getDstCalendarRef());
			if (info.getCalendar() != null && info.getCalendar().getId() == calendarId) {
				if (rule.getHash().contentEquals(ruleHash)) {
					if (!result.contentEquals("")) {
						result += ", ";
					}
					result += rule.getName();
				}
			}
		}
		return result;
	}

	private void logEvent(ClonerLog log, Event event, String title) {
		LogLines logLines = log.createLogLines();
		if (logLines != null) {
			logLines.logEvent(event);
			List<DbAttendee> atts = DbAttendee.getByEvent(new AttendeesTable(ClonerApp.getDb(true)), event.getId());
			if (atts.size() > 0) {
				logLines.addEmptyLine();
				logLines.logAttendees(ClonerLog.LOG_INFO, atts);
			}
			List<DbReminder> rems = DbReminder.getReminders(new RemindersTable(ClonerApp.getDb(true)), event.getId());
			if (rems.size() > 0) {
				logLines.addEmptyLine();
				logLines.logReminders(ClonerLog.LOG_INFO, rems, null, null);
			}
		}
		LogLine summary = log.createLogLine(ClonerLog.LOG_INFO, null, title);
		log.log(summary, logLines);
	}

	private void updateQuery() {
		mSearchCloneSourceView.setEnabled(mSearchEventType != SEARCH_ALL_EVENTS
				&& mSearchEventType != SEARCH_ONLY_NORMAL);
		getLoaderManager().restartLoader(0, null, this);
	}

	private class QueryParams {
		String selection = "";
		ArrayList<String> args = new ArrayList<String>();
	}

	private QueryParams getQueryParams() {
		QueryParams result = new QueryParams();
		if (mDuplicatesSelection == null) {
			result.selection = "";
			if (!mSearchFilter.contentEquals("")) {
				if (!result.selection.contentEquals("")) {
					result.selection += " AND ";
				}
				result.selection += "(" + Events._ID + "=? OR " + Events.TITLE + " LIKE ? OR " + Events.EVENT_LOCATION
						+ " LIKE ? OR " + Events.DESCRIPTION + " LIKE ? OR " + Events.ORIGINAL_ID + " LIKE ?)";
				result.args.add(mSearchFilter);
				result.args.add("%" + mSearchFilter + "%");
				result.args.add("%" + mSearchFilter + "%");
				result.args.add("%" + mSearchFilter + "%");
				result.args.add("%" + mSearchFilter + "%");
			}
			if (mSearchCalendarId >= 0) {
				if (!result.selection.contentEquals("")) {
					result.selection += " AND ";
				}
				result.selection += Events.CALENDAR_ID + " = ?";
				result.args.add("" + mSearchCalendarId);
			}
			if (mSearchEventType == SEARCH_ONLY_NORMAL) {
				if (!result.selection.contentEquals("")) {
					result.selection += " AND ";
				}
				result.selection += EventMarker.buildDbSelect(EventMarker.TYPE_NORMAL, null, null, result.args);
			}

			// For all other types we need the ruleHash
			String ruleHash = mSearchCloneSourceRule != null ? mSearchCloneSourceRule.getHash() : null;
			if (mSearchEventType == SEARCH_ONLY_CLONES) {
				if (!result.selection.contentEquals("")) {
					result.selection += " AND ";
				}
				result.selection += EventMarker.buildDbSelect(EventMarker.TYPE_CLONE, ruleHash, null, result.args);
			}
		} else {
			result.selection = mDuplicatesSelection;
		}
		return result;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		QueryParams query = this.getQueryParams();
		return mEventsTable.getLoader(this, query.selection, query.args.toArray(new String[query.args.size()]),
				Events.DTSTART + " ASC, " + Events.ORIGINAL_INSTANCE_TIME + " ASC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);
		if (data != null) {
			mRowCount = data.getCount();
			TextView counter = (TextView) findViewById(R.id.searchCount);
			counter.setText(ClonerApp.translate(R.string.browse_textview_event_count, new String[] { "" + mRowCount }));
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go back
			this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem mi = menu.add(ClonerApp.translate(R.string.browse_menu_delete_all_events));
		mi.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				QueryParams query = getQueryParams();
				EventRemover remover = new EventRemover();
				remover.init(null, query.selection, query.args,
						ClonerApp.translate(R.string.browse_menu_delete_all_events_title),
						ClonerApp.translate(R.string.ask_delete_n_events, new String[] { "" + mRowCount }));
				remover.setOnDoneListener(new EventRemover.OnDoneListener() {
					@Override
					public void onDone(boolean removed, boolean success, long removeCount, String errorMessage) {
						AlertDialog.Builder builder = new AlertDialog.Builder(BrowseActivity.this);
						if (removed) {
							if (success) {
								builder.setTitle(
										ClonerApp.translate(R.string.msg_events_deleted, new String[] { ""
												+ removeCount }))
										.setMessage(ClonerApp.translate(R.string.msg_events_deleted_info))
										.setPositiveButton(ClonerApp.translate(R.string.ok), null).setCancelable(true)
										.show();
							} else {
								builder.setTitle(ClonerApp.translate(R.string.error_deleting_events))
										.setMessage(errorMessage)
										.setPositiveButton(ClonerApp.translate(R.string.ok), null).setCancelable(true)
										.show();
							}
						}
					}
				});
				remover.execute(BrowseActivity.this);
				return true;
			}
		});
		return true;
	}
}
