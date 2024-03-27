package com.skagit.flashCardsGame;

import java.util.ArrayList;

public class CardParts extends ArrayList<String> {
	private static final long serialVersionUID = 1L;
	int _maxLen;
	CardParts(final String fullSideString, final int maxLen0) {
		super();
		String part = "";
		int maxLen = 0;
		final String[] fields = fullSideString.split(Statics._WhiteSpace);
		final int nFields = fields.length;
		for (int k = 0; k < nFields; ++k) {
			final String field = fields[k];
			final int fieldLen = field.length();
			if (fieldLen > 0) {
				final int partLen = part.length();
				if (partLen + (partLen > 0 ? 1 : 0) + fieldLen > maxLen0) {
					if (partLen > 0) {
						add(part);
						maxLen = Math.max(maxLen, partLen);
					}
					part = field;
				} else {
					part += (partLen > 0 ? " " : "") + field;
				}
			}
		}
		final int partLen = part.length();
		if (partLen > 0) {
			add(part);
			maxLen = Math.max(maxLen, partLen);
		}
		_maxLen = maxLen;
	}
}