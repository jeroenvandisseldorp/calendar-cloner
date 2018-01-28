package com.dizzl.android.CalendarCloner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class LogActivity extends Activity {
	private LogAdapter mAdapter;
	private ClonerLog mLog = null;
	private boolean mUseLogLevel = true;
	private int mLogLevel = ClonerLog.LOG_UPDATE;
	private boolean mDisplayHeader = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.overridePendingTransition(R.anim.animation_enter_right, R.anim.animation_leave_left);
		setContentView(R.layout.logactivity_layout);
		this.getActionBar().setDisplayHomeAsUpEnabled(true);

		mLog = (ClonerLog) ClonerApp.getParameter("log");

		Intent i = this.getIntent();
		if (i.getExtras() != null) {
			mDisplayHeader = i.getExtras().getBoolean("displayHeader");
			this.getActionBar().setTitle(
					i.getExtras().getString("title", ClonerApp.translate(R.string.rulelog_activity_title)));
			mUseLogLevel = i.getExtras().getBoolean("uselevel", true);
		} else {
			this.getActionBar().setTitle(ClonerApp.translate(R.string.rulelog_activity_title));
		}

		// Prepare the event type filter
		final Spinner logFilterView = (Spinner) findViewById(R.id.logLevelView);
		String[] eventTypes = new String[] { ClonerApp.translate(R.string.log_type_info),
				ClonerApp.translate(R.string.log_type_warning), ClonerApp.translate(R.string.log_type_update),
				ClonerApp.translate(R.string.log_type_error) };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, eventTypes);
		adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		logFilterView.setAdapter(adapter);
		logFilterView.setSelection(mLogLevel);
		logFilterView.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> view, View item, int position, long id) {
				mLogLevel = position;
				updateView();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				mLogLevel = 0;
				updateView();
			}
		});

		// Disable filter if not used
		if (!mUseLogLevel) {
			mLogLevel = ClonerLog.LOG_INFO;
			View filter = findViewById(R.id.logLevelLine);
			filter.setVisibility(View.GONE);
		}
		this.updateView();
	}

	private void updateView() {
		TextView tv = (TextView) findViewById(R.id.tvLogName);
		ExpandableListView elv = (ExpandableListView) findViewById(R.id.elvLog);

		// Remove default icons
		elv.setGroupIndicator(null);

		if (mLog != null) {
			if (mLog.getTitle() != null && !mLog.getTitle().contentEquals("") && mDisplayHeader) {
				tv.setText(ClonerApp.translate(R.string.rulelog_rule) + ": " + mLog.getTitle());
				tv.setVisibility(View.VISIBLE);
			} else {
				tv.setVisibility(View.GONE);
			}

			if (mLog instanceof LogMemory) {
				LogMemory log = (LogMemory) mLog;
				LogLine[] summaries = new LogLine[log.size()];
				log.getSummaries(summaries);
				LogLines[] lineSets = new LogLines[log.size()];
				log.getLineSets(lineSets);
				mAdapter = new LogAdapter(this, summaries, lineSets, mLogLevel);
			} else {
				mAdapter = new LogAdapter(this, new LogLine[0], new LogLines[0], mLogLevel);
			}
			elv.setAdapter(mAdapter);

			// Expand if requested by caller
			Intent i = this.getIntent();
			if (i.getExtras() != null && i.getExtras().getBoolean("expand", false)) {
				for (int index = 0; index < mAdapter.getGroupCount(); index++) {
					elv.expandGroup(index);
				}
			}
		} else {
			tv.setText("");
			elv.setAdapter((BaseExpandableListAdapter) null);
		}
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
}
