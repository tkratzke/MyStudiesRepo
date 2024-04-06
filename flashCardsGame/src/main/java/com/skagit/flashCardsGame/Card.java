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
	final private FullSide _clueSide;
	final private FullSide _answerSide;
	String[] _commentLines;

	Card(final boolean switchSides, final TreeMap<String, File> allSoundFiles,
			final TreeMap<String, String> partToStem, final int cardNumber,
			final String clueSideString, final String answerSideString,
			final String[] commentLines) {
		_cardNumber = cardNumber;
		_clueSide = new FullSide(allSoundFiles, partToStem,
				switchSides ? answerSideString : clueSideString);
		_answerSide = new FullSide(allSoundFiles, partToStem,
				switchSides ? clueSideString : answerSideString);
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

	static final Comparator<Card> _ByClueSideOnly = new Comparator<>() {

		@Override
		public int compare(final Card card0, final Card card1) {
			final int compareValue = Statics.NullCompare(card0, card1);
			if (-1 <= compareValue && compareValue <= 1) {
				return compareValue;
			}
			return card0._clueSide.compareTo(card1._clueSide);
		}
	};

	static final Comparator<Card> _ByAnswerSideOnly = new Comparator<>() {

		@Override
		public int compare(final Card card0, final Card card1) {
			final int compareValue = Statics.NullCompare(card0, card1);
			if (-1 <= compareValue && compareValue <= 1) {
				return compareValue;
			}
			return card0._answerSide.compareTo(card1._answerSide);
		}
	};

	static final Comparator<Card> _ByClueThenAnswer = new Comparator<>() {

		@Override
		public int compare(final Card card0, final Card card1) {
			final int compareValue = _ByClueSideOnly.compare(card0, card1);
			if (compareValue != 0) {
				return compareValue;
			}
			return _ByAnswerSideOnly.compare(card0, card1);
		}
	};

	@Override
	public boolean equals(final Object o) {
		if (o == null || !(o instanceof Card)) {
			return false;
		}
		return _ByClueThenAnswer.compare(this, (Card) o) == 0;
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
			System.out.printf("%04d.\t%s:\t%s", _cardNumber, _clueSide.getFullString(),
					_answerSide.getFullString());
			s = baos.toString();
			System.out.flush();
			System.setOut(old);
		} catch (final IOException e) {
		}
		return s;
	}

	public String getStringPart(final boolean forClue) {
		return (forClue ? _clueSide : _answerSide).getStringPart();
	}

	public File getSoundFile(final boolean forClue) {
		return (forClue ? _clueSide : _answerSide).getSoundFile();
	}

	public String getFullString(final boolean forClue) {
		return (forClue ? _clueSide : _answerSide).getFullString();
	}

	public void playSoundFileIfPossible(final boolean silentMode, final boolean forClue) {
		if (!silentMode) {
			SimpleAudioPlayer //
					.playSoundFileIfPossible((forClue ? _clueSide : _answerSide)._soundFile);
		}
	}

	@Override
	public String toString() {
		return getString();
	}

}