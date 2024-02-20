package com.skagit.flashCardsGame;

import java.util.Scanner;

public class ResponseEvaluator {
	final boolean _gotItRight;
	final String _diffString;

	ResponseEvaluator(final Scanner sc, final boolean ignoreVnDiacritics, final Card card,
			final boolean quizIsAToB, final String rawResponse) {

		final String rawAnswer = quizIsAToB ? card._fullBSide : card._fullASide;
		if (equalsIgnoreCaseAndPunct(/* ignoreVnDiacritics= */false, rawAnswer,
				rawResponse)) {
			_gotItRight = true;
			_diffString = null;
			return;
		}

		/**
		 * We didn't get it exactly right. See if we got it "right enough" and then fill
		 * _diffString with the answer plus one difference.
		 */
		if (ignoreVnDiacritics) {
			_gotItRight = equalsIgnoreCaseAndPunct(true, rawAnswer, rawResponse);
		} else {
			_gotItRight = false;
		}

		final String[] rawAnswerFields = rawAnswer.split(FlashCardsGame._WhiteSpace);
		final String[] rawResponseFields = rawResponse.split(FlashCardsGame._WhiteSpace);
		final int nAnswerFields = rawAnswerFields.length;
		final int nResponseFields = rawResponseFields.length;

		String diffString = "";
		if (_gotItRight) {
			diffString += FlashCardsGame._HeavyCheckChar + " ";
		}
		diffString += card.getStringFromParts(!quizIsAToB);

		final int nSmaller = Math.min(nAnswerFields, nResponseFields);
		for (int k = 0; k < nSmaller; ++k) {
			final String answerField = rawAnswerFields[k];
			final String responseField = rawResponseFields[k];
			final boolean thisIsTheOne;
			if (_gotItRight) {
				/** Got it right, so ANY diff is interesting; do NOT ignore VN Diacritics. */
				thisIsTheOne = !equalsIgnoreCaseAndPunct(false, answerField, responseField);
			} else {
				/**
				 * Got it wrong, so some diff with the prescribed ignoring of VN diacritics
				 * exists.
				 */
				thisIsTheOne = !equalsIgnoreCaseAndPunct(ignoreVnDiacritics, answerField,
						responseField);
			}
			if (thisIsTheOne) {
				_diffString = diffString + String.format(" (%s/%s)", answerField, responseField);
				return;
			}
		}
		if (nAnswerFields < nResponseFields) {
			_diffString = diffString
					+ String.format(" (null/%s)", rawResponseFields[nAnswerFields]);
		} else {
			_diffString = diffString
					+ String.format(" (%s/null)", rawAnswerFields[nResponseFields]);
		}
	}

	private static boolean equalsIgnoreCaseAndPunct(final boolean ignoreVnDiacritics,
			final String s0, final String s1) {
		final String s0a = FlashCardsGame.CleanWhiteSpace(FlashCardsGame.KillPunct(s0));
		final String s1a = FlashCardsGame.CleanWhiteSpace(FlashCardsGame.KillPunct(s1));
		if (!ignoreVnDiacritics) {
			return s0a.equalsIgnoreCase(s1a);
		}
		final String s0b = FlashCardsGame.StripVNDiacritics(s0a);
		final String s1b = FlashCardsGame.StripVNDiacritics(s0b);
		return s0b.equalsIgnoreCase(s1b);
	}
}