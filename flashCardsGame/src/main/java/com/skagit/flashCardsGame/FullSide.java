package com.skagit.flashCardsGame;

import java.io.File;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FullSide implements Comparable<FullSide> {

    final String _trimmedInputString;
    final String _stringPart;
    final File _soundFile;

    public FullSide(final TreeMap<String, File> allSoundFiles, final TreeMap<String, String> partToStem,
	    final String inputString) {
	final String trimmedInputString = inputString.trim();
	/** Do we have a putative file? */
	final int len = trimmedInputString.length();
	final int idx0 = trimmedInputString.indexOf(Statics._FileDelimiter);
	if (idx0 == -1) {
	    _stringPart = _trimmedInputString = Statics.CleanWhiteSpace(trimmedInputString);
	    _soundFile = null;
	    return;
	}
	final int idx1 = (0 <= idx0 && idx0 < len) ? trimmedInputString.indexOf(Statics._FileDelimiter, idx0 + 1) : -1;
	if (idx1 == -1) {
	    _stringPart = _trimmedInputString = Statics.CleanWhiteSpace(trimmedInputString);
	    _soundFile = null;
	    return;
	}
	if (idx1 < idx0 + 3) {
	    _stringPart = _trimmedInputString = Statics.CleanWhiteSpace(trimmedInputString);
	    _soundFile = null;
	    return;
	}
	String fileStringPart = trimmedInputString.substring(idx0, idx1 + 1);
	_stringPart = trimmedInputString.replaceFirst(Pattern.quote(fileStringPart), Matcher.quoteReplacement(""));
	final String fileString = fileStringPart.substring(1, fileStringPart.length() - 1);
	final String stem = partToStem.get(fileString);
	if (stem != null) {
	    fileStringPart = Character.toString(Statics._FileDelimiter) + stem + Statics._FileDelimiter;
	}
	_trimmedInputString = fileStringPart + " " + _stringPart;
	_soundFile = allSoundFiles.get(fileString);
    }

    public String getStringPart() {
	return _stringPart;
    }

    public File getSoundFile() {
	return _soundFile;
    }

    public String getFullString() {
	return _trimmedInputString;
    }

    @Override
    public String toString() {
	return getFullString();
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
