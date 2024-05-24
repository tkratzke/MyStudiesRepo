package com.skagit.roth.taxYear;

import com.skagit.roth.currentYear.OutsideIncome0;
import com.skagit.util.NamedEntity;
import com.skagit.util.TypeOfDouble;

public class OutsideIncome1 extends NamedEntity {

    public final OutsideIncome0 _oi0;
    public final double _amount;

    public OutsideIncome1(final OutsideIncome0 oi0, final TaxYear taxYear) {
	super(oi0._name, taxYear._thisYear);
	final int thisYear = taxYear._thisYear;
	_oi0 = oi0;
	if (_oi0._year > 0) {
	    /** Signals a "one-off" year. */
	    _amount = thisYear == _oi0._year ? _oi0._amount : 0d;
	    return;
	}
	final OutsideIncome1 pvsOi1 = taxYear._rothCalculator.getOi1(_oi0, thisYear - 1);
	if (pvsOi1 == null) {
	    _amount = _oi0._amount;
	} else {
	    _amount = taxYear._inflationFactor * pvsOi1._amount;
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
