package com.skagit.rothProblem.owner0;

public enum TypeOfAccount {
    REG_IRA("Reg IRA"), INH_IRA("Inh IRA"), POST_TAX("Post Tax"), ROTH("Roth");

    public final String _english;

    TypeOfAccount(final String english) {
	_english = english;
    }
}
