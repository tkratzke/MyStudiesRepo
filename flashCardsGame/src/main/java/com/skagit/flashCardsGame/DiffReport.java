package com.skagit.flashCardsGame;

public class DiffReport {
	final boolean _gotItRight;
	final String _diffString;

	DiffReport(final boolean ignoreDiacritics, final String answer0,
			final String response0) {
		if (answer0.equalsIgnoreCase(response0)) {
			_gotItRight = true;
			_diffString = null;
			return;
		}
		final String[] answer0Fields = answer0.split(FlashCardsGame._WhiteSpace);
		final String[] response0Fields = response0.split(FlashCardsGame._WhiteSpace);
		final int nAnswerFields = answer0Fields.length;
		final int nResponseFields = response0Fields.length;
		if (ignoreDiacritics) {
			final String answer = FlashCardsGame.StripVNDiacritics(answer0);
			final String response = FlashCardsGame.StripVNDiacritics(response0);
			_gotItRight = answer.equalsIgnoreCase(response);
		} else {
			_gotItRight = false;
		}

		if (_gotItRight) {
			/** We must be ignoring diacritics, got it right, but not exactly right. */
			final String s = String.format("%c [%c%s%c", FlashCardsGame._CheckSymbol,
					FlashCardsGame._RtArrow, answer0, FlashCardsGame._LtArrow);
			for (int k = 0; k < nAnswerFields; ++k) {
				final String answer0Field = answer0Fields[k];
				final String response0Field = response0Fields[k];
				if (!answer0Field.equalsIgnoreCase(response0Field)) {
					_diffString = String.format("%s (%s/%s)]", s, answer0Field, response0Field);
					return;
				}
			}
			_diffString = null;
			return;
		}

		/** Got it wrong. Start with the answer and add one differences. */
		final String diffString = String.format("[%c%s%c", FlashCardsGame._RtArrow, answer0,
				FlashCardsGame._LtArrow);
		final int nSmaller = Math.min(nAnswerFields, nResponseFields);
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
			if (!answerField.equalsIgnoreCase(responseField)) {
				_diffString = diffString
						+ String.format(" (%s/%s)]", answer0Field, response0Field);
				return;
			}
		}
		if (nAnswerFields < nResponseFields) {
			_diffString = diffString
					+ String.format(" (null/%s)]", response0Fields[nAnswerFields]);
		} else {
			_diffString = diffString
					+ String.format(" (%s/null)]", answer0Fields[nResponseFields]);
		}
	}
}