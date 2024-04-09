package com.skagit.flashCardsGame;

import com.skagit.flashCardsGame.enums.ChangeType;

class QuizPlusTransition {
    final QuizPlus _newQuizPlus;
    final ChangeType _changeType;
    final String _transitionString;

    /** Constructs a String useful for printing out. */
    QuizPlusTransition(final QuizPlus quizPlus, final QuizPlus newQuizPlus, final ChangeType changeType) {
	_newQuizPlus = newQuizPlus;
	_changeType = changeType;
	final String oldSummaryString;
	if (quizPlus == null) {
	    oldSummaryString = String.format("{%c|%c}", Statics._EmptySetChar, Statics._EmptySetChar);
	} else {
	    oldSummaryString = quizPlus.getSummaryString();
	}
	final String newSummaryString = _newQuizPlus.getSummaryString();
	_transitionString = String.format("%s%c%s", oldSummaryString, Statics._RtArrowChar, newSummaryString);
    }

}