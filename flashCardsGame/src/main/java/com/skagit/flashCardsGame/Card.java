package com.skagit.flashCardsGame;

import java.util.ArrayList;
import java.util.Comparator;

class Card {
	final private static int _MaxLenForPart = 40;

	private static class MyArrayList extends ArrayList<String> {
		private static final long serialVersionUID = 1L;
		int _maxLen;
		MyArrayList(final String s) {
			super();
			String part = "";
			int maxLen = 0;
			final String[] fields = s.split(FlashCardsGame._WhiteSpace);
			final int nFields = fields.length;
			for (int k = 0; k < nFields; ++k) {
				final String field = fields[k];
				final int fieldLen = field.length();
				if (fieldLen > 0) {
					final int partLen = part.length();
					if (partLen + (partLen > 0 ? 1 : 0) + fieldLen > _MaxLenForPart) {
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

	int _cardNumber;
	final String _fullASide;
	final String _fullBSide;
	final MyArrayList _aParts;
	final MyArrayList _bParts;

	Card(final int cardNumber, final String aSide, final String bSide) {
		_cardNumber = cardNumber;
		_fullASide = aSide;
		_fullBSide = bSide;
		_aParts = new MyArrayList(aSide);
		_bParts = new MyArrayList(bSide);
	}

	ArrayList<String> getASides() {
		return _aParts;
	}

	ArrayList<String> getBSides() {
		return _bParts;
	}

	int getMaxALen() {
		return _aParts._maxLen;
	}

	int getMaxBSide() {
		return _bParts._maxLen;
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
			return card0._fullASide.compareToIgnoreCase(card1._fullASide);
		}
	};

	static final Comparator<Card> _ByBSideOnly = new Comparator<>() {

		@Override
		public int compare(final Card card0, final Card card1) {
			final int compareValue = NullCompare(card0, card1);
			if (-1 <= compareValue && compareValue <= 1) {
				return compareValue;
			}
			return card0._fullBSide.compareToIgnoreCase(card1._fullBSide);
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
		return String.format("%04d.\t%s:\t%s", _cardNumber, _fullASide, _fullBSide);
	}

	@Override
	public String toString() {
		return getString();
	}
}