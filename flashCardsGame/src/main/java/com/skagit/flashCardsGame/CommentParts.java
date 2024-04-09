package com.skagit.flashCardsGame;

import java.util.ArrayList;

public class CommentParts extends ArrayList<String> {
    private static final long serialVersionUID = 1L;

    CommentParts(final String fullComment, final int maxLineLen) {
	super();
	if (fullComment == null || fullComment.isBlank()) {
	    return;
	}
	final String[] fields = fullComment.split(Statics._WhiteSpace);
	final int nFields = fields.length;
	String currentPart = "!";
	int currentLineLen = currentPart.length();
	for (int k = 0; k < nFields; ++k) {
	    final boolean kIs0 = k == 0;
	    final String field = fields[k];
	    final int fieldLen = field.length();
	    if (fieldLen > 0) {
		final boolean addToCurrentPart;
		if (kIs0) {
		    addToCurrentPart = true;
		} else {
		    addToCurrentPart = currentLineLen + 1 + fieldLen <= maxLineLen;
		}
		if (addToCurrentPart) {
		    currentPart += " " + field;
		    currentLineLen += 1 + fieldLen;
		} else {
		    add(currentPart);
		    currentPart = "\t" + field;
		    currentLineLen = Statics._NominalTabLen + fieldLen;
		}
	    }
	}
	add(currentPart);
    }
}