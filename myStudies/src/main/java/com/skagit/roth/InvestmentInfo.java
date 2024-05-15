package com.skagit.roth;

import com.skagit.util.NamedEntity;

public class InvestmentInfo extends NamedEntity {
    public double _d;

    public InvestmentInfo(final String name, final double d) {
	super(name);
	_d = d;
    }

    public String getString() {
	return String.format("%s: $%.2f", _name, _d);
    }

    @Override
    public String toString() {
	return getString();
    }

}
