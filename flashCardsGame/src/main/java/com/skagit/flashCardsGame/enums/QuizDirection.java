package com.skagit.flashCardsGame.enums;

import com.skagit.flashCardsGame.Statics;

public enum QuizDirection {
	A_TO_B(String.format("A%cB", Statics._RtArrowChar), "A->B"), //
	B_TO_A(String.format("B%cA", Statics._RtArrowChar), "B->A"); //

	final public static QuizDirection[] _Values = values();

	final public String _fancyString;
	final public String _typableString;

	QuizDirection(final String fancyString, final String typableString) {
		_fancyString = fancyString;
		_typableString = typableString;
	}

	public static QuizDirection get(final String typableString) {
		for (final QuizDirection quizDirection : _Values) {
			if (quizDirection._typableString.equals(typableString)) {
				return quizDirection;
			}
		}
		return null;
	}
}
