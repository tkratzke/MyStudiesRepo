package com.skagit.roth.taxYear;

import com.skagit.roth.baseYear.OutsideIncome0;
import com.skagit.roth.rothCalculator.RothCalculator;
import com.skagit.util.NamedEntity;
import com.skagit.util.TypeOfDouble;

public class OutsideIncome1 extends NamedEntity {

    public final OutsideIncome0 _outsideIncome0;
    public final double _amount;

    public OutsideIncome1(final OutsideIncome0 outsideIncome0, final RothCalculator rothCalculator,
	    final int thisYear) {
	super(outsideIncome0._name, thisYear);
	_outsideIncome0 = outsideIncome0;
	if (_outsideIncome0._year > 0) {
	    /** Signals a "one-off" year. */
	    _amount = thisYear == _outsideIncome0._year ? _outsideIncome0._amount : 0d;
	    return;
	}
	final int baseDateYear = rothCalculator._baseYear._baseDateYear;
	if (thisYear == baseDateYear) {
	    _amount = _outsideIncome0._amount;
	} else {
	    final OutsideIncome1 pvsOutsideIncome1 = rothCalculator.getOutsideIncome1(_outsideIncome0, thisYear - 1);
	    final double inflationFactor = rothCalculator.getInflationFactor(thisYear);
	    _amount = inflationFactor * pvsOutsideIncome1._amount;
	}
    }

    public String getString() {
	return String.format("OutsideIncome1[%s], Amnt[%s]", _name, //
		TypeOfDouble.MONEY.format(_amount, 2));
    }

    @Override
    public String toString() {
	return getString();
    }
}
