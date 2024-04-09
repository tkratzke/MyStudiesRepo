package com.skagit.flashCardsGame.enums;

public enum Mode {
    NORMAL("Run a Sequence of Quizzes."), //
    SWITCH("Switch Clues and Answers and run no quizzes."), //
    STEP("Step through the cards without regard to correctness.");

    final public static Mode[] _Values = values();

    final String _explanation;

    Mode(final String explanation) {
	_explanation = String.format("%s: %s", name(), explanation);
    }
}
