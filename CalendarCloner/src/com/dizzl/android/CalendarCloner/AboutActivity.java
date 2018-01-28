package com.dizzl.android.CalendarCloner;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutActivity extends Activity {
	private int mLongPressCounter = 0;
	private Button mPurchaseButton = null;
	private int mPurchaseCounter = 0;
	private final Handler mHandler = new Handler();
	private Timer mTimer = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.overridePendingTransition(R.anim.animation_enter_right, R.anim.animation_leave_left);
		setContentView(R.layout.aboutactivity_layout);
		this.getActionBar().setDisplayHomeAsUpEnabled(true);
		this.getActionBar().setTitle(R.string.about_activity_title);

		TextView versionView = (TextView) findViewById(R.id.aboutAppVersion);
		String version = ClonerApp.getVersion();
		if (!version.contentEquals("")) {
			versionView.setText(ClonerApp.translate(R.string.app_version_x, new String[] { ClonerApp.getVersion() }));
		} else {
			versionView.setText("");
		}
		// Look up the purchase button
		mPurchaseButton = (Button) findViewById(R.id.purchaseButton);
		if (ClonerVersion.IS_FREE_VERSION()) {
			mPurchaseButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse("market://details?id=" + ClonerVersion.PAID_VERSION_PACKAGENAME()));
					startActivity(intent);
				}
			});
		} else {
			mPurchaseButton.setVisibility(View.GONE);
			mPurchaseButton = null;
		}

		// Set the correct text for the view
		String text = ClonerApp.translate(R.string.about_description_text) + "\n";
		if (ClonerVersion.IS_FREE_VERSION()) {
			text += ClonerApp.translate(R.string.about_description_free) + "\n";
		}
		text += ClonerApp.translate(R.string.about_description_end);
		TextView tv = (TextView) findViewById(R.id.aboutTextView);
		tv.setText(text);

		// Debug easter egg: listen for 4 long presses to activate debug mode
		ImageView logo = (ImageView) findViewById(R.id.cc_logo);
		logo.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				boolean toggle = false;
				if (!ClonerApp.isDebugMode()) {
					mLongPressCounter = (mLongPressCounter + 1) % 4;
					if (mLongPressCounter == 0) {
						toggle = true;
					}
				} else {
					toggle = true;
				}
				if (toggle) {
					ClonerApp.toggleDebugMode();
					ClonerApp.toast(ClonerApp.translate(R.string.about_debug_mode, new String[] { (ClonerApp
							.isDebugMode() ? ClonerApp.translate(R.string.on) : ClonerApp.translate(R.string.off)) }));
				}
				return true;
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mPurchaseButton != null) {
			// Activate a timer to update the resync menu item
			mTimer = new Timer("About purchase timer", false);
			mTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							onUpdatePurchaseButton();
						}
					});
				}
			}, 0, 2000);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
	}

	private void onUpdatePurchaseButton() {
		if (mPurchaseCounter == 0) {
			mPurchaseButton.setText(ClonerApp.translate(R.string.about_purchase1,
					new String[] { ClonerVersion.paidVersionName() }));
		} else {
			mPurchaseButton.setText(ClonerApp.translate(R.string.about_purchase2));
		}
		mPurchaseCounter = (mPurchaseCounter + 1) % 2;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go back
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
