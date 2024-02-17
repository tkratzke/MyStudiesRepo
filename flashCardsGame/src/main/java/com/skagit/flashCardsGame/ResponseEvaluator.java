package com.skagit.flashCardsGame;

public class ResponseEvaluator {
	final boolean _gotItRight;
	final String _diffString;

	ResponseEvaluator(final boolean ignoreVnDiacritics, final String answer0,
			final String response0) {
		/** Get rid of the punctuation. */
		final String answer1a = answer0.replaceAll("\\p{Punct}", " ");
		final String response1a = response0.replaceAll("\\p{Punct}", " ");
		final String answer1 = FlashCardsGame.CleanString(answer1a);
		final String response1 = FlashCardsGame.CleanString(response1a);

		if (answer0.equalsIgnoreCase(response0)) {
			_gotItRight = true;
			_diffString = null;
			return;
		}
		if (ignoreVnDiacritics) {
			final String answer = FlashCardsGame.StripVNDiacritics(answer1);
			final String response = FlashCardsGame.StripVNDiacritics(response1);
			_gotItRight = answer.equalsIgnoreCase(response);
		} else {
			_gotItRight = false;
		}

		final String[] answer1Fields = answer1.split(FlashCardsGame._WhiteSpace);
		final String[] response1Fields = response1.split(FlashCardsGame._WhiteSpace);
		final int nAnswerFields = answer1Fields.length;
		final int nResponseFields = response1Fields.length;
		if (_gotItRight) {
			/** We must be ignoring diacritics, got it right, but not exactly right. */
			final String s = String.format("%c [%c%s%c", FlashCardsGame._HeavyCheckSymbol,
					FlashCardsGame._RtArrow, answer0, FlashCardsGame._LtArrow);
			for (int k = 0; k < nAnswerFields; ++k) {
				final String answer1Field = answer1Fields[k];
				final String response1Field = response1Fields[k];
				if (!answer1Field.equalsIgnoreCase(response1Field)) {
					_diffString = String.format("%s (%s/%s)]", s, answer1Field, response1Field);
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
			final String answer1Field = answer1Fields[k];
			final String response1Field = response1Fields[k];
			final String answerField, responseField;
			if (ignoreVnDiacritics) {
				answerField = FlashCardsGame.StripVNDiacritics(answer1Field);
				responseField = FlashCardsGame.StripVNDiacritics(response1Field);
			} else {
				answerField = answer1Field;
				responseField = response1Field;
			}
			if (!answerField.equalsIgnoreCase(responseField)) {
				_diffString = diffString
						+ String.format(" (%s/%s)]", answer1Field, response1Field);
				return;
			}
		}
		if (nAnswerFields < nResponseFields) {
			_diffString = diffString
					+ String.format(" (null/%s)]", response1Fields[nAnswerFields]);
		} else {
			_diffString = diffString
					+ String.format(" (%s/null)]", answer1Fields[nResponseFields]);
		}
	}
}