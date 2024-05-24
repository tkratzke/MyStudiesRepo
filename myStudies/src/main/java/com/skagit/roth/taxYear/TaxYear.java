package com.skagit.roth.taxYear;

import com.skagit.roth.baseYear.Owner0;
import com.skagit.roth.rothCalculator.RothCalculator;

public class TaxYear {

    public final RothCalculator _rothCalculator;
    public final int _thisYear;
    public final ParameterSet _parameterSet;

    public final Owner1[] _owner1s;
    public final KeyValues _keyValues;

    public TaxYear(final RothCalculator rothCalculator, final int thisYear) {
	_rothCalculator = rothCalculator;
	_thisYear = thisYear;
	_parameterSet = new ParameterSet(rothCalculator, thisYear, rothCalculator._baseYear._baseDateYear);
	final Owner0[] owner0s = _rothCalculator._baseYear._owner0s;
	final int nOwner0s = owner0s.length;
	_owner1s = new Owner1[nOwner0s];
	for (int k = 0; k < nOwner0s; ++k) {
	    _owner1s[k] = new Owner1(owner0s[k], _rothCalculator, _thisYear);
	}
	_keyValues = new KeyValues(this);
    }

    public String getString() {
	String s = String.format("Year[%d], %s", _thisYear, _parameterSet.getString());
	final int nOwner1s = _owner1s.length;
	for (int k = 0; k < nOwner1s; ++k) {
	    s += "\n\n" + _owner1s[k].getString();
	}
	return s;
    }

}
