package com.skagit.flashCardsGame.enums;

import java.util.Properties;

public enum PropertyPlus {
    CARDS_FILE("", //
	    "Strings that indicate where to find the Cards File," //
		    + "\nthe directory of sound files, and" //
		    + "\nwhether or not to play the sounds."), //
    SOUND_FILES_DIR("", ""), //
    SILENT_MODE(Boolean.FALSE.toString(), ""), //

    TOP_CARD_INDEX(String.valueOf(0),
	    "Highest card index in first quiz," + "\nplus 4 lines that specify normal quiz generation."), //
    NUMBER_OF_NEW_WORDS(String.valueOf(1), ""), //
    NUMBER_OF_RECENT_WORDS(String.valueOf(3), ""), //
    NUMBER_OF_TIMES_FOR_NEW_WORDS(String.valueOf(1), ""), //
    PERCENTAGE_FOR_RECENT_WORDS(String.valueOf(75) + "%", ""), //
    LAG_LENGTH_IN_MILLISECONDS(String.valueOf(1000), ""), //

    DECAY_TYPE("LINEAR", ""), //

    DIACRITICS_TREATMENT( //
	    DiacriticsTreatment.STRICT.name(), //
	    DiacriticsTreatment.STRICT._explanation + //
		    "\n" + DiacriticsTreatment.LENIENT._explanation + //
		    "\n" + DiacriticsTreatment.RELAXED._explanation //
    ),

    MODE(Mode.NORMAL.name(), //
	    Mode.NORMAL._explanation + //
		    "\n" + Mode.SWITCH._explanation + //
		    "\n" + Mode.STEP._explanation //
    ),

    RANDOM_SEED(String.valueOf(0), //
	    "0: Cards Retain their Order.\n" //
		    + "Positive #: Use that for the Random Seed.\n" //
		    + "Any Negative #: Use SuperRandom." //
    ),

    ALLOWABLE_MISS_PERCENTAGE(String.valueOf(10) + "%", "Determines if each quiz is \"passed\" or not." //
    ),

    CLUMPING(Clumping.NO_CLUMPING.name(), //
	    Clumping.BY_CLUE._explanation + //
		    "\n" + Clumping.BY_ANSWER._explanation + //
		    "\n" + Clumping.NO_CLUMPING._explanation //
    ),;

    final public static PropertyPlus[] _Values = values();

    final public String _propertyName;
    final String _defaultStringValue;
    final public String _indicatorString;
    final public String _comment;

    PropertyPlus(final String defaultStringValue, final String comment) {
	final String enumName0 = name();
	final StringBuilder sb0 = new StringBuilder(enumName0);
	final int len0 = enumName0.length();
	for (int k = 0; k < len0; ++k) {
	    final char c = sb0.charAt(k);
	    if (c == '_') {
		sb0.setCharAt(k, '.');
	    } else if (k > 0 && sb0.charAt(k - 1) != '.') {
		sb0.setCharAt(k, Character.toLowerCase(c));
	    } else {
		sb0.setCharAt(k, Character.toUpperCase(c));
	    }
	}
	_propertyName = sb0.toString();
	final String enumName1 = enumName0.replaceAll("_FOR_", "_").replaceAll("_OF_", "_");
	final int len1 = enumName1.length();
	final StringBuilder sb1 = new StringBuilder();
	for (int k = 0; k < len1; ++k) {
	    if (k == 0 || enumName1.charAt(k - 1) == '_') {
		sb1.append(enumName1.charAt(k));
	    }
	}
	_indicatorString = sb1.toString();
	_defaultStringValue = defaultStringValue;
	_comment = comment;
    }

    public String getValidString(final Object o) {
	if (o == null || !(o instanceof String)) {
	    return _defaultStringValue;
	}
	final String s = (String) o;
	String stringToParseForAnInt = s;
	switch (this) {
	case CARDS_FILE:
	case SOUND_FILES_DIR:
	    return s;
	case ALLOWABLE_MISS_PERCENTAGE:
	case PERCENTAGE_FOR_RECENT_WORDS:
	    final int len = s == null ? 0 : s.length();
	    if (len < 2 || s.charAt(len - 1) != '%') {
		return _defaultStringValue;
	    }
	    stringToParseForAnInt = s.substring(0, len - 1);
	case NUMBER_OF_NEW_WORDS:
	case NUMBER_OF_RECENT_WORDS:
	case NUMBER_OF_TIMES_FOR_NEW_WORDS:
	case TOP_CARD_INDEX:
	case RANDOM_SEED:
	case LAG_LENGTH_IN_MILLISECONDS:
	    try {
		final int i = Integer.parseInt(stringToParseForAnInt);
		switch (this) {
		case ALLOWABLE_MISS_PERCENTAGE:
		case PERCENTAGE_FOR_RECENT_WORDS:
		    return 0 <= i && i <= 100 ? s : _defaultStringValue;
		case NUMBER_OF_NEW_WORDS:
		    return i >= 1 ? s : _defaultStringValue;
		case NUMBER_OF_RECENT_WORDS:
		case NUMBER_OF_TIMES_FOR_NEW_WORDS:
		case TOP_CARD_INDEX:
		    return i >= 0 ? s : _defaultStringValue;
		case RANDOM_SEED:
		    return s;
		case LAG_LENGTH_IN_MILLISECONDS:
		    return i >= 0 ? s : _defaultStringValue;
		default:
		    /** Cannot get to the following: */
		    return null;
		}
	    } catch (final NumberFormatException e) {
		return _defaultStringValue;
	    }
	case DECAY_TYPE:
	case DIACRITICS_TREATMENT:
	case CLUMPING:
	case MODE:
	    try {
		if (this == DECAY_TYPE) {
		    DecayType.valueOf(s);
		} else if (this == DIACRITICS_TREATMENT) {
		    DiacriticsTreatment.valueOf(s);
		} else if (this == CLUMPING) {
		    Clumping.valueOf(s);
		} else if (this == MODE) {
		    Mode.valueOf(s);
		}
	    } catch (final IllegalArgumentException e) {
		return _defaultStringValue;
	    }
	    return s;
	case SILENT_MODE:
	    return String.valueOf(Boolean.valueOf(s));
	}
	/** To keep the compiler happy: */
	return null;
    }

    public String getValidString(final Properties properties) {
	return getValidString(properties.get(_propertyName));
    }

}
