package com.skagit.flashCardsGame;

import java.util.Scanner;

class InputString {
	final boolean _lastLineWasBlank;
	final String _inputString;
	InputString(final Scanner sc) {
		String s = "";
		boolean lastLineWasBlank = false;
		for (;;) {
			final String rawLine = sc.nextLine();
			final int rawLineLen = rawLine.length();
			final boolean mustContinue = rawLineLen > 0
					&& rawLine.charAt(rawLineLen - 1) == 't';
			if (!rawLine.isBlank()) {
				lastLineWasBlank = false;
				s += " " + rawLine;
			} else if (mustContinue) {
				lastLineWasBlank = true;
			}
			if (!mustContinue) {
				break;
			}
			System.out.print("\t");
		}
		_inputString = FlashCardsGame.CleanWhiteSpace(s);
		_lastLineWasBlank = lastLineWasBlank;
	}
}
