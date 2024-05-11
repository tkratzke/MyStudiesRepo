package com.skagit.roth;

public class YearOfData {
    final private static String _BracketsSheetName = RothCalculator._SheetNames[RothCalculator._BracketsIdx];

    public final RothCalculator _rothCalculator;
    public final int _thisYear;
    public final double _standardDeduction;
    public final double _partBStandardPremium;
    public final Brackets[] _bracketsS;

    public YearOfData(final RothCalculator rothCalculator, final int year) {
	_rothCalculator = rothCalculator;
	_thisYear = year;
	final int myIdx = _thisYear - _rothCalculator.getCurrentYear();
	final YearOfData pvsYear = myIdx == 0 ? null : _rothCalculator._yearsOfData[myIdx - 1];
	final int nBracketsS = RothCalculator._BracketsNames.length;
	final RothCalculator.GrowthRate inflationRate = _rothCalculator.getGrowthRate(RothCalculator._InflationIdx);
	final double inflationExpGrowthRate = inflationRate._expGrowthRate;
	final double deltaT = pvsYear == null ? _rothCalculator._remainderOfCurrentYear : 1d;
	final double inflationFactor = Math.exp(inflationExpGrowthRate * deltaT);
	if (pvsYear == null) {
	    _standardDeduction = _rothCalculator._standardDeductionCurrentYear;
	    _partBStandardPremium = _rothCalculator._partBPremiumCurrentYear;
	    _bracketsS = _rothCalculator._bracketsS;
	} else {
	    _standardDeduction = pvsYear._standardDeduction * inflationFactor;
	    _partBStandardPremium = pvsYear._partBStandardPremium * inflationFactor;
	    _bracketsS = new Brackets[nBracketsS];
	    for (int k0 = 0; k0 < nBracketsS; ++k0) {
		final Brackets oldBrackets = pvsYear._bracketsS[k0];
		final String name = oldBrackets._name;
		final String name2 = name + " " + _thisYear;
		final Block block2 = _rothCalculator.getBlock(_BracketsSheetName, name2);
		final Line[] lines2 = block2 == null ? null : block2._lines;
		final int nLines2 = lines2 == null ? 0 : lines2.length;
		final Brackets newBrackets = new Brackets(oldBrackets);
		final int nPerCentCeilings = oldBrackets._perCentCeilings.length;
		for (int k1 = 0; k1 < nPerCentCeilings; ++k1) {
		    newBrackets._perCentCeilings[k1]._ceiling *= inflationFactor;
		    if (nLines2 == nPerCentCeilings) {
			newBrackets._perCentCeilings[k1]._perCent = 100d * lines2[k1]._header._d;
		    }
		}
		_bracketsS[k0] = newBrackets;
	    }
	}
    }

    public String getString() {
	String s = String.format("Year %4d, Std Ded[$%.2f] PartBPrem[$%.2f]", _thisYear, _standardDeduction,
		_partBStandardPremium);
	final int nBracketsS = _bracketsS.length;
	for (int k = 0; k < nBracketsS; ++k) {
	    s += "\n\n" + _bracketsS[k].getString();
	}
	return s;
    }

}
