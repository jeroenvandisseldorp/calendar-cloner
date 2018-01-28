package com.dizzl.android.CalendarCloner;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class Period {
	private DateTime mStart;
	private DateTime mEnd;

	public Period(DateTime start, DateTime end) {
		if (start.isBefore(end)) {
			mStart = start;
			mEnd = end;
		} else {
			mStart = end;
			mEnd = start;
		}
	}

	public Period(DateTime point) {
		mStart = point;
		mEnd = point;
	}

	@Override
	public String toString() {
		if (!this.isNull()) {
			return mStart + " - " + mEnd;
		}
		return "Null";
	}

	public DateTime getStart() {
		return mStart;
	}

	public DateTime getEnd() {
		return mEnd;
	}

	@Override
	public Period clone() {
		return new Period(mStart, mEnd);
	}

	public boolean isNull() {
		return mStart == mEnd;
	}

	public long distanceTo(DateTime time) {
		return this.distanceTo(new Period(time, time));
	}

	public long distanceTo(Period other) {
		if (other.mStart.isAfter(mEnd)) {
			return other.mStart.getMillis() - mEnd.getMillis();
		}
		if (other.mEnd.isBefore(mStart)) {
			return mStart.getMillis() - other.mEnd.getMillis();
		}
		return 0;
	}

	public Period getOverlap(Period other) {
		if (!other.mEnd.isBefore(mStart) && !mEnd.isBefore(other.mStart)) {
			DateTime start = mStart.isAfter(other.mStart) ? mStart : other.mStart;
			DateTime end = mEnd.isBefore(other.mEnd) ? mEnd : other.mEnd;
			Period overlap = new Period(start, end);
			return overlap;
		}
		return null;
	}

	public boolean startsAtOrAfter(DateTime time) {
		return !mStart.isBefore(time);
	}

	public boolean endsBefore(DateTime time) {
		return mEnd.isBefore(time);
	}

	public void merge(Period other) {
		if (this.isNull()) {
			mStart = other.mStart;
			mEnd = other.mEnd;
			return;
		}

		if (other.mStart.isBefore(mStart)) {
			mStart = other.mStart;
		}
		if (other.mEnd.isAfter(mEnd)) {
			mEnd = other.mEnd;
		}
	}

	public Period[] subtract(Period other) {
		// If the other period doesn't overlap, return ourselves
		if (!this.overlaps(other)) {
			Period result[] = new Period[1];
			result[0] = this.clone();
			return result;
		}

		// If the other period splits this period in two, return the two
		// halves
		if (other.startsAfterStartOf(this) && other.endsBeforeEndOf(this)) {
			Period result[] = new Period[2];
			result[0] = new Period(mStart, other.mStart);
			result[1] = new Period(other.mEnd, mEnd);
			return result;
		}

		// If the subtracted period overlaps this period in full, return an
		// empty array of periods
		if (!other.startsAfterStartOf(this) && !other.endsBeforeEndOf(this)) {
			Period result[] = new Period[0];
			return result;
		}

		// If the other period overlaps at the beginning, return a new period
		// that starts at the end of the subtracted period
		if (!other.startsAfterStartOf(this)) {
			Period result[] = new Period[1];
			result[0] = new Period(other.mEnd, mEnd);
			return result;
		}

		// The other period overlaps at the end, so return a new period that
		// ends at the start of the subtracted period
		Period result[] = new Period[1];
		result[0] = new Period(mStart, other.mStart);
		return result;
	}

	public void subtractAfter(DateTime time) {
		if (mEnd.isAfter(time)) {
			if (mStart.isBefore(time)) {
				mEnd = time;
			} else {
				mEnd = mStart;
			}
		}
	}

	public void subtractBefore(DateTime time) {
		if (mStart.isBefore(time)) {
			if (mEnd.isAfter(time)) {
				mStart = time;
			} else {
				mStart = mEnd;
			}
		}
	}

	public static Period now(DateTimeZone zone) {
		DateTime now = new DateTime(zone);
		return new Period(now, now);
	}

	public boolean overlaps(Period other) {
		if (mStart.isAfter(other.mEnd) || other.mStart.isAfter(mEnd)) {
			return false;
		}
		return true;
	}

	public boolean startsAfterEndOf(Period other) {
		// mStart >= other.mEnd
		return !other.mEnd.isAfter(mStart);
	}

	public boolean startsAfterStartOf(Period other) {
		// mStart > other.mStart
		return mStart.isAfter(other.mStart);
	}

	public boolean startsBeforeEndOf(Period other) {
		// mStart <= other.mEnd
		return !other.mEnd.isBefore(mStart);
	}

	public boolean startsBeforeStartOf(Period other) {
		// mStart < other.mStart
		return mStart.isBefore(other.mStart);
	}

	public boolean endsAfterEndOf(Period other) {
		// mEnd > other.mEnd
		return mEnd.isAfter(other.mEnd);
	}

	public boolean endsAfterStartOf(Period other) {
		// mEnd > other.mStart
		return mEnd.isAfter(other.mStart);
	}

	public boolean endsBeforeEndOf(Period other) {
		// mEnd < other.mEnd
		return mEnd.isBefore(other.mEnd);
	}

	public boolean endsBeforeStartOf(Period other) {
		// mEnd <= other.mStart
		return !other.mStart.isBefore(mEnd);
	}
}
