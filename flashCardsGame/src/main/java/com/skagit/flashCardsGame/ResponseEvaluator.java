package com.skagit.flashCardsGame;

import java.util.Scanner;

import com.skagit.flashCardsGame.enums.DiacriticsTreatment;
import com.skagit.flashCardsGame.enums.MatchType;
import com.skagit.flashCardsGame.enums.QuizDirection;

public class ResponseEvaluator {
	final boolean _gotItRight;
	final String _diffString;

	ResponseEvaluator(final Scanner sc, final DiacriticsTreatment diacriticsTreatment,
			final Card card, final QuizDirection quizDirection, final String rawResponse,
			final int maxLenForQuizQuestion) {

		final String rawAnswer = quizDirection == QuizDirection.A_TO_B
				? card._fullBSide
				: card._fullASide;
		final MatchType matchType = getMatchType(rawAnswer, rawResponse);
		if (matchType == MatchType.MATCHED_INCLUDING_DIACRITICS
				|| (diacriticsTreatment == DiacriticsTreatment.RELAXED
						&& matchType == MatchType.MATCHED_EXCEPT_DIACRITICS_ARE_WRONG)) {
			_gotItRight = true;
			_diffString = null;
			return;
		}

		/**
		 * We didn't get it exactly right. See if we got it "right enough" and then fill
		 * _diffString with the answer plus up to two differences.
		 */
		_gotItRight = diacriticsTreatment == DiacriticsTreatment.LENIENT
				&& matchType == MatchType.MATCHED_EXCEPT_DIACRITICS_ARE_WRONG;

		final String[] answerFields = rawAnswer.split(FlashCardsGame._WhiteSpace);
		final String[] responseFields = rawResponse.split(FlashCardsGame._WhiteSpace);
		final int nAnswerFields = answerFields.length;
		final int nResponseFields = responseFields.length;

		String diffString = "";
		if (_gotItRight) {
			diffString += FlashCardsGame._HeavyCheckChar + " ";
		}
		diffString += card.getBrokenUpString(quizDirection == QuizDirection.B_TO_A,
				maxLenForQuizQuestion);

		final int nSmaller = Math.min(nAnswerFields, nResponseFields);
		int nDiffs = 0;
		for (int k = 0; k < nSmaller; ++k) {
			final String answerField = answerFields[k];
			final String responseField = responseFields[k];
			final boolean thisIsADiff;
			final MatchType typeOfMatch1 = getMatchType(answerField, responseField);
			if (_gotItRight) {
				/** Got it right; ANY diff is interesting. */
				thisIsADiff = typeOfMatch1 != MatchType.MATCHED_INCLUDING_DIACRITICS;
			} else {
				switch (typeOfMatch1) {
					case MATCHED_INCLUDING_DIACRITICS :
						thisIsADiff = false;
						break;
					case MATCHED_EXCEPT_DIACRITICS_ARE_WRONG :
						/**
						 * Since we got it wrong, a LENIENT requirement will show something as
						 * NOT_MATCHED_EVEN_WHEN_IGNORING_DIACRITICS. Hence, we only count this as a
						 * diff if we are STRICT.
						 */
						thisIsADiff = diacriticsTreatment == DiacriticsTreatment.STRICT;
						break;
					case NOT_MATCHED_EVEN_WHEN_IGNORING_DIACRITICS :
					default :
						thisIsADiff = true;
						break;
				}
			}
			if (thisIsADiff) {
				diffString += String.format(" (%s/%s)", answerField, responseField);
				if (++nDiffs == 2) {
					_diffString = diffString;
					return;
				}
			}
		}
		for (int k = nAnswerFields; k < nResponseFields; ++k) {
			diffString += String.format(" (null/%s)", responseFields[k]);
			if (++nDiffs == 2) {
				_diffString = diffString;
				return;
			}
		}
		for (int k = nResponseFields; k < nAnswerFields; ++k) {
			diffString += String.format(" (%s/null)", answerFields[k]);
			if (++nDiffs == 2) {
				_diffString = diffString;
				return;
			}
		}
		_diffString = diffString;
	}

	private static MatchType getMatchType(final String s0, final String s1) {
		final String s0a = FlashCardsGame.CleanWhiteSpace(FlashCardsGame.KillPunct(s0));
		final String s1a = FlashCardsGame.CleanWhiteSpace(FlashCardsGame.KillPunct(s1));
		if (s0a.equalsIgnoreCase(s1a)) {
			return MatchType.MATCHED_INCLUDING_DIACRITICS;
		}
		final String s0b = FlashCardsGame.StripVNDiacritics(s0a);
		final String s1b = FlashCardsGame.StripVNDiacritics(s0b);
		if (s0b.equalsIgnoreCase(s1b)) {
			return MatchType.MATCHED_EXCEPT_DIACRITICS_ARE_WRONG;
		}
		return MatchType.NOT_MATCHED_EVEN_WHEN_IGNORING_DIACRITICS;
	}
}