package com.skagit.roth.baseYear;

import java.util.Arrays;

import com.skagit.roth.rothCalculator.InvestmentItem;
import com.skagit.roth.rothCalculator.InvestmentsEnum;
import com.skagit.roth.workBookConcepts.Line;
import com.skagit.roth.workBookConcepts.WorkBookConcepts;
import com.skagit.util.MyStudiesStringUtils;
import com.skagit.util.NamedEntity;
import com.skagit.util.TypeOfDouble;

public class Account0 extends NamedEntity {

    public final double _balanceBeginningOfBaseYear;
    public final double _baseBalance;
    /** If it's an IRA: */
    public final Owner0 _owner;
    public final double _ageOfRmd;
    public final double _baseDivisor;
    /** Otherwise: */
    public final InvestmentItem[] _investmentItems;

    public Account0(final String name, final WorkBookConcepts workBookConcepts, final Owner0 owner) {
	super(name);
	_owner = owner;
	final Line forLookUp = new Line(_name);
	final String staticsSheetName = WorkBookConcepts.getSheetName(WorkBookConcepts._StaticsSheetIdx);
	if (_owner != null) {
	    final Line[] ageBlockLines = workBookConcepts.getBlock(staticsSheetName, "Age of RMD")._lines;
	    final int idx0 = Arrays.binarySearch(ageBlockLines, forLookUp);
	    _ageOfRmd = idx0 < 0 ? 0d : ageBlockLines[idx0]._data._d;
	    final Line[] divisorLines = workBookConcepts.getBlock(staticsSheetName, "Divisor Base Year")._lines;
	    final int idx2 = Arrays.binarySearch(divisorLines, forLookUp);
	    _baseDivisor = idx2 < 0 ? Double.NaN : divisorLines[idx2]._data._d;
	    _investmentItems = null;
	} else {
	    _ageOfRmd = 0d;
	    _baseDivisor = Double.NaN;
	    _investmentItems = new InvestmentItem[InvestmentsEnum._Values.length];
	}
	final Line[] balance0Lines = workBookConcepts.getBlock(staticsSheetName,
		"Balance Beginning of Base Year")._lines;
	final int idx3 = Arrays.binarySearch(balance0Lines, forLookUp);
	_balanceBeginningOfBaseYear = balance0Lines[idx3]._data._d;
	final Line[] balance1Lines = workBookConcepts.getBlock(staticsSheetName, "Base Balance")._lines;
	final int idx4 = Arrays.binarySearch(balance1Lines, forLookUp);
	_baseBalance = balance1Lines[idx4]._data._d;
    }

    public String getString() {
	String s = String.format("ACCNT[%s]", _name);
	if (_owner != null) {
	    s += String.format(", OWNR[%s]", _owner._name);
	}
	s += String.format(", BlncBgnnngYr[%s] CrrntBlnc[%s]", //
		TypeOfDouble.MONEY.format(_balanceBeginningOfBaseYear, 2), //
		TypeOfDouble.MONEY.format(_baseBalance, 2));
	if (_ageOfRmd > 0d) {
	    s += String.format(", Age at RMD[%s]", MyStudiesStringUtils.formatOther(_ageOfRmd, 1));
	} else if (_baseDivisor > 0d) {
	    s += String.format(", CrrntDvsr[%s]", //
		    MyStudiesStringUtils.formatOther(_baseDivisor, 1));
	} else if (_investmentItems != null) {
	    s += "\nInvestmentItems";
	    final int nInvestmentItems = _investmentItems.length;
	    for (int k = 0; k < nInvestmentItems; ++k) {
		final InvestmentItem investmentItem = _investmentItems[k];
		s += String.format("\n%03d. %s", k, investmentItem.getString());
	    }
	}
	return s;
    }

    @Override
    public String toString() {
	return getString();
    }
}