package com.skagit.flashCardsGame;

import java.util.ArrayList;
import java.util.Scanner;

public class ResponseEvaluator {
	final boolean _gotItRight;
	final String _diffString;

	ResponseEvaluator(final Scanner sc, final boolean ignoreVnDiacritics, final Card card,
			final boolean quizIsAToB, final String firstResponse) {

		/** Gather the rest, if any of the response. */
		String rawResponse = firstResponse;
		for (;;) {
			System.out.print("\t");
			String line = sc.nextLine();
			line = line.replaceAll(FlashCardsGame._RegExForPunct, " ");
			line = FlashCardsGame.CleanWhiteSpace(line);
			if (line.length() == 0) {
				break;
			}
			rawResponse += " " + line;
		}
		final String rawAnswer = quizIsAToB ? card._fullBSide : card._fullASide;
		final String response0 = FlashCardsGame
				.CleanWhiteSpace(rawResponse.replaceAll(FlashCardsGame._RegExForPunct, " "));
		final String answer0 = FlashCardsGame
				.CleanWhiteSpace(rawAnswer.replaceAll(FlashCardsGame._RegExForPunct, " "));

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
			answer1 = answer0;
			response1 = response0;
			_gotItRight = false;
		}

		final String[] answer1Fields = answer1.split(FlashCardsGame._WhiteSpace);
		final String[] response1Fields = response1.split(FlashCardsGame._WhiteSpace);
		final int nAnswerFields = answer1Fields.length;
		final int nResponseFields = response1Fields.length;
		if (_gotItRight) {
			/** We must be ignoring diacritics, got it right, but not exactly right. */
			String s = "" + FlashCardsGame._HeavyCheckChar + " [" + FlashCardsGame._RtArrowChar;
			final ArrayList<String> rawAnswerParts = quizIsAToB ? card._bParts : card._aParts;
			final int nAnswerParts = rawAnswerParts.size();
			for (int k = 1; k < nAnswerParts; ++k) {
				s += "\n\t" + rawAnswerParts.get(k);
			}
			s += ']' + FlashCardsGame._LtArrowChar;
			for (int k = 0; k < nAnswerFields; ++k) {
				final String answer1Field = answer1Fields[k];
				final String response1Field = response1Fields[k];
				if (!answer1Field.equalsIgnoreCase(response1Field)) {
					final String[] rawAnswerFields = rawAnswer.split(FlashCardsGame._WhiteSpace);
					_diffString = String.format("%s (%s/%s)]", s, rawAnswerFields[k],
							response1Field);
					return;
				}
			}
			_diffString = null;
			return;
		}

		/** Got it wrong. Start with the answer and add one differences. */
		final String diffString = String.format("[%c%s%c", FlashCardsGame._RtArrowChar, answer0,
				FlashCardsGame._LtArrowChar);
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