package com.skagit.roth.accountOwner;

import com.skagit.util.MyStudiesStringUtils;
import com.skagit.util.NamedEntity;

public class OutsideIncome extends NamedEntity {
    public final AccountOwner _owner;
    public final double _amount;
    public final int _year;

    public OutsideIncome(final AccountOwner owner, final String name, final double amount, final int year) {
	super(name);
	_owner = owner;
	_amount = amount;
	_year = year;
    }

    @Override
    public String getString() {
	String s = String.format("%s: %s", _name, MyStudiesStringUtils.formatDollars(_amount));
	if (_year > 0) {
	    s += String.format("(%d)", _year);
	}
	return s;
    }
}
