package com.skagit.util;

import java.util.Scanner;

public class InputString {
    final public boolean _lastLineWasBlank;
    final public int _nLinesOfResponse;
    final public String _inputString;

    public InputString(final Scanner sysInScanner) {
	String s = "";
	boolean lastLineWasBlank = false;
	int nLinesOfResponse = 1;
	for (;;) {
	    final String thisLine = sysInScanner.nextLine();
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
	    System.out.print(Statics._IndentString);
	    ++nLinesOfResponse;
	}
	_inputString = Statics.CleanWhiteSpace(s);
	_lastLineWasBlank = lastLineWasBlank;
	_nLinesOfResponse = nLinesOfResponse;
    }
}
