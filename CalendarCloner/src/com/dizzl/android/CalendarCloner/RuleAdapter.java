package com.dizzl.android.CalendarCloner;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RuleAdapter extends ArrayAdapter<Rule> {
	private static int mResourceId = R.layout.rulesrow_layout;

	private Bitmap mEmptyBitmap = null;
	private SparseArray<Bitmap> mBitmaps = new SparseArray<Bitmap>();
	private LayoutInflater mLi = null;
	private Rule[] mRules = null;
	private Resources mResources = null;

	public RuleAdapter(Context context, Rule[] rules) {
		super(context, mResourceId, rules);
		mLi = LayoutInflater.from(context);
		mResources = context.getResources();
		mRules = rules;
	}

	private Bitmap getBitmap(boolean executed, int executionStatus) {
		if (!executed) {
			if (mEmptyBitmap == null) {
				mEmptyBitmap = BitmapFactory.decodeResource(mResources, R.drawable.status_none);
			}
			return mEmptyBitmap;
		}
		Bitmap result = mBitmaps.get(executionStatus);
		if (result == null) {
			if (executionStatus == RuleExecutor.Result.STATUS_SUCCESS) {
				result = BitmapFactory.decodeResource(mResources, R.drawable.status_success);
			}
			if (executionStatus == RuleExecutor.Result.STATUS_NOT_COMPLETED) {
				result = BitmapFactory.decodeResource(mResources, R.drawable.status_needmoretime);
			}
			if (executionStatus == RuleExecutor.Result.STATUS_FAIL) {
				result = BitmapFactory.decodeResource(mResources, R.drawable.status_fail);
			}
			mBitmaps.put(executionStatus, result);
		}
		return result;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = mLi.inflate(mResourceId, parent, false);
		}

		TextView title = (TextView) view.findViewById(R.id.title);
		TextView subTitle = (TextView) view.findViewById(R.id.subtitle);
		ImageView icon = (ImageView) view.findViewById(R.id.ruleIcon);
		ProgressBar pb = (ProgressBar) view.findViewById(R.id.progressIcon);

		if (mRules.length > 0) {
			Rule rule = mRules[position];
			if (rule.isEnabled()) {
				title.setTextColor(Color.WHITE);
			} else {
				title.setTextColor(Color.GRAY);
			}
			if (rule.isReadOnly()) {
				SpannableString str = new SpannableString(rule.getName());
				str.setSpan(new UnderlineSpan(), 0, str.length(), 0);
				str.setSpan(new StyleSpan(Typeface.ITALIC), 0, str.length(), 0);
				title.setText(str);
			} else {
				title.setText(rule.getName());
			}
			if (rule.isExecuting()) {
				icon.setVisibility(View.GONE);
				pb.setVisibility(View.VISIBLE);
			} else {
				icon.setImageBitmap(getBitmap(rule.hasExecuted(), rule.getStatus()));
				pb.setVisibility(View.GONE);
				icon.setVisibility(View.VISIBLE);
			}
			subTitle.setText(rule.getSummary());
		} else {
			title.setText(ClonerApp.translate(R.string.rules_add_new_rule));
			title.setTextColor(Color.WHITE);
			subTitle.setText(ClonerApp.translate(R.string.rules_add_new_rule_info));
			icon.setImageBitmap(getBitmap(false, RuleExecutor.Result.STATUS_FAIL));
			icon.setVisibility(View.VISIBLE);
			pb.setVisibility(View.GONE);
		}
		return view;
	}

	@Override
	public int getCount() {
		if (mRules != null) {
			if (mRules.length > 0) {
				return mRules.length;
			}
		}
		return 1;
	}

	@Override
	public Rule getItem(int idx) {
		return (mRules != null) ? mRules[idx] : null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public int getItemViewType(int pos) {
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}
}
