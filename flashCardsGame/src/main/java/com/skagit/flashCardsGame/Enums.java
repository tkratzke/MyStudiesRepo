package com.skagit.flashCardsGame;

enum PropertyPlus {
	N_NEW_WORDS("Number.Of.New.Words", "1", "Specification of Window:"), //
	N_RECENT_WORDS("Number.Of.Recent.Words", "3", ""), //
	TCI("Top.Card.Index", "3", ""), //
	SEED("Random.Seed", "0", //
			"0: Cards Retain their Order\n" //
					+ "Positive #: Use that for the Seed\n" //
					+ "Any Negative #: Use SuperRandom:"), //
	N_TIMES_FOR_NEW_WORDS("Number.Of.Times.For.New.Words", "1", //
			"Parameters for Generating Quiz:"), //
	PFR("Percentage.For.Recents", "75%", ""), //
	FR("Failure.Rate", "10%", ""), //
	DECAY("Type.Of.Decay", "LINEAR", ""), //
	QUIZ_TYPE("Quiz.Type.Is.A_to_B", "FALSE", "Types of Quizzes:\n" //
			+ "\"Which Way to Quiz, " + //
			"Ignore Diacritics or not, and" + //
			"\"JulieAnswer\":"), //
	IGNORE_DIACRITICS("Ignore.Diacritics", "FALSE", ""), //
	JULIE_MODE("Julie.Mode", "FALSE", "");

	final static PropertyPlus[] _Values = values();

	final String _realName;
	final String _defaultStringValue;
	final String _comment;
	PropertyPlus(final String realName, final String defaultStringValue,
			final String comment) {
		_realName = realName;
		_defaultStringValue = defaultStringValue;
		_comment = comment;
	}
}

enum TypeOfDecay {
	EXPONENTIAL, LINEAR, NO_DECAY;
	final static TypeOfDecay[] _Values = values();
}

enum TypeOfChange {
	NOTHING_TO_SOMETHING, RESTART, WIN, LOSS, PARAMETERS_CHANGED, NO_CHANGE;
	final static TypeOfChange[] _Values = values();
}

enum JulieAnswer {
	RIGHT, WRONG, NOT_JULIE_MODE
}