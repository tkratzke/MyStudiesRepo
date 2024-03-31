package com.skagit.flashCardsGame;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.TreeMap;

import com.skagit.SimpleAudioPlayer;

class Card {
	int _cardNumber;
	final private FullSide _aSide;
	final private FullSide _bSide;
	String[] _commentLines;

	Card(final boolean switchSides, final TreeMap<String, File> allSoundFiles,
			final int cardNumber, final String aSideString, final String bSideString,
			final String[] commentLines) {
		_cardNumber = cardNumber;
		_aSide = new FullSide(allSoundFiles, switchSides ? bSideString : aSideString);
		_bSide = new FullSide(allSoundFiles, switchSides ? aSideString : bSideString);
		_commentLines = commentLines;
	}

	static final Comparator<Card> _ByCardNumberOnly = new Comparator<>() {

		@Override
		public int compare(final Card card0, final Card card1) {
			final int compareValue = Statics.NullCompare(card0, card1);
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
			final int compareValue = Statics.NullCompare(card0, card1);
			if (-1 <= compareValue && compareValue <= 1) {
				return compareValue;
			}
			return card0._aSide.compareTo(card1._aSide);
		}
	};

	static final Comparator<Card> _ByBSideOnly = new Comparator<>() {

		@Override
		public int compare(final Card card0, final Card card1) {
			final int compareValue = Statics.NullCompare(card0, card1);
			if (-1 <= compareValue && compareValue <= 1) {
				return compareValue;
			}
			return card0._bSide.compareTo(card1._bSide);
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
			System.out.printf("%04d.\t%s:\t%s", _cardNumber, _aSide.getString(),
					_bSide.getString());
			s = baos.toString();
			System.out.flush();
			System.setOut(old);
		} catch (final IOException e) {
		}
		return s;
	}

	public boolean hasSoundFile(final boolean aSide) {
		return (aSide ? _aSide : _bSide).hasSoundFile();
	}
	public boolean hasStringPart(final boolean aSide) {
		return (aSide ? _aSide : _bSide).hasStringPart();
	}
	public String getStringPart(final boolean aSide) {
		return (aSide ? _aSide : _bSide).getStringPart();
	}

	public String getFullString(final boolean aSide) {
		return (aSide ? _aSide : _bSide).reconstructFullString();
	}

	public boolean playSoundFile(final boolean aSide) {
		return SimpleAudioPlayer.validate((aSide ? _aSide : _bSide)._soundFile,
				/* play= */true);
	}

	@Override
	public String toString() {
		return getString();
	}

}