package com.skagit.roth.taxYear;

import java.util.Arrays;

import com.skagit.roth.baseYear.Account0;
import com.skagit.roth.baseYear.OutsideIncome0;
import com.skagit.roth.baseYear.Owner0;
import com.skagit.roth.rothCalculator.RothCalculator;
import com.skagit.util.NamedEntity;
import com.skagit.util.TypeOfDouble;

public class Owner1 extends NamedEntity {

    public final Owner0 _owner0;
    public final double _ssaIncome;
    public final Account1[] _account1s;
    public final OutsideIncome1[] _outsideIncome1s;

    public Owner1(final Owner0 owner0, final RothCalculator rothCalculator, final int thisYear) {
	super(owner0._name);
	_owner0 = owner0;
	final double inflationFactor = rothCalculator.getInflationFactor(thisYear);
	final Owner1 pvsOwner1 = rothCalculator.getOwner1(_owner0, thisYear - 1);
	_ssaIncome = pvsOwner1 == null ? _owner0._ssIncome : (pvsOwner1._ssaIncome * inflationFactor);

	final Account0[] ira0s = _owner0._iras;
	final int nIras = ira0s.length;
	_account1s = new Account1[nIras];
	for (int k = 0; k < nIras; ++k) {
	    _account1s[k] = new Account1(ira0s[k], rothCalculator, thisYear);
	}
	Arrays.sort(_account1s);
	final OutsideIncome0[] outsideIncome0s = _owner0._outsideIncome0s;
	final int nOutsideIncome0s = outsideIncome0s.length;
	_outsideIncome1s = new OutsideIncome1[nOutsideIncome0s];
	for (int k = 0; k < nOutsideIncome0s; ++k) {
	    _outsideIncome1s[k] = new OutsideIncome1(outsideIncome0s[k], rothCalculator, thisYear);
	}
	Arrays.sort(_outsideIncome1s);
    }

    public String getString() {
	String s = String.format("Owner1[%s], SSA[%s]", _name, //
		TypeOfDouble.MONEY.format(_ssaIncome, 2));
	final int nIras = _account1s.length;
	for (int k = 0; k < nIras; ++k) {
	    s += "\n\t" + _account1s[k].getString();
	}
	final int nOutsideIncomes = _outsideIncome1s.length;
	for (int k = 0; k < nOutsideIncomes; ++k) {
	    s += "\n\t" + _outsideIncome1s[k].getString();
	}
	return s;
    }

    @Override
    public String toString() {
	return getString();
    }
}
