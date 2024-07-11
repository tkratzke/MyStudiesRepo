package com.skagit.rothProblem.taxYear;

import com.skagit.rothProblem.RothProblem;
import com.skagit.rothProblem.owner0.Account0;
import com.skagit.rothProblem.owner0.OutsideIncome0;
import com.skagit.rothProblem.owner0.Owner0;
import com.skagit.rothProblem.parameters.Parameters;
import com.skagit.rothProblem.workBookConcepts.WorkBookConcepts;
import com.skagit.util.MyStudiesStringUtils;

public class TaxYear {

    final RothProblem _rothProblem;
    final Parameters _parameters;
    final TaxYear _pvsYear;
    final double _ttlSsa;
    final double _ttlOutsideIncome;
    final double _livingExpenses;
    private final Account1[] _account1s;

    public TaxYear(final RothProblem rothProblem, final Parameters parameters) {
	_rothProblem = rothProblem;
	_parameters = parameters;
	_pvsYear = null;
	final int firstYear = _parameters._thisYear;
	double ttlSsa = 0d;
	double ttlOutsideIncome = 0d;
	final int nOwner0s = _rothProblem._owner0s.length;
	int nAccount1s = 0;
	for (int k0 = 0; k0 < nOwner0s; ++k0) {
	    final Owner0 owner0 = _rothProblem._owner0s[k0];
	    final double currentSsa = owner0._currentSsa;
	    if (Double.isFinite(currentSsa)) {
		ttlSsa += currentSsa;
	    }
	    final OutsideIncome0[] outsideIncome0s = owner0._outsideIncome0s;
	    final int nOutsideIncome0s = outsideIncome0s.length;
	    for (int k1 = 0; k1 < nOutsideIncome0s; ++k1) {
		final OutsideIncome0 outsideIncome0 = outsideIncome0s[k1];
		final int outsideIncome0Year = outsideIncome0._year;
		if (outsideIncome0Year != WorkBookConcepts._NotAnInteger) {
		    ttlOutsideIncome += outsideIncome0Year == firstYear ? outsideIncome0._amount : 0d;
		} else {
		    ttlOutsideIncome += outsideIncome0._amount;
		}
	    }
	    nAccount1s += owner0._account0s.length;
	}
	_ttlSsa = ttlSsa;
	_ttlOutsideIncome = ttlOutsideIncome;
	_livingExpenses = _rothProblem._livingExpenses;
	_account1s = new Account1[nAccount1s];
	for (int k0 = 0, k1 = 0; k0 < nOwner0s; ++k0) {
	    final Owner0 owner0 = _rothProblem._owner0s[k0];
	    final Account0[] account0s = owner0._account0s;
	    final int nAccount0s = account0s.length;
	    for (int k2 = 0; k2 < nAccount0s; ++k2) {
		_account1s[k1++] = new Account1(account0s[k2]);
	    }
	}
    }

    public TaxYear(final TaxYear pvsYear) {
	_pvsYear = pvsYear;
	_rothProblem = _pvsYear._rothProblem;
	_parameters = new Parameters(_pvsYear._parameters);
	final int thisYear = _parameters._thisYear;
	final double inflationFactor = Math.exp(_parameters._inflationGrowthRate._expGrowthRate);
	_ttlSsa = inflationFactor * pvsYear._ttlSsa;
	double ttlOutsideIncome = 0d;
	final int nOwner0s = _rothProblem._owner0s.length;
	for (int k0 = 0; k0 < nOwner0s; ++k0) {
	    final Owner0 owner0 = _rothProblem._owner0s[k0];
	    final OutsideIncome0[] outsideIncome0s = owner0._outsideIncome0s;
	    final int nOutsideIncome0s = outsideIncome0s.length;
	    for (int k1 = 0; k1 < nOutsideIncome0s; ++k1) {
		final OutsideIncome0 outsideIncome0 = outsideIncome0s[k1];
		final int outsideIncome0Year = outsideIncome0._year;
		if (outsideIncome0Year != WorkBookConcepts._NotAnInteger) {
		    ttlOutsideIncome += outsideIncome0Year == thisYear ? outsideIncome0._amount : 0d;
		} else {
		    ttlOutsideIncome += outsideIncome0._amount * inflationFactor;
		}
	    }
	}
	_ttlOutsideIncome = ttlOutsideIncome;
	_livingExpenses = pvsYear._livingExpenses * inflationFactor;
	final int nAccount1s = _pvsYear._account1s.length;
	_account1s = new Account1[nAccount1s];
	for (int k1 = 0; k1 < nAccount1s; ++k1) {
	    _account1s[k1] = new Account1(_pvsYear._account1s[k1]);
	}
    }

    public String getString() {
	String s = _parameters.getString();
	s += String.format("\n\nttlSsa[%s] ttlOutsideIncome[%s] livingExpenses[%s]", //
		MyStudiesStringUtils.formatDollars(_ttlSsa), //
		MyStudiesStringUtils.formatDollars(_ttlOutsideIncome), //
		MyStudiesStringUtils.formatDollars(_livingExpenses) //
	);
	return s;
    }

    @Override
    public String toString() {
	return getString();
    }

}
