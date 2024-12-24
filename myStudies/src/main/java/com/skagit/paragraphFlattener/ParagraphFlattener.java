package com.skagit.paragraphFlattener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ParagraphFlattener {

    static private enum BufferType {
	UNKNOWN(false, ""), BODY(false, "\n\n"), NEW_FOOTNOTE(true, "\n"), CONTINUE_FOOTNOTE(true, "\n");

	final boolean _isPartOfFootnote;
	final String _prelim;

	BufferType(final boolean isPartOfFootnote, final String prelim) {
	    _isPartOfFootnote = isPartOfFootnote;
	    _prelim = prelim;
	}
    };

    static final String _NewFootnoteMarkup = "(Footnote";
    static final String _ContinueFootnoteMarkup = "(Continue Footnote";
    static final String _NewFootnoteMarkupLc = _NewFootnoteMarkup.toLowerCase();
    static final String _ContinueFootnoteMarkupLc = _ContinueFootnoteMarkup.toLowerCase();

    public static void main(final String[] args) {
	// final File fIn = new File("../GiBill/TestFile.txt");
	// final File fOut = new File("../GiBill/Processed TestFile.txt");
	final File fIn = new File("../GiBill/GI Bill SourceText.txt");
	final File fOut = new File("../GiBill/Processed GI Bill SourceText.txt");
	String buffer = "";
	boolean havePrinted = false;
	BufferType bufferType = BufferType.UNKNOWN;
	try (final FileReader fileReader = new FileReader(fIn);
		final BufferedReader bufferedReader = new BufferedReader(fileReader);
		final PrintWriter pw = new PrintWriter(fOut)) {
	    for (;;) {
		try {
		    final String line = bufferedReader.readLine().trim();
		    /** An empty line dumps the buffer and sets bufferType to UNKNOWN. */
		    if (line.length() == 0) {
			dumpBuffer(pw, havePrinted, buffer, bufferType);
			if (buffer.length() > 0) {
			    havePrinted = true;
			    buffer = "";
			    bufferType = BufferType.UNKNOWN;
			}
			continue;
		    }
		    /** It's not an empty line. */
		    final String lineLc = line.toLowerCase();
		    final boolean bufferIsFootnote = bufferType._isPartOfFootnote;
		    final boolean lineStartsFootnote = lineLc.startsWith(_NewFootnoteMarkupLc);
		    final boolean lineStartsFootnoteContinuation;
		    if (!lineLc.startsWith(_ContinueFootnoteMarkupLc)) {
			lineStartsFootnoteContinuation = false;
		    } else if (!bufferIsFootnote) {
			lineStartsFootnoteContinuation = false;
		    } else {
			lineStartsFootnoteContinuation = true;
		    }
		    if (lineStartsFootnote) {
			dumpBuffer(pw, havePrinted, buffer, bufferType);
			havePrinted = buffer.length() > 0;
			buffer = line;
			bufferType = BufferType.NEW_FOOTNOTE;
		    } else if (lineStartsFootnoteContinuation) {
			dumpBuffer(pw, havePrinted, buffer, bufferType);
			havePrinted = buffer.length() > 0;
			buffer = line.substring(_ContinueFootnoteMarkup.length()).trim();
			if (buffer.charAt(0) == ':') {
			    buffer = buffer.substring(1);
			}
			bufferType = BufferType.CONTINUE_FOOTNOTE;
		    } else if (bufferType == BufferType.UNKNOWN) {
			bufferType = BufferType.BODY;
			buffer = line;
		    } else {
			buffer = buffer + " " + line;
		    }
		} catch (final IOException | NullPointerException e) {
		    dumpBuffer(pw, havePrinted, buffer, bufferType);
		    break;
		}
	    }
	} catch (final IOException e) {
	}
    }

    private static boolean dumpBuffer(final PrintWriter pw, final boolean havePrinted, final String buffer0,
	    final BufferType bufferType) {
	final String buffer = buffer0.trim().replaceAll("  ", " ");
	if (buffer.length() == 0) {
	    return /* dumpedSomething= */false;
	}
	final String prelim = havePrinted ? bufferType._prelim : "";
	final String format0;
	if (bufferType == BufferType.BODY) {
	    format0 = "%s";
	} else if (bufferType == BufferType.NEW_FOOTNOTE) {
	    format0 = "%s";
	} else {
	    format0 = "\t%s";
	}
	pw.printf("%s" + format0, prelim, buffer);
	System.out.printf("%s" + format0, prelim, buffer);
	return /* dumpedSomething= */true;
    }

}
