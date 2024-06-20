package com.skagit.rothProblem.taxYear;

import com.skagit.rothProblem.RothProblem;
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

    public TaxYear(final RothProblem rothProblem, final Parameters parameters) {
	_rothProblem = rothProblem;
	_parameters = parameters;
	_pvsYear = null;
	final int firstYear = _parameters._thisYear;
	double ttlSsa = 0d;
	double ttlOutsideIncome = 0d;
	final int nOwner0s = _rothProblem._owner0s.length;
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
	}
	_ttlSsa = ttlSsa;
	_ttlOutsideIncome = ttlOutsideIncome;
	_livingExpenses = _rothProblem._livingExpenses;
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
