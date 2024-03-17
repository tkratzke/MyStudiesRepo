package com.skagit.flashCardsGame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;

class Card {
	class CardParts extends ArrayList<String> {
		private static final long serialVersionUID = 1L;
		int _maxLen;
		CardParts(final boolean useASide, final int maxLen0) {
			super();
			String part = "";
			final String fullSide = useASide ? _fullASide : _fullBSide;
			int maxLen = 0;
			final String[] fields = fullSide.split(FlashCardsGame._WhiteSpace);
			final int nFields = fields.length;
			for (int k = 0; k < nFields; ++k) {
				final String field = fields[k];
				final int fieldLen = field.length();
				if (fieldLen > 0) {
					final int partLen = part.length();
					if (partLen + (partLen > 0 ? 1 : 0) + fieldLen > maxLen0) {
						if (partLen > 0) {
							add(part);
							maxLen = Math.max(maxLen, partLen);
						}
						part = field;
					} else {
						part += (partLen > 0 ? " " : "") + field;
					}
				}
			}
			final int partLen = part.length();
			if (partLen > 0) {
				add(part);
				maxLen = Math.max(maxLen, partLen);
			}
			_maxLen = maxLen;
		}
	}

	class CommentParts extends ArrayList<String> {
		private static final long serialVersionUID = 1L;
		CommentParts(final String fullComment, final int maxLineLen) {
			super();
			if (fullComment == null || fullComment.isBlank()) {
				return;
			}
			final String[] fields = fullComment.split(FlashCardsGame._WhiteSpace);
			final int nFields = fields.length;
			String currentPart = "!";
			int currentLineLen = currentPart.length();
			for (int k = 0; k < nFields; ++k) {
				final boolean kIs0 = k == 0;
				final String field = fields[k];
				final int fieldLen = field.length();
				if (fieldLen > 0) {
					final boolean addToCurrentPart;
					if (kIs0) {
						addToCurrentPart = true;
					} else {
						addToCurrentPart = currentLineLen + 1 + fieldLen <= maxLineLen;
					}
					if (addToCurrentPart) {
						currentPart += " " + field;
						currentLineLen += 1 + fieldLen;
					} else {
						add(currentPart);
						currentPart = "\t" + field;
						currentLineLen = FlashCardsGame._NominalTabLen + fieldLen;
					}
				}
			}
			add(currentPart);
		}
	}

	int _cardNumber;
	final String _fullASide;
	final String _fullBSide;
	String[] _commentLines;

	Card(final int cardNumber, final String aSide, final String bSide,
			final String[] commentLines) {
		_cardNumber = cardNumber;
		_fullASide = aSide;
		_fullBSide = bSide;
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
			final String a0 = card0._fullASide;
			final String a1 = card1._fullASide;
			final String a0a = FlashCardsGame.CleanWhiteSpace(FlashCardsGame.KillPunct(a0));
			final String a1a = FlashCardsGame.CleanWhiteSpace(FlashCardsGame.KillPunct(a1));
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
			final String b0 = card0._fullBSide;
			final String b1 = card1._fullBSide;
			final String b0a = FlashCardsGame.CleanWhiteSpace(FlashCardsGame.KillPunct(b0));
			final String b1a = FlashCardsGame.CleanWhiteSpace(FlashCardsGame.KillPunct(b1));
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
				System.out.println(FlashCardsGame._CommentString + _commentLines[k]);
			}
			System.out.printf("%04d.\t%s:\t%s", _cardNumber, _fullASide, _fullBSide);
			s = baos.toString();
			System.out.flush();
			System.setOut(old);
		} catch (final IOException e) {
		}
		return s;
	}

	String getBrokenUpString(final boolean a, final int specMaxLen) {
		final CardParts parts = new CardParts(a, specMaxLen);
		final int nParts = parts.size();
		String s = "" + FlashCardsGame._RtArrowChar;
		for (int k = 0; k < nParts; ++k) {
			s += parts.get(k);
			if (k < nParts - 1) {
				s += "\n\t";
			} else {
				s += "" + FlashCardsGame._LtArrowChar;
			}
		}
		return s;
	}

	@Override
	public String toString() {
		return getString();
	}
}