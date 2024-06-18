package com.skagit.rothProblem.taxYear;

import com.skagit.rothProblem.CapitalGains;
import com.skagit.rothProblem.RothProblem;
import com.skagit.rothProblem.owner0.Account0;
import com.skagit.rothProblem.owner0.OutsideIncome0;
import com.skagit.rothProblem.owner0.Owner0;
import com.skagit.rothProblem.parameters.Parameters;
import com.skagit.rothProblem.workBookConcepts.WorkBookConcepts;
import com.skagit.util.MyStudiesDateUtils;

public class TaxYear {

    final RothProblem _rothProblem;
    final int _thisYear;
    final double _inflationFactor;
    final double _ttlSsa;
    final double _ttlOutsideIncome;
    final double _livingExpenses;
    final CapitalGains _capitalGains;

    public TaxYear(final RothProblem rothProblem) {
	/** This ctor creates the TaxYear for the currentYear. */
	_rothProblem = rothProblem;
	final Parameters parameters = Parameters._CurrentYearParameters;
	final int firstYear = parameters._currentYear;
	_thisYear = firstYear;
	final double proportionOfYear = MyStudiesDateUtils.getProportionOfYear(_rothProblem._currentDate);
	_inflationFactor = Math
		.exp(Parameters._CurrentYearParameters._inflationGrowthRate._expGrowthRate * proportionOfYear);
	double ttlSsa = 0d;
	double ttlOutsideIncome = 0d;
	final int nOwner0s = _rothProblem._owner0s.length;
	for (int k0 = 0; k0 < nOwner0s; ++k0) {
	    final Owner0 owner0 = _rothProblem._owner0s[k0];
	    ttlSsa += owner0._currentSsa;
	    final OutsideIncome0[] outsideIncome0s = owner0._outsideIncome0s;
	    final int nOutsideIncome0s = outsideIncome0s.length;
	    for (int k1 = 0; k1 < nOutsideIncome0s; ++k1) {
		final OutsideIncome0 outsideIncome0 = outsideIncome0s[k1];
		final int outsideIncome0Year = outsideIncome0._year;
		if (outsideIncome0Year != WorkBookConcepts._NotAnInteger) {
		    ttlOutsideIncome += outsideIncome0Year == _thisYear ? outsideIncome0._amount : 0d;
		} else {
		    ttlOutsideIncome += outsideIncome0._amount;
		}
	    }
	}
	_ttlSsa = ttlSsa;
	_ttlOutsideIncome = ttlOutsideIncome;
	_livingExpenses = _rothProblem._currentLivingExpenses;
	final double maxCapitalGainsLoss = parameters._maxCapitalGainsLoss;
	double thisYearShortTermCg = 0d;
	double thisYearLongTermCg = 0d;
	for (int k0 = 0; k0 < nOwner0s; ++k0) {
	    final Owner0 owner0 = _rothProblem._owner0s[k0];
	    ttlSsa += owner0._currentSsa;
	    final Account0[] account0s = owner0._account0s;
	    final int nAccount0s = account0s.length;
	    for (int k1 = 0; k1 < nAccount0s; ++k1) {
		final Account0 account0 = account0s[k1];
		double thisShortTermCg = account0._shortTermCapitalGain;
		double thisLongTermCg = account0._longTermCapitalGain;
		if (account0._projectCapitalGains) {
		    thisShortTermCg *= 1d / proportionOfYear;
		    thisLongTermCg *= 1d / proportionOfYear;
		}
		thisYearShortTermCg += thisShortTermCg;
		thisYearLongTermCg += thisLongTermCg;
	    }
	}
	final double shortTermCarryForwardIn = _rothProblem._currentShCf;
	final double longTermCarryForwardIn = _rothProblem._currentElCf;
	_capitalGains = new CapitalGains(maxCapitalGainsLoss, //
		thisYearShortTermCg, thisYearLongTermCg, //
		shortTermCarryForwardIn, longTermCarryForwardIn);
    }

    public TaxYear(final RothProblem rothProblem, final TaxYear pvsYear) {
	_rothProblem = rothProblem;
	final int firstYear = Parameters._CurrentYearParameters._currentYear;
	_thisYear = pvsYear._thisYear + 1;
	_inflationFactor = Math.exp(Parameters._CurrentYearParameters._inflationGrowthRate._expGrowthRate);
	_ttlSsa = _inflationFactor * pvsYear._ttlSsa;
	double ttlOutsideIncome = 0d;
	final double outsideIncomeInflationFactor = Math
		.exp(Parameters._CurrentYearParameters._inflationGrowthRate._expGrowthRate * (_thisYear - firstYear));
	final int nOwner0s = _rothProblem._owner0s.length;
	for (int k0 = 0; k0 < nOwner0s; ++k0) {
	    final Owner0 owner0 = _rothProblem._owner0s[k0];
	    final OutsideIncome0[] outsideIncome0s = owner0._outsideIncome0s;
	    final int nOutsideIncome0s = outsideIncome0s.length;
	    for (int k1 = 0; k1 < nOutsideIncome0s; ++k1) {
		final OutsideIncome0 outsideIncome0 = outsideIncome0s[k1];
		final int outsideIncome0Year = outsideIncome0._year;
		if (outsideIncome0Year != WorkBookConcepts._NotAnInteger) {
		    ttlOutsideIncome += outsideIncome0Year == _thisYear ? outsideIncome0._amount : 0d;
		} else {
		    ttlOutsideIncome += outsideIncome0._amount * outsideIncomeInflationFactor;
		}
	    }
	}
	_ttlOutsideIncome = ttlOutsideIncome;
	_livingExpenses = pvsYear._livingExpenses * _inflationFactor;
	/** Still must do this. */
	_capitalGains = null;
    }

}
