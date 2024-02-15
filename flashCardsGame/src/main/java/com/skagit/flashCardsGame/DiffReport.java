package com.skagit.flashCardsGame;

import java.util.ArrayList;

public class DiffReport {
	final boolean _gotItRight, _gotItExactlyRight;
	final String _diffString;

	DiffReport(final HonorResult honorResult, final boolean ignoreDiacritics,
			final String response0, final String answer0) {
		final String response0Lc = response0.toLowerCase();
		if (honorResult != HonorResult.NOT_HONOR_MODE) {
			_gotItRight = _gotItExactlyRight = honorResult == HonorResult.RIGHT;
			_diffString = null;
			return;
		}
		final String answer0Lc = answer0.toLowerCase();
		_gotItExactlyRight = response0Lc.equals(answer0Lc);
		if (_gotItExactlyRight) {
			_gotItRight = true;
			_diffString = null;
			return;
		}
		final String responseLc, answerLc;
		if (ignoreDiacritics) {
			responseLc = FlashCardsGame.StripVNDiacritics(response0);
			answerLc = FlashCardsGame.StripVNDiacritics(answer0);
			_gotItRight = responseLc.equals(answerLc);
		} else {
			responseLc = response0Lc;
			answerLc = answer0Lc;
			_gotItRight = false;
		}

		final String[] answer0Fields = answer0Lc.split(FlashCardsGame._WhiteSpace);
		if (_gotItRight) {
			/** We must be ignoring diacritics and did not get it exactly right. */
			String diffString = String.format("%c [%s", FlashCardsGame._CheckSymbol, answer0);
			final String[] responseFields = response0Lc.split(FlashCardsGame._WhiteSpace);
			final int nResponseFields = responseFields.length;
			final int nAnswer0Fields = answer0Fields.length;
			if (nResponseFields != nAnswer0Fields) {
				diffString += "(Different number of fields?!)";
			} else {
				boolean foundSomething = false;
				for (int k = 0; k < nAnswer0Fields; ++k) {
					final String responseField = responseFields[k];
					final String answer0Field = answer0Fields[k];
					if (!responseField.equals(answer0Field)) {
						diffString += String.format(" (%s)", answer0Field);
						foundSomething = true;
						break;
					}
				}
				if (!foundSomething) {
					diffString += "(Couldn't find the difference?!)";
				}
			}
			diffString += ']';
			_diffString = diffString;
			return;
		}

		/** Flat out got it wrong. Start with the answer and add the differences. */
		String diffString = String.format("%c%s%c", FlashCardsGame._RtArrow, answer0Lc,
				FlashCardsGame._LtArrow);

		final String[] responseFields = responseLc.split(FlashCardsGame._WhiteSpace);
		final String[] answerFields = answerLc.split(FlashCardsGame._WhiteSpace);
		final ArrayList<String> wrongs = new ArrayList<>();
		final int nResponseFields = responseFields.length;
		final int nAnswerFields = answerFields.length;
		final int nSmaller = Math.min(nResponseFields, nAnswerFields);
		for (int k = 0; k < nSmaller; ++k) {
			final String responseField = responseFields[k];
			final String answerField = answerFields[k];
			if (!responseField.equals(answerField)) {
				final String answer0Field = answer0Fields[k];
				wrongs.add(answer0Field);
			}
		}
		for (int k = nAnswerFields; k < nResponseFields; ++k) {
			wrongs.add(String.format("%s?", responseFields[k]));
		}
		for (int k = nResponseFields; k < nAnswerFields; ++k) {
			wrongs.add(answer0Fields[k]);
		}
		final int nWrongs = wrongs.size();
		final int nToReport = Math.min(2, nWrongs);
		for (int k = 0; k < nToReport; ++k) {
			if (k == 0) {
				diffString += " (";
			} else {
				diffString += " ";
			}
			diffString += wrongs.get(k);
		}
		diffString += ")";
		_diffString = diffString;
	}
}