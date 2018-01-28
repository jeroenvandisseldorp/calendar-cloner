package com.dizzl.android.CalendarClonerFree;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class TextViewActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.overridePendingTransition(R.anim.animation_enter_right, R.anim.animation_leave_left);
		setContentView(R.layout.textviewactivity_layout);
		this.getActionBar().setDisplayHomeAsUpEnabled(true);

		Bundle params = this.getIntent().getExtras();
		String title = params.getString("title");
		String text = params.getString("text");

		this.getActionBar().setTitle(title);
		TextView tv = (TextView) findViewById(R.id.tvTitle);
		tv.setText(title);
		tv = (TextView) findViewById(R.id.tvText);
		tv.setText(text);
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
