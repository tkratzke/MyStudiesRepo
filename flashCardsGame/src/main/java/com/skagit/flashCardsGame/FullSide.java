package com.skagit.flashCardsGame;

import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.skagit.util.Statics;

public class FullSide implements Comparable<FullSide> {

    final String _trimmedInputString;
    final String _stringPart;
    final String _fullSoundFileString;

    public FullSide(final TreeMap<String, String> partToStem, final String inputString) {
	final String trimmedInputString = inputString.trim();
	/** Do we have a putative file? */
	final int len = trimmedInputString.length();
	final int idx0 = trimmedInputString.indexOf(Statics._FileDelimiter);
	if (idx0 == -1) {
	    _stringPart = _trimmedInputString = Statics.CleanWhiteSpace(trimmedInputString);
	    _fullSoundFileString = null;
	    return;
	}
	final int idx1 = (0 <= idx0 && idx0 < len) ? trimmedInputString.indexOf(Statics._FileDelimiter, idx0 + 1) : -1;
	if (idx1 == -1) {
	    _stringPart = _trimmedInputString = Statics.CleanWhiteSpace(trimmedInputString);
	    _fullSoundFileString = null;
	    return;
	}
	if (idx1 < idx0 + 3) {
	    _stringPart = _trimmedInputString = Statics.CleanWhiteSpace(trimmedInputString);
	    _fullSoundFileString = null;
	    return;
	}
	String soundFileString = trimmedInputString.substring(idx0, idx1 + 1);
	_stringPart = Statics.CleanWhiteSpace(
		trimmedInputString.replaceFirst(Pattern.quote(soundFileString), Matcher.quoteReplacement("")));
	final String fileString = soundFileString.substring(1, soundFileString.length() - 1);
	final String stem = partToStem.get(fileString);
	if (stem != null) {
	    soundFileString = Character.toString(Statics._FileDelimiter) + stem + Statics._FileDelimiter;
	}
	_trimmedInputString = soundFileString + " " + _stringPart;
	_fullSoundFileString = soundFileString;
    }

    @Override
    public String toString() {
	return _trimmedInputString;
    }

    @Override
    public int compareTo(final FullSide fullSide) {
	final int compareValue = Statics.NullCompare(this, fullSide);
	if (-1 <= compareValue && compareValue <= 1) {
	    return compareValue;
	}
	return _trimmedInputString.compareToIgnoreCase(fullSide._trimmedInputString);
    }

}
