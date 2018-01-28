package com.dizzl.android.CalendarCloner;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

public class Periods {
	// List of incrementally sorted non-overlapping periods
	private final ArrayList<Period> mPeriods = new ArrayList<Period>();

	public Periods() {
	}

	List<Period> getPeriods() {
		return mPeriods;
	}

	public void merge(Period period) {
		if (mPeriods.size() > 0) {
			int firstOverlap = -1;
			int lastOverlap = -1;
			int firstStartAfter = mPeriods.size();

			for (int index = 0; index < mPeriods.size(); index++) {
				Period p = mPeriods.get(index);
				if (index < firstStartAfter && p.startsAfterEndOf(period)) {
					firstStartAfter = index;
					break;
				}
				if (p.overlaps(period)) {
					if (firstOverlap < 0) {
						firstOverlap = index;
					}
					lastOverlap = index;
				} else {
					if (firstOverlap >= 0) {
						// We don't need to traverse the rest of the list
						break;
					}
				}
			}

			// Check to see if we found periods overlapping the new period
			if (firstOverlap >= 0) {
				// New period overlaps from firstOverlap to lastOverlap, so
				// we replace those periods here with one new period that
				// merges them all
				Period newPeriod = mPeriods.get(firstOverlap);
				newPeriod.merge(mPeriods.get(lastOverlap));
				newPeriod.merge(period);
				// Set the new period at index "firstOverlap"
				mPeriods.set(firstOverlap, newPeriod);
				// Remove all following periods up to lastOverlap
				for (int index = firstOverlap + 1; index <= lastOverlap; index++) {
					mPeriods.remove(firstOverlap + 1);
				}
			} else {
				// New period does not overlap, so insert as new period before
				// the first period that starts after it
				mPeriods.add(firstStartAfter, period.clone());
			}
		} else {
			// Add first period
			mPeriods.add(period);
		}

		// Merge adjacent periods into one
		int index = 0;
		while (index < mPeriods.size() - 1) {
			if (mPeriods.get(index).getEnd() == mPeriods.get(index + 1).getStart()) {
				mPeriods.get(index).merge(mPeriods.get(index + 1));
				mPeriods.remove(index + 1);
			} else {
				index++;
			}
		}
	}

	public void subtract(Period period) {
		int index = 0;
		boolean processedOverlap = false;
		while (index < mPeriods.size()) {
			Period p = mPeriods.get(index);
			if (p.overlaps(period)) {
				Period newPeriods[] = p.subtract(period);
				if (newPeriods.length == 0) {
					mPeriods.remove(index);
					index--;
				}
				if (newPeriods.length == 1) {
					mPeriods.set(index, newPeriods[0]);
				}
				if (newPeriods.length == 2) {
					mPeriods.set(index, newPeriods[0]);
					mPeriods.add(index + 1, newPeriods[1]);
					index++;
				}
				processedOverlap = true;
			} else {
				if (processedOverlap) {
					return;
				}
			}
			index++;
		}
	}

	public void subtractBefore(DateTime time) {
		for (Period p : mPeriods) {
			p.subtractBefore(time);
		}
	}

	public void subtractAfter(DateTime time) {
		for (Period p : mPeriods) {
			p.subtractAfter(time);
		}
	}
}
