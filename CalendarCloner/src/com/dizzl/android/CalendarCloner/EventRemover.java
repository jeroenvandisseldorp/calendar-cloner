package com.dizzl.android.CalendarCloner;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;

public class EventRemover {
	private EventsTable mEventsTable = new EventsTable(ClonerApp.getDb(false));

	private CalendarLoader.CalendarInfo mCalendar = null;
	private String mSelection;
	private ArrayList<String> mSelectionArgs;

	private Context mContext = null;
	private String mQuestion = "";
	private String mTitle = "";
	private OnDoneListener mOnDoneListener = null;

	private ClonerLog mLog = new LogLogcat("EventRemover", ClonerLog.TYPE_EXTENDED, ClonerLog.LOG_WARNING);

	public static abstract class OnDoneListener {
		public abstract void onDone(boolean removed, boolean success, long removeCount, String errorMessage);
	}

	public void init(String calRef, String selection, ArrayList<String> selectionArgs, String title, String question) {
		mCalendar = CalendarLoader.getCalendarByRef(calRef);
		mSelection = selection;
		mSelectionArgs = selectionArgs;
		mTitle = title;
		mQuestion = question;
	}

	public void setOnDoneListener(OnDoneListener listener) {
		mOnDoneListener = listener;
	}

	private void done(boolean removed, boolean success, long count, String errorMessage) {
		if (mOnDoneListener != null) {
			mOnDoneListener.onDone(removed, success, count, errorMessage);
		}
	}

	private void removeEvents() {
		final ProgressDialog pd = ProgressDialog.show(mContext, "",
				ClonerApp.translate(R.string.msg_deleting_events, new String[] { mTitle }));

		new AsyncTask<Void, Void, String>() {
			int mEventCount = 0;

			class QueryParams {
				String selection = mSelection;
				ArrayList<String> args = mSelectionArgs;
			}

			private QueryParams getQueryParams() {
				QueryParams result = new QueryParams();
				if (mCalendar.getCalendar() != null) {
					if (!mSelection.contentEquals("")) {
						result.selection = "(" + mEventsTable.CALENDAR_ID.getName() + "=? AND (" + result.selection
								+ "))";
						result.args.add(0, "" + mCalendar.getCalendar().getId());
					} else {
						result.selection = "(" + mEventsTable.CALENDAR_ID.getName() + "=?)";
						result.args = new ArrayList<String>();
						result.args.add("" + mCalendar.getCalendar().getId());
					}
				}
				return result;
			}

			private void doRemoveEvents() {
				QueryParams query = this.getQueryParams();
				List<Long> ids = new LinkedList<Long>();
				Cursor cur = mEventsTable.query(query.selection, query.args.toArray(new String[] {}), null);
				try {
					while (cur.moveToNext()) {
						Event event = new DbEvent(mEventsTable, new DbObject(cur));
						ids.add(event.getId());
					}
				} finally {
					cur.close();
				}
				for (Long id : ids) {
					// Delete the event
					mLog.debug("Deleting event " + id);
					mEventsTable.delete(id);
				}
				mEventCount = ids.size();
			}

			@Override
			protected String doInBackground(Void... arg0) {
				String response = "";
				try {
					// Remove events here
					doRemoveEvents();
				} catch (Exception e) {
					mLog.stacktrace(e);
					response = ClonerApp.translate(R.string.log_exception) + ": " + e.toString();
				}
				return response;
			}

			@Override
			protected void onPostExecute(String result) {
				try {
					pd.dismiss();
				} catch (Exception e) {
					// eat
				}
				done(true, result == "", mEventCount, result);
			}
		}.execute();
	}

	public void execute(Context context) {
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					// Yes button clicked
					removeEvents();
				} else {
					done(false, false, 0, "");
				}
			}
		};

		mContext = context;
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(mTitle).setMessage(mQuestion)
				.setNegativeButton(ClonerApp.translate(R.string.msg_dont_delete), listener)
				.setPositiveButton(ClonerApp.translate(R.string.msg_delete), listener).show();
	}
}
