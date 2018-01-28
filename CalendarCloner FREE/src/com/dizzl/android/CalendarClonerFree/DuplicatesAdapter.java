package com.dizzl.android.CalendarClonerFree;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DuplicatesAdapter extends ArrayAdapter<DuplicatesEntry> {
	LayoutInflater mLi = null;
	DuplicatesEntry[] mDuplicates = null;
	Resources mResources = null;
	int mResourceId;
	int mMaxDuplicates = 0;
	SparseArray<Bitmap> mBitmaps = new SparseArray<Bitmap>();
	private final static int MAX_ROWS = 32;

	public DuplicatesAdapter(Context context, int resourceId, DuplicatesEntry[] duplicates) {
		super(context, resourceId, duplicates != null ? duplicates : new DuplicatesEntry[0]);
		mLi = LayoutInflater.from(context);
		mResources = context.getResources();
		mResourceId = resourceId;
		this.setDuplicates(duplicates);
	}

	private void setDuplicates(DuplicatesEntry[] duplicates) {
		if (duplicates == null) {
			duplicates = new DuplicatesEntry[0];
		}
		mDuplicates = duplicates;
		mMaxDuplicates = 0;
		for (int index = 0; index < mDuplicates.length; index++) {
			if (mMaxDuplicates <= mDuplicates[index].events.size()) {
				mMaxDuplicates = mDuplicates[index].events.size();
			}
		}
	}

	public DuplicatesEntry[] getDuplicates() {
		return mDuplicates;
	}

	public void swapData(DuplicatesEntry[] data) {
		this.setDuplicates(data);
		this.notifyDataSetChanged();
	}

	private int getRowCount(int position) {
		int rowCount = mDuplicates[position].events.size();
		if (rowCount > MAX_ROWS) {
			rowCount = MAX_ROWS;
		}
		return rowCount;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = mLi.inflate(mResourceId, parent, false);
		}

		LinearLayout table = (LinearLayout) view.findViewById(R.id.table);
		int rowCount = this.getRowCount(position);
		while (table.getChildCount() < rowCount) {
			mLi.inflate(R.layout.eventrow_layout, table, true);
		}
		while (table.getChildCount() > rowCount) {
			table.removeViewAt(table.getChildCount() - 1);
		}

		TextView topline = (TextView) view.findViewById(R.id.topline);
		topline.setText("ID: " + mDuplicates[position].events.get(0).uniqueId);
		for (int index = 0; index < rowCount; index++) {
			View subview = table.getChildAt(index);
			DuplicatesEntry.Entry entry = mDuplicates[position].events.get(index);
			TextView title = (TextView) subview.findViewById(R.id.title);
			TextView subline1 = (TextView) subview.findViewById(R.id.subline1);
			TextView subline2 = (TextView) subview.findViewById(R.id.subline2);

			String calendarName = CalendarLoader.getCalendarNameOrErrorMessage(entry.calendarId);
			title.setText(entry.title);
			subline1.setText(entry.eventId + "  " + calendarName + "  " + Utilities.dateTimeToString(entry.startTime));
			if (entry.location != null) {
				subline2.setText(entry.location);
			} else {
				subline2.setText("");
			}

			ImageView recurringIcon = (ImageView) subview.findViewById(R.id.eventRecurring);
			ImageView exceptionIcon = (ImageView) subview.findViewById(R.id.eventException);
			ImageView singleIcon = (ImageView) subview.findViewById(R.id.eventSingle);
			ImageView trashcanIcon = (ImageView) subview.findViewById(R.id.trashcan);
			recurringIcon.setVisibility(entry.isRecurring ? View.VISIBLE : View.GONE);
			exceptionIcon.setVisibility(entry.isException ? View.VISIBLE : View.GONE);
			singleIcon.setVisibility(!entry.isRecurring && !entry.isException ? View.VISIBLE : View.GONE);
			trashcanIcon.setVisibility(entry.isDeleted ? View.VISIBLE : View.GONE);
		}
		return view;
	}

	@Override
	public int getCount() {
		if (mDuplicates != null) {
			if (mDuplicates.length > 0) {
				return mDuplicates.length;
			}
		}
		return 0;
	}

	@Override
	public DuplicatesEntry getItem(int idx) {
		return (mDuplicates != null) ? mDuplicates[idx] : null;
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
		return this.getRowCount(pos) - 2;
	}

	@Override
	public int getViewTypeCount() {
		return MAX_ROWS;
	}
}
