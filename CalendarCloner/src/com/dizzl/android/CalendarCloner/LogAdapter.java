package com.dizzl.android.CalendarCloner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LogAdapter extends BaseExpandableListAdapter {
	private static int mLogLineResourceId = R.layout.logline_layout;

	private LayoutInflater mLi = null;
	private LogLine[] mSummaries = null;
	private LogLines[] mLogLines = null;
	private Bitmap mBitmapContracted;
	private Bitmap mBitmapExpanded;
	private Bitmap mBitmapNone;

	public LogAdapter(Context context, LogLine[] summaries, LogLines[] logLines, int logLevel) {
		super();
		mLi = LayoutInflater.from(context);

		// Count the number of log entries with level >= logLevel
		int count = 0;
		for (int i = 0; i < summaries.length; i++) {
			if (summaries[i].getLevel() >= logLevel || (logLines[i] != null && logLines[i].getMaxLevel() >= logLevel)) {
				count++;
			}
		}

		// Copy the summaries and lines into our local arrays
		mSummaries = new LogLine[count];
		mLogLines = new LogLines[count];
		for (int i = summaries.length - 1; i >= 0; i--) {
			if (summaries[i].getLevel() >= logLevel || (logLines[i] != null && logLines[i].getMaxLevel() >= logLevel)) {
				mSummaries[--count] = summaries[i];
				mLogLines[count] = logLines[i];
			}
		}

		mBitmapContracted = BitmapFactory.decodeResource(context.getResources(), R.drawable.tree_contracted_16);
		mBitmapExpanded = BitmapFactory.decodeResource(context.getResources(), R.drawable.tree_expanded_16);
		mBitmapNone = BitmapFactory.decodeResource(context.getResources(), R.drawable.tree_none_16);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mLogLines[groupPosition].getLines().get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if (mLogLines[groupPosition] != null) {
			return mLogLines[groupPosition].getLines().size();
		}
		return 0;
	}

	public View getView(LogLine logLine, boolean isGroup, boolean groupIsExpanded, boolean groupHasChildren,
			View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = mLi.inflate(mLogLineResourceId, parent, false);
		}

		// Determine the text color
		int textColor = Color.WHITE;
		switch (logLine.getLevel()) {
		case ClonerLog.LOG_INFO:
			textColor = Color.LTGRAY;
			break;
		case ClonerLog.LOG_WARNING:
			textColor = Color.YELLOW;
			break;
		case ClonerLog.LOG_UPDATE:
			textColor = Color.GREEN;
			break;
		case ClonerLog.LOG_ERROR:
			textColor = Color.RED;
			break;
		default:
			textColor = Color.WHITE;
		}

		// Set the logPrefix
		TextView logPrefix = (TextView) view.findViewById(R.id.logPrefix2);
		if (logLine.getLogPrefix() != null) {
			logPrefix.setVisibility(View.VISIBLE);
			logPrefix.setText(logLine.getLogPrefix());
			logPrefix.setTextColor(textColor);
		} else {
			logPrefix.setVisibility(View.GONE);
		}

		// Set the icon
		ImageView icon = (ImageView) view.findViewById(R.id.logIcon);
		if (isGroup) {
			icon.setVisibility(View.VISIBLE);
			icon.setImageBitmap(groupHasChildren ? (groupIsExpanded ? mBitmapExpanded : mBitmapContracted)
					: mBitmapNone);
		} else {
			icon.setVisibility(View.GONE);
		}

		// Set the text columns
		TextView column0 = (TextView) view.findViewById(R.id.column0);
		TextView column1 = (TextView) view.findViewById(R.id.column1);
		TextView column2 = (TextView) view.findViewById(R.id.column2);
		switch (logLine.getColumnCount()) {
		case 1:
			column0.setVisibility(View.GONE);
			column1.setVisibility(View.VISIBLE);
			column2.setVisibility(View.GONE);
			column1.setText(logLine.getColumn(0));
			column1.setTextColor(textColor);
			column1.setWidth((int) (parent.getWidth() - column1.getX()));
			break;
		case 2:
			column0.setVisibility(View.VISIBLE);
			column1.setVisibility(View.VISIBLE);
			column2.setVisibility(View.GONE);
			column0.setText(logLine.getColumn(0));
			column0.setTextColor(textColor);
			column1.setText(logLine.getColumn(1));
			column1.setTextColor(textColor);
			break;
		case 3:
			column0.setVisibility(View.VISIBLE);
			column1.setVisibility(View.VISIBLE);
			column2.setVisibility(View.VISIBLE);
			column0.setText(logLine.getColumn(0));
			column0.setTextColor(textColor);
			column1.setText(logLine.getColumn(1));
			column1.setTextColor(textColor);
			column2.setText(logLine.getColumn(2));
			column2.setTextColor(textColor);
			column1.setWidth((int) (parent.getWidth() - column1.getX()) / 2);
			column2.setWidth((int) (parent.getWidth() - column1.getX()) / 2);
			break;
		}
		return view;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
			ViewGroup parent) {
		return this.getView(mLogLines[groupPosition].getLines().get(childPosition), false, false, false, convertView,
				parent);
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mSummaries[groupPosition];
	}

	@Override
	public int getGroupCount() {
		return mSummaries.length;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		return this.getView(mSummaries[groupPosition], true, isExpanded, mLogLines[groupPosition] != null
				&& !mLogLines[groupPosition].empty(), convertView, parent);
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}
}
