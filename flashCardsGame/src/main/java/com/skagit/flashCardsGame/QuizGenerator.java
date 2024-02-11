package com.skagit.flashCardsGame;

import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

public class QuizGenerator {
	static enum TypeOfDecay {
		EXPONENTIAL, LINEAR, NO_DECAY
	}

	static enum TypeOfChange {
		NOTHING_TO_SOMETHING, RESTART, WIN, LOSS, PARAMETERS_CHANGED, NO_STATUS_CHANGE
	}

	private int _topIndexInCards;
	private final int _maxNNewWords;
	private final int _maxNRecentWords;
	private final int _nRepeatsOfNew;
	private final TypeOfDecay _typeOfDecay;
	private final int _failureRateI;
	private final int _percentageForRecentsI;
	private final long _shuffleCardsSeed;

	private final Random _r;

	private boolean _changedQuizGeneratorParameters;

	QuizGenerator(final Properties properties, final int nCards, final long seed) {
		_topIndexInCards = FlashCardsGame.keyToInt(properties, "Top.Card.Index", 1);
		_maxNNewWords = FlashCardsGame.keyToInt(properties, "Number.Of.New.Words", 1);
		_nRepeatsOfNew = FlashCardsGame.keyToInt(properties,
				"Number.Of.Times.To.Show.New.Words", 1);
		_maxNRecentWords = FlashCardsGame.keyToInt(properties, "Number.Of.Recent.Words", 3);
		_typeOfDecay = FlashCardsGame.keyToTypeOfDecay(properties, "Type.Of.Decay",
				TypeOfDecay.NO_DECAY);
		_failureRateI = FlashCardsGame.keyToPercentI(properties, "Failure.Rate", 50);
		_shuffleCardsSeed = FlashCardsGame.keyToLong(properties, "Shuffle.Cards.Seed", 0);
		if (_shuffleCardsSeed < 0) {
			/**
			 * For super-random, we don't allow the user to start anywhere except at the
			 * beginning.
			 */
			_topIndexInCards = Math.min(_topIndexInCards, _maxNNewWords + _maxNRecentWords - 1);
		}
		_percentageForRecentsI = FlashCardsGame.keyToPercentI(properties,
				"Percentage.For.Recents", 50);
		_r = new Random();
		_r.setSeed(seed);
		_changedQuizGeneratorParameters = false;
		correctQuizGeneratorProperties(nCards);
	}

	void updateProperties(final Properties properties) {
		properties.put("Top.Card.Index", Integer.toString(_topIndexInCards));
		properties.put("Number.Of.New.Words", Integer.toString(_maxNNewWords));
		properties.put("Number.Of.Times.To.Show.New.Words", Integer.toString(_nRepeatsOfNew));
		properties.put("Number.Of.Recent.Words", Integer.toString(_maxNRecentWords));
		properties.put("Type.Of.Decay", _typeOfDecay.name());
		properties.put("Failure.Rate", Integer.toString(_failureRateI) + '%');
		properties.put("Shuffle.Cards.Seed", Long.toString(_shuffleCardsSeed));
		properties.put("Percentage.For.Recents",
				Integer.toString(_percentageForRecentsI) + '%');
	}

	long[] getPropertyValues() {
		return new long[]{_topIndexInCards};
	}

	void correctQuizGeneratorProperties(final int nCards) {
		_topIndexInCards = Math.max(0, Math.min(_topIndexInCards, nCards - 1));
	}

	public void shuffleCards(final Card[] cards) {
		if (_shuffleCardsSeed == 0) {
			return;
		}
		final Random r = new Random();
		if (_shuffleCardsSeed > 0) {
			r.setSeed(_shuffleCardsSeed);
			while (Math.abs(r.nextLong()) < Long.MAX_VALUE / 10) {
			}
		}
		final int nCards = cards.length;
		for (int k = 0; k < nCards; ++k) {
			final int kk = k + r.nextInt(nCards - k);
			final Card card = cards[k];
			cards[k] = cards[kk];
			cards[kk] = card;
		}
	}

	QuizPlus createNewQuizPlus(final int nCards) {
		correctQuizGeneratorProperties(nCards);
		final int nAvail = _topIndexInCards + 1;
		final int nNewWordsInQuiz = Math.min(nAvail - 1, _maxNNewWords);
		final int nRecentWordsInQuiz = Math.min(_maxNRecentWords, nAvail - nNewWordsInQuiz);
		final int nDifferentWordsInQuiz = nNewWordsInQuiz + nRecentWordsInQuiz;
		final int array1Len = nNewWordsInQuiz * _nRepeatsOfNew;
		final int[] array1 = new int[array1Len];
		final int[] temp = new int[_nRepeatsOfNew];
		for (int k0 = 0; k0 < nNewWordsInQuiz; ++k0) {
			Arrays.fill(temp, _topIndexInCards - k0);
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
		return new QuizPlus(quiz, criticalQuizIndices);
	}

	private int[] createArray2(final int nNewWords, final int nRecentWords) {
		final int arrayLen = nNewWords + nRecentWords;
		final int[] array = new int[arrayLen];
		/** Fill in one copy of each of the new words. */
		for (int k = 0; k < nNewWords; ++k) {
			array[k] = _topIndexInCards - k;
		}
		/**
		 * If there are more to fill in than we have number of choices for, or we are not
		 * using exponential or linear draws, then cycle through the choices.
		 */
		final int topChoiceForSlots = _topIndexInCards - nNewWords;
		final int nChoicesForSlots = topChoiceForSlots + 1;
		if (nRecentWords >= nChoicesForSlots || _typeOfDecay == TypeOfDecay.NO_DECAY) {
			for (int k = 0; k < nRecentWords; ++k) {
				array[nNewWords + k] = topChoiceForSlots - (k % nChoicesForSlots);
			}
		} else {
			final double[] cums = computeCums(nRecentWords, nChoicesForSlots);
			for (int k = 0; k < nRecentWords; ++k) {
				final double cum = _r.nextDouble();
				final int indexInCards = selectIndexInCards(cums, cum);
				array[nNewWords + k] = indexInCards;
				annihilateIndexInCards(cums, indexInCards);
			}
		}
		return array;
	}

	private static int selectIndexInCards(final double[] cums, final double cum) {
		if (cum <= cums[0]) {
			return 0;
		} else if (cum >= cums[cums.length - 1]) {
			return cums.length - 1;
		}
		final int indexInCards = Arrays.binarySearch(cums, cum);
		if (indexInCards >= 0) {
			return indexInCards;
		}
		return -indexInCards - 1;
	}

	private static void annihilateIndexInCards(final double[] cums,
			final int indexInCards) {
		final int nCums = cums.length;
		final double probToDistribute = cums[indexInCards]
				- (indexInCards == 0 ? 0d : cums[indexInCards - 1]);
		final double scale = 1d / (1d - probToDistribute);
		/** Convert cums to probs. */
		for (int k = nCums - 1; k > 0; --k) {
			if (k == indexInCards) {
				cums[k] = 0d;
			} else {
				cums[k] = scale * (cums[k] - cums[k - 1]);
			}
		}
		/** Convert cums from probs back to cums. */
		for (int k = 1; k < nCums; ++k) {
			cums[k] += cums[k - 1];
		}
	}

	/** Creates cums, using exponential or linear decay. */
	private double[] computeCums(final int nRecentWords, final int nChoicesForSlots) {
		final double[] vector = new double[nChoicesForSlots];
		final double proportionForDregs = 1d - _percentageForRecentsI / 100d;
		final int nDregs = nChoicesForSlots - nRecentWords;
		final double pForDregs;
		if (_typeOfDecay == TypeOfDecay.EXPONENTIAL) {
			pForDregs = computePForExponential(nDregs, proportionForDregs);
		} else if (_typeOfDecay == TypeOfDecay.LINEAR) {
			pForDregs = computePForLinear(nDregs, proportionForDregs);
		} else {
			return null;
		}

		for (int k = 0; k < nDregs; ++k) {
			final double p;
			if (_typeOfDecay == TypeOfDecay.EXPONENTIAL) {
				p = Math.pow(pForDregs, nDregs - k);
			} else if (_typeOfDecay == TypeOfDecay.LINEAR) {
				p = (k + 1d) * pForDregs;
			} else {
				return null;
			}
			vector[k] = p;
		}
		final double proportionForRecentWords = 1d - proportionForDregs;
		final double pForRecentWords;
		if (_typeOfDecay == TypeOfDecay.EXPONENTIAL) {
			pForRecentWords = computePForExponential(nRecentWords, proportionForRecentWords);
		} else if (_typeOfDecay == TypeOfDecay.LINEAR) {
			pForRecentWords = computePForLinear(nRecentWords, proportionForRecentWords);
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
			vector[nDregs + k] = p;
		}
		/** Convert vector from probs to cums. */
		for (int k = 1; k < nChoicesForSlots; ++k) {
			vector[k] += vector[k - 1];
		}
		/** Normalize; should be unnecessary. */
		final double f = vector[nChoicesForSlots - 1];
		for (int k = 0; k < nChoicesForSlots; ++k) {
			vector[k] /= f;
		}
		vector[nChoicesForSlots - 1] = 1d;
		return vector;
	}

	private static double computePForExponential(final int n, final double targetSum) {
		if (targetSum < 0d) {
			return Double.NaN;
		}
		if (n <= 0) {
			return targetSum == 0d ? 0d : Double.NaN;
		}
		if (n == 1) {
			return targetSum;
		}
		if (targetSum == 0d) {
			return 0d;
		}
		double tooLow = 0d;
		/** Since n >= 2, target is certainly too high. */
		double tooHigh = targetSum;
		while (tooHigh > tooLow * (1d + 1.e-10)) {
			final double thisP = (tooLow + tooHigh) / 2d;
			double v = (Math.pow(thisP, n + 1) - thisP) / (thisP - 1d);
			if (!Double.isFinite(v)) {
				/** thisP is too close to 1. So the computation of v is simple. */
				v = n;
			}
			if (v > targetSum) {
				tooHigh = thisP;
			} else {
				tooLow = thisP;
			}
		}
		return (tooLow + tooHigh) / 2d;
	}

	private static double computePForLinear(final int n, final double targetProportion) {
		if (n <= 0) {
			return targetProportion == 0d ? 0d : Double.NaN;
		}
		/**
		 * <pre>
		 * n(n + 1)/2 * p = target
		 * p = target * 2/(n(n + 1))
		 * </pre>
		 */
		return targetProportion * 2d / (n * (n + 1d));
	}

	String getTypeIIPrompt() {
		return String.format("\n\tTCI(Top Card Index<%d>)", _topIndexInCards);
	}

	void modifySingleProperty(final String[] fields) {
		final String field0 = fields[0].toUpperCase();
		if (field0.equalsIgnoreCase("TCI")) {
			final int oldTopIndexInCards = _topIndexInCards;
			_topIndexInCards = FlashCardsGame.fieldsToInt(fields, _topIndexInCards);
			_changedQuizGeneratorParameters = oldTopIndexInCards != _topIndexInCards;
		}
	}

	QuizPlusStatusChange getStatusChange(final int nCards, final boolean restarted,
			final QuizPlus quizPlus) {
		if (restarted) {
			final QuizPlus newQuizPlus = quizPlus;
			newQuizPlus.resetForFullMode();
			return new QuizPlusStatusChange(quizPlus, newQuizPlus, TypeOfChange.RESTART);
		}
		if (quizPlus == null) {
			final QuizPlus newQuizPlus = createNewQuizPlus(nCards);
			return new QuizPlusStatusChange(quizPlus, newQuizPlus,
					TypeOfChange.NOTHING_TO_SOMETHING);
		}
		if (_changedQuizGeneratorParameters) {
			final QuizPlus newQuizPlus = createNewQuizPlus(nCards);
			_changedQuizGeneratorParameters = false;
			return new QuizPlusStatusChange(quizPlus, newQuizPlus,
					TypeOfChange.PARAMETERS_CHANGED);
		}
		if (quizPlus.haveWon(_failureRateI)) {
			final QuizPlus newQuizPlus;
			if (!quizPlus._criticalQuizIndicesOnly) {
				_topIndexInCards += _maxNNewWords;
				correctQuizGeneratorProperties(nCards);
				newQuizPlus = createNewQuizPlus(nCards);
			} else {
				newQuizPlus = new QuizPlus(quizPlus);
				newQuizPlus.resetForFullMode();
			}
			return new QuizPlusStatusChange(quizPlus, newQuizPlus, TypeOfChange.WIN);
		}
		if (quizPlus.haveLost(_failureRateI)) {
			final QuizPlus newQuizPlus = new QuizPlus(quizPlus);
			newQuizPlus.adjustQuizForLoss(_r);
			return new QuizPlusStatusChange(quizPlus, newQuizPlus, TypeOfChange.LOSS);
		}
		return new QuizPlusStatusChange(quizPlus, quizPlus, TypeOfChange.NO_STATUS_CHANGE);
	}

	String getString() {
		String s = String.format(//
				"TopIIC=%d, #New/Recent Words=%d/%d, Failure=%d%%, Decay=%s", //
				_topIndexInCards, _maxNNewWords, _maxNRecentWords, _failureRateI,
				_typeOfDecay.name());
		if (_shuffleCardsSeed < 0) {
			s += ", Random.Shuffle.";
		} else if (_shuffleCardsSeed > 0) {
			s += String.format(", Card.Shuffle.Seed=%d.", _shuffleCardsSeed);
		} else {
			s += '.';
		}
		return s;
	}

	@Override
	public String toString() {
		return getString();
	}

}
