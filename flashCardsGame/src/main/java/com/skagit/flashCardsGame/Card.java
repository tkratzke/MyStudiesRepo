package com.skagit.flashCardsGame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Comparator;

class Card {
	int _cardNumber;
	final String _aSideString;
	final String _bSideString;
	String[] _commentLines;

	Card(final int cardNumber, final String aSideString, final String bSideString,
			final String[] commentLines) {
		_cardNumber = cardNumber;
		_aSideString = aSideString;
		_bSideString = bSideString;
		_commentLines = commentLines;
	}

	private static int NullCompare(final Card card0, final Card card1) {
		if ((card0 == null) != (card1 == null)) {
			return card0 == null ? -1 : 1;
		}
		if (card0 == null) {
			return 0;
		}
		return 2;
	}

	static final Comparator<Card> _ByCardNumberOnly = new Comparator<>() {

		@Override
		public int compare(final Card card0, final Card card1) {
			final int compareValue = NullCompare(card0, card1);
			if (-1 <= compareValue && compareValue <= 1) {
				return compareValue;
			}
			final int idx0 = card0._cardNumber, idx1 = card1._cardNumber;
			return idx0 < idx1 ? -1 : (idx0 > idx1 ? 1 : 0);
		}
	};

	static final Comparator<Card> _ByASideOnly = new Comparator<>() {

		@Override
		public int compare(final Card card0, final Card card1) {
			final int compareValue = NullCompare(card0, card1);
			if (-1 <= compareValue && compareValue <= 1) {
				return compareValue;
			}
			final String a0 = card0._aSideString;
			final String a1 = card1._aSideString;
			final String a0a = Statics.CleanWhiteSpace(Statics.KillPunct(a0));
			final String a1a = Statics.CleanWhiteSpace(Statics.KillPunct(a1));
			return a0a.compareToIgnoreCase(a1a);
		}
	};

	static final Comparator<Card> _ByBSideOnly = new Comparator<>() {

		@Override
		public int compare(final Card card0, final Card card1) {
			final int compareValue = NullCompare(card0, card1);
			if (-1 <= compareValue && compareValue <= 1) {
				return compareValue;
			}
			final String b0 = card0._bSideString;
			final String b1 = card1._bSideString;
			final String b0a = Statics.CleanWhiteSpace(Statics.KillPunct(b0));
			final String b1a = Statics.CleanWhiteSpace(Statics.KillPunct(b1));
			return b0a.compareToIgnoreCase(b1a);
		}
	};

	static final Comparator<Card> _ByAThenB = new Comparator<>() {

		@Override
		public int compare(final Card card0, final Card card1) {
			final int compareValue = _ByASideOnly.compare(card0, card1);
			if (compareValue != 0) {
				return compareValue;
			}
			return _ByBSideOnly.compare(card0, card1);
		}
	};

	@Override
	public boolean equals(final Object o) {
		if (o == null || !(o instanceof Card)) {
			return false;
		}
		return _ByAThenB.compare(this, (Card) o) == 0;
	}

	String getString() {
		final int nCommentLines = _commentLines == null ? 0 : _commentLines.length;
		String s = null;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			final PrintStream ps = new PrintStream(baos);
			final PrintStream old = System.out;
			System.setOut(ps);
			for (int k = 0; k < nCommentLines; ++k) {
				System.out.println(Statics._CommentString + _commentLines[k]);
			}
			System.out.printf("%04d.\t%s:\t%s", _cardNumber, _aSideString, _bSideString);
			s = baos.toString();
			System.out.flush();
			System.setOut(old);
		} catch (final IOException e) {
		}
		return s;
	}

	String getBrokenUpString(final boolean aSide, final int maxLen) {
		final CardParts parts = new CardParts(aSide ? _aSideString : _bSideString, maxLen);
		final int nParts = parts.size();
		String s = "" + Statics._RtArrowChar;
		for (int k = 0; k < nParts; ++k) {
			s += parts.get(k);
			if (k < nParts - 1) {
				s += "\n\t";
			} else {
				s += "" + Statics._LtArrowChar;
			}
		}
		return s;
	}

	@Override
	public String toString() {
		return getString();
	}
}