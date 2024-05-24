package com.skagit.roth.taxYear;

import com.skagit.roth.currentYear.CurrentYear;
import com.skagit.roth.currentYear.Owner0;
import com.skagit.roth.rothCalculator.Brackets;
import com.skagit.roth.rothCalculator.RothCalculator;
import com.skagit.roth.workBookConcepts.Block;
import com.skagit.roth.workBookConcepts.Line;
import com.skagit.roth.workBookConcepts.WorkBookConcepts;
import com.skagit.util.TypeOfDouble;

public class TaxYear {

    public final RothCalculator _rothCalculator;
    public final int _thisYear;
    public final double _inflationFactor;
    public final double _investmentsFactor;
    public final double _standardDeduction;
    public final double _partBStandardPremium;
    public final Brackets[] _bracketsS;
    public final Owner1[] _owner1s;
    public final KeyValues _keyValues;

    public TaxYear(final RothCalculator rothCalculator, final int thisYear) {
	_rothCalculator = rothCalculator;
	_thisYear = thisYear;
	final CurrentYear currentYear = _rothCalculator._currentYear;
	final int myIdx = _thisYear - currentYear.getCurrentYear();
	final TaxYear pvsYear = myIdx == 0 ? null : _rothCalculator._taxYears[myIdx - 1];
	final int nBracketsS = RothCalculator._BracketsNames.length;
	final double inflationExpRate = _rothCalculator._inflationGrowthRate._expGrowthRate;
	final double deltaT = pvsYear == null ? currentYear._perCentLeftOfCurrentYear : 1d;
	_inflationFactor = Math.exp(inflationExpRate * deltaT);
	final double investmentsExpRate = _rothCalculator._investmentsGrowthRate._expGrowthRate;
	_investmentsFactor = Math.exp(investmentsExpRate * deltaT);
	if (pvsYear == null) {
	    _standardDeduction = currentYear._standardDeductionCurrentYear;
	    _partBStandardPremium = currentYear._partBPremiumCurrentYear;
	    _bracketsS = currentYear._bracketsCurrentYear;
	} else {
	    _standardDeduction = pvsYear._standardDeduction * _inflationFactor;
	    _partBStandardPremium = pvsYear._partBStandardPremium * _inflationFactor;
	    _bracketsS = new Brackets[nBracketsS];
	    for (int k0 = 0; k0 < nBracketsS; ++k0) {
		final Brackets oldBrackets = pvsYear._bracketsS[k0];
		final Block newBracketsBlock = getNewBracketsBlock(oldBrackets);
		final Line[] newLines = newBracketsBlock == null ? null : newBracketsBlock._lines;
		final int nNewLines = newLines == null ? 0 : newLines.length;
		final Brackets newBrackets = new Brackets(oldBrackets);
		final int nOldLines = oldBrackets._perCentCeilings.length;
		if (nNewLines == nOldLines) {
		    for (int k1 = 0; k1 < nOldLines; ++k1) {
			newBrackets._perCentCeilings[k1]._ceiling *= _inflationFactor;
			if (nNewLines == nOldLines) {
			    newBrackets._perCentCeilings[k1]._perCent = 100d * newLines[k1]._header._d;
			}
		    }
		}
		_bracketsS[k0] = newBrackets;
	    }
	}
	final Owner0[] owner0s = _rothCalculator._currentYear._owner0s;
	final int nOwner0s = owner0s.length;
	_owner1s = new Owner1[nOwner0s];
	for (int k = 0; k < nOwner0s; ++k) {
	    _owner1s[k] = new Owner1(owner0s[k], this);
	}
	_keyValues = new KeyValues(this);
    }

    public Block getNewBracketsBlock(final Brackets oldBrackets) {
	final WorkBookConcepts workBookConcepts = _rothCalculator._workBookConcepts;
	return workBookConcepts.getBlock(WorkBookConcepts._SheetNames[WorkBookConcepts._BracketsSheetIdx],
		oldBrackets._name + " " + _thisYear);
    }

    private final static boolean _DumpBracketsS = false;

    public String getString() {
	String s = String.format("Year %d, StdDdctn[%s] PrtBPrmm[%s]", //
		_thisYear, //
		TypeOfDouble.MONEY.format(_standardDeduction, 2), //
		TypeOfDouble.MONEY.format(_partBStandardPremium, 2) //
	);
	if (_DumpBracketsS) {
	    final int nBracketsS = _bracketsS.length;
	    for (int k = 0; k < nBracketsS; ++k) {
		s += "\n\n" + _bracketsS[k].getString();
	    }
	}
	final int nOwner1s = _owner1s.length;
	for (int k = 0; k < nOwner1s; ++k) {
	    s += "\n\n" + _owner1s[k].getString();
	}
	return s;
    }

}
