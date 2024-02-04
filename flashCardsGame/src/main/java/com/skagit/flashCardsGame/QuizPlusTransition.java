package com.skagit.flashCardsGame;

public class QuizPlusTransition {
	public final QuizPlus _oldQuizPlus;
	public final QuizPlus _newQuizPlus;
	public final TypeOfChange _typeOfChange;
	public QuizPlusTransition(final QuizPlus oldQuizPlus, final QuizPlus newQuizPlus,
			final TypeOfChange typeOfChange) {
		_oldQuizPlus = oldQuizPlus;
		_newQuizPlus = newQuizPlus;
		_typeOfChange = typeOfChange;
	}
}
