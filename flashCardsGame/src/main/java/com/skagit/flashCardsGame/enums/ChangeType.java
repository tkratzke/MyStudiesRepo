package com.skagit.flashCardsGame.enums;

public enum ChangeType {
	NOTHING_TO_SOMETHING("Initial Quiz Summary:"), //
	RESTART("Restarting Current Quiz:"), //
	CRITICAL_ONLY_WIN("Re-do Original Quiz:"), //
	MOVE_ON_WIN("Moving on!:"), //
	LOSS("Critical Only:"), //
	PARAMETERS_CHANGED("User Changed Properties:"), //
	NO_CHANGE("");

	final public static ChangeType[] _Values = values();

	final public String _reasonForChangeString;
	ChangeType(final String reasonForChangeString) {
		_reasonForChangeString = reasonForChangeString;
	}
}
