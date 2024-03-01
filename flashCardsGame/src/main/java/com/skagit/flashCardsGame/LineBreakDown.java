package com.skagit.flashCardsGame;

public class LineBreakDown {
	final String _aSide;
	final String _bSide;
	final boolean _nextLineIsContinuation;

	LineBreakDown(final String line) {
		final int len = line == null ? 0 : line.length();
		_nextLineIsContinuation = len < 1 ? false : line.charAt(len - 1) == '\t';

		if (line.isBlank()) {
			_aSide = _bSide = "";
			return;
		}
		final String[] fields = line.split(FlashCardsGame._FieldSeparator);
		final int nFields = fields.length;

		/** Set field0 to the first non-blank field and k0 to its index. */
		int k0 = 0;
		for (; k0 < nFields; ++k0) {
			if (!fields[k0].isBlank()) {
				break;
			}
		}
		if (k0 == nFields) {
			/** Never found a non-blank field. */
			_aSide = _bSide = "";
			return;
		}
		final String field0 = fields[k0];

		/** If field0 is a number, we will skip over it when looking for data fields. */
		int cardNumber = -1;
		try {
			/** We kill punctuation to get rid of a period or a colon. */
			cardNumber = Integer
					.parseInt(FlashCardsGame.CleanWhiteSpace(FlashCardsGame.KillPunct(field0)));
		} catch (final NumberFormatException e) {
		}

		/** Set field1 to the first data field, and k1 to its index. */
		final int k1 = k0 + (cardNumber >= 0 ? 1 : 0);
		final String field1 = fields[k1];
		if (k1 >= nFields) {
			/** There's no data. */
			_aSide = _bSide = "";
			return;
		}
		if (k1 == nFields - 1) {
			/** There's only one data field. Is it a or b? */
			final int startTabCount;
			if (cardNumber >= 0) {
				/** We start our count of tabs just after field0. */
				startTabCount = line.indexOf(field0) + field0.length();
			} else {
				/** There was no number; start the count of tabs at 0. */
				startTabCount = 0;
			}
			int nTabs = 0;
			for (int k = startTabCount;; ++k) {
				final char c = line.charAt(k);
				if (!Character.isWhitespace(c)) {
					break;
				}
				if (c == '\t') {
					if (++nTabs == 2) {
						break;
					}
				}
			}
			final String dataField = FlashCardsGame.CleanWhiteSpace(field1);
			if (nTabs == 1) {
				_aSide = dataField;
				_bSide = "";
			} else {
				_aSide = "";
				_bSide = dataField;
			}
		} else {
			/** We have at least two data fields. */
			_aSide = FlashCardsGame.CleanWhiteSpace(fields[k1]);
			_bSide = FlashCardsGame.CleanWhiteSpace(fields[k1 + 1]);
		}
	}

	String getString() {
		return String.format("a:%s, b:%s, %s", _aSide, _bSide,
				_nextLineIsContinuation ? "Cont" : "Orig");
	}

	@Override
	public String toString() {
		return getString();
	}

	public static void main(final String[] args) {
		final String[] fields = "\t  ab".split(FlashCardsGame._FieldSeparator);
		final int nFields = fields.length;

		/** Set field0 to the first non-blank field and k0 to its index. */
		int k0 = 0;
		for (; k0 < nFields; ++k0) {
			if (!fields[k0].isBlank()) {
				break;
			}
		}
		@SuppressWarnings("unused")
		final int x = 0;

	}

}
