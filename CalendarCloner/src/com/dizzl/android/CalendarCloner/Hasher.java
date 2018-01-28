package com.dizzl.android.CalendarCloner;

import java.security.MessageDigest;

public class Hasher {
	private static MessageDigest mDigest = null;
	private static final char mHexChars[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
			'e', 'f' };

	private static void getDigest() {
		try {
			mDigest = java.security.MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String bytesToHex(byte[] bytes) {
		final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xff;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0f];
		}
		return new String(hexChars);
	}

	public static String hash(String s) {
		if (mDigest == null) {
			getDigest();
		}
		if (mDigest != null) {
			mDigest.update(s.getBytes());
			byte messageDigest[] = mDigest.digest();
			return bytesToHex(messageDigest);
		}
		return "";
	}

	public static String shortHash(String s) {
		if (mDigest == null) {
			getDigest();
		}
		if (mDigest != null) {
			mDigest.update(s.getBytes());
			byte messageDigest[] = mDigest.digest();
			if (messageDigest.length == 16) {
				StringBuilder hexString = new StringBuilder("00000000000000000000000000000000");
				for (int i = 0; i < messageDigest.length; i++) {
					int b = messageDigest[i] & 0xff;
					if (b >= 0x10) {
						hexString.setCharAt(i * 2, mHexChars[b >> 4]);
					}
					hexString.setCharAt(i * 2 + 1, mHexChars[b & 0xf]);
				}
				return hexString.toString();
			} else {
				// Create Hex String
				StringBuffer hexString = new StringBuffer(32);
				for (int i = 0; i < messageDigest.length; i++) {
					int b = messageDigest[i] & 0xff;
					if (b <= 0xf) {
						hexString.append("0");
					}
					hexString.append(Integer.toHexString(b));
				}
				return hexString.toString();
			}
		}
		return "";
	}
}
