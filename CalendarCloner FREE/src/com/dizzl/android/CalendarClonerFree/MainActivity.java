package com.dizzl.android.CalendarClonerFree;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

public class MainActivity extends PreferenceActivity {
	static final String CC_SETTINGS_FILENAME = ClonerVersion.thisVersionName() + " Settings.txt";

	public static class GeneralPrefsFragment extends PreferenceFragment {
		private long mClonerStateOrResyncTime = ClonerStateRunnable.CLONER_NOT_RUNNING;
		private final Handler mHandler = new Handler();
		private Preference mEnabled = null;
		private Timer mTimer = null;
		private final ClonerStateRunnable mTimerSignalRunnable = new ClonerStateRunnable() {
			@Override
			public void run(long clonerStateOrResyncTime) {
				mClonerStateOrResyncTime = clonerStateOrResyncTime;
			}
		};

		private void fillCheckBox(String name, boolean checked, String checkedSummary, String uncheckedSummary,
				boolean enabled) {
			CheckBoxPreference cbp = (CheckBoxPreference) findPreference(name);
			cbp.setChecked(checked);
			cbp.setSummary(checked ? checkedSummary : uncheckedSummary);
			cbp.setEnabled(enabled);
		}

		private void setSummary(Preference pref, String value, String[] keys, String[] names) {
			String summary = null;
			for (int index = 0; index < keys.length; index++) {
				if (keys[index].contentEquals(value)) {
					summary = names[index];
				}
			}
			pref.setSummary(summary != null ? summary : "");
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.main_preferences);

			final Settings settings = ClonerApp.getSettings();

			SwitchPreference sp = (SwitchPreference) findPreference("clonerEnabled");
			sp.setChecked(settings.isClonerEnabled());
			sp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference pref, Object newValue) {
					settings.setClonerEnabled((Boolean) newValue);
					return true;
				}

			});
			mEnabled = sp;

			final ListPreference ctw = (ListPreference) findPreference("clonerTimeWait");
			final String[] ctwKeys = new String[] { "60", "120", "300", "600", "900", "1800", "3600", "7200", "14400",
					"28800", "86400" };
			final String[] ctwNames = new String[] { ClonerApp.translate(R.string.msg_n_minute, new String[] { "1" }),
					ClonerApp.translate(R.string.msg_n_minutes, new String[] { "2" }),
					ClonerApp.translate(R.string.msg_n_minutes, new String[] { "5" }),
					ClonerApp.translate(R.string.msg_n_minutes, new String[] { "10" }),
					ClonerApp.translate(R.string.msg_n_minutes, new String[] { "15" }),
					ClonerApp.translate(R.string.msg_n_minutes, new String[] { "30" }),
					ClonerApp.translate(R.string.msg_n_hour, new String[] { "1" }),
					ClonerApp.translate(R.string.msg_n_hours, new String[] { "2" }),
					ClonerApp.translate(R.string.msg_n_hours, new String[] { "4" }),
					ClonerApp.translate(R.string.msg_n_hours, new String[] { "8" }),
					ClonerApp.translate(R.string.msg_n_hours, new String[] { "24" }) };
			ctw.setEntries(ctwNames);
			ctw.setEntryValues(ctwKeys);
			ctw.setValue(((Integer) settings.getClonerTimeWait()).toString());
			this.setSummary(ctw, "" + settings.getClonerTimeWait(), ctwKeys, ctwNames);

			ctw.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference pref, Object newValue) {
					settings.setClonerTimeWait(Integer.parseInt((String) newValue));
					setSummary(ctw, "" + settings.getClonerTimeWait(), ctwKeys, ctwNames);
					return true;
				}

			});

			final ListPreference elp = (ListPreference) findPreference("eventLimit");
			final String[] elpKeys = new String[Limits.EVENT_LIMITS.length];
			final String[] elpNames = new String[Limits.EVENT_LIMITS.length];
			for (int index = 0; index < Limits.EVENT_LIMITS.length; index++) {
				elpKeys[index] = "" + Limits.EVENT_LIMITS[index];
				if (Limits.EVENT_LIMITS[index] != 0) {
					elpNames[index] = ClonerApp.translate(R.string.msg_n_max_updates_per_hour, new String[] { ""
							+ Limits.EVENT_LIMITS[index] });
				} else {
					elpNames[index] = ClonerApp.translate(R.string.msg_unlimited);
				}
			}
			elp.setEntries(elpNames);
			elp.setEntryValues(elpKeys);
			elp.setValue(((Integer) Limits.getTypeLimit(Limits.TYPE_EVENT)).toString());
			this.setSummary(elp, "" + Limits.getTypeLimit(Limits.TYPE_EVENT), elpKeys, elpNames);

			elp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference pref, Object newValue) {
					if (settings.setTypeLimit(Limits.TYPE_EVENT, Integer.parseInt((String) newValue))) {
						setSummary(elp, "" + Limits.getTypeLimit(Limits.TYPE_EVENT), elpKeys, elpNames);
						return true;
					} else {
						ClonerVersion.msgPaidVersionOnly();
						return false;
					}
				}

			});

			final ListPreference alp = (ListPreference) findPreference("attendeeLimit");
			final String[] alpKeys = new String[Limits.ATTENDEE_LIMITS.length];
			final String[] alpNames = new String[Limits.ATTENDEE_LIMITS.length];
			for (int index = 0; index < Limits.ATTENDEE_LIMITS.length; index++) {
				alpKeys[index] = "" + Limits.ATTENDEE_LIMITS[index];
				if (Limits.ATTENDEE_LIMITS[index] != 0) {
					alpNames[index] = ClonerApp.translate(R.string.msg_n_max_updates_per_hour, new String[] { ""
							+ Limits.ATTENDEE_LIMITS[index] });
				} else {
					alpNames[index] = ClonerApp.translate(R.string.msg_unlimited);
				}
			}
			alp.setEntries(alpNames);
			alp.setEntryValues(alpKeys);
			alp.setValue(((Integer) Limits.getTypeLimit(Limits.TYPE_ATTENDEE)).toString());
			this.setSummary(alp, "" + Limits.getTypeLimit(Limits.TYPE_ATTENDEE), alpKeys, alpNames);

			alp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference pref, Object newValue) {
					if (settings.setTypeLimit(Limits.TYPE_ATTENDEE, Integer.parseInt((String) newValue))) {
						setSummary(alp, "" + Limits.getTypeLimit(Limits.TYPE_ATTENDEE), alpKeys, alpNames);
						return true;
					} else {
						ClonerVersion.msgPaidVersionOnly();
						return false;
					}
				}

			});

			final ListPreference ltp = (ListPreference) findPreference("logType");
			final String[] ltpKeys = new String[] { "" + ClonerLog.TYPE_SUMMARY, "" + ClonerLog.TYPE_EXTENDED };
			final String[] ltpNames = new String[] { ClonerApp.translate(R.string.main_logtype_summary),
					ClonerApp.translate(R.string.main_logtype_extended) };
			ltp.setEntries(ltpNames);
			ltp.setEntryValues(ltpKeys);
			ltp.setValue("" + settings.getLogType());
			this.setSummary(ltp, "" + settings.getLogType(), ltpKeys, ltpNames);

			ltp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference pref, Object newValue) {
					settings.setLogType(Integer.parseInt((String) newValue));
					setSummary(ltp, "" + settings.getLogType(), ltpKeys, ltpNames);
					return true;
				}

			});

			final CheckBoxPreference llp = (CheckBoxPreference) findPreference("logToLogcat");
			this.fillCheckBox("logToLogcat", settings.getLogToLogcat(),
					ClonerApp.translate(R.string.main_log_logcat_on_summary),
					ClonerApp.translate(R.string.main_log_logcat_off_summary), true);
			llp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference pref, Object newValue) {
					settings.setLogToLogcat((Boolean) newValue);
					fillCheckBox("logToLogcat", settings.getLogToLogcat(),
							ClonerApp.translate(R.string.main_log_logcat_on_summary),
							ClonerApp.translate(R.string.main_log_logcat_off_summary), true);
					return true;
				}
			});

			final CheckBoxPreference lmp = (CheckBoxPreference) findPreference("logToMemory");
			this.fillCheckBox("logToMemory", settings.getLogToMemory(),
					ClonerApp.translate(R.string.main_log_memory_on_summary),
					ClonerApp.translate(R.string.main_log_memory_off_summary), true);
			lmp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference pref, Object newValue) {
					settings.setLogToMemory((Boolean) newValue);
					fillCheckBox("logToMemory", settings.getLogToMemory(),
							ClonerApp.translate(R.string.main_log_memory_on_summary),
							ClonerApp.translate(R.string.main_log_memory_off_summary), true);
					return true;
				}
			});

			final Preference rp = findPreference("rules");
			rp.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					Intent i = new Intent(rp.getContext(), RulesActivity.class);
					startActivity(i);
					return false;
				}

			});

			final Preference be = findPreference("browseEvents");
			if (be != null) {
				be.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Intent i = new Intent(be.getContext(), BrowseActivity.class);
						startActivity(i);
						return false;
					}
				});
			}

			final Preference de = findPreference("duplicateEvents");
			if (de != null) {
				de.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Settings settings = ClonerApp.getSettings();
						if (!settings.isClonerEnabled()) {
							Intent i = new Intent(de.getContext(), DuplicatesActivity.class);
							startActivity(i);
							return true;
						}
						showClonerStillEnabledMessage();
						return false;
					}
				});
			}

			final Preference ls = findPreference("loadSettingsFromFile");
			ls.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					DialogInterface.OnClickListener loadSettingsFromFileListener = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (which == DialogInterface.BUTTON_POSITIVE) {
								// Yes button clicked
								Settings settings = ClonerApp.getSettings();
								if (!settings.isClonerEnabled()) {
									SettingsMap map = SettingsMapStreamer.loadFromFile(CC_SETTINGS_FILENAME);
									// Always disable the cloner when loading
									// new settings from file
									if (map != null) {
										map.put("clonerEnabled", false);
										settings.loadfromMap(map);
										ClonerApp.toast(ClonerApp.translate(R.string.msg_settings_loaded_from_file,
												new String[] { CC_SETTINGS_FILENAME }));
									} else {
										ClonerApp.toast(ClonerApp.translate(
												R.string.error_could_not_load_settings_from_file,
												new String[] { CC_SETTINGS_FILENAME }));
									}
								} else {
									showClonerStillEnabledMessage();
								}
							}
						}
					};

					AlertDialog.Builder builder = new AlertDialog.Builder(GeneralPrefsFragment.this.getActivity());
					builder.setTitle("Load settings")
							.setMessage(
									ClonerApp.translate(R.string.ask_load_settings_from_file,
											new String[] { CC_SETTINGS_FILENAME }))
							.setNegativeButton(ClonerApp.translate(R.string.msg_dont_load_settings),
									loadSettingsFromFileListener)
							.setPositiveButton(ClonerApp.translate(R.string.msg_load_settings),
									loadSettingsFromFileListener).show();

					return true;
				}
			});

			final Preference ss = findPreference("saveSettingsToFile");
			ss.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					Settings settings = ClonerApp.getSettings();
					if (!settings.isClonerEnabled()) {
						SettingsMap map = new SettingsMap();
						settings.saveToMap(map);
						if (SettingsMapStreamer.saveToFile(CC_SETTINGS_FILENAME, map)) {
							ClonerApp.toast(ClonerApp.translate(R.string.msg_settings_saved_to_file,
									new String[] { CC_SETTINGS_FILENAME }));
						} else {
							ClonerApp.toast(ClonerApp.translate(R.string.error_could_not_save_settings_to_file,
									new String[] { CC_SETTINGS_FILENAME }));
						}
					} else {
						showClonerStillEnabledMessage();
					}
					return true;
				}
			});

			final Preference hp = findPreference("help");
			hp.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent i = new Intent(hp.getContext(), HelpActivity.class);
					startActivity(i);
					return true;
				}
			});

			final Preference ap = findPreference("about");
			ap.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent i = new Intent(ap.getContext(), AboutActivity.class);
					startActivity(i);
					return false;
				}
			});
		}

		private void showClonerStillEnabledMessage() {
			AlertDialog.Builder builder = new AlertDialog.Builder(GeneralPrefsFragment.this.getActivity());
			builder.setTitle(ClonerApp.translate(R.string.msg_cloner_still_enabled))
					.setMessage(ClonerApp.translate(R.string.msg_cloner_still_enabled_info))
					.setPositiveButton(ClonerApp.translate(R.string.ok), null).setCancelable(true).show();
		}

		@Override
		public void onStart() {
			super.onStart();
			// Register for resync timer signals
			ClonerApp.registerClonerStateRunnable(mTimerSignalRunnable, true);
			// Activate a timer to update the resync menu item
			mTimer = new Timer("Resync timer display", false);
			mTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							onUpdateResyncTimer();
						}
					});
				}
			}, 0, 1000);
		}

		@Override
		public void onStop() {
			mTimer.cancel();
			mTimer = null;
			ClonerApp.unregisterClonerStateRunnable(mTimerSignalRunnable);
			super.onStop();
		}

		private void onUpdateResyncTimer() {
			Settings settings = ClonerApp.getSettings();
			if (!settings.isClonerEnabled()) {
				mEnabled.setSummary(ClonerApp.translate(R.string.main_resync_disabled));
				return;
			}
			if (mClonerStateOrResyncTime == ClonerStateRunnable.CLONER_RUNNING) {
				mEnabled.setSummary(ClonerApp.translate(R.string.main_resync_running));
				return;
			}
			if (mClonerStateOrResyncTime == ClonerStateRunnable.CLONER_NOT_RUNNING) {
				mEnabled.setSummary(ClonerApp.translate(R.string.main_resync_not_running));
				return;
			}

			long timeRemaining = mClonerStateOrResyncTime - System.currentTimeMillis();
			if (timeRemaining < 0) {
				mEnabled.setSummary(ClonerApp.translate(R.string.main_resync_running_soon));
				return;
			}

			long hours = timeRemaining / (3600 * 1000);
			timeRemaining -= hours * (3600 * 1000);
			long minutes = timeRemaining / (60 * 1000);
			long seconds = (timeRemaining % (60 * 1000)) / 1000;
			String time = "";
			if (hours > 0) {
				time += "" + hours + ":";
			}
			if (minutes < 10 && hours > 0) {
				time += "0";
			}
			time += "" + minutes + ":";
			if (seconds < 10) {
				time += "0";
			}
			time += "" + seconds;
			mEnabled.setSummary(ClonerApp.translate(R.string.main_resync_in_x_time, new String[] { time }));
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction().replace(android.R.id.content, new GeneralPrefsFragment()).commit();
	}

	@Override
	public void onBuildHeaders(List<Header> target) {
		super.onBuildHeaders(target);
		// loadHeadersFromResource(R.xml.pref_headers, target);
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.overridePendingTransition(R.anim.animation_enter_left, R.anim.animation_leave_right);
	}
}
