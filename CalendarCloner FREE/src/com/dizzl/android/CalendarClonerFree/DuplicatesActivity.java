package com.dizzl.android.CalendarClonerFree;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.provider.CalendarContract.Events;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class DuplicatesActivity extends Activity implements LoaderCallbacks<DuplicatesEntry[]> {
	CalendarsTable mCalendarsTable = new CalendarsTable(ClonerApp.getDb(true));
	List<String> mCalendarRefs = CalendarLoader.getValidRefs();
	DuplicatesAdapter mAdapter;
	Spinner mSearchCalendarView;
	MenuItem mRefresh;
	long mRowCount = 0;

	long mSearchCalendarId = -1;
	Loader<DuplicatesEntry[]> mLastLoader = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.overridePendingTransition(R.anim.animation_enter_right, R.anim.animation_leave_left);

		setContentView(R.layout.duplicatesactivity_layout);
		this.getActionBar().setDisplayHomeAsUpEnabled(true);
		this.getActionBar().setTitle(R.string.duplicates_activity_title);

		String[] calNames = new String[mCalendarRefs.size() + 1];
		calNames[0] = ClonerApp.translate(R.string.duplicates_all_calendars);
		for (int index = 0; index < mCalendarRefs.size(); index++) {
			CalendarLoader.CalendarInfo info = CalendarLoader.getCalendarByRef(mCalendarRefs.get(index));
			if (info.getCalendar() != null) {
				calNames[index + 1] = info.getCalendar().getDisplayName();
			} else {
				calNames[index + 1] = "###";
			}
		}

		mSearchCalendarView = (Spinner) findViewById(R.id.searchCalendar);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, calNames);
		adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		mSearchCalendarView.setAdapter(adapter);
		mSearchCalendarView.setSelection(0);
		mSearchCalendarView.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> view, View item, int position, long id) {
				long newCalId;
				if (position > 0) {
					CalendarLoader.CalendarInfo info = CalendarLoader.getCalendarByRef(mCalendarRefs.get(position - 1));
					if (info.getCalendar() != null) {
						newCalId = info.getCalendar().getId();
					} else {
						newCalId = -1;
					}
				} else {
					newCalId = -1;
				}
				if (newCalId != mSearchCalendarId) {
					mSearchCalendarId = newCalId;
					updateQuery();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				mSearchCalendarId = -1;
				updateQuery();
			}
		});

		ListView lv = (ListView) findViewById(R.id.eventList);
		mAdapter = new DuplicatesAdapter(this, R.layout.duplicatesrow_layout, null);
		lv.setAdapter(mAdapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> view, View item, int position, long id) {
				Intent i = new Intent(DuplicatesActivity.this, BrowseActivity.class);
				String selection = "";
				ArrayList<DuplicatesEntry.Entry> events = mAdapter.getDuplicates()[position].events;
				for (int index = 0; index < events.size(); index++) {
					if (index > 0) {
						selection += " OR ";
					}
					selection += Events._ID + "=" + events.get(index).eventId;
				}
				i.putExtra("duplicatesSelection", selection);
				startActivity(i);
			}
		});
	}

	@Override
	public void onStart() {
		getLoaderManager().initLoader(0, null, this);
		super.onStart();
	}

	private void updateQuery() {
		getLoaderManager().restartLoader(0, null, this);
	}

	private class QueryParams {
		String selection = "";
		ArrayList<String> args = new ArrayList<String>();
	}

	private QueryParams getQueryParams() {
		QueryParams result = new QueryParams();
		result.selection = "";
		if (mSearchCalendarId >= 0) {
			if (!result.selection.contentEquals("")) {
				result.selection += " AND ";
			}
			result.selection += Events.CALENDAR_ID + " = ?";
			result.args.add("" + mSearchCalendarId);
		}
		return result;
	}

	@Override
	public Loader<DuplicatesEntry[]> onCreateLoader(int arg0, Bundle arg1) {
		this.updateRefresh(true);
		TextView counter = (TextView) findViewById(R.id.searchCount);
		counter.setText(ClonerApp.translate(R.string.duplicates_loading));

		QueryParams query = this.getQueryParams();
		mLastLoader = new DuplicatesLoader(this, query.selection, query.args.toArray(new String[query.args.size()]));
		return mLastLoader;
	}

	@Override
	public void onLoadFinished(Loader<DuplicatesEntry[]> loader, DuplicatesEntry[] data) {
		DuplicatesLoader dupLoader = (DuplicatesLoader) loader;
		if (!dupLoader.isReset()) {
			mAdapter.swapData(data);
			if (loader == mLastLoader && data != null) {
				this.updateRefresh(false);
				mRowCount = data.length;
				TextView counter = (TextView) findViewById(R.id.searchCount);
				counter.setText(ClonerApp.translate(R.string.duplicates_textview_event_count, new String[] { ""
						+ mRowCount }));
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<DuplicatesEntry[]> loader) {
		mAdapter.swapData(null);
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

	private void updateRefresh(boolean refresh) {
		if (mRefresh == null) {
			return;
		}

		LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext().getSystemService(
				LAYOUT_INFLATER_SERVICE);
		if (refresh) {
			mRefresh.setActionView(inflater.inflate(R.layout.actionbar_indeterminate_progress, null));
			mRefresh.setOnMenuItemClickListener(null);
		} else {
			mRefresh.setActionView(null);
			mRefresh.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem arg0) {
					updateQuery();
					return true;
				}
			});
			// refreshView = inflater.inflate(R.layout.actionbar_refresh_button, null);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mRefresh = menu.add(0, R.id.browse_calendar, 0, ClonerApp.translate(R.string.duplicates_refresh));
		mRefresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		this.updateRefresh(true);
		return super.onCreateOptionsMenu(menu);
	}
}
