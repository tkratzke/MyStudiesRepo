package com.skagit.flashCardsGame;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Random;

public class QuizGenerator {
	/**
	 * The following fields are "properties." Only _topCardIndex changes. _r is final and
	 * its seed is never reset.
	 */
	private int _topCardIndex;
	private final int _maxNNewWords;
	private final int _maxNRecentWords;
	private final int _nRepeatsOfNew;
	private final TypeOfDecay _typeOfDecay;
	private final int _failureRateI;

	private final Random _r;

	private boolean _adjustCurrentQuiz;

	QuizGenerator(final Properties properties, final int nCards, final long seed) {
		_topCardIndex = FlashCardsGame.keyToInt(properties, "Top.Card.Index", 1);
		_maxNNewWords = FlashCardsGame.keyToInt(properties, "Number.Of.New.Words", 1);
		_nRepeatsOfNew = FlashCardsGame.keyToInt(properties,
				"Number.Of.Times.To.Show.New.Words", 1);
		_maxNRecentWords = FlashCardsGame.keyToInt(properties, "Number.Of.Recent.Words", 3);
		_typeOfDecay = FlashCardsGame.keyToTypeOfDecay(properties, "Type.Of.Decay",
				TypeOfDecay.NONE);
		_failureRateI = FlashCardsGame.keyToPercentI(properties, "Failure.Rate", 25);
		_r = new Random();
		_r.setSeed(seed);
		_adjustCurrentQuiz = false;
		correctQuizGeneratorProperties(nCards);
	}

	void updateProperties(final Properties properties) {
		properties.put("Top.Card.Index", Integer.toString(_topCardIndex));
		properties.put("Number.Of.New.Words", Integer.toString(_maxNNewWords));
		properties.put("Number.Of.Times.To.Show.New.Words", Integer.toString(_nRepeatsOfNew));
		properties.put("Number.Of.Recent.Words", Integer.toString(_maxNRecentWords));
		properties.put("Type.Of.Decay", _typeOfDecay.name());
		properties.put("Failure.Rate", Integer.toString(_failureRateI) + '%');
	}

	long[] getPropertyValues() {
		return new long[]{_topCardIndex};
	}

	void correctQuizGeneratorProperties(final int nCards) {
		_topCardIndex = Math.max(0, Math.min(_topCardIndex, nCards - 1));
	}

	QuizPlus createNewQuizPlus(final int nCards) {
		correctQuizGeneratorProperties(nCards);
		final int nAvail = _topCardIndex + 1;
		final int nNewWordsInQuiz = Math.min(nAvail - 1, _maxNNewWords);
		final int nRecentWordsInQuiz = Math.min(_maxNRecentWords, nAvail - nNewWordsInQuiz);
		final int nDifferentWordsInQuiz = nNewWordsInQuiz + nRecentWordsInQuiz;
		final int array1Len = nNewWordsInQuiz * _nRepeatsOfNew;
		final int[] array1 = new int[array1Len];
		final int[] temp = new int[_nRepeatsOfNew];
		for (int k0 = 0; k0 < nNewWordsInQuiz; ++k0) {
			Arrays.fill(temp, _topCardIndex - k0);
			System.arraycopy(temp, 0, array1, k0 * _nRepeatsOfNew, _nRepeatsOfNew);
		}
		FlashCardsGame.shuffleArray(array1, _r, /* lastValue= */-1);
		final int[] array2 = createArray2(nNewWordsInQuiz, nRecentWordsInQuiz);
		final int lastValue = array1Len > 0 ? array1[array1Len - 1] : -1;
		FlashCardsGame.shuffleArray(array2, _r, lastValue);
		final int fullQuizLen = array1Len + nDifferentWordsInQuiz;
		final int[] quiz = new int[fullQuizLen];
		System.arraycopy(array1, 0, quiz, 0, array1Len);
		System.arraycopy(array2, 0, quiz, array1Len, nDifferentWordsInQuiz);
		final int[] criticalQuizIndices = new int[nDifferentWordsInQuiz];
		for (int k = 0; k < nDifferentWordsInQuiz; ++k) {
			criticalQuizIndices[k] = array1Len + k;
		}
		return new QuizPlus(quiz, criticalQuizIndices, _failureRateI);
	}

	private int[] createArray2(final int nNewWords, final int nRecentWords) {
		final int arrayLen = nNewWords + nRecentWords;
		final int[] array = new int[arrayLen];
		/** Fill in one copy of each of the new words. */
		for (int k = 0; k < nNewWords; ++k) {
			array[k] = _topCardIndex - k;
		}
		/**
		 * If there are more to fill in than we have number of choices for, or we are not
		 * using exponential draws, then cycle through the choices.
		 */
		final int topChoiceForSlots = _topCardIndex - nNewWords;
		final int nChoicesForSlots = topChoiceForSlots + 1;
		if (nRecentWords >= nChoicesForSlots || _typeOfDecay == TypeOfDecay.NONE) {
			for (int k = 0; k < nRecentWords; ++k) {
				array[nNewWords + k] = topChoiceForSlots - (k % nChoicesForSlots);
			}
		} else {
			final HashSet<Integer> alreadyUsed = new HashSet<>();
			final double[] vectorOfCums = computeVectorOfCums(nRecentWords, nChoicesForSlots);
			for (int k = 0; k < nRecentWords; ++k) {
				for (int kk = 0; kk <= 5; ++kk) {
					final double cum = _r.nextDouble();
					final int cardIndex = selectCardIndex(vectorOfCums, cum);
					if (kk == 5 || alreadyUsed.add(cardIndex)) {
						array[nNewWords + k] = cardIndex;
						break;
					}
				}
			}
		}
		return array;
	}

	private static int selectCardIndex(final double[] vectorOfCums, final double cum) {
		if (cum <= vectorOfCums[0]) {
			return 0;
		} else if (cum >= vectorOfCums[vectorOfCums.length - 1]) {
			return vectorOfCums.length - 1;
		}
		final int cardIndex = Arrays.binarySearch(vectorOfCums, cum);
		if (cardIndex >= 0) {
			return cardIndex;
		}
		return -cardIndex - 1;
	}

	static final double _ProportionForDregs = 0.5;

	/** Fills in a vector of cums, using exponential or linear decay. */
	private double[] computeVectorOfCums(final int nRecentWords,
			final int nChoicesForSlots) {
		final double[] vectorOfProbs = new double[nChoicesForSlots];
		final int nDregs = nChoicesForSlots - nRecentWords;
		final double pForDregs;
		if (_typeOfDecay == TypeOfDecay.EXPONENTIAL) {
			pForDregs = computePForExponential(nDregs, _ProportionForDregs);
		} else if (_typeOfDecay == TypeOfDecay.LINEAR) {
			pForDregs = computePForLinear(nDregs, _ProportionForDregs);
		} else {
			return null;
		}

		double sumOfProbs = 0d;
		for (int k = 0; k < nDregs; ++k) {
			final double p;
			if (_typeOfDecay == TypeOfDecay.EXPONENTIAL) {
				p = Math.pow(pForDregs, nDregs - k);
			} else if (_typeOfDecay == TypeOfDecay.LINEAR) {
				p = (k + 1d) * pForDregs;
			} else {
				return null;
			}
			vectorOfProbs[k] = p;
			sumOfProbs += p;
		}
		final double targetForRecentWords = 1d - sumOfProbs;
		final double pForRecentWords;
		if (_typeOfDecay == TypeOfDecay.EXPONENTIAL) {
			pForRecentWords = computePForExponential(nRecentWords, targetForRecentWords);
		} else if (_typeOfDecay == TypeOfDecay.LINEAR) {
			pForRecentWords = computePForLinear(nRecentWords, targetForRecentWords);
		} else {
			return null;
		}
		for (int k = 0; k < nRecentWords; ++k) {
			final double p;
			if (_typeOfDecay == TypeOfDecay.EXPONENTIAL) {
				p = Math.pow(pForRecentWords, nRecentWords - k);
			} else if (_typeOfDecay == TypeOfDecay.LINEAR) {
				p = (k + 1d) * pForRecentWords;
			} else {
				return null;
			}
			vectorOfProbs[nDregs + k] = p;
			sumOfProbs += p;
		}
		/**
		 * Make vectorOfCums from vectorOfProbs. Since we can do this in place, we'll just
		 * rename it.
		 */
		final double[] vectorOfCums = vectorOfProbs;
		for (int k = 1; k < nChoicesForSlots; ++k) {
			vectorOfCums[k] += vectorOfCums[k - 1];
		}
		/** Normalize the cums. */
		final double f = vectorOfCums[nChoicesForSlots - 1];
		for (int k = 0; k < nChoicesForSlots; ++k) {
			vectorOfCums[k] /= f;
		}
		vectorOfCums[nChoicesForSlots - 1] = 1d;
		return vectorOfCums;
	}

	private static double computePForExponential(final int n, final double target) {
		if (target < 0d) {
			return Double.NaN;
		}
		if (n <= 0) {
			return target == 0d ? 0d : Double.NaN;
		}
		if (n == 1) {
			return target;
		}
		if (target == 0d) {
			return 0d;
		}
		double tooLow = 0d;
		/** Since n >=2, target is certainly too high. */
		double tooHigh = target;
		while (tooHigh > tooLow * (1d + 1.e-10)) {
			final double thisP = (tooLow + tooHigh) / 2d;
			double v = (Math.pow(thisP, n + 1) - thisP) / (thisP - 1d);
			if (!Double.isFinite(v)) {
				/** thisP is too close to 1. So the computation of v is simple. */
				v = n;
			}
			if (v > target) {
				tooHigh = thisP;
			} else {
				tooLow = thisP;
			}
		}
		return (tooLow + tooHigh) / 2d;
	}

	private static double computePForLinear(final int n, final double target) {
		if (n <= 0) {
			return target == 0d ? 0d : Double.NaN;
		}
		/**
		 * <pre>
		 * n(n + 1)/2 * p = target
		 * p = target * 2/(n(n + 1))
		 * p = target * 2/(n*n + n))
		 * </pre>
		 */
		return target * (2d / (n * n + n));
	}

	int getFailureRateI() {
		return _failureRateI;
	}

	String getTypeIIPrompt() {
		return String.format("\n\tTCI(Top Card Index<%d>)", _topCardIndex);
	}

	void modifyFromFields(final String[] fields) {
		final String field0 = fields[0].toUpperCase();
		if (field0.equalsIgnoreCase("TCI")) {
			_topCardIndex = FlashCardsGame.fieldsToInt(fields, _topCardIndex);
			_adjustCurrentQuiz = true;
		}
	}

	QuizPlusTransition reactToQuizPlus(final int nCards, final QuizPlus quizPlus) {
		if (_adjustCurrentQuiz) {
			final QuizPlus newQuizPlus = createNewQuizPlus(nCards);
			return new QuizPlusTransition(quizPlus, newQuizPlus, TypeOfChange.ADJUST);
		}
		_adjustCurrentQuiz = false;
		if (quizPlus.haveWon()) {
			final QuizPlus newQuizPlus;
			if (!quizPlus._criticalQuizIndicesOnly) {
				_topCardIndex += _maxNNewWords;
				correctQuizGeneratorProperties(nCards);
				newQuizPlus = createNewQuizPlus(nCards);
			} else {
				newQuizPlus = new QuizPlus(quizPlus);
				newQuizPlus.resetForFullMode();
			}
			return new QuizPlusTransition(quizPlus, newQuizPlus, TypeOfChange.WIN);
		}
		if (quizPlus.haveLost()) {
			final QuizPlus newQuizPlus = new QuizPlus(quizPlus);
			newQuizPlus.adjustQuizForLoss(_r);
			return new QuizPlusTransition(quizPlus, newQuizPlus, TypeOfChange.LOSS);
		}
		return new QuizPlusTransition(quizPlus, quizPlus, TypeOfChange.NULL);
	}

	String getString() {
		final String s = String.format(//
				"TopCardIndex=%d, #New Words=%d, #Recent Words=%d, Failure Rate=%d%%, Type of Decay=%s", //
				_topCardIndex, _maxNNewWords, _maxNRecentWords, _failureRateI,
				_typeOfDecay.name());
		return s;
	}

	@Override
	public String toString() {
		return getString();
	}

}
