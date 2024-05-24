package com.skagit.roth.taxYear;

import com.skagit.roth.currentYear.Account0;
import com.skagit.roth.currentYear.OutsideIncome0;
import com.skagit.roth.currentYear.Owner0;
import com.skagit.roth.rothCalculator.RothCalculator;
import com.skagit.util.NamedEntity;
import com.skagit.util.TypeOfDouble;

/**
 * We assume RMDs are taken at the beginning of each year, and Roth Conversions
 * are taken at the end of each year.
 */
public class Owner1 extends NamedEntity {

    public final Owner0 _owner0;
    public final double _ssa;
    public final Account1[] _myAccounts1;
    public final OutsideIncome1[] _oi1s;

    public Owner1(final Owner0 owner0, final TaxYear taxYear) {
	super(owner0._name, taxYear._thisYear);
	final int thisYear = taxYear._thisYear;
	_owner0 = owner0;
	final RothCalculator rothCalculator = taxYear._rothCalculator;
	final double inflationFactor = taxYear._inflationFactor;
	final Owner1 pvsOwner1 = rothCalculator.getOwner1(_owner0, thisYear - 1);
	_ssa = pvsOwner1 == null ? _owner0._ssa : (pvsOwner1._ssa * inflationFactor);

	final Account0[] ira0s = _owner0._iras;
	final int nIras = ira0s.length;
	_myAccounts1 = new Account1[nIras];
	for (int k = 0; k < nIras; ++k) {
	    _myAccounts1[k] = new Account1(ira0s[k], taxYear);
	}
	final OutsideIncome0[] oi0s = _owner0._outsideIncomes;
	final int nOis = oi0s.length;
	_oi1s = new OutsideIncome1[nOis];
	for (int k = 0; k < nOis; ++k) {
	    _oi1s[k] = new OutsideIncome1(oi0s[k], taxYear);
	}
    }

    public String getString() {
	String s = String.format("Owner1[%s], SSA[%s]", _name, //
		TypeOfDouble.MONEY.format(_ssa, 2));
	final int nIras = _myAccounts1.length;
	for (int k = 0; k < nIras; ++k) {
	    s += "\n\t" + _myAccounts1[k].getString();
	}
	final int nOutsideIncomes = _oi1s.length;
	for (int k = 0; k < nOutsideIncomes; ++k) {
	    s += "\n\t" + _oi1s[k].getString();
	}
	return s;
    }

    @Override
    public String toString() {
	return getString();
    }
}
