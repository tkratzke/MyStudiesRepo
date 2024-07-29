package com.skagit.flashCardsGame.enums;

import java.util.Properties;

public enum PropertyPlus {
    CARDS_FILE(/* propertyName= */"Cards.File", //
	    /* shortName= */ null, //
	    /* defaultValue= */null, //
	    /* comment= */ "Strings that indicate where to find the Cards File," //
		    + "\nthe directory of sound files, " //
		    + "\nwhether or not to play the sounds, " //
		    + "\nwhether or not to back up the property and Cards files, " //
		    + "\nand how long to pause for each sound to finish."), //
    SOUND_FILES_DIR(/* propetyName= */ "Sound.Files.Dir", //
	    /* shortName= */ null, //
	    /* defaultValue= */ "", //
	    /* comment= */ ""), //
    BE_SILENT(/* propertyName= */ "Be.Silent", //
	    /* shortName= */null, //
	    /* defaultValue= */Boolean.FALSE.toString(), //
	    /* comment= */""), //
    BACK_UP_FCG_AND_CARDS_FILES(/* propertyName= */"Back.Up.fcg.And.Cards.Files", //
	    /* shortName= */ null, //
	    Boolean.TRUE.toString(), //
	    /* comment= */""), //
    LAG_LENGTH_IN_MILLISECONDS(/* propertyName= */"Lag.Length.In.Milliseconds", //
	    /* shortName= */ null, //
	    /* defaultValue= */ "1000", //
	    /* comment= */""), //

    TOP_CARD_INDEX(/* propertyName= */"Top.Card.Index", //
	    /* shortName= */"TCI", //
	    /* defaultValue= */"0", //
	    /* comment= */"Highest card index in first quiz," + //
		    "\nplus 5 lines that specify normal quiz generation."), //

    NUMBER_OF_NEW_WORDS(/* propertyName= */"Number.Of.New.Words", //
	    /* shortName= */ null, //
	    /* defaultValue= */ "1", //
	    /* comment= */""), //
    NUMBER_OF_RECENT_WORDS(/* propertyName= */"Number.Of.Recent.Words", //
	    /* shortName= */ null, //
	    /* defaultValue= */ "3", //
	    /* comment= */""), //
    NUMBER_OF_TIMES_FOR_NEW_WORDS(/* propertyName= */"Number.Of.Times.For.New.Words", //
	    /* shortName= */ null, //
	    /* defaultValue= */ "1", //
	    /* comment= */""), //
    PERCENTAGE_FOR_RECENT_WORDS(/* propertyName= */"Percentage.For.Recent.Words", //
	    /* shortName= */ null, //
	    /* defaultValue= */ "75%", //
	    /* comment= */""), //
    DECAY_TYPE(/* propertyName= */"Decay.Type", //
	    /* shortName= */ null, //
	    /* defaultValue= */ "LINEAR", //
	    /* comment= */""), //

    DIACRITICS_TREATMENT(/* propertyName= */"Diacritics.Treatment", //
	    /* shortName= */ null, //
	    /* defaultValue= */DiacriticsTreatment.STRICT.name(), //
	    /* comment= */DiacriticsTreatment.STRICT._explanation + //
		    "\n" + DiacriticsTreatment.LENIENT._explanation + //
		    "\n" + DiacriticsTreatment.RELAXED._explanation //
    ),

    MODE(/* propertyName= */"Mode", //
	    /* shortName= */ null, //
	    /* defaultValue= */ Mode.NORMAL.name(), //
	    /* comment= */Mode.NORMAL._explanation + //
		    "\n" + Mode.STEP._explanation + //
		    "\n" + Mode.SWITCH_AND_DUMP._explanation + //
		    "\n" + Mode.REGULAR_SORT_AND_DUMP._explanation + //
		    "\n" + Mode.STRANGE_SORT_AND_DUMP._explanation + //
		    "\n" + Mode.SUPPRESS_ANSWERS_AND_DUMP._explanation //
    ),

    BLOCK_SIZE(/* propertyName= */"Block.Size", //
	    /* shortName= */ null, //
	    /* defaultValue= */ "10", //
	    /* comment= */"Size of block when dumping Cards File"),

    RANDOM_SEED(/* propertyName= */"Random.Size", //
	    /* shortName= */ null, //
	    /* defaultValue= */ "0", //
	    /* comment= */"0: Cards Retain their Order.\n" //
		    + "Positive #: Use that for the Random Seed.\n" //
		    + "Any Negative #: Use SuperRandom." //
    ),

    ALLOWABLE_MISS_PERCENTAGE(/* propertyName= */"Allowable.Miss.Percentage", //
	    /* shortName= */ null, //
	    /* defaultValue= */ "10%", //
	    /* comment= */"Determines if each quiz is \"passed\" or not." //
    ),

    CLUMPING(/* propertyName= */"Clumping", //
	    /* shortName= */ null, //
	    /* defaultValue= */ Clumping.NO_CLUMPING.name(), //
	    /* comment= */Clumping.BY_CLUE._explanation + //
		    "\n" + Clumping.BY_ANSWER._explanation + //
		    "\n" + Clumping.NO_CLUMPING._explanation //
    ),;

    final public static PropertyPlus[] _Values = values();

    final public String _propertyName;
    final public String _shortName;
    final String _defaultValue;
    final public String _comment;

    PropertyPlus(final String propertyName, final String shortName, final String defaultValue, final String comment) {
	_propertyName = propertyName;
	_shortName = shortName;
	_defaultValue = defaultValue;
	_comment = comment;
    }

    public String getValidString(final Object o) {
	if (o == null || !(o instanceof String)) {
	    return _defaultValue;
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
		return _defaultValue;
	    }
	    stringToParseForAnInt = s.substring(0, len - 1);
	case NUMBER_OF_NEW_WORDS:
	case NUMBER_OF_RECENT_WORDS:
	case NUMBER_OF_TIMES_FOR_NEW_WORDS:
	case TOP_CARD_INDEX:
	case RANDOM_SEED:
	case BLOCK_SIZE:
	case LAG_LENGTH_IN_MILLISECONDS:
	    try {
		final int i = Integer.parseInt(stringToParseForAnInt);
		switch (this) {
		case ALLOWABLE_MISS_PERCENTAGE:
		case PERCENTAGE_FOR_RECENT_WORDS:
		    return 0 <= i && i <= 100 ? s : _defaultValue;
		case NUMBER_OF_NEW_WORDS:
		    return i >= 1 ? s : _defaultValue;
		case NUMBER_OF_RECENT_WORDS:
		case NUMBER_OF_TIMES_FOR_NEW_WORDS:
		case TOP_CARD_INDEX:
		    return i >= 0 ? s : _defaultValue;
		case BLOCK_SIZE:
		    return i >= 0 ? s : _defaultValue;
		case RANDOM_SEED:
		    return s;
		case LAG_LENGTH_IN_MILLISECONDS:
		    return i >= 0 ? s : _defaultValue;
		default:
		    /** Cannot get to the following: */
		    return null;
		}
	    } catch (final NumberFormatException e) {
		return _defaultValue;
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
		return _defaultValue;
	    }
	    return s;
	case BE_SILENT:
	    return String.valueOf(Boolean.valueOf(s));
	case BACK_UP_FCG_AND_CARDS_FILES:
	    return String.valueOf(Boolean.valueOf(s));
	}
	/** To keep the compiler happy: */
	return null;
    }

    public String getValidString(final Properties properties) {
	return getValidString(properties.get(_propertyName));
    }

}
