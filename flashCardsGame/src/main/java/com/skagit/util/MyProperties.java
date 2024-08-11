package com.skagit.util;

import java.util.Properties;

import com.skagit.flashCardsGame.enums.Clumping;
import com.skagit.flashCardsGame.enums.DecayType;
import com.skagit.flashCardsGame.enums.DiacriticsTreatment;
import com.skagit.flashCardsGame.enums.Mode;
import com.skagit.flashCardsGame.enums.PropertyPlus;

public class MyProperties extends Properties {
    private static final long serialVersionUID = 1L;

    /**
     * Validates the attempt at a value, and returns the defaultValue for the
     * PropertyPlus otherwise.
     */
    public String getValidString(final PropertyPlus propertyPlus, final String value) {
	if (propertyPlus == null) {
	    return null;
	}
	final String defaultValue = propertyPlus._defaultValue;
	if (value == null) {
	    return defaultValue;
	}
	String stringToParseForAnInt = value;
	switch (propertyPlus) {
	case CARDS_FILE:
	case SOUND_FILES:
	    return value;
	case ALLOWABLE_MISS_PERCENTAGE:
	case PERCENTAGE_FOR_RECENT_WORDS:
	    final int len = value == null ? 0 : value.length();
	    if (len < 2 || value.charAt(len - 1) != '%') {
		return defaultValue;
	    }
	    stringToParseForAnInt = value.substring(0, len - 1);
	case NUMBER_OF_NEW_WORDS:
	case NUMBER_OF_RECENT_WORDS:
	case NUMBER_OF_TIMES_FOR_NEW_WORDS:
	case TOP_CARD_INDEX:
	case RANDOM_SEED:
	case BLOCK_SIZE:
	case LAG_LENGTH_IN_MILLISECONDS:
	    try {
		final int i = Integer.parseInt(stringToParseForAnInt);
		switch (propertyPlus) {
		case ALLOWABLE_MISS_PERCENTAGE:
		case PERCENTAGE_FOR_RECENT_WORDS:
		    return 0 <= i && i <= 100 ? value : defaultValue;
		case NUMBER_OF_NEW_WORDS:
		    return i >= 1 ? value : defaultValue;
		case NUMBER_OF_RECENT_WORDS:
		case NUMBER_OF_TIMES_FOR_NEW_WORDS:
		case TOP_CARD_INDEX:
		    return i >= 0 ? value : defaultValue;
		case BLOCK_SIZE:
		    return i >= 0 ? value : defaultValue;
		case RANDOM_SEED:
		    return value;
		case LAG_LENGTH_IN_MILLISECONDS:
		    return i >= 0 ? value : defaultValue;
		default:
		    /** Cannot get to the following: */
		    return null;
		}
	    } catch (final NumberFormatException e) {
		return defaultValue;
	    }
	case DECAY_TYPE:
	case DIACRITICS_TREATMENT:
	case CLUMPING:
	case MODE:
	    try {
		if (propertyPlus == PropertyPlus.DECAY_TYPE) {
		    DecayType.valueOf(value);
		} else if (propertyPlus == PropertyPlus.DIACRITICS_TREATMENT) {
		    DiacriticsTreatment.valueOf(value);
		} else if (propertyPlus == PropertyPlus.CLUMPING) {
		    Clumping.valueOf(value);
		} else if (propertyPlus == PropertyPlus.MODE) {
		    Mode.valueOf(value);
		}
	    } catch (final IllegalArgumentException e) {
		return defaultValue;
	    }
	    return value;
	case BE_SILENT:
	    return String.valueOf(Boolean.valueOf(value));
	}
	/** To keep the compiler happy: */
	return null;
    }

}
