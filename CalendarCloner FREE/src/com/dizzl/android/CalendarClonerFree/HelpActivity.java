package com.dizzl.android.CalendarClonerFree;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Locale;

public class HelpActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.overridePendingTransition(R.anim.animation_enter_right, R.anim.animation_leave_left);
		setContentView(R.layout.helpactivity_layout);
		this.getActionBar().setDisplayHomeAsUpEnabled(true);

		WebView view = (WebView) findViewById(R.id.help_view);
		view.getSettings().setLoadWithOverviewMode(true);
		view.getSettings().setUseWideViewPort(true);
		view.getSettings().setBuiltInZoomControls(true);
		view.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});

		String lang = Locale.getDefault().getLanguage();
		view.loadUrl("http://dizzl.com/apps/calendarcloner/index.php?lang=" + lang);
	}

	@Override
	public void onBackPressed() {
		this.finish();
		super.onBackPressed();
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
}
