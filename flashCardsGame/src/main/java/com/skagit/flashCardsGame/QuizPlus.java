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
	final private int[] _fullQuiz;
	final private int[] _origFullQuiz;
	final private int[] _criticalIndicesInQuiz;
	private int _currentQuizIdx;
	private int _nWrongs, _nRights;
	boolean _criticalQuizIndicesOnly;
	final private HashMap<Integer, NWrongsAndHits> _indexInCardsToNWrongsAndHits;
	private int _firstWrongIndexInCards;

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

	QuizPlus(final int[] fullQuiz, final int[] criticalQuizIndices) {
		_fullQuiz = fullQuiz;
		_origFullQuiz = _fullQuiz.clone();
		_criticalIndicesInQuiz = criticalQuizIndices;
		Arrays.sort(_criticalIndicesInQuiz);
		_indexInCardsToNWrongsAndHits = new HashMap<>();
		resetForFullMode();
	}

	QuizPlus(final QuizPlus quizPlus) {
		_fullQuiz = quizPlus._fullQuiz.clone();
		_origFullQuiz = quizPlus._origFullQuiz.clone();
		_criticalIndicesInQuiz = quizPlus._criticalIndicesInQuiz.clone();
		_currentQuizIdx = quizPlus._currentQuizIdx;
		_nWrongs = quizPlus._nWrongs;
		_nRights = quizPlus._nRights;
		_firstWrongIndexInCards = quizPlus._firstWrongIndexInCards;
		_indexInCardsToNWrongsAndHits = new HashMap<>();
		final Map<Integer, NWrongsAndHits> from = quizPlus._indexInCardsToNWrongsAndHits;
		final Map<Integer, NWrongsAndHits> to = _indexInCardsToNWrongsAndHits;
		final Iterator<Map.Entry<Integer, NWrongsAndHits>> it = from.entrySet().iterator();
		while (it.hasNext()) {
			final Map.Entry<Integer, NWrongsAndHits> entry = it.next();
			final int indexInCards = entry.getKey();
			to.put(indexInCards, entry.getValue().clone());
		}
		_criticalQuizIndicesOnly = quizPlus._criticalQuizIndicesOnly;
	}

	int getCurrentQuiz_CardIndex(final int k) {
		return _fullQuiz[_criticalQuizIndicesOnly ? _criticalIndicesInQuiz[k] : k];
	}

	int getCurrentQuizLen() {
		return _criticalQuizIndicesOnly ? _criticalIndicesInQuiz.length : _fullQuiz.length;
	}

	int getCurrentQuiz_CardIndex() {
		return getCurrentQuiz_CardIndex(_currentQuizIdx);
	}

	boolean isCriticalQuizIndex(final int currentQuizIndex) {
		if (_criticalQuizIndicesOnly) {
			return true;
		}
		return Arrays.binarySearch(_criticalIndicesInQuiz, currentQuizIndex) >= 0;
	}

	int getCurrentIdxInQuiz() {
		return _currentQuizIdx;
	}

	int getNRights() {
		return _nRights;
	}

	int getNWrongs() {
		return _nWrongs;
	}

	void reactToRightResponse(final boolean wasWrongAtLeastOnce) {
		if (isCriticalQuizIndex(_currentQuizIdx)) {
			final int indexInCards = getCurrentQuiz_CardIndex(_currentQuizIdx);
			final NWrongsAndHits nWrongsAndHits = _indexInCardsToNWrongsAndHits
					.get(indexInCards);
			++nWrongsAndHits._nHits;
			if (wasWrongAtLeastOnce) {
				++nWrongsAndHits._nWrongs;
				if (_nWrongs == 0) {
					_firstWrongIndexInCards = indexInCards;
				}
				++_nWrongs;
			} else {
				++_nRights;
			}
		}
		++_currentQuizIdx;
	}

	void resetForFullMode() {
		/** Restore the full quiz. */
		System.arraycopy(_origFullQuiz, 0, _fullQuiz, 0, _origFullQuiz.length);
		_indexInCardsToNWrongsAndHits.clear();
		/** Temporarily set _criticalQuizIndicesOnly to true, to re-set nW_And_O. */
		_criticalQuizIndicesOnly = true;
		final int nCriticalQuizIndices = getCurrentQuizLen();
		for (int k = 0; k < nCriticalQuizIndices; ++k) {
			final int indexInCards = getCurrentQuiz_CardIndex(k);
			_indexInCardsToNWrongsAndHits.put(indexInCards, new NWrongsAndHits(0, 0));
		}
		/** Put it in FullMode. */
		_criticalQuizIndicesOnly = false;
		_currentQuizIdx = 0;
		_nWrongs = _nRights = 0;
		_firstWrongIndexInCards = -1;
	}

	boolean haveWon(final int failurePerCent) {
		/**
		 * We need a wrong rate that is <= failurePerCent / 2 even assuming that we'll get the
		 * rest of the critical quiz indices wrong.
		 */
		final int nCriticalQuizIndices = _criticalIndicesInQuiz.length;
		final int nWrongs = nCriticalQuizIndices - _nRights;
		return 200 * nWrongs <= failurePerCent * nCriticalQuizIndices;
	}

	boolean haveLost(final int failurePerCent) {
		/**
		 * We need a wrong rate that is >= failurePerCent even assuming that we'll get the
		 * rest of the critical quiz indices right.
		 */
		final int nCriticalQuizIndices = _criticalIndicesInQuiz.length;
		return 100 * _nWrongs >= failurePerCent * nCriticalQuizIndices;
	}

	void adjustQuizForLoss(final Random r) {
		int indexInCardsToReplace = -1;
		double smallestRatio = Double.POSITIVE_INFINITY;
		int mostHitsWithSmallestRatio = 0;
		final Iterator<Map.Entry<Integer, NWrongsAndHits>> it = _indexInCardsToNWrongsAndHits
				.entrySet().iterator();
		for (; it.hasNext();) {
			final Map.Entry<Integer, NWrongsAndHits> entry = it.next();
			final int indexInCards = entry.getKey();
			final NWrongsAndHits thisNWrongsAndHits = entry.getValue();
			final double thisRatio = ((double) thisNWrongsAndHits._nWrongs)
					/ thisNWrongsAndHits._nHits;
			boolean newLoser = thisRatio < smallestRatio;
			if (!newLoser && thisRatio == smallestRatio) {
				newLoser = thisNWrongsAndHits._nHits > mostHitsWithSmallestRatio;
			}
			if (newLoser) {
				indexInCardsToReplace = indexInCards;
				smallestRatio = thisRatio;
				mostHitsWithSmallestRatio = thisNWrongsAndHits._nHits;
				continue;
			}
		}
		/**
		 * Extract one occurrence of the first indexInCards that was wrong, with the
		 * indexInCards that has the lowest ratio of nWrongs to nHits and, among those that
		 * have the same ratio, the one that has the most hits.
		 */
		final int nCriticalQuizIndices = _criticalIndicesInQuiz.length;
		final int[] newIndicesInCards = new int[nCriticalQuizIndices];
		for (int k = 0; k < nCriticalQuizIndices; ++k) {
			final int indexInCards = _fullQuiz[_criticalIndicesInQuiz[k]];
			if (_firstWrongIndexInCards >= 0 && indexInCards == indexInCardsToReplace) {
				newIndicesInCards[k] = _firstWrongIndexInCards;
				_firstWrongIndexInCards = -1;
			} else {
				newIndicesInCards[k] = indexInCards;
			}
		}
		/** Shuffle newIndicesInCards and put them back into the criticalQuizIndices. */
		FlashCardsGame.shuffleArray(newIndicesInCards, r, /* lastValue= */-1);
		for (int k = 0; k < nCriticalQuizIndices; ++k) {
			_fullQuiz[_criticalIndicesInQuiz[k]] = newIndicesInCards[k];
		}
		_currentQuizIdx = 0;
		_nWrongs = _nRights = 0;
		_criticalQuizIndicesOnly = true;
	}

	final int getTopIndexInCardsInCurrentQuiz() {
		final int nInQuiz = getCurrentQuizLen();
		int topIndexInCards = -1;
		for (int k = 0; k < nInQuiz; ++k) {
			topIndexInCards = Math.max(topIndexInCards, getCurrentQuiz_CardIndex(k));
		}
		return topIndexInCards;
	}

	String getSummaryString() {
		int lo = Integer.MAX_VALUE, hi = Integer.MIN_VALUE;
		final TreeMap<Integer, int[]> mapofNonCriticals = new TreeMap<>();
		final TreeMap<Integer, int[]> mapOfCriticals = new TreeMap<>();
		final int quizLen = getCurrentQuizLen();
		for (int k = 0; k < quizLen; ++k) {
			final int indexInCards = getCurrentQuiz_CardIndex(k);
			lo = Math.min(lo, indexInCards);
			hi = Math.max(hi, indexInCards);
		}
		for (int k = 0; k < quizLen; ++k) {
			final TreeMap<Integer, int[]> map;
			if (isCriticalQuizIndex(k)) {
				map = mapOfCriticals;
			} else {
				map = mapofNonCriticals;
			}
			final int indexInCards = getCurrentQuiz_CardIndex(k);
			final int[] count = map.get(indexInCards);
			if (count == null) {
				map.put(indexInCards, new int[]{1});
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
			if (mapString.indexOf(FlashCardsGame._EmptySetChar) >= 0) {
				s += mapString;
			} else {
				// s += String.format("(%s)", mapString);
				s += mapString;
			}
		}
		s += "}";
		return s;
	}

	private static final String mapToString(final TreeMap<Integer, int[]> map) {
		final int nEntries = map.size();
		if (nEntries == 0) {
			return "" + FlashCardsGame._EmptySetChar;
		}
		final int[][] indexInCardsAndCount = new int[nEntries][];
		final Iterator<Map.Entry<Integer, int[]>> it = map.entrySet().iterator();
		for (int k = 0; k < nEntries; ++k) {
			final Map.Entry<Integer, int[]> entry = it.next();
			indexInCardsAndCount[k] = new int[]{entry.getKey(), entry.getValue()[0]};
		}
		Arrays.sort(indexInCardsAndCount, new Comparator<int[]>() {

			@Override
			public int compare(final int[] entry0, final int[] entry1) {
				final int indexInCards0 = entry0[0];
				final int indexInCards1 = entry1[1];
				return indexInCards0 < indexInCards1
						? -1
						: (indexInCards0 > indexInCards1 ? 1 : 0);
			}
		});
		/** Initialize the first streak. */
		final int[] entry0 = indexInCardsAndCount[0];
		int firstMemberOfStreak, lastMemberOfStreak;
		firstMemberOfStreak = lastMemberOfStreak = entry0[0];
		int countOfStreak = entry0[1];
		String s = "";
		for (int k = 1; k < nEntries; ++k) {
			final int[] entry = indexInCardsAndCount[k];
			final int indexInCards = entry[0];
			final int count = entry[1];
			final boolean wrapUpOldStreak = indexInCards != (lastMemberOfStreak + 1)
					|| count != countOfStreak;
			if (wrapUpOldStreak) {
				if (s.length() > 0) {
					s += ",";
				}
				s += getStreakString(countOfStreak, firstMemberOfStreak, lastMemberOfStreak);
				/** Start a new streak. */
				firstMemberOfStreak = indexInCards;
				countOfStreak = count;
			}
			lastMemberOfStreak = indexInCards;
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
		return String.format("%d%c%d%s", firstMemberOfStreak, FlashCardsGame._RtArrowChar,
				lastMemberOfStreak, countString);
	}

	public String getString() {
		return getSummaryString();
	}

	@Override
	public String toString() {
		return getString();
	}

}
