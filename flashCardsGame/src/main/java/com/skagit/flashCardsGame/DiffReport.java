package com.skagit.flashCardsGame;

public class DiffReport {
	final boolean _gotItRight;
	final String _diffString;

	DiffReport(final boolean ignoreDiacritics, final String response0,
			final String answer0) {
		final String response0Lc = response0.toLowerCase();
		final String answer0Lc = answer0.toLowerCase();
		if (response0Lc.equals(answer0Lc)) {
			_gotItRight = true;
			_diffString = null;
			return;
		}
		if (ignoreDiacritics) {
			final String responseLc = FlashCardsGame.StripVNDiacritics(response0);
			final String answerLc = FlashCardsGame.StripVNDiacritics(answer0);
			_gotItRight = responseLc.equals(answerLc);
		} else {
			_gotItRight = false;
		}

		final String[] answer0Fields = answer0Lc.split(FlashCardsGame._WhiteSpace);
		final String[] response0Fields = response0Lc.split(FlashCardsGame._WhiteSpace);
		final int nAnswer0Fields = answer0Fields.length;
		if (_gotItRight) {
			/** We must be ignoring diacritics and did not get it exactly right. */
			String diffString = String.format("%c [%c%s%c", FlashCardsGame._CheckSymbol,
					FlashCardsGame._RtArrow, answer0Lc, FlashCardsGame._LtArrow);
			final String[] responseFields = response0Lc.split(FlashCardsGame._WhiteSpace);
			for (int k = 0; k < nAnswer0Fields; ++k) {
				final String answer0Field = answer0Fields[k];
				final String responseField = responseFields[k];
				if (!responseField.equals(answer0Field)) {
					diffString += String.format(" (%s/%s)", answer0Field, responseField);
					break;
				}
			}
			diffString += ']';
			_diffString = diffString;
			return;
		}

		/** Got it wrong. Start with the answer and add one differences. */
		final String diffString = String.format("[%c%s%c", FlashCardsGame._RtArrow, answer0Lc,
				FlashCardsGame._LtArrow);
		final int nResponse0Fields = response0Fields.length;
		final int nSmaller = Math.min(nAnswer0Fields, nResponse0Fields);
		for (int k = 0; k < nSmaller; ++k) {
			final String answer0Field = answer0Fields[k];
			final String response0Field = response0Fields[k];
			final String answerField, responseField;
			if (ignoreDiacritics) {
				answerField = FlashCardsGame.StripVNDiacritics(answer0Field);
				responseField = FlashCardsGame.StripVNDiacritics(response0Field);
			} else {
				answerField = answer0Field;
				responseField = response0Field;
			}
			if (!answerField.equals(responseField)) {
				_diffString = diffString
						+ String.format(" (%s/%s)]", answer0Field, response0Field);
				return;
			}
		}
		for (int k = nAnswer0Fields; k < nResponse0Fields; ++k) {
			_diffString = diffString + String.format(" (null/%s)]", response0Fields[k]);
			return;
		}
		for (int k = nResponse0Fields; k < nAnswer0Fields; ++k) {
			_diffString = diffString + String.format(" (%s/null)]", answer0Fields[k]);
			return;
		}
		_diffString = null;
	}
}