package com.skagit.flashCardsGame;

class QuizPlusTransition {
	final QuizPlus _newQuizPlus;
	final TypeOfChange _typeOfChange;
	final String _reasonForChangeString;
	final String _transitionString;

	/**
	 * Converts an input QuizPlus to a "transitioned" QuizPlus, and specifies the reason for
	 * doing so, along with two Strings that are useful for printouts.
	 */
	QuizPlusTransition(final QuizPlus quizPlus, final QuizPlus newQuizPlus,
			final TypeOfChange typeOfChange) {
		_newQuizPlus = newQuizPlus;
		_typeOfChange = typeOfChange;
		switch (_typeOfChange) {
			case RESTART :
				_reasonForChangeString = "Restarting Current Quiz";
				break;
			case NOTHING_TO_SOMETHING :
				_reasonForChangeString = "Initial Quiz Summary";
				break;
			case WIN :
				if (quizPlus._criticalQuizIndicesOnly) {
					_reasonForChangeString = "Re-do Original Quiz:";
				} else {
					_reasonForChangeString = "Moving on!:";
				}
				break;
			case LOSS :
				_reasonForChangeString = "Critical Only:";
				break;
			case PARAMETERS_CHANGED :
				_reasonForChangeString = "User Changed Properties:";
				break;
			case NO_CHANGE :
			default :
				_reasonForChangeString = "";
				break;
		}
		final String oldSummaryString;
		if (quizPlus == null) {
			oldSummaryString = String.format("{%c|%c}", FlashCardsGame._EmptySetChar,
					FlashCardsGame._EmptySetChar);
		} else {
			oldSummaryString = quizPlus.getSummaryString();
		}
		final String newSummaryString = _newQuizPlus.getSummaryString();
		_transitionString = String.format("%s%c%s", oldSummaryString, FlashCardsGame._RtArrow,
				newSummaryString);
	}

}