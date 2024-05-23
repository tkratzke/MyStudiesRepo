package com.skagit.roth;

import java.util.Arrays;

import com.skagit.util.MyStudiesStringUtils;
import com.skagit.util.NamedEntity;
import com.skagit.util.TypeOfDouble;

public class Account extends NamedEntity {

    public final RothCalculator.Owner _owner;
    public final double _ageOfRmd;
    public final double _currentDivisor;
    public final double _balanceBeginningOfCurrentYear;
    public final double _currentBalance;

    public Account(final String name, final RothCalculator rothCalculator, final RothCalculator.Owner owner) {
	super(name);
	_owner = owner;
	final Line forLookUp = new Line(_name);
	final String staticsSheetName = RothCalculator.getSheetName(RothCalculator._StaticsSheetIdx);
	if (_owner != null) {
	    final Line[] ageBlockLines = rothCalculator.getBlock(staticsSheetName, "Age of RMD")._lines;
	    final int idx0 = Arrays.binarySearch(ageBlockLines, forLookUp);
	    _ageOfRmd = idx0 < 0 ? 0d : ageBlockLines[idx0]._data._d;
	    final Line[] divisorLines = rothCalculator.getBlock(staticsSheetName, "Divisor Current Year")._lines;
	    final int idx2 = Arrays.binarySearch(divisorLines, forLookUp);
	    _currentDivisor = idx2 < 0 ? Double.NaN : divisorLines[idx2]._data._d;
	} else {
	    _ageOfRmd = 0d;
	    _currentDivisor = Double.NaN;
	}
	final Line[] balance0Lines = rothCalculator.getBlock(staticsSheetName,
		"Balance Beginning of Current Year")._lines;
	final int idx3 = Arrays.binarySearch(balance0Lines, forLookUp);
	_balanceBeginningOfCurrentYear = balance0Lines[idx3]._data._d;
	final Line[] balance1Lines = rothCalculator.getBlock(staticsSheetName, "Current Balance")._lines;
	final int idx4 = Arrays.binarySearch(balance1Lines, forLookUp);
	_currentBalance = balance1Lines[idx4]._data._d;
    }

    public String getString() {
	String s = String.format("ACCNT[%s]", _name);
	if (_owner != null) {
	    s += String.format(", OWNR[%s]", _owner._name);
	}
	s += String.format(", BlncBgnnngYr[%s] CrrntBlnc[%s]", //
		TypeOfDouble.MONEY.format(_balanceBeginningOfCurrentYear, 2), //
		TypeOfDouble.MONEY.format(_currentBalance, 2));
	if (_ageOfRmd > 0d) {
	    s += String.format(", Age at RMD[%s]", MyStudiesStringUtils.formatOther(_ageOfRmd, 1));
	} else if (_currentDivisor > 0d) {
	    s += String.format(", CrrntDvsr[%s]", //
		    MyStudiesStringUtils.formatOther(_currentDivisor, 1));
	}
	return s;
    }

    @Override
    public String toString() {
	return getString();
    }
}
