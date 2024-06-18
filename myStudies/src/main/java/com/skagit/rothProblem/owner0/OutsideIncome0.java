package com.skagit.rothProblem.owner0;

import com.skagit.util.MyStudiesStringUtils;
import com.skagit.util.NamedEntity;

public class OutsideIncome0 extends NamedEntity {
    public final Owner0 _owner0;
    public final double _amount;
    public final int _year;

    public OutsideIncome0(final Owner0 owner0, final String name, final double amount, final int year) {
	super(name);
	_owner0 = owner0;
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
