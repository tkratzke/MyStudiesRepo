package com.skagit.flashCardsGame;

class QuizPlusTransition {
	final QuizPlus _newQuizPlus;
	final TypeOfChange _typeOfChange;
	final String _transitionString;

	/**
	 * Converts an input QuizPlus to a "transitioned" QuizPlus, and specifies the reason for
	 * doing so, along with two Strings that are useful for printouts.
	 */
	QuizPlusTransition(final QuizPlus quizPlus, final QuizPlus newQuizPlus,
			final TypeOfChange typeOfChange) {
		_newQuizPlus = newQuizPlus;
		_typeOfChange = typeOfChange;
		final String oldSummaryString;
		if (quizPlus == null) {
			oldSummaryString = String.format("{%c|%c}", FlashCardsGame._EmptySetChar,
					FlashCardsGame._EmptySetChar);
		} else {
			oldSummaryString = quizPlus.getSummaryString();
		}
		final String newSummaryString = _newQuizPlus.getSummaryString();
		_transitionString = String.format("%s%c%s", oldSummaryString, FlashCardsGame._RtArrowChar,
				newSummaryString);
	}

}