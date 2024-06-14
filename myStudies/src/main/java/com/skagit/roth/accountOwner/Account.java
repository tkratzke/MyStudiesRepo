package com.skagit.roth.accountOwner;

import com.skagit.roth.workBookConcepts.Block;
import com.skagit.roth.workBookConcepts.WorkBookConcepts;
import com.skagit.util.MyStudiesStringUtils;
import com.skagit.util.NamedEntity;

public class Account extends NamedEntity {
    final int _useToPayPriority;
    final int _convertToRothPriority;
    final int _ageOfFirstRmd;
    final double _divisorForCurrentYearRmd;
    final double _currentBalance;
    final double _basis;
    final double _currentShort;
    final double _currentLong;

    public Account(final Block[] accountsBlocks, final AccountOwner owner, final String name) {
	super(name);
	final double useToPayPriority = WorkBookConcepts.getDouble(accountsBlocks, "Accounts and Use to Pay Priorities",
		_name);
	if (useToPayPriority >= 0d) {
	    _useToPayPriority = (int) Math.round(useToPayPriority);
	} else {
	    _useToPayPriority = Integer.MAX_VALUE;
	}
	final double convertToRothPriority = WorkBookConcepts.getDouble(accountsBlocks,
		"Accounts and Convert to Roth Priorities", _name);
	if (convertToRothPriority >= 0d) {
	    _convertToRothPriority = (int) Math.round(convertToRothPriority);
	} else {
	    _convertToRothPriority = Integer.MAX_VALUE;
	}
	final double ageOfFirstRmd = WorkBookConcepts.getDouble(accountsBlocks, "Accounts and Ages of First RMD",
		_name);
	if (ageOfFirstRmd >= 0d) {
	    _ageOfFirstRmd = (int) Math.round(ageOfFirstRmd);
	} else {
	    _ageOfFirstRmd = Integer.MAX_VALUE;
	}
	final double divisorForCurrentYearRmd = WorkBookConcepts.getDouble(accountsBlocks,
		"Accounts and Divisors for Current Year", _name);
	if (divisorForCurrentYearRmd >= 0d) {
	    _divisorForCurrentYearRmd = divisorForCurrentYearRmd;
	} else {
	    _divisorForCurrentYearRmd = Double.NaN;
	}
	final double currentBalance = WorkBookConcepts.getDouble(accountsBlocks, "Accounts and Current Balances",
		_name);
	if (currentBalance >= 0d) {
	    _currentBalance = currentBalance;
	} else {
	    _currentBalance = Double.NaN;
	}
	final double basis = WorkBookConcepts.getDouble(accountsBlocks, "Accounts and Bases", _name);
	if (basis >= 0d) {
	    _basis = basis;
	} else {
	    _basis = Double.NaN;
	}
	final double currentShort = WorkBookConcepts.getDouble(accountsBlocks, _name, "Current Short-Term");
	if (currentShort >= 0d) {
	    _currentShort = currentShort;
	} else {
	    _currentShort = Double.NaN;
	}
	final double currentLong = WorkBookConcepts.getDouble(accountsBlocks, _name, "Current Long-Term");
	if (currentLong >= 0d) {
	    _currentLong = currentLong;
	} else {
	    _currentLong = Double.NaN;
	}
    }

    @Override
    public String getString() {
	String s = _name;
	if (0 <= _useToPayPriority && _useToPayPriority < Integer.MAX_VALUE) {
	    s += String.format(", UseToPayPri[%d]", _useToPayPriority);
	}
	if (0 <= _convertToRothPriority && _convertToRothPriority < Integer.MAX_VALUE) {
	    s += String.format(", ConvertToRothPri[%d]", _convertToRothPriority);
	}
	if (0 <= _ageOfFirstRmd && _ageOfFirstRmd < Integer.MAX_VALUE) {
	    s += String.format(", AgeOfFirstRmd[%d]", _ageOfFirstRmd);
	}
	if (_divisorForCurrentYearRmd > 0d) {
	    s += String.format(", DvsrFrCrrntYr[%s]", MyStudiesStringUtils.formatOther(_divisorForCurrentYearRmd, 1));
	}
	if (Double.isFinite(_currentBalance)) {
	    s += String.format(", CrrntBlnc[%s]", MyStudiesStringUtils.formatDollars(_currentBalance));
	}
	if (Double.isFinite(_basis)) {
	    s += String.format(", Bss[%s]", MyStudiesStringUtils.formatDollars(_basis));
	}
	if (Double.isFinite(_currentShort)) {
	    s += String.format(", CrrntShrt[%s]", MyStudiesStringUtils.formatDollars(_currentShort));
	}
	if (Double.isFinite(_currentLong)) {
	    s += String.format(", CrrntLng[%s]", MyStudiesStringUtils.formatDollars(_currentLong));
	}
	return s;
    }

}
