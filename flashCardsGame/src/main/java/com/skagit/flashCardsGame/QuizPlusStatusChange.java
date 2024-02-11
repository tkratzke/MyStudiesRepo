package com.skagit.flashCardsGame;

class QuizPlusStatusChange {
	final QuizPlus _oldQuizPlus;
	final QuizPlus _newQuizPlus;
	final QuizGenerator.TypeOfChange _typeOfChange;
	final String _reasonForChangeString;
	final String _transitionString;
	QuizPlusStatusChange(final QuizPlus oldQuizPlus, final QuizPlus newQuizPlus,
			final QuizGenerator.TypeOfChange typeOfChange) {
		_oldQuizPlus = oldQuizPlus;
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
				if (_oldQuizPlus._criticalQuizIndicesOnly) {
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
			case NO_STATUS_CHANGE :
			default :
				_reasonForChangeString = "";
				break;
		}
		final String oldSummaryString;
		if (_oldQuizPlus == null) {
			oldSummaryString = String.format("{%c|%c}", FlashCardsGame._EmptySet,
					FlashCardsGame._EmptySet);
		} else {
			oldSummaryString = _oldQuizPlus.getSummaryString();
		}
		final String newSummaryString = _newQuizPlus.getSummaryString();
		_transitionString = String.format("%s%c%s", oldSummaryString, FlashCardsGame._RtArrow,
				newSummaryString);
	}
}