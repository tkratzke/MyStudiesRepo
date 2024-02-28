package com.skagit.flashCardsGame.enums;

import java.util.Properties;

public enum PropertyPlus {
	NUMBER_OF_NEW_WORDS(String.valueOf(1), //
			"*=Non-QuizGenerator Property\n" + //
					"Specification of Window:"), //
	NUMBER_OF_RECENT_WORDS(String.valueOf(3), ""), //
	TOP_CARD_INDEX(String.valueOf(3), ""), //

	QUIZ_DIRECTION("A->B", "\"A->B\" or \"B->A\":"), //
	CLUMPING("A", //
			"A: Clump Duplicate As\n" //
					+ "B: Clump Duplicate Bs\n" //
					+ "NONE: No clumping"), //

	DIACRITICS_TREATMENT( //
			DiacriticsTreatment.STRICT.name(), //
			DiacriticsTreatment.STRICT._explanation + //
					"\n" + DiacriticsTreatment.LENIENT._explanation + //
					"\n" + DiacriticsTreatment.RELAXED._explanation //
	),

	NUMBER_OF_TIMES_FOR_NEW_WORDS(String.valueOf(1), //
			"Parameters for Generating Quizzes for a particular window:"), //
	PERCENTAGE_FOR_RECENT_WORDS(String.valueOf(75) + "%", ""), //
	DECAY_TYPE("LINEAR", ""), //

	RANDOM_SEED(String.valueOf(0), //
			"0: Cards Retain their Order\n" //
					+ "Positive #: Use that for the Random Seed\n" //
					+ "Any Negative #: Use SuperRandom"), //

	ALLOWABLE_MISS_PERCENTAGE(String.valueOf(10) + "%",
			"Determines if each quiz is \"passed\" or not"); //

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
		final int len = s == null ? 0 : s.length();
		if (len == 0) {
			return _defaultStringValue;
		}
		final String s1;
		if (this == ALLOWABLE_MISS_PERCENTAGE || this == PERCENTAGE_FOR_RECENT_WORDS) {
			if (len == 1 || s.charAt(len - 1) != '%') {
				return _defaultStringValue;
			}
			s1 = s.substring(0, len - 1);
		} else {
			s1 = s;
		}
		switch (this) {
			case ALLOWABLE_MISS_PERCENTAGE :
			case PERCENTAGE_FOR_RECENT_WORDS :
			case NUMBER_OF_NEW_WORDS :
			case NUMBER_OF_RECENT_WORDS :
			case NUMBER_OF_TIMES_FOR_NEW_WORDS :
			case TOP_CARD_INDEX :
			case RANDOM_SEED :
				try {
					final int i = Integer.parseInt(s1);
					switch (this) {
						case ALLOWABLE_MISS_PERCENTAGE :
						case PERCENTAGE_FOR_RECENT_WORDS :
							return 0 <= i && i <= 100 ? s : _defaultStringValue;
						case NUMBER_OF_NEW_WORDS :
							return i >= 1 ? s : _defaultStringValue;
						case NUMBER_OF_RECENT_WORDS :
						case NUMBER_OF_TIMES_FOR_NEW_WORDS :
						case TOP_CARD_INDEX :
							return i >= 0 ? s : _defaultStringValue;
						case RANDOM_SEED :
							return s;
						default :
					}
				} catch (final NumberFormatException e) {
					return _defaultStringValue;
				}
			default :
		}

		switch (this) {
			case DECAY_TYPE :
			case DIACRITICS_TREATMENT :
			case CLUMPING :
				try {
					if (this == DECAY_TYPE) {
						DecayType.valueOf(s);
					} else if (this == DIACRITICS_TREATMENT) {
						DiacriticsTreatment.valueOf(s);
					} else {
						Clumping.valueOf(s);
					}
				} catch (final IllegalArgumentException e) {
					return _defaultStringValue;
				}
				break;
			case QUIZ_DIRECTION :
				for (final QuizDirection quizDirection : QuizDirection._Values) {
					if (quizDirection._typableString.equalsIgnoreCase(s)) {
						return quizDirection._typableString;
					}
				}
				return _defaultStringValue;
			default :
		}
		return s;
	}

	public String getValidString(final Properties properties) {
		return getValidString(properties.get(_propertyName));
	}

}
