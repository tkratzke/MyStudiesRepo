package com.skagit.roth.taxYear;

import java.util.Arrays;

import com.skagit.roth.rothCalculator.Brackets;
import com.skagit.roth.rothCalculator.RothCalculator;
import com.skagit.roth.workBookConcepts.Block;
import com.skagit.roth.workBookConcepts.Line;
import com.skagit.roth.workBookConcepts.WorkBookConcepts;
import com.skagit.util.TypeOfDouble;

public class ParameterSet {

    public final double _standardDeduction;
    public final double _partBStandardPremium;
    public final double _maxCapitalGainsLoss;
    public final double _medicareTaxThreshold;
    public final Brackets[] _bracketsS;

    public ParameterSet(final RothCalculator rothCalculator, final int thisYear, final int baseDateYear) {
	final int nBracketsS = RothCalculator._BracketsNames.length;
	if (thisYear == baseDateYear) {
	    final WorkBookConcepts workBookConcepts = rothCalculator._workBookConcepts;
	    _standardDeduction = workBookConcepts.getMiscellaneousData("Standard Deduction Base Year")._d;
	    _partBStandardPremium = workBookConcepts.getMiscellaneousData("Part B Standard Premium Base Year")._d;
	    _maxCapitalGainsLoss = workBookConcepts.getMiscellaneousData("Max Capital Gains Loss Base Year")._d;
	    _medicareTaxThreshold = workBookConcepts.getMiscellaneousData("Medicare Tax Threshold Base Year")._d;
	    _bracketsS = new Brackets[nBracketsS];
	    for (int k = 0; k < nBracketsS; ++k) {
		_bracketsS[k] = new Brackets(workBookConcepts, RothCalculator._BracketsNames[k]);
	    }
	    Arrays.sort(_bracketsS);
	} else {
	    final int myIdx = thisYear - baseDateYear;
	    final ParameterSet pvsParameterSet = rothCalculator._taxYears[myIdx - 1]._parameterSet;
	    final double inflationFactor = rothCalculator.getInflationFactor(thisYear);
	    _standardDeduction = pvsParameterSet._standardDeduction * inflationFactor;
	    _partBStandardPremium = pvsParameterSet._partBStandardPremium * inflationFactor;
	    _maxCapitalGainsLoss = pvsParameterSet._maxCapitalGainsLoss * inflationFactor;
	    _medicareTaxThreshold = pvsParameterSet._medicareTaxThreshold * inflationFactor;
	    _bracketsS = new Brackets[nBracketsS];
	    final Brackets[] pvsYearBracketsS = pvsParameterSet._bracketsS;
	    for (int k0 = 0; k0 < nBracketsS; ++k0) {
		final Brackets oldBrackets = pvsYearBracketsS[k0];
		final Brackets newBrackets = new Brackets(oldBrackets);
		final Block newBracketsBlock = getNewBracketsBlock(oldBrackets, rothCalculator, thisYear);
		final Line[] newLines = newBracketsBlock == null ? null : newBracketsBlock._lines;
		final int nNewLines = newLines == null ? 0 : newLines.length;
		final int nOldLines = oldBrackets._perCentCeilings.length;
		if (nNewLines == nOldLines) {
		    for (int k1 = 0; k1 < nOldLines; ++k1) {
			newBrackets._perCentCeilings[k1]._ceiling *= inflationFactor;
			if (nNewLines == nOldLines) {
			    newBrackets._perCentCeilings[k1]._perCent = 100d * newLines[k1]._header._d;
			}
		    }
		}
		_bracketsS[k0] = newBrackets;
	    }
	}
    }

    public Block getNewBracketsBlock(final Brackets oldBrackets, final RothCalculator rothCalculator,
	    final int thisYear) {
	final WorkBookConcepts workBookConcepts = rothCalculator._workBookConcepts;
	return workBookConcepts.getBlock(WorkBookConcepts._SheetNames[WorkBookConcepts._BracketsSheetIdx],
		oldBrackets._name + " " + thisYear);
    }

    private final static boolean _DumpBracketsS = false;

    public String getString() {
	String s = String.format("StdDdctn[%s] PrtBPrmm[%s]", //
		TypeOfDouble.MONEY.format(_standardDeduction, 2), //
		TypeOfDouble.MONEY.format(_partBStandardPremium, 2), //
		TypeOfDouble.MONEY.format(_maxCapitalGainsLoss, 2), //
		TypeOfDouble.MONEY.format(_medicareTaxThreshold, 2) //
	);
	if (_DumpBracketsS) {
	    final int nBracketsS = _bracketsS.length;
	    for (int k = 0; k < nBracketsS; ++k) {
		s += "\n\n" + _bracketsS[k].getString();
	    }
	}
	return s;
    }

    @Override
    public String toString() {
	return getString();
    }

}
