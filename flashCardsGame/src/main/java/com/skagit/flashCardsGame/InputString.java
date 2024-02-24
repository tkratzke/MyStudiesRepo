package com.skagit.flashCardsGame;

import java.util.Scanner;

class InputString {
	final boolean _lastLineWasBlank;
	final int _nLinesOfResponse;
	final String _inputString;
	InputString(final Scanner sc) {
		String s = "";
		boolean lastLineWasBlank = false;
		int nLinesOfResponse = 1;
		for (;;) {
			final String thisLine = sc.nextLine();
			final int len = thisLine.length();
			final boolean moreLines = len > 0 && thisLine.charAt(len - 1) == '\t';
			if (!thisLine.isBlank()) {
				lastLineWasBlank = false;
				s += " " + thisLine;
			} else if (moreLines) {
				lastLineWasBlank = true;
			}
			if (!moreLines) {
				break;
			}
			/** The next line has indented input. */
			System.out.print(FlashCardsGame._Indent);
			++nLinesOfResponse;
		}
		_inputString = FlashCardsGame.CleanWhiteSpace(s);
		_lastLineWasBlank = lastLineWasBlank;
		_nLinesOfResponse = nLinesOfResponse;
	}
}
