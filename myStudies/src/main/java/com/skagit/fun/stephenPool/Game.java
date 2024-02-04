package com.skagit.fun.stephenPool;

import java.util.Arrays;
import java.util.Date;

public class Game extends OutputLine implements Comparable<Game> {

	public final Team _awayTeam, _homeTeam;
	public final String _clusterName, _teamAName;

	public Game(final Date date, final Team awayTeam, final Team homeTeam,
			final int awayLine, final int homeLine, final String clusterName, final String teamAName) {
		_date = date;
		_awayTeam = awayTeam;
		_homeTeam = homeTeam;
		_awayLine = awayLine;
		_homeLine = homeLine;
		_clusterName = clusterName;
		_teamAName = teamAName;
		final double[] probs = linesToProbs(_awayLine, _homeLine, /* normalize= */true);
		_probAwayWins = probs[0];
		_probHomeWins = probs[1];
	}

	public static double[] linesToProbs(final int line0, final int line1,
			final boolean normalize) {
		final double[] probs = new double[2];
		double sum = 0d;
		for (int iPass = 0; iPass < 2; ++iPass) {
			final int line = iPass == 0 ? line0 : line1;
			final double absLine = Math.abs(line);
			probs[iPass] = (line < 0 ? absLine : 100d) / (100d + absLine);
			sum += probs[iPass];
		}
		if (normalize) {
			for (int iPass = 0; iPass < 2; ++iPass) {
				probs[iPass] /= sum;
			}
		}
		return probs;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(new Team[]{_awayTeam, _homeTeam});
	}

	@Override
	public boolean equals(final Object game) {
		if (game == null || !(game instanceof Game)) {
			return false;
		}
		return compareTo((Game) game) == 0;
	}

	@Override
	public int compareTo(final Game game) {
		int compareValue = _date.compareTo(game._date);
		if (compareValue != 0) {
			return compareValue;
		}
		compareValue = _awayTeam.compareTo(game._awayTeam);
		if (compareValue != 0) {
			return compareValue;
		}
		return _homeTeam.compareTo(game._homeTeam);
	}

	public String getString() {
		final boolean homeIsFavorite = getFavoriteName().equals(getHomeName());
		final String awayAuxString = homeIsFavorite ? "UA" : "FA";
		final String homeAuxString = homeIsFavorite ? "FH" : "UH";
		return String.format("%s-%s\t%s-%s\t%f", getAwayName(), awayAuxString, getHomeName(),
				homeAuxString, getProbFavoriteWins());
	}

	@Override
	public String getAwayName() {
		return _awayTeam._name;
	}

	@Override
	public String getHomeName() {
		return _homeTeam._name;
	}

	@Override
	public String toString() {
		return getString();
	}
}
