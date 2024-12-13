package com.skagit.paragraphFlattener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ParagraphFlattener2 {

    public static void main(final String[] args) {
	final File fIn = new File("../GiBill/Part1.txt");
	final File fOut = new File("../GiBill/Part1-Out.txt");
	String pvsLine0 = "";
	boolean pvsLineIsFootnote = false;
	try (final FileReader fileReader = new FileReader(fIn);
		final BufferedReader bufferedReader = new BufferedReader(fileReader);
		final PrintWriter pw = new PrintWriter(fOut)) {
	    for (;;) {
		try {
		    final String line = bufferedReader.readLine().trim();
		    boolean pvsLineEnded = line.length() == 0;
		    boolean thisLineIsFootnote = false;
		    if (!pvsLineEnded) {
			final String lineLc = line.toLowerCase();
			thisLineIsFootnote = lineLc.startsWith("(footnote");
			if (thisLineIsFootnote) {
			    pvsLineEnded = true;
			}
		    }
		    if (pvsLineEnded && pvsLine0.length() > 0) {
			final String pvsLine = pvsLine0.replaceAll("  ", " ");
			System.out.printf(pvsLineIsFootnote ? "\n%s" : "\n\n%s", pvsLine);
			pw.printf(pvsLineIsFootnote ? "\n%s" : "\n\n%s", pvsLine);
			pvsLine0 = thisLineIsFootnote ? line : "";
			pvsLineIsFootnote = thisLineIsFootnote;
			continue;
		    }
		    pvsLine0 += (line.length() > 0 ? " " : "") + line;
		} catch (final IOException | NullPointerException e) {
		    if (pvsLine0.length() > 0) {
			final String pvsLine = pvsLine0.replaceAll("  ", " ");
			System.out.printf(pvsLineIsFootnote ? "\n%s" : "\n\n%s", pvsLine);
			pw.printf(pvsLineIsFootnote ? "\n%s" : "\n\n%s", pvsLine);
		    }
		    break;
		}
	    }
	} catch (final IOException e) {
	}
    }

}
