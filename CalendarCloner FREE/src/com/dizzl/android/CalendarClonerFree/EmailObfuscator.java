package com.dizzl.android.CalendarClonerFree;

import java.util.Locale;

public class EmailObfuscator {
	private static String dummyMethod1(String orgEmail, String dummyEmailDomain) {
		if (orgEmail == null || orgEmail.contentEquals("")) {
			orgEmail = "null";
		}
		// Replace @ with _at_ and append default domain
		while (orgEmail.contains("@")) {
			int pos = orgEmail.indexOf("@");
			orgEmail = orgEmail.substring(0, pos) + "_at_" + orgEmail.substring(pos + 1, orgEmail.length());
		}

		return (orgEmail + "@" + dummyEmailDomain).toLowerCase(Locale.getDefault());
	}

	@SuppressWarnings("unused")
	private static String dummyMethod2(String orgEmail, String dummyEmailDomain) {
		final char zeroWidthSpace = '\u200b';
		final String insertString = "" + zeroWidthSpace + zeroWidthSpace;

		if (orgEmail == null || orgEmail.contentEquals("")) {
			orgEmail = "null@" + dummyEmailDomain;
		}
		for (int index = orgEmail.length() - 1; index >= 0; index--) {
			char c = orgEmail.charAt(index);
			if (c == zeroWidthSpace) {
				// Remove Zero Width Space
				orgEmail = orgEmail.substring(0, index) + orgEmail.substring(index + 1);
			}
			if (c == '@') {
				// Insert string after @-sign
				orgEmail = orgEmail.substring(0, index + 1) + insertString + orgEmail.substring(index + 1);
			}
		}
		return orgEmail;
	}

	public static String generateDummyEmailFrom(String orgEmail, String dummyEmailDomain) {
		return dummyMethod1(orgEmail, dummyEmailDomain);
	}
}
