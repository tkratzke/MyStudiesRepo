package com.skagit.paragraphFlattener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ParagraphFlattener {

    public static void main(final String[] args) {
	final File fIn = new File("../GiBill/Part1.txt");
	final File fOut = new File("../GiBill/Part1-Out.txt");
	String activeLine = "";
	boolean onFootnote = false;
	int nFootnoteLinesPrinted = 0;
	boolean havePrinted = false;
	try (final FileReader fileReader = new FileReader(fIn);
		final BufferedReader bufferedReader = new BufferedReader(fileReader);
		final PrintWriter pw = new PrintWriter(fOut)) {
	    for (;;) {
		try {
		    final String line = bufferedReader.readLine().trim();
		    final boolean emptyLine = line.length() == 0;
		    final String lineLc = line.toLowerCase();
		    final boolean startsFootnote = lineLc.startsWith("(footnote");
		    final boolean endsFootnote = lineLc.endsWith("footnote)");
		    final boolean wasOnFootnote = onFootnote;
		    if (emptyLine) {
			/** Wrap up the previous line and print it if it's not empty. */
			activeLine = activeLine.replaceAll("  ", " ");
			if (activeLine.length() > 0) {
			    final String format;
			    if (!havePrinted) {
				format = "%s";
				havePrinted = true;
			    } else if (onFootnote) {
				if (nFootnoteLinesPrinted == 0) {
				    format = "\n%s";
				} else {
				    format = "\n\t%s";
				}
				++nFootnoteLinesPrinted;
			    } else {
				format = "\n\n%s";
			    }
			    pw.printf(format, activeLine);
			    System.out.printf(format, activeLine);
			    activeLine = "";
			}
			continue;
		    }
		    if (!wasOnFootnote) {
			if (!startsFootnote) {
			    /** Just add to the line. */
			    activeLine += " " + line;
			} else {
			    /** We were on body, and switched to footnote. Must print out body. */
			    activeLine = activeLine.replaceAll("  ", " ");
			    if (activeLine.length() > 0) {
				final String format;
				if (!havePrinted) {
				    format = "%s";
				    havePrinted = true;
				} else {
				    format = "\n\n%s";
				}
				pw.printf(format, activeLine);
				System.out.printf(format, activeLine);
			    }
			    /** We're now on a footnote. */
			    activeLine = line;
			    onFootnote = true;
			    nFootnoteLinesPrinted = 0;
			    if (endsFootnote) {
				/** We started and ended the footnote with line. Print the footnote. */
				activeLine = activeLine.replaceAll("  ", " ");
				final String format;
				if (!havePrinted) {
				    format = "%s";
				    havePrinted = true;
				} else {
				    format = "\n%s";
				}
				pw.printf(format, activeLine);
				System.out.printf(format, activeLine);
				activeLine = "";
				onFootnote = false;
			    }
			}
			continue;
		    }
		    if (wasOnFootnote) {
			activeLine += " " + line;
			if (endsFootnote) {
			    activeLine = activeLine.replaceAll("  ", " ");
			    final String format;
			    if (!havePrinted) {
				format = "%s";
				havePrinted = true;
			    } else {
				if (nFootnoteLinesPrinted == 0) {
				    format = "\n%s";
				} else {
				    format = "\n\t%s";
				}
			    }
			    pw.printf(format, activeLine);
			    System.out.printf(format, activeLine);
			    activeLine = "";
			    onFootnote = false;
			    nFootnoteLinesPrinted = 0;
			    continue;
			} else {
			    /**
			     * We were on a footnote and did not end it. We've already added line to the
			     * activeLine so there's nothing to do.
			     */
			}
		    }
		} catch (final IOException | NullPointerException e) {
		    if (activeLine.length() > 0) {
			activeLine = activeLine.replaceAll("  ", " ");
			final String format;
			if (!havePrinted) {
			    format = "%s";
			    havePrinted = true;
			} else if (onFootnote) {
			    if (nFootnoteLinesPrinted == 0) {
				format = "\n%s";
			    } else {
				format = "\n\t%s";
			    }
			} else {
			    format = "\n\n%s";
			}
			pw.printf(format, activeLine);
			System.out.printf(format, activeLine);
		    }
		    break;
		}
	    }
	} catch (final IOException e) {
	}
    }

}
