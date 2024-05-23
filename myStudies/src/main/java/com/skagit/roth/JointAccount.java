package com.skagit.roth;

import com.skagit.util.TypeOfDouble;

public class JointAccount extends Account {
    public final InvestmentItem[] _investmentItems;

    public JointAccount(final String name, final RothCalculator rothCalculator) {
	super(name, rothCalculator, /* owner= */null);
	_investmentItems = new InvestmentItem[InvestmentsEnum._Values.length];
    }

    @Override
    public String getString() {
	String s = String.format("JNT_ACCT[%s]", _name);
	s += String.format(", BlncBgnnngYr[%s] CrrntBlnc[%s]", //
		TypeOfDouble.MONEY.format(_balanceBeginningOfCurrentYear, 2), //
		TypeOfDouble.MONEY.format(_currentBalance, 2));
	s += "\nInvestmentItems";
	final int nInvestmentItems = _investmentItems.length;
	for (int k = 0; k < nInvestmentItems; ++k) {
	    final InvestmentItem investmentItem = _investmentItems[k];
	    s += String.format("\n%03d. %s", k, investmentItem.getString());
	}
	return s;
    }
}
