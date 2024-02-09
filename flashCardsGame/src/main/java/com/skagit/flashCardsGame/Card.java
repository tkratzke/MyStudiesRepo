package com.skagit.flashCardsGame;

import java.util.Comparator;

class Card {

	int _cardIndex;
	final String _aSide;
	final String _bSide;
	String _comment;

	Card(final int cardIndex, final String aSide, final String bSide) {
		_cardIndex = cardIndex;
		_aSide = FlashCardsGame.cleanUpString(aSide);
		_bSide = FlashCardsGame.cleanUpString(bSide);
		_comment = "";
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

	static final Comparator<Card> _ByIndexOnly = new Comparator<>() {

		@Override
		public int compare(final Card card0, final Card card1) {
			final int compareValue = NullCompare(card0, card1);
			if (-1 <= compareValue && compareValue <= 1) {
				return compareValue;
			}
			final int idx0 = card0._cardIndex, idx1 = card1._cardIndex;
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
			return card0._aSide.compareToIgnoreCase(card1._aSide);
		}
	};

	static final Comparator<Card> _ByBSideOnly = new Comparator<>() {

		@Override
		public int compare(final Card card0, final Card card1) {
			final int compareValue = NullCompare(card0, card1);
			if (-1 <= compareValue && compareValue <= 1) {
				return compareValue;
			}
			return card0._bSide.compareToIgnoreCase(card1._bSide);
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
		if (_comment == null || _comment.length() == 0) {
			return String.format("%04d.\t%s:\t%s", _cardIndex, _aSide, _bSide);
		}
		return String.format("%s\n%04d.\t%s:\t%s", _comment, _cardIndex, _aSide, _bSide);
	}

	@Override
	public String toString() {
		return getString();
	}
}