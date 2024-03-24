package com.skagit.flashCardsGame;

import java.io.File;

import com.skagit.SimpleAudioPlayer;

public class FullSide {

	final String _s;
	final File _f;
	public FullSide(final String fullString, final File soundFilesDir) {
		if (soundFilesDir == null || !soundFilesDir.isDirectory()) {
			_s = fullString.trim();
			_f = null;
			return;
		}
		/** Do we have a putative file? */
		final int len = fullString.length();
		final int idx0 = fullString.indexOf(Statics._FileDelimiter);
		final int idx1 = (0 <= idx0 && idx0 < len)
				? fullString.indexOf(Statics._FileDelimiter, idx0 + 1)
				: -1;
		if (idx1 == -1 || idx1 < idx0 + 3) {
			_s = fullString.trim();
			_f = null;
			return;
		}
		final String fileString = fullString.substring(idx0, idx1 + 1);
		final File f = new File(soundFilesDir,
				fileString.substring(1, fileString.length() - 1));
		if (SimpleAudioPlayer.validate(f, /* play= */true)) {
			_f = f;
			_s = fullString.replaceFirst(fileString, "").trim();
			return;
		}
		_s = fullString.trim();
		_f = null;
	}

	public String getString() {
		if (_f == null) {
			return _s;
		}
		return String.format("File[%s], %s", _f.toString(), _s);
	}

	@Override
	public String toString() {
		return getString();
	}

	public static void main(final String[] args) {
		final FullSide fullSide = new FullSide("%junk.aiff% more strings",
				new File("Data/SoundFiles"));
		System.out.println(fullSide.getString());
	}

}
