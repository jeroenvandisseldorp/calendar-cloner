package com.dizzl.android.CalendarCloner;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class RulesActivity extends ListActivity {
	private boolean mPaused = false;
	private Rule[] mRules = null;

	private int mRetainedIndex = 0;
	private int mRetainedTop = 0;

	private TouchInterceptor mList;
	private TouchInterceptor.DropListener mDropListener = new TouchInterceptor.DropListener() {
		public void drop(int from, int to) {
			// Ignore people that move the helper list item on empy list
			if (mRules.length > 0) {
				// Assuming that item is moved up the list
				int direction = -1;
				int loop_start = from;
				int loop_end = to;
	
				// For instance where the item is dragged down the list
				if (from < to) {
					direction = 1;
				}
	
				// Move the items
				Rule target = mRules[from];
				for (int i = loop_start; i != loop_end; i = i + direction) {
					mRules[i] = mRules[i + direction];
				}
				mRules[to] = target;
	
				RulesActivity.this.saveRules(null);
				((BaseAdapter) mList.getAdapter()).notifyDataSetChanged();
			}
		}
	};

	private Runnable mRuleStatusChangeRunnable = new Runnable() {
		@Override
		public void run() {
			onRuleStatusChange();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPaused = false;
		this.overridePendingTransition(R.anim.animation_enter_right, R.anim.animation_leave_left);
		setContentView(R.layout.rulesactivity_layout);
		this.getActionBar().setDisplayHomeAsUpEnabled(true);
		this.getActionBar().setTitle(ClonerApp.translate(R.string.rules_activity_title));
	}

	@Override
	public void onResume() {
		super.onResume();
		// Clear the log parameter for the LogActivity
		ClonerApp.setParameter("log", null);

		// Update view
		this.updateListView();
		if (mPaused) {
			this.restoreTopPosition();
			this.overridePendingTransition(R.anim.animation_enter_left, R.anim.animation_leave_right);
			mPaused = false;
		}

		// Register for rule update notifications
		Settings settings = ClonerApp.getSettings();
		settings.registerRuleStatusChangeHandler(mRuleStatusChangeRunnable, true);
	}

	@Override
	public void onPause() {
		super.onPause();
		mPaused = true;
		this.retainTopPosition();
		Settings settings = ClonerApp.getSettings();
		settings.unregisterRuleStatusChangeHandler(mRuleStatusChangeRunnable);
	}

	public void retainTopPosition() {
		mRetainedIndex = mList.getFirstVisiblePosition();
		View v = mList.getChildAt(0);
		mRetainedTop = (v == null) ? 0 : v.getTop();
	}

	public void restoreTopPosition() {
		mList.setSelectionFromTop(mRetainedIndex, mRetainedTop);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (mRules.length > 0) {
			// Start RuleActivity
			Intent i = new Intent(this, RuleActivity.class);
			i.putExtra("ruleid", mRules[position].getId());
			startActivityForResult(i, 1);
		}
	}

	private void onRuleInfoClick(int rulenr) {
		Intent i = new Intent(this, LogActivity.class);
		ClonerApp.setParameter("log", mRules[rulenr].getLog());
		i.putExtra("title", ClonerApp.translate(R.string.rulelog_activity_title));
		startActivity(i);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Settings settings = ClonerApp.getSettings();
		if (requestCode == 1 && data != null) {
			if (data.getBooleanExtra("changed", false)) {
				settings.notifyRuleStatusChange();
			}
		}
		if (requestCode == 2) {
			settings.notifyRuleStatusChange();
		}
	}

	private void removeRule(int ruleIndex) {
		Rule[] rules = new Rule[mRules.length - 1];
		for (int index = 0; index < ruleIndex; index++) {
			rules[index] = mRules[index];
		}
		for (int index = ruleIndex + 1; index < mRules.length; index++) {
			rules[index - 1] = mRules[index];
		}
		mRules = rules;

		// Save the rules to preferences and notify changes
		this.saveRules(null);
		updateListView();
	}

	private void askUserToRemoveRule(final int ruleIndex) {
		final EventRemover.OnDoneListener listener = new EventRemover.OnDoneListener() {
			@Override
			public void onDone(boolean removed, boolean success, long removeCount, String errorMessage) {
				removeRule(ruleIndex);
			}
		};

		DialogInterface.OnClickListener removeRuleClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					// Yes button clicked
					askUserToRemoveRelatedClones(ruleIndex, ClonerApp.translate(R.string.ask_delete_related_clones),
							listener);
					break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(RulesActivity.this);
		builder.setTitle(mRules[ruleIndex].getName()).setMessage(ClonerApp.translate(R.string.ask_delete_rule))
				.setNegativeButton(ClonerApp.translate(R.string.msg_dont_delete), removeRuleClickListener)
				.setPositiveButton(ClonerApp.translate(R.string.msg_delete), removeRuleClickListener).show();
	}

	private void askUserToRemoveRelatedClones(int ruleIndex, String question, EventRemover.OnDoneListener onDoneListener) {
		Settings settings = ClonerApp.getSettings();
		if (!settings.isClonerEnabled()) {
			Rule rule = mRules[ruleIndex];
			if (!rule.isExecuting()) {
				CloneRemover remover = new CloneRemover();
				try {
					remover.init(rule, question);
					remover.setOnDoneListener(onDoneListener);
					remover.execute(this);
				} catch (Exception e) {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle(ClonerApp.translate(R.string.error_deleting_events))
							.setMessage(e.getClass().getName() + ": " + e.getMessage())
							.setPositiveButton(ClonerApp.translate(R.string.ok), null).setCancelable(true).show();
				}
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(ClonerApp.translate(R.string.msg_rule_still_active))
						.setMessage(ClonerApp.translate(R.string.msg_rule_still_active_info))
						.setPositiveButton(ClonerApp.translate(R.string.ok), null).setCancelable(true).show();
			}
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(ClonerApp.translate(R.string.msg_cloner_still_enabled))
					.setMessage(ClonerApp.translate(R.string.msg_cloner_still_enabled_info))
					.setPositiveButton(ClonerApp.translate(R.string.ok), null).setCancelable(true).show();
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		// If there is only a dummy menu item, exit here
		if (mRules.length == 0) {
			return;
		}

		final ListView.AdapterContextMenuInfo info = (ListView.AdapterContextMenuInfo) menuInfo;
		menu.setHeaderTitle(mRules[info.position].getName());

		MenuItem mi = menu.add(ClonerApp.translate(R.string.rules_menu_clone_this_rule));
		mi.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				ClonerApp.getSettings().executeRule(info.position);
				return true;
			}
		});
		mi.setEnabled(ClonerApp.getSettings().isClonerEnabled());

		final EventRemover.OnDoneListener listener = new EventRemover.OnDoneListener() {
			@Override
			public void onDone(boolean removed, boolean success, long removeCount, String errorMessage) {
				AlertDialog.Builder builder = new AlertDialog.Builder(RulesActivity.this);
				if (removed) {
					if (success) {
						builder.setTitle(
								ClonerApp.translate(R.string.msg_clones_deleted, new String[] { "" + removeCount }))
								.setMessage(ClonerApp.translate(R.string.msg_clones_deleted_info))
								.setPositiveButton(ClonerApp.translate(R.string.ok), null).setCancelable(true).show();
					} else {
						builder.setTitle(ClonerApp.translate(R.string.error_deleting_clones)).setMessage(errorMessage)
								.setPositiveButton(ClonerApp.translate(R.string.ok), null).setCancelable(true).show();
					}
				}
			}
		};

		mi = menu.add(ClonerApp.translate(R.string.rules_menu_delete_related_clones));
		mi.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				askUserToRemoveRelatedClones(info.position, ClonerApp.translate(R.string.ask_delete_related_clones),
						listener);
				return true;
			}
		});

		mi = menu.add(ClonerApp.translate(R.string.rules_menu_delete_rule));
		mi.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				askUserToRemoveRule(info.position);
				return true;
			}
		});

		mi = menu.add(ClonerApp.translate(R.string.rules_menu_rule_log));
		mi.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem arg0) {
				onRuleInfoClick(info.position);
				return true;
			}
		});
	}

	@Override
	public void onBackPressed() {
		this.finish();
		super.onBackPressed();
	}

	private void addNewRule() {
		if (ClonerVersion.setNumRules(mRules.length + 1) > mRules.length) {
			Intent i = new Intent(RulesActivity.this, RuleActivity.class);
			i.putExtra("rulenr", -1);
			startActivityForResult(i, 2);
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(RulesActivity.this);
			builder.setTitle(ClonerApp.translate(R.string.msg_free_version_limitation))
					.setMessage(
							ClonerApp.translate(R.string.msg_free_version_limitation_info, new String[] { ""
									+ ClonerVersion.setNumRules(999999999) }))
					.setPositiveButton(ClonerApp.translate(R.string.ok), null).setCancelable(true).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem mi = menu.add(ClonerApp.translate(R.string.rules_menu_add_new_rule));
		mi.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				addNewRule();
				return true;
			}
		});

		mi = menu.add(ClonerApp.translate(R.string.rules_menu_clone_now));
		mi.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				ClonerApp.resync("Clone now");
				return true;
			}
		});
		mi.setEnabled(ClonerApp.getSettings().isClonerEnabled());

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go back
			this.onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void updateListView() {
		Settings settings = ClonerApp.getSettings();
		mRules = new Rule[settings.getNumberOfRules()];
		for (int index = 0; index < settings.getNumberOfRules(); index++) {
			mRules[index] = settings.getRule(index);
		}

		RuleAdapter adp = new RuleAdapter(this, mRules);
		setListAdapter(adp);
		mList = (TouchInterceptor) getListView();
		mList.setDropListener(mDropListener);
		registerForContextMenu(mList);
	}

	private void onRuleStatusChange() {
		final RuleAdapter ra = (RuleAdapter) mList.getAdapter();
		ra.notifyDataSetChanged();
	}

	private void saveRules(Rule updatedRule) {
		if (updatedRule != null) {
			updatedRule.markDirty();
		}
		Settings settings = ClonerApp.getSettings();
		settings.setRules(mRules, false);
	}
}
