package com.skagit.flashCardsGame;

import java.io.File;

public class FullSide {

	final String _s;
	final File _f;
	public FullSide(final String fullString0) {
		/**
		 * If the beginning starts with a " and there's a matching ", then we have a putative
		 * file.
		 */
		final String fullString = fullString0.trim();
		final int len = fullString.length();
		final int idx0 = fullString.indexOf('"');
		final int idx1 = (0 <= idx0 && idx0 < len) ? fullString.indexOf('"', idx0 + 1) : -1;
		if (idx1 == -1) {
			_s = fullString;
			_f = null;
			return;
		}
		final String fileString0 = fullString.substring(idx0, idx1 + 1);
		_s = fullString.replaceFirst(fileString0, "");
		_f = new File(fileString0.substring(1, fileString0.length() - 1));
	}

}
