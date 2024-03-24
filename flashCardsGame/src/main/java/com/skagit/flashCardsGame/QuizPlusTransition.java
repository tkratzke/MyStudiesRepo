package com.skagit.flashCardsGame;

import com.skagit.flashCardsGame.enums.ChangeType;

class QuizPlusTransition {
	final QuizPlus _newQuizPlus;
	final ChangeType _changeType;
	final String _transitionString;

	/**
	 * Converts an input QuizPlus to a "transitioned" QuizPlus, and specifies the reason for
	 * doing so, along with two Strings that are useful for printouts.
	 */
	QuizPlusTransition(final QuizPlus quizPlus, final QuizPlus newQuizPlus,
			final ChangeType changeType) {
		_newQuizPlus = newQuizPlus;
		_changeType = changeType;
		final String oldSummaryString;
		if (quizPlus == null) {
			oldSummaryString = String.format("{%c|%c}", Statics._EmptySetChar,
					Statics._EmptySetChar);
		} else {
			oldSummaryString = quizPlus.getSummaryString();
		}
		final String newSummaryString = _newQuizPlus.getSummaryString();
		_transitionString = String.format("%s%c%s", oldSummaryString, Statics._RtArrowChar,
				newSummaryString);
	}

}