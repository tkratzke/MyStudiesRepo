package com.skagit.flashCardsGame;

public class LineBreakDown {
	final String _aSide;
	final String _bSide;
	final boolean _nextLineIsContinuation;

	/**
	 * If the 1st non-blank field is a number, it takes 2 tabs to get to the B-Side. If it's
	 * not a number then it only takes one tab to get to the B side.
	 */
	LineBreakDown(final String line) {
		final int len = line == null ? 0 : line.length();
		_nextLineIsContinuation = len < 1 ? false : line.charAt(len - 1) == '\t';

		/**
		 * <pre>
		 * Some interesting examples:
		 * 1.  "  \t " or "": []
		 * 2.  "    ": [    ]
		 * 3.  "  \t a" or " \t a \t": [, a]
		 * 4.  "  a" or "  a\t": [  a]
		 * NB:
		 *     A.  An initial separator (followed by non-blank) creates an
		 *         empty field, but a trailing separator is ignored.
		 *     B.  Only the first field can be empty and then if and only if:
		 *         There is a second, non-empty field, which will start with
		 *         non-white-space.
		 *     C.  There will be 0 fields if and only if:
		 *         i).  The entire line is white space and there is a tab, or
		 *         ii). The entire line is empty.
		 * </pre>
		 */
		final String[] fields = line.split(FlashCardsGame._FieldSeparator);
		final int nFields = fields.length;
		final String field0 = nFields >= 1 ? fields[0] : null;
		final String field1 = nFields >= 2 ? fields[1] : null;
		final String field2 = nFields >= 3 ? fields[2] : null;
		final String field3 = nFields >= 4 ? fields[3] : null;

		/** Take care of the first two examples above: */
		if (field0 == null || (nFields == 1 && field0.isBlank())) {
			_aSide = _bSide = "";
			return;
		}

		if (field0.length() == 0) {
			/**
			 * First field is blank; third example. There is a tab before the non-blank field.
			 * Start by checking if field1 is a number or not.
			 */
			if (isInteger(field1)) {
				if (field2 == null) {
					/** No fields past the number. */
					_aSide = _bSide = "";
				} else if (field3 == null) {
					/**
					 * There is exactly one field past the number. There must be at least one tab in
					 * the separator. If there are two or more, this is b-side.
					 */
					final int nTabs = countTabs(line, field1);
					_aSide = nTabs >= 2 ? "" : field2;
					_bSide = nTabs >= 2 ? field2 : "";
				} else {
					/** There are at least two fields past the number. */
					_aSide = field2;
					_bSide = field3;
				}
			} else {
				/**
				 * There is a tab before the first non-blank field, which is not an integer. But
				 * it does exist. 1 or 2 tabs in the initial separator gets you to the a-side.
				 */
				if (field2 == null) {
					/**
					 * There is only one field. There must be at least one tab in the initial
					 * separator. If there are two or more, this is b-side.
					 */
					final int nTabs = countTabs(line, "");
					_aSide = nTabs >= 2 ? "" : field1;
					_bSide = nTabs >= 2 ? field1 : "";
				} else {
					/** There are at least two fields. */
					_aSide = field1;
					_bSide = field2;
				}
			}
		} else {
			/** First field is not blank. */
			if (isInteger(field0)) {
				if (field1 == null) {
					/** No fields past the number. */
					_aSide = _bSide = "";
				} else if (field2 == null) {
					/**
					 * There is exactly one field past the number. There must be at least one tab in
					 * the separator. If there are two or more, this is b-side.
					 */
					final int nTabs = countTabs(line, field0);
					_aSide = nTabs >= 2 ? "" : field1;
					_bSide = nTabs >= 2 ? field1 : "";
				} else {
					/** There are at least two fields past the number. */
					_aSide = field1;
					_bSide = field2;
				}
			} else {
				_aSide = field0;
				_bSide = field1 == null ? "" : field1;
			}
		}
	}

	private static boolean isInteger(final String s) {
		try {
			Integer.parseInt(FlashCardsGame.CleanWhiteSpace(FlashCardsGame.KillPunct(s)));
			return true;
		} catch (final NumberFormatException e) {
		}
		return false;
	}

	private static int countTabs(final String line, final String field) {
		final int start = line.indexOf(field) + field.length();
		int nTabs = 0;
		for (int k = start; k < line.length(); ++k) {
			final char c = line.charAt(k);
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
		return String.format("A-Side|%s|, B-Side|%s|", _aSide, _bSide);
	}

	@Override
	public String toString() {
		return getString();
	}

	public static void main(final String[] args) {
		final String[] lines = { //
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
			final String toPrint = String.format("%s|| %s", line2, lbd);
			System.out.println(toPrint);
		}
	}

}
