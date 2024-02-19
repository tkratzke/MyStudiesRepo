package com.skagit.flashCardsGame;

import java.util.ArrayList;
import java.util.Scanner;

public class ResponseEvaluator {
	final boolean _gotItRight;
	final String _diffString;

	ResponseEvaluator(final Scanner sc, final boolean ignoreVnDiacritics, final Card card,
			final boolean quizIsAToB, final String rawResponse) {

		final String rawAnswer = quizIsAToB ? card._fullBSide : card._fullASide;
		final String response0 = //
				FlashCardsGame.CleanWhiteSpace( //
						rawResponse.replaceAll( //
								FlashCardsGame._RegExForPunct, " "));
		final String answer0 = FlashCardsGame.CleanWhiteSpace( //
				rawAnswer.replaceAll( //
						FlashCardsGame._RegExForPunct, " "));
		if (answer0.equalsIgnoreCase(response0)) {
			_gotItRight = true;
			_diffString = null;
			return;
		}

		final String answer1, response1;
		if (ignoreVnDiacritics) {
			answer1 = FlashCardsGame.StripVNDiacritics(answer0);
			response1 = FlashCardsGame.StripVNDiacritics(response0);
			_gotItRight = answer1.equalsIgnoreCase(response1);
		} else {
			answer1 = response1 = null;
			_gotItRight = false;
		}

		final String[] answer0Fields = answer0.split(FlashCardsGame._WhiteSpace);
		final String[] response0Fields = response0.split(FlashCardsGame._WhiteSpace);
		final int nAnswerFields = answer0Fields.length;
		final int nResponseFields = response0Fields.length;
		String s = "";
		if (_gotItRight) {
			s += FlashCardsGame._HeavyCheckChar;
		}
		s += " " + FlashCardsGame._RtArrowChar;
		final ArrayList<String> rawAnswerParts = quizIsAToB ? card._bParts : card._aParts;
		final int nAnswerParts = rawAnswerParts.size();
		for (int k = 0; k < nAnswerParts; ++k) {
			if (k > 0) {
				s += "\n\t";
			}
			s += rawAnswerParts.get(k);
		}
		s += "" + FlashCardsGame._LtArrowChar;

		if (_gotItRight) {
			/**
			 * We must be ignoring diacritics, got it right, but not exactly right. Any
			 * difference between answer0 and response0 will do.
			 */
			for (int k = 0; k < nAnswerFields; ++k) {
				final String answer0Field = answer0Fields[k];
				final String response0Field = response0Fields[k];
				if (!answer0Field.equalsIgnoreCase(response0Field)) {
					final String[] rawAnswerFields = rawAnswer.split(FlashCardsGame._WhiteSpace);
					_diffString = String.format("%s (%s/%s)", s, rawAnswerFields[k],
							response0Field);
					return;
				}
			}
			_diffString = null;
			return;
		}

		/** Got it wrong. Start with the answer and add one difference. */
		final int nSmaller = Math.min(nAnswerFields, nResponseFields);
		for (int k = 0; k < nSmaller; ++k) {
			final String answer1Field = answer0Fields[k];
			final String response1Field = response0Fields[k];
			final String answerField, responseField;
			if (ignoreVnDiacritics) {
				answerField = FlashCardsGame.StripVNDiacritics(answer1Field);
				responseField = FlashCardsGame.StripVNDiacritics(response1Field);
			} else {
				answerField = answer1Field;
				responseField = response1Field;
			}
			if (!answerField.equalsIgnoreCase(responseField)) {
				_diffString = s + String.format(" (%s/%s)", answer1Field, response1Field);
				return;
			}
		}
		if (nAnswerFields < nResponseFields) {
			_diffString = s + String.format(" (null/%s)", response0Fields[nAnswerFields]);
		} else {
			_diffString = s + String.format(" (%s/null)", answer0Fields[nResponseFields]);
		}
	}
}