package com.skagit.flashCardsGame;

public class LineBreakDown {
	final String _aSide;
	final String _bSide;
	final boolean _nextLineIsContinuation;

	/**
	 * Assumes that nextLine is not blank. Furthermore, it is not a comment nor a part of a
	 * comment. If the 1st non-blank field is a number, it takes 2 tabs to get to the
	 * B-Side. If it's not a number then it only takes one tab to get to the B side.
	 */
	LineBreakDown(final String nextLine) {
		/** Not blank and not a comment; split the original line and process it. */
		final int len = nextLine.length();
		_nextLineIsContinuation = nextLine.charAt(len - 1) == '\t';

		/**
		 * <pre>
		 * Some interesting examples of the fields produced by the split command:
		 * 1.  "  \t " or "": [] (already eliminated)
		 * 2.  "    ": [    ] (already eliminated)
		 * 3.  "  \t a" or " \t a \t": [, a]
		 * 4.  "  a" or "  a\t": [  a]
		 * NB:
		 *     A.  An initial separator (followed by non-blank) creates an
		 *         empty field, but a trailing separator is ignored.
		 *         An initial separator that is not followed by a non-blank
		 *         results in no fields at all.
		 *     B.  Only the first field can be empty and then if and only if:
		 *         There is a second, non-empty field, which will start with
		 *         non-white-space.
		 *     C.  There will be 0 fields if and only if:
		 *         i).  The entire line is white space and there is a tab, or
		 *         ii). The entire line is empty.
		 *         Either way, we consider it a blank line and ignore it.
		 * </pre>
		 */
		final String[] fields = nextLine.split(FlashCardsGame._FieldSeparator);
		final int nFields = fields.length;
		/** Check for blank or only one non-blank which is an integer. */
		boolean seenInteger = false;
		boolean haveSomething = false;
		for (int k = 0; k < nFields; ++k) {
			final String field = fields[k];
			if (field.isBlank()) {
				continue;
			}
			if (isInteger(field)) {
				if (seenInteger) {
					haveSomething = true;
					break;
				}
				seenInteger = true;
				continue;
			}
			haveSomething = true;
			break;
		}
		if (!haveSomething) {
			_aSide = _bSide = null;
			return;
		}

		final String field0 = nFields >= 1 ? fields[0] : null;
		final String field1 = nFields >= 2 ? fields[1] : null;
		final String field2 = nFields >= 3 ? fields[2] : null;
		final String field3 = nFields >= 4 ? fields[3] : null;

		if (field0.length() == 0) {
			/**
			 * First field is blank; third example. There is a tab before the non-blank field.
			 * Start by checking if field1 is a number or not.
			 */
			if (isInteger(field1)) {
				if (field3 == null) {
					/**
					 * There is exactly one field past the number. There must be at least one tab in
					 * the separator. If there are two or more, this is b-side.
					 */
					final int nTabs = countTabs(nextLine, field1);
					_aSide = nTabs >= 2 ? "" : field2;
					_bSide = nTabs >= 2 ? field2 : "";
					return;
				}
				/** There are at least two fields past the number. */
				_aSide = field2;
				_bSide = field3;
				return;
			}
			/**
			 * There is a tab before the first non-blank field, which is not an integer. But it
			 * does exist. 1 or 2 tabs in the initial separator gets you to the a-side.
			 */
			if (field2 == null) {
				/**
				 * There is only one field. There must be at least one tab in the initial
				 * separator. If there are two or more, this is b-side.
				 */
				final int nTabs = countTabs(nextLine, "");
				_aSide = nTabs >= 2 ? "" : field1;
				_bSide = nTabs >= 2 ? field1 : "";
				return;
			}
			/** There are at least two fields. */
			_aSide = field1;
			_bSide = field2;
			return;
		}
		/** First field is not blank. */
		if (isInteger(field0)) {
			if (field2 == null) {
				/**
				 * There is exactly one field past the number. There must be at least one tab in
				 * the separator. If there are two or more, this is b-side.
				 */
				final int nTabs = countTabs(nextLine, field0);
				_aSide = nTabs >= 2 ? "" : field1;
				_bSide = nTabs >= 2 ? field1 : "";
				return;
			}
			/** There are at least two fields past the number. */
			_aSide = field1;
			_bSide = field2;
			return;
		}
		/**
		 * First field is not blank and not an integer. Hence, there are no tabs before the
		 * first non-white space, and the first field is _aSide.
		 */
		_aSide = field0;
		_bSide = field1 == null ? "" : field1;
	}

	private static boolean isInteger(final String s) {
		try {
			Integer.parseInt(FlashCardsGame.CleanWhiteSpace(FlashCardsGame.KillPunct(s)));
			return true;
		} catch (final NumberFormatException e) {
		}
		return false;
	}

	private static int countTabs(final String nextLine, final String field) {
		final int start = nextLine.indexOf(field) + field.length();
		int nTabs = 0;
		for (int k = start; k < nextLine.length(); ++k) {
			final char c = nextLine.charAt(k);
			if (c == '\t') {
				if (++nTabs == 2) {
					return nTabs;
				}
				if (!Character.isWhitespace(c)) {
					break;
				}
			}
		}
		return nTabs;
	}

	String getString() {
		return String.format("A-Side|%s|, B-Side|%s| %s", _aSide, _bSide,
				"" + (_nextLineIsContinuation
						? FlashCardsGame._RtArrowChar2
						: FlashCardsGame._SpadeSymbolChar));
	}

	@Override
	public String toString() {
		return getString();
	}

	public static void main(final String[] args) {
		final String[] lines = { //
				"\t", //
				"\t0\t\tX", //
				"\t0\t\tX\tY", //
				"0.\t\tX\tY", //
				"0.\t\tX", //
				"0.\t\t\tX", //
				"\t\tX", //
		};
		final int nLines = lines.length;
		for (int k = 0; k < nLines; ++k) {
			final String line = lines[k];
			final String line2 = line.replaceAll("\t", "\\\\t");
			final LineBreakDown lbd = new LineBreakDown(line);
			final String toPrint = String.format("line2[%s] %s", line2, lbd);
			System.out.println(toPrint);
		}
	}

}
