package com.skagit.fun.stephenPool;

import java.util.ArrayList;
import java.util.Arrays;

public class ClusterOfGames extends OutputLine implements Comparable<ClusterOfGames> {

	private static class GameAndTeamA {
		private final Game _game;
		private final Team _teamA;
		private GameAndTeamA(final Game game, final Team teamA) {
			_game = game;
			_teamA = teamA;
		}
	}

	public static int[] probsToLines(final double pA, final double pB) {
		for (double p0 = pA, p1 = pB;; p0 *= 1.01, p1 *= 1.01) {
			int line0 = Integer.MIN_VALUE, line1 = Integer.MIN_VALUE;
			for (int iPass = 0; iPass < 2; ++iPass) {
				final double p = iPass == 0 ? p0 : p1;
				final double line;
				if (p <= 0.5) {
					/**
					 * <pre>
					 * Invert 100 / (100 + L) = p to get L:
					 * 100 / p = 100 + L
					 * 100 / p - 100 = L
					 * 100 * (1 / p - 1) = L
					 * 100 * (1 - p) / p = L
					 * </pre>
					 */
					line = 100d * (1d - p) / p;
				} else {
					/**
					 * <pre>
					 * Invert L / (100 + L) = p to get L.
					 * 1 / p = (100 + L) / L = 100 / L + 1
					 * 1 / p - 1 = 100 / L
					 * (1 - p ) / p = 100 / L
					 * L = 100 * p / (1 - p)
					 * </pre>
					 */
					line = 100 * p / (1d - p);
				}
				final int lineInt = (int) Math.round(5d * Math.ceil(line / 5d));
				if (iPass == 0) {
					line0 = p >= 0.5 ? -lineInt : lineInt;
				} else {
					line1 = p >= 0.5 ? -lineInt : lineInt;
				}
			}
			final double[] probs = Game.linesToProbs(line0, line1, /* normalize= */false);
			final double sum = probs[0] + probs[1];
			if (sum >= 1.0475) {
				return new int[]{line0, line1};
			}
		}
	}

	final public String _clusterName, _aName, _bName;
	final private ArrayList<GameAndTeamA> _gameAndTeamA = new ArrayList<>();
	private boolean _aIsHome;

	public ClusterOfGames(final String clusterName) {
		final String[] fields = clusterName.split("\\s+vs\\s+");
		_aName = fields[0];
		_bName = fields[1];
		_clusterName = _aName + " vs " + _bName;
		_date = null;
	}

	public void addGameAndTeamA(final Game game, final Team teamA) {
		_gameAndTeamA.add(new GameAndTeamA(game, teamA));
		if (_date == null) {
			_date = game._date;
		} else {
			_date = _date.compareTo(game._date) >= 0 ? _date : game._date;
		}
	}

	public void finishUp() {
		final int nGames = _gameAndTeamA.size();
		final int nPointsNeeded = 1 + nGames / 2;
		Game specialGameIfBettingOnA = null;
		Game specialGameIfBettingOnB = null;
		if (nGames % 2 == 0) {
			for (int iPass = 0; iPass < 2; ++iPass) {
				Game specialGame = null;
				double bestProb = 0d;
				final boolean bettingOnA = iPass == 0;
				for (final GameAndTeamA gameAndTeamA : _gameAndTeamA) {
					final Game game = gameAndTeamA._game;
					final Team teamA = gameAndTeamA._teamA;
					final double probFavoriteWins = game.getProbFavoriteWins();
					final boolean ourTeamIsFavorite = bettingOnA == (teamA._name
							.equals(game.getFavoriteName()));
					final double probGoesOurWay = ourTeamIsFavorite
							? probFavoriteWins
							: (1d - probFavoriteWins);
					if (specialGame == null || probGoesOurWay > bestProb) {
						specialGame = game;
						bestProb = probGoesOurWay;
					}
				}
				if (bettingOnA) {
					specialGameIfBettingOnA = specialGame;
				} else {
					specialGameIfBettingOnB = specialGame;
				}
			}
		}
		/** Set _aIsHome. */
		int nA = 0, nB = 0;
		for (final GameAndTeamA gata : _gameAndTeamA) {
			final Game g = gata._game;
			if (g.getHomeName().equals(gata._teamA._name)) {
				++nA;
			} else {
				++nB;
			}
		}
		_aIsHome = nA >= nB;
		final double probAWins = recurse(/* nDone= */0, /* nStillNeeded= */nPointsNeeded,
				/* bettingOnA= */true, /* specialGame= */specialGameIfBettingOnA);
		final double probBWins = recurse(/* nDone= */0, /* nStillNeeded= */nPointsNeeded,
				/* bettingOnA= */false, /* specialGame= */specialGameIfBettingOnB);
		_probAwayWins = _aIsHome ? probBWins : probAWins;
		_probHomeWins = _aIsHome ? probAWins : probBWins;
		final int[] lines = probsToLines(_probAwayWins, _probHomeWins);
		_awayLine = lines[0];
		_homeLine = lines[1];
	}

	private double recurse(final int nDone, final int nStillNeeded,
			final boolean bettingOnA, final Game specialGame) {
		if (nStillNeeded <= 0) {
			return 1d;
		}
		if (nDone >= _gameAndTeamA.size()) {
			return 0;
		}
		final GameAndTeamA gameAndTeamA = _gameAndTeamA.get(nDone);
		final Game game = gameAndTeamA._game;
		final Team teamA = gameAndTeamA._teamA;
		final double probFavoriteWins = game.getProbFavoriteWins();
		final boolean ourTeamIsFavorite = bettingOnA == teamA._name
				.equals(game.getFavoriteName());
		final double probGoesOurWay = ourTeamIsFavorite
				? probFavoriteWins
				: (1d - probFavoriteWins);
		final double prob1 = probGoesOurWay * recurse(nDone + 1,
				nStillNeeded - (game == specialGame ? 2 : 1), bettingOnA, specialGame);
		final double prob2 = (1d - probGoesOurWay)
				* recurse(nDone + 1, nStillNeeded, bettingOnA, specialGame);
		return prob1 + prob2;
	}

	@Override
	public boolean equals(final Object cluster) {
		if (cluster == null || !(cluster instanceof ClusterOfGames)) {
			return false;
		}
		return compareTo((ClusterOfGames) cluster) == 0;
	}

	@Override
	public String getAwayName() {
		return _aIsHome ? _bName : _aName;
	}

	@Override
	public String getHomeName() {
		return _aIsHome ? _aName : _bName;
	}

	private String getSmallName() {
		return _aName.compareTo(_bName) <= 0 ? _aName : _bName;
	}

	private String getBigName() {
		return _aName.compareTo(_bName) > 0 ? _aName : _bName;
	}

	@Override
	public int compareTo(final ClusterOfGames c) {
		if (c == null) {
			return 1;
		}
		final String mySmallName = getSmallName();
		final String hisSmallName = c.getSmallName();
		final int compareValue = mySmallName.compareTo(hisSmallName);
		if (compareValue != 0) {
			return compareValue;
		}
		final String myBigName = getBigName();
		final String hisBigName = c.getBigName();
		return myBigName.compareTo(hisBigName);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(getSortedGames());
	}

	Game[] getSortedGames() {
		final Game[] games = new Game[_gameAndTeamA.size()];
		final int nGames = _gameAndTeamA.size();
		for (int k = 0; k < nGames; ++k) {
			games[k] = _gameAndTeamA.get(k)._game;
		}
		Arrays.sort(games);
		return games;
	}

	public String getString() {
		int k = 0;
		String s = String.format("%s vs %s", _aName, _bName);
		for (final GameAndTeamA gameAndTeamA : _gameAndTeamA) {
			s += String.format("\n%d.\t%s\t%s", ++k, gameAndTeamA._game.getString(),
					gameAndTeamA._teamA.getString());
		}
		return s;
	}

	@Override
	public String toString() {
		return getString();
	}

}
