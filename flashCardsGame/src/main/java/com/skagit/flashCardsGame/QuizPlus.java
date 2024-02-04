package com.skagit.flashCardsGame;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

class QuizPlus implements Serializable {
	private static final long serialVersionUID = 1L;
	final int[] _fullQuiz;
	final int[] _origFullQuiz;
	private final int[] _criticalQuizIndices;
	private final int _failureRateI;
	private int _currentQuizIndex;
	private int _nWrongs, _nRights;
	boolean _criticalQuizIndicesOnly;
	private final HashMap<Integer, NWrongsAndHits> _cardIndexToNWrongsAndHits;
	private int _firstWrongCardIndex;

	static class NWrongsAndHits implements Cloneable {
		int _nWrongs = 0;
		int _nHits = 0;

		NWrongsAndHits(final int nWrongs, final int nHits) {
			_nWrongs = nWrongs;
			_nHits = nHits;
		}

		@Override
		protected NWrongsAndHits clone() {
			return new NWrongsAndHits(_nWrongs, _nHits);
		}
	}

	QuizPlus(final int[] fullQuiz, final int[] criticalQuizIndices,
			final int failureRateI) {
		_fullQuiz = fullQuiz;
		_origFullQuiz = _fullQuiz.clone();
		_criticalQuizIndices = criticalQuizIndices;
		Arrays.sort(_criticalQuizIndices);
		_failureRateI = Math.max(0, failureRateI);
		_cardIndexToNWrongsAndHits = new HashMap<>();
		resetForFullMode();
	}

	QuizPlus(final QuizPlus quizPlus) {
		_fullQuiz = quizPlus._fullQuiz.clone();
		_origFullQuiz = quizPlus._origFullQuiz.clone();
		_criticalQuizIndices = quizPlus._criticalQuizIndices.clone();
		_failureRateI = quizPlus._failureRateI;
		_currentQuizIndex = quizPlus._currentQuizIndex;
		_nWrongs = quizPlus._nWrongs;
		_nRights = quizPlus._nRights;
		_firstWrongCardIndex = quizPlus._firstWrongCardIndex;
		_cardIndexToNWrongsAndHits = new HashMap<>();
		final Map<Integer, NWrongsAndHits> from = quizPlus._cardIndexToNWrongsAndHits;
		final Map<Integer, NWrongsAndHits> to = _cardIndexToNWrongsAndHits;
		final Iterator<Map.Entry<Integer, NWrongsAndHits>> it = from.entrySet().iterator();
		while (it.hasNext()) {
			final Map.Entry<Integer, NWrongsAndHits> entry = it.next();
			final int cardIndex = entry.getKey();
			to.put(cardIndex, entry.getValue().clone());
		}
		_criticalQuizIndicesOnly = quizPlus._criticalQuizIndicesOnly;
	}

	int getCurrentQuizCardIndex(final int k) {
		return _fullQuiz[_criticalQuizIndicesOnly ? _criticalQuizIndices[k] : k];
	}

	int getCurrentQuizLen() {
		return _criticalQuizIndicesOnly ? _criticalQuizIndices.length : _fullQuiz.length;
	}

	int getCurrentQuizIndex() {
		return _currentQuizIndex;
	}

	public int getCurrentQuizCardIndex() {
		return getCurrentQuizCardIndex(_currentQuizIndex);
	}

	boolean isCriticalQuizIndex(final int currentQuizIndex) {
		if (_criticalQuizIndicesOnly) {
			return true;
		}
		return Arrays.binarySearch(_criticalQuizIndices, currentQuizIndex) >= 0;
	}

	void reactToRightResponse(final boolean wasWrongAtLeastOnce) {
		if (isCriticalQuizIndex(_currentQuizIndex)) {
			final int cardIndex = getCurrentQuizCardIndex(_currentQuizIndex);
			final NWrongsAndHits nWrongsAndHits = _cardIndexToNWrongsAndHits.get(cardIndex);
			++nWrongsAndHits._nHits;
			if (wasWrongAtLeastOnce) {
				++nWrongsAndHits._nWrongs;
				if (_nWrongs == 0) {
					_firstWrongCardIndex = cardIndex;
				}
				++_nWrongs;
			} else {
				++_nRights;
			}
		}
		++_currentQuizIndex;
	}

	void resetForFullMode() {
		/** Restore the full quiz. */
		System.arraycopy(_origFullQuiz, 0, _fullQuiz, 0, _origFullQuiz.length);
		_cardIndexToNWrongsAndHits.clear();
		/** Temporarily set _criticalQuizIndicesOnly to true, to re-set nW_And_O. */
		_criticalQuizIndicesOnly = true;
		final int nCriticalQuizIndices = getCurrentQuizLen();
		for (int k = 0; k < nCriticalQuizIndices; ++k) {
			final int cardIndex = getCurrentQuizCardIndex(k);
			_cardIndexToNWrongsAndHits.put(cardIndex, new NWrongsAndHits(0, 0));
		}
		/** Put it in FullMode. */
		_criticalQuizIndicesOnly = false;
		_currentQuizIndex = 0;
		_nWrongs = _nRights = 0;
		_firstWrongCardIndex = -1;
	}

	boolean haveWon() {
		/**
		 * We need a wrong rate that is <= failureRateI / 2 even assuming that we'll get the
		 * rest of the critical quiz indices wrong.
		 */
		final int nCriticalQuizIndices = _criticalQuizIndices.length;
		final int nWrongs = nCriticalQuizIndices - _nRights;
		return 200 * nWrongs <= _failureRateI * nCriticalQuizIndices;
	}

	boolean haveLost() {
		/**
		 * We need a wrong rate that is >= failureRateI even assuming that we'll get the rest
		 * of the critical quiz indices right.
		 */
		final int nCriticalQuizIndices = _criticalQuizIndices.length;
		return 100 * _nWrongs >= _failureRateI * nCriticalQuizIndices;
	}

	void adjustQuizForLoss(final Random r) {
		int cardIndexToReplace = -1;
		double smallestRatio = Double.POSITIVE_INFINITY;
		int mostHitsWithSmallestRatio = 0;
		final Iterator<Map.Entry<Integer, NWrongsAndHits>> it = _cardIndexToNWrongsAndHits
				.entrySet().iterator();
		for (; it.hasNext();) {
			final Map.Entry<Integer, NWrongsAndHits> entry = it.next();
			final int cardIndex = entry.getKey();
			final NWrongsAndHits thisNWrongsAndHits = entry.getValue();
			final double thisRatio = ((double) thisNWrongsAndHits._nWrongs)
					/ thisNWrongsAndHits._nHits;
			boolean newLoser = thisRatio < smallestRatio;
			if (!newLoser && thisRatio == smallestRatio) {
				newLoser = thisNWrongsAndHits._nHits > mostHitsWithSmallestRatio;
			}
			if (newLoser) {
				cardIndexToReplace = cardIndex;
				smallestRatio = thisRatio;
				mostHitsWithSmallestRatio = thisNWrongsAndHits._nHits;
				continue;
			}
		}
		/**
		 * Extract one occurrence of the first cardIndex that was wrong, with the cardIndex
		 * that has the lowest ratio of nWrongs to nHits and, among those that have the same
		 * ratio, the one that has the most hits.
		 */
		final int nCriticalQuizIndices = _criticalQuizIndices.length;
		final int[] newCardIndices = new int[nCriticalQuizIndices];
		for (int k = 0; k < nCriticalQuizIndices; ++k) {
			final int cardIndex = _fullQuiz[_criticalQuizIndices[k]];
			if (_firstWrongCardIndex >= 0 && cardIndex == cardIndexToReplace) {
				newCardIndices[k] = _firstWrongCardIndex;
				_firstWrongCardIndex = -1;
			} else {
				newCardIndices[k] = cardIndex;
			}
		}
		/** Shuffle newCardIndices and put them back into the criticalQuizIndices. */
		FlashCardsGame.shuffleArray(newCardIndices, r, /* lastValue= */-1);
		for (int k = 0; k < nCriticalQuizIndices; ++k) {
			_fullQuiz[_criticalQuizIndices[k]] = newCardIndices[k];
		}
		_currentQuizIndex = 0;
		_nWrongs = _nRights = 0;
		_criticalQuizIndicesOnly = true;
	}

	final int getTopCardIndexInCurrentQuiz() {
		final int nInQuiz = getCurrentQuizLen();
		int topCardIndex = -1;
		for (int k = 0; k < nInQuiz; ++k) {
			topCardIndex = Math.max(topCardIndex, getCurrentQuizCardIndex(k));
		}
		return topCardIndex;
	}

	String getSummaryString() {
		int lo = Integer.MAX_VALUE, hi = Integer.MIN_VALUE;
		final TreeMap<Integer, int[]> mapofNonCriticals = new TreeMap<>();
		final TreeMap<Integer, int[]> mapOfCriticals = new TreeMap<>();
		final int quizLen = getCurrentQuizLen();
		for (int k = 0; k < quizLen; ++k) {
			final int cardIndex = getCurrentQuizCardIndex(k);
			lo = Math.min(lo, cardIndex);
			hi = Math.max(hi, cardIndex);
		}
		for (int k = 0; k < quizLen; ++k) {
			final TreeMap<Integer, int[]> map;
			if (isCriticalQuizIndex(k)) {
				map = mapOfCriticals;
			} else {
				map = mapofNonCriticals;
			}
			final int cardIndex = getCurrentQuizCardIndex(k);
			final int[] count = map.get(cardIndex);
			if (count == null) {
				map.put(cardIndex, new int[]{1});
			} else {
				++count[0];
			}
		}
		String s = "{";
		for (int iPass = 0; iPass < 2; ++iPass) {
			if (iPass == 1) {
				s += ":";
			}
			final TreeMap<Integer, int[]> map = iPass == 0 ? mapofNonCriticals : mapOfCriticals;
			final String mapString = mapToString(map);
			if (mapString.indexOf(FlashCardsGame._EmptySet) >= 0) {
				s += mapString;
			} else {
				s += String.format("(%s)", mapString);
			}
		}
		s += "}";
		return s;
	}

	private static final String mapToString(final TreeMap<Integer, int[]> map) {
		final int nEntries = map.size();
		if (nEntries == 0) {
			return "" + FlashCardsGame._EmptySet;
		}
		final int[][] cardIndexAndCount = new int[nEntries][];
		final Iterator<Map.Entry<Integer, int[]>> it = map.entrySet().iterator();
		for (int k = 0; k < nEntries; ++k) {
			final Map.Entry<Integer, int[]> entry = it.next();
			cardIndexAndCount[k] = new int[]{entry.getKey(), entry.getValue()[0]};
		}
		Arrays.sort(cardIndexAndCount, new Comparator<int[]>() {

			@Override
			public int compare(final int[] entry0, final int[] entry1) {
				final int cardIndex0 = entry0[0];
				final int cardIndex1 = entry1[1];
				return cardIndex0 < cardIndex1 ? -1 : (cardIndex0 > cardIndex1 ? 1 : 0);
			}
		});
		/** Initialize the first streak. */
		final int[] entry0 = cardIndexAndCount[0];
		int firstMemberOfStreak, lastMemberOfStreak;
		firstMemberOfStreak = lastMemberOfStreak = entry0[0];
		int countOfStreak = entry0[1];
		String s = "";
		for (int k = 1; k < nEntries; ++k) {
			final int[] entry = cardIndexAndCount[k];
			final int cardIndex = entry[0];
			final int count = entry[1];
			final boolean wrapUpOldStreak = cardIndex != (lastMemberOfStreak + 1)
					|| count != countOfStreak;
			if (wrapUpOldStreak) {
				if (s.length() > 0) {
					s += ",";
				}
				s += getStreakString(countOfStreak, firstMemberOfStreak, lastMemberOfStreak);
				/** Start a new streak. */
				firstMemberOfStreak = cardIndex;
				countOfStreak = count;
			}
			lastMemberOfStreak = cardIndex;
		}
		/** Must wrap up the old streak. */
		if (s.length() > 0) {
			s += ",";
		}
		s += getStreakString(countOfStreak, firstMemberOfStreak, lastMemberOfStreak);
		return s;
	}

	private static String getStreakString(final int countOfStreak,
			final int firstMemberOfStreak, final int lastMemberOfStreak) {
		final int nInStreak = lastMemberOfStreak - firstMemberOfStreak + 1;
		if (countOfStreak == 1 && nInStreak == 2) {
			return String.format("%d,%d", firstMemberOfStreak, lastMemberOfStreak);
		}
		final String countString = countOfStreak > 1
				? String.format("(%d)", countOfStreak)
				: "";
		if (nInStreak == 1) {
			return String.format("%d%s", firstMemberOfStreak, countString);
		}
		return String.format("%d%c%d%s", firstMemberOfStreak, FlashCardsGame._RtArrow,
				lastMemberOfStreak, countString);
	}

	public String getTypeIPrompt(final int cardIndex, final String clue) {
		String s = "";
		if (isCriticalQuizIndex(_currentQuizIndex)) {
			s += "*";
		}
		final int quizLen = getCurrentQuizLen();
		s += String.format("%d(Card #%d) of %d", _currentQuizIndex + 1, cardIndex, quizLen);
		final int nTrialsSoFar = _nRights + _nWrongs;
		if (nTrialsSoFar > 0) {
			final long successRateI = Math.round((100d * _nRights) / nTrialsSoFar);
			s += String.format(",(#Rt/Wr=%d/%d SccRt=%d%%). ", _nRights, _nWrongs,
					successRateI);
		} else {
			s += ". ";
		}
		return s + clue;
	}

	public String getString() {
		return getSummaryString();
	}

	@Override
	public String toString() {
		return getString();
	}

}
