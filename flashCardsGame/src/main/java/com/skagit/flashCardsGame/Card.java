package com.skagit.flashCardsGame;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.TreeMap;

import com.skagit.util.Statics;

class Card {
    int _cardNumber;
    final FullSide _clueSide;
    final FullSide _answerSide;
    String[] _commentLines;

    Card(final boolean switchSides, final TreeMap<String, File> allSoundFiles, final TreeMap<String, String> partToStem,
	    final int cardNumber, final String clueSideString, final String answerSideString,
	    final String[] commentLines) {
	_cardNumber = cardNumber;
	_clueSide = new FullSide(allSoundFiles, partToStem, switchSides ? answerSideString : clueSideString);
	_answerSide = new FullSide(allSoundFiles, partToStem, switchSides ? clueSideString : answerSideString);
	_commentLines = commentLines;
    }

    final static Comparator<Card> _ByCardNumberOnly = new Comparator<>() {

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

    /**
     * As written, this puts the ones that have Answers that have Sound Files on the
     * top, and then by increasing number of spaces in the answers, and then by
     * increasing length of the answers.
     */
    final static Comparator<Card> _StrangeSortSet = new Comparator<>() {

	@Override
	public int compare(final Card card0, final Card card1) {
	    int compareValue = Statics.NullCompare(card0, card1);
	    if (-1 <= compareValue && compareValue <= 1) {
		return compareValue;
	    }
	    final FullSide answerSide0 = card0._answerSide;
	    final FullSide answerSide1 = card1._answerSide;
	    if ((answerSide0._soundFile != null) != (answerSide1._soundFile != null)) {
		return answerSide0._soundFile != null ? -1 : 1;
	    }
	    if (answerSide0._soundFile != null) {
		return _ByCardNumberOnly.compare(card0, card1);
	    }
	    final String s0 = answerSide0._stringPart;
	    final String s1 = answerSide1._stringPart;
	    final int nSpaces0 = countSpaces(s0);
	    final int nSpaces1 = countSpaces(s1);
	    if (nSpaces0 != nSpaces1) {
		return nSpaces0 < nSpaces1 ? -1 : 1;
	    }
	    final int len0 = s0.length();
	    final int len1 = s1.length();
	    if (len0 != len1) {
		return len0 < len1 ? -1 : 1;
	    }
	    final String s00 = Statics.StripVNDiacritics(s0);
	    final String s10 = Statics.StripVNDiacritics(s1);
	    compareValue = s00.compareTo(s10);
	    if (compareValue != 0) {
		return compareValue;
	    }
	    compareValue = s0.compareTo(s1);
	    if (compareValue != 0) {
		return compareValue;
	    }
	    return _ByCardNumberOnly.compare(card0, card1);
	}
    };

    private static int countSpaces(final String s) {
	if (s == null) {
	    return -1;
	}
	return s.length() - s.replaceAll(" ", "").length();
    }

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
	    System.out.printf("%04d.\t%s:\t%s", _cardNumber, _clueSide.getFullString(), _answerSide.getFullString());
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

    @Override
    public String toString() {
	return getString();
    }

}