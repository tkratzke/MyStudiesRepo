package com.skagit.flashCardsGame;

class Card {

	int _cardIndex;
	final String _aSide;
	final String _bSide;
	String _comment;

	Card(final int cardIndex, final String aSide, final String bSide) {
		_cardIndex = cardIndex;
		_aSide = aSide;
		_bSide = bSide;
		_comment = "";
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || !(o instanceof Card)) {
			return false;
		}
		final Card card = (Card) o;
		return card._aSide.equalsIgnoreCase(_aSide) && card._bSide.equalsIgnoreCase(_bSide);
	}

	String getString() {
		return String.format("%s%04d.\t%s:\t%s", _comment, _cardIndex, _aSide, _bSide);
	}

	@Override
	public String toString() {
		return getString();
	}
}