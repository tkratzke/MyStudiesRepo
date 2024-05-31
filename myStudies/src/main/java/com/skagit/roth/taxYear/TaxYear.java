package com.skagit.roth.taxYear;

import com.skagit.roth.baseYear.Owner0;
import com.skagit.roth.rothCalculator.RothCalculator;
import com.skagit.util.TypeOfDouble;

public class TaxYear {

    public final RothCalculator _rothCalculator;
    public final int _thisYear;
    public final ParameterSet _parameterSet;
    public final KeyValues _keyValues;

    public final Owner1[] _owner1s;

    public TaxYear(final RothCalculator rothCalculator, final int thisYear) {
	_rothCalculator = rothCalculator;
	_thisYear = thisYear;
	_parameterSet = new ParameterSet(rothCalculator, thisYear, rothCalculator._baseYear._baseDateYear);
	final Owner0[] owner0s = _rothCalculator._baseYear._owner0s;
	final int nOwner0s = owner0s.length;
	_owner1s = new Owner1[nOwner0s];
	double ttlSocialSecurityBenefits = 0d;
	for (int k = 0; k < nOwner0s; ++k) {
	    _owner1s[k] = new Owner1(owner0s[k], _rothCalculator, _thisYear);
	    ttlSocialSecurityBenefits += _owner1s[k]._ssaIncome;
	}
	_keyValues = new KeyValues(this, ttlSocialSecurityBenefits);
    }

    public String getString() {
	String s = String.format("Year[%d], TtlSsa[%s] %s", _thisYear, _parameterSet.getString(),
		TypeOfDouble.MONEY.format(_keyValues._ttlSocialSecurityBenefits, /* nDigits= */2));
	final int nOwner1s = _owner1s.length;
	for (int k = 0; k < nOwner1s; ++k) {
	    s += "\n\n" + _owner1s[k].getString();
	}
	return s;
    }

    public void computeKeyValues(final int perCentToConvert) {

    }

}
