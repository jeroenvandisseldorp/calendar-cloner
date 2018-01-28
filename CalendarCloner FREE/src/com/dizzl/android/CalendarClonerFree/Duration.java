package com.dizzl.android.CalendarClonerFree;

public class Duration {
	private static class ParseState {
		public String str = "";
		public int pos = 0;
	}

	private static char parseToken(ParseState state) {
		if (state != null && state.str.length() > state.pos) {
			return state.str.charAt(state.pos++);
		}
		return '\0';
	}

	// Return the duration in milliseconds
	public static long parseDuration(String duration) {
		if (duration == null || duration.length() == 0) {
			// Don't further parse empty string
			return 0;
		}

		long result = 0;
		boolean negativeDuration = false;
		ParseState state = new ParseState();
		state.str = duration;

		char token = parseToken(state);
		if (token == '+' || token == '-') {
			if (token == '-') {
				negativeDuration = true;
			}
			token = parseToken(state);
		}

		if (token != 'P') {
			// Should never happen
			return 0;
		}

		// Keep parsing until done, adding up lengths in seconds
		token = parseToken(state);
		while (token != '\0') {
			// Ignore time indicators (superfluous characters in spec)
			if (token == 'T') {
				token = parseToken(state);
				continue;
			}

			// Parse a number
			long subresult = 0;
			while (token >= '0' && token <= '9') {
				subresult = subresult * 10 + ((byte) token - (byte) '0');
				token = parseToken(state);
			}

			// Parse length
			switch (token) {
			case 'W':
				result += subresult * 60 * 60 * 24 * 7;
				break;
			case 'D':
				result += subresult * 60 * 60 * 24;
				break;
			case 'H':
				result += subresult * 60 * 60;
				break;
			case 'M':
				result += subresult * 60;
				break;
			case 'S':
				result += subresult;
				break;
			default:
				// Error situation
				return 0;
			}

			token = parseToken(state);
		}

		// Convert to milliseconds
		result *= 1000;

		if (negativeDuration) {
			return -result;
		}
		return result;
	}

	// Encode a duration in milliseconds to string
	public static String encodeDuration(long duration) {
		String result = "";
		if (duration < 0) {
			result += "-";
			duration = -duration;
		}

		// Convert to seconds
		duration /= 1000;

		result += "P";
		if (duration > 0) {
			if (duration >= 24 * 60 * 60) {
				long days = duration / (24 * 60 * 60);
				duration = duration % (24 * 60 * 60);
				result += "" + days + "D";
			}
			if (duration > 0) {
				result += "T";
				if (duration >= 60 * 60) {
					long hours = duration / (60 * 60);
					duration = duration % (60 * 60);
					result += "" + hours + "H";
				}
				if (duration >= 60) {
					long minutes = duration / 60;
					duration = duration % 60;
					result += "" + minutes + "M";
				}
				if (duration > 0) {
					result += "" + duration + "S";
				}
			}
		} else {
			result += "T0S";
		}
		return result;
	}
}
