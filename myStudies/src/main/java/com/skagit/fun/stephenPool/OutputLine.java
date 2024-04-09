package com.skagit.fun.stephenPool;

import java.util.Date;

public abstract class OutputLine {
    public static enum OutputColumn {
	DATE_TIME("Date/Time"), //
	AWAY_NAME("Away"), AWAY_LINE("Away Line"), PROB_AWAY_WINS("Prob Away Wins"), //
	HOME_NAME("Home"), HOME_LINE("Home Line"), PROB_HOME_WINS("Prob Home Wins"), //
	BOOK_PICK("Book Pick"), BOOK_WEIGHT("Book Weight"), //
	YOUR_PICK("Your Pick"), YOUR_WEIGHT("Your Weight");

	final public String _displayString;

	private OutputColumn(final String displayString) {
	    _displayString = displayString;
	}

	final public static OutputColumn[] _AllOutputColumns = OutputColumn.values();
    };

    public Date _date;
    public int _awayLine, _homeLine;
    public double _probAwayWins, _probHomeWins;
    public double _weight;

    public double getProbFavoriteWins() {
	return Math.max(_probAwayWins, _probHomeWins);
    }

    public abstract String getAwayName();

    public abstract String getHomeName();

    public String getFavoriteName() {
	return getProbFavoriteWins() == _probHomeWins ? getHomeName() : getAwayName();
    }

}
