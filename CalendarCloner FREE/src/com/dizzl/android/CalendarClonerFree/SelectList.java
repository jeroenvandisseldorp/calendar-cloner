package com.dizzl.android.CalendarClonerFree;

public class SelectList {
	private int mCount = 0;
	private int[] mKeys = null;
	private String[] mNames = null;
	private boolean[] mSelected = null;

	public SelectList() {
		this.init();
		for (int index = 0; index < this.getCount(); index++) {
			this.selectByIndex(index, false);
		}
	}

	public SelectList(boolean allSelected) {
		this.init();
		this.selectAll(allSelected);
	}

	public SelectList(String encoded) {
		this.init();
		this.decode(encoded);
	}

	protected void init() {
		this.init(new int[] {}, new String[] {});
	}

	protected void init(int[] keys, String[] names) {
		mCount = keys.length;
		mKeys = keys;
		mNames = names;
		mSelected = new boolean[mCount];

		for (int index = 0; index < mCount; index++) {
			mSelected[index] = false;
		}
	}

	public void sort() {
		for (int i = 0; i < mKeys.length - 1; i++) {
			for (int j = i + 1; j < mKeys.length; j++) {
				if (mNames[i].compareTo(mNames[j]) > 0) {
					int key = mKeys[i];
					String name = mNames[i];
					boolean selected = mSelected[i];
					mKeys[i] = mKeys[j];
					mNames[i] = mNames[j];
					mSelected[i] = mSelected[j];
					mKeys[j] = key;
					mNames[j] = name;
					mSelected[j] = selected;
				}
			}
		}
	}

	public int getCount() {
		return mCount;
	}

	public int getKey(int index) {
		return mKeys[index];
	}

	public String getKeyName(int key) {
		for (int index = 0; index < mCount; index++) {
			if (mKeys[index] == key) {
				return mNames[index];
			}
		}
		return "" + key;
	}

	public String getKeyNameAndValue(int key) {
		return getKeyName(key) + " (" + key + ")";
	}

	public String getName(int index) {
		return mNames[index];
	}

	public void selectByKey(int key, boolean selected) {
		for (int index = 0; index < mCount; index++) {
			if (mKeys[index] == key) {
				mSelected[index] = selected;
			}
		}
	}

	public void selectByIndex(int index, boolean selected) {
		mSelected[index] = selected;
	}

	public void selectAll(boolean allSelected) {
		for (int index = 0; index < mCount; index++) {
			mSelected[index] = allSelected;
		}
	}

	public boolean isIndexSelected(int index) {
		return mSelected[index];
	}

	public boolean isKeySelected(int key) {
		for (int index = 0; index < mCount; index++) {
			if (mKeys[index] == key) {
				return mSelected[index];
			}
		}
		return false;
	}

	public int indexOf(int key) {
		for (int index = 0; index < mCount; index++) {
			if (mKeys[index] == key) {
				return index;
			}
		}
		return -1;
	}

	protected void decode(String encoded) {
		if (encoded == null) {
			encoded = "";
		}

		if (encoded.length() == mCount) {
			for (int index = 0; index < mCount; index++) {
				mSelected[index] = encoded.substring(index, index + 1).contentEquals("1");
			}
		} else {
			for (int index = 0; index < mCount; index++) {
				mSelected[index] = false;
			}

			String[] parts = encoded.split(",");
			for (int partindex = 0; partindex < parts.length; partindex++) {
				String[] elements = parts[partindex].split("=");
				if (elements.length == 2) {
					for (int index = 0; index < mCount; index++) {
						if (elements[0].contentEquals("" + mKeys[index])) {
							mSelected[index] = (elements[1].contentEquals("1"));
						}
					}
				}
			}
		}
	}

	public String toString() {
		String result = "";
		for (int index = 0; index < mCount; index++) {
			if (!result.contentEquals("")) {
				result += ",";
			}
			result += mKeys[index] + "=" + (mSelected[index] ? "1" : "0");
		}
		return result;
	}

	public int getSelectedCount() {
		int count = 0;
		for (int index = 0; index < mCount; index++) {
			if (mSelected[index]) {
				count++;
			}
		}
		return count;
	}

	public int[] getSelectedKeys() {
		int[] result = new int[this.getSelectedCount()];
		int count = 0;
		for (int index = 0; index < mCount; index++) {
			if (mSelected[index]) {
				result[count++] = mKeys[index];
			}
		}
		return result;
	}

	public String[] getSelectedNames() {
		String[] result = new String[this.getSelectedCount()];
		int count = 0;
		for (int index = 0; index < mCount; index++) {
			if (mSelected[index]) {
				result[count++] = mNames[index];
			}
		}
		return result;
	}
}
