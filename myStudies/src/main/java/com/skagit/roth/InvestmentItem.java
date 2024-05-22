package com.skagit.roth;

import com.skagit.util.TypeOfDouble;

public class InvestmentItem {
    InvestmentsEnum _investmentsEnum;
    public double _d;

    public InvestmentItem(final InvestmentsEnum investmentsEnum, final double d) {
	_investmentsEnum = investmentsEnum;
	_d = d;
    }

    public String getString() {
	return String.format("%s(%s): %s", _investmentsEnum.name(), _investmentsEnum._originalString,
		TypeOfDouble.MONEY.format(_d, 2));
    }

    @Override
    public String toString() {
	return getString();
    }

}
