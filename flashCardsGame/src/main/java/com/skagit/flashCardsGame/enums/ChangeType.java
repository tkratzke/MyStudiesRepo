package com.skagit.flashCardsGame.enums;

import java.util.EnumSet;

public enum ChangeType {
    NOTHING_TO_SOMETHING("Initial Quiz Summary:"), //
    RESTART("Restarting Current Quiz:"), //
    CRITICAL_ONLY_WIN("Re-do Original Quiz:"), //
    MOVE_ON_WIN("Moving on!:"), //
    LOSS("Critical Only:"), //
    PARAMETERS_CHANGED("User Changed Properties:"), //
    NO_CHANGE("");

    final public String _reasonForChangeString;

    ChangeType(final String reasonForChangeString) {
	_reasonForChangeString = reasonForChangeString;
    }

    final public static ChangeType[] _Values = values();

    final public static EnumSet<ChangeType> _NewQuizSet = EnumSet.of(//
	    ChangeType.CRITICAL_ONLY_WIN, //
	    ChangeType.MOVE_ON_WIN, //
	    ChangeType.LOSS, //
	    ChangeType.NOTHING_TO_SOMETHING, //
	    ChangeType.PARAMETERS_CHANGED, //
	    ChangeType.RESTART//
    );

    final public static EnumSet<ChangeType> _ReallyNewQuizSet = EnumSet.of(//
	    ChangeType.MOVE_ON_WIN, //
	    ChangeType.NOTHING_TO_SOMETHING, //
	    ChangeType.PARAMETERS_CHANGED //
    );

}
