package com.skagit.flashCardsGame;

import java.io.File;

import com.skagit.SimpleAudioPlayer;

public class FullSide implements Comparable<FullSide> {

	final String _trimmedInputString;
	final String _stringPart;
	final String _fileString;
	final File _soundFile;
	public FullSide(final String inputString, final File soundFilesDir) {
		_trimmedInputString = inputString.trim();
		if (soundFilesDir == null || !soundFilesDir.isDirectory()) {
			_stringPart = Statics.CleanWhiteSpace(_trimmedInputString);
			_fileString = null;
			_soundFile = null;
			return;
		}
		/** Do we have a putative file? */
		final int len = _trimmedInputString.length();
		final int idx0 = _trimmedInputString.indexOf(Statics._FileDelimiter);
		final int idx1 = (0 <= idx0 && idx0 < len)
				? _trimmedInputString.indexOf(Statics._FileDelimiter, idx0 + 1)
				: -1;
		if (idx1 == -1 || idx1 < idx0 + 3) {
			_stringPart = Statics.CleanWhiteSpace(_trimmedInputString);
			_fileString = null;
			_soundFile = null;
			return;
		}
		_fileString = _trimmedInputString.substring(idx0, idx1 + 1);
		final File f = new File(soundFilesDir,
				_fileString.substring(1, _fileString.length() - 1));
		if (SimpleAudioPlayer.validate(f, /* play= */false)) {
			_soundFile = f;
			_stringPart = Statics
					.CleanWhiteSpace(_trimmedInputString.replaceFirst(_fileString, ""));
			return;
		}
		_stringPart = Statics.CleanWhiteSpace(_trimmedInputString);
		_soundFile = null;
	}

	public String getStringPart() {
		return _stringPart;
	}

	public String getString() {
		if (_soundFile == null) {
			return _stringPart;
		}
		return String.format("File[%s], %s", _fileString, _stringPart);
	}

	public String reconstructFullString() {
		if (_soundFile == null) {
			return _stringPart;
		}
		return String.format("%s %s", _fileString, _stringPart);
	}

	@Override
	public String toString() {
		return getString();
	}

	@Override
	public int compareTo(final FullSide fullSide) {
		final int compareValue = Statics.NullCompare(this, fullSide);
		if (-1 <= compareValue && compareValue <= 1) {
			return compareValue;
		}
		return _trimmedInputString.compareToIgnoreCase(fullSide._trimmedInputString);
	}

	public static void main(final String[] args) {
		final FullSide fullSide = new FullSide("%junk.aiff% more strings",
				new File("Data/SoundFiles"));
		System.out.println(fullSide.getString());
	}

}
