package com.skagit.roth.yearOfData;

import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;

import com.skagit.roth.Block;
import com.skagit.roth.Brackets;
import com.skagit.roth.Field;
import com.skagit.roth.Line;
import com.skagit.roth.RothCalculator;
import com.skagit.util.DateUtils;
import com.skagit.util.NamedEntity;

public class YearOfData {
    /**
     * We assume RMDs are taken at the beginning of each year, and Roth Conversions
     * are taken at the end of each year.
     */
    public class TP1 extends NamedEntity {

	public class IRA1 extends NamedEntity {

	    public final RothCalculator.TaxPayer.Ira _ira0;
	    public double _initialBalance;
	    public final double _rmd;
	    public double _rothConversion;
	    public double _finalBalance;

	    public IRA1(final RothCalculator.TaxPayer.Ira ira0) {
		super(ira0._name, _thisYear);
		_ira0 = ira0;
		final IRA1 pvsIra1 = _rothCalculator.getIra1(ira0, _thisYear - 1);
		if (pvsIra1 == null) {
		    _initialBalance = ira0._balanceBeginningOfCurrentYear;
		} else {
		    _initialBalance = pvsIra1._finalBalance;
		}
		final double divisor;
		final int beginningYear = _rothCalculator.getCurrentYear();
		if (_ira0._ageOfRmd < 0) {
		    divisor = ira0._currentDivisor + (_thisYear - beginningYear);
		} else {
		    final RothCalculator.TaxPayer tp0 = _ira0.getOwner();
		    final int yearOfBirth = DateUtils.getAPartOfADate(tp0._dateOfBirth, ChronoField.YEAR);
		    final int age = yearOfBirth - _thisYear;
		    if (age < ira0._ageOfRmd) {
			divisor = Double.NaN;
		    } else {
			divisor = _rothCalculator._lifeExpectancies[age];
		    }
		}
		_rmd = divisor > 0d ? (_initialBalance / divisor) : 0d;
		_finalBalance = (_investmentsFactor * (_initialBalance - _rmd)) - _rothConversion;
	    }

	    public String getString() {
		String s = String.format("IRA1[%s], InitBlnc[$%.2f]", _name, _initialBalance);
		if (_rmd > 0d) {
		    s += String.format(", RMD[$%.2f]", _rmd);
		}
		if (_rothConversion > 0d) {
		    s += String.format(", RothCnvrsn[$%.2f]", _rothConversion);
		}
		return s;
	    }

	    @Override
	    public String toString() {
		return getString();
	    }
	}

	public class OI1 extends NamedEntity {

	    public final RothCalculator.TaxPayer.OutsideIncome _oi0;
	    public final double _amount;

	    public OI1(final RothCalculator.TaxPayer.OutsideIncome oi0) {
		super(oi0._name, _thisYear);
		_oi0 = oi0;
		if (_oi0._year > 0) {
		    _amount = _thisYear == _oi0._year ? _oi0._amount : 0d;
		    return;
		}
		final OI1 pvsOi1 = _rothCalculator.getOi1(_oi0, _thisYear - 1);
		if (pvsOi1 == null) {
		    _amount = _oi0._amount;
		} else {
		    _amount = _inflationFactor * pvsOi1._amount;
		}
	    }

	    public String getString() {
		return String.format("OI1[%s], Amount[$%.2f]", _name, _amount);
	    }

	    @Override
	    public String toString() {
		return getString();
	    }
	}

	public final RothCalculator.TaxPayer _tp0;
	public final int _ageAtEndOfYear;
	public final IRA1[] _ira1s;
	public final OI1[] _oi1s;
	public final double _ssa;

	public TP1(final RothCalculator.TaxPayer tp0) {
	    super(tp0._name, _thisYear);
	    _tp0 = tp0;
	    final RothCalculator rothCalculator = _tp0.getRothCalculator();
	    final TP1 _pvsTp1 = rothCalculator.getTp1(_tp0, _thisYear - 1);
	    _ageAtEndOfYear = _thisYear - DateUtils.getAPartOfADate(_tp0._dateOfBirth, ChronoField.YEAR);

	    final RothCalculator.TaxPayer.Ira[] ira0s = tp0._iras;
	    final String staticsSheetName = getSheetName(_StaticsIdx);
	    final Line[] ssaLines = getBlock(staticsSheetName, "SSA Current Year")._lines;
	    final int idx = Arrays.binarySearch(ssaLines, new Line(_taxPayerName));
	    _currentYearSsa = idx < 0 ? 0d : ssaLines[idx]._data._d;
	    final Line[] iraDefnLines = getBlock(staticsSheetName, "IRA")._lines;
	    final int nIraDefns = iraDefnLines.length;
	    final ArrayList<IRA1> iraList = new ArrayList<>();
	    for (int k = 0; k < nIraDefns; ++k) {
		final Line iraDefnLine = iraDefnLines[k];
		final Field iraDefnLineData = iraDefnLine._data;
		if (iraDefnLineData._s.equals(_taxPayerName)) {
		    final String iraName = iraDefnLine._header._s;
		    iraList.add(new IRA1(iraName));
		}
	    }
	    _ira1s = iraList.toArray(new IRA1[iraList.size()]);
	    final Line[] outsideIncomeDefnLines = getBlock(staticsSheetName, "Outside Income")._lines;
	    final int nOidls = outsideIncomeDefnLines.length;
	    final ArrayList<OI1> oiList = new ArrayList<>();
	    for (int k = 0; k < nOidls; ++k) {
		final Line oiLine = outsideIncomeDefnLines[k];
		final Field oiData = oiLine._data;
		if (oiData._s.equals(_taxPayerName)) {
		    final String oiName = oiLine._header._s;
		    oiList.add(new OI1(oiName));
		}
	    }
	    _oi1s = oiList.toArray(new OI1[oiList.size()]);
	}

	public String getString() {
	    String s = String.format("TP1[%s], DateOfBirth[%s]", //
		    getName(), DateUtils.formatDateOnly(_dateOfBirth));
	    if (_currentYearSsa > 0d) {
		s += String.format(" Current Year SSA[$%.2f]", _currentYearSsa);
	    }
	    final int nIras = _ira1s.length;
	    for (int k = 0; k < nIras; ++k) {
		s += "\n\t" + _ira1s[k].getString();
	    }
	    final int nOutsideIncomes = _oi1s.length;
	    for (int k = 0; k < nOutsideIncomes; ++k) {
		s += "\n\t" + _oi1s[k].getString();
	    }
	    return s;
	}

	@Override
	public String toString() {
	    return getString();
	}
    }

    public class CG1 extends NamedEntity {

	public final RothCalculator.CapitalGain _cg0;
	public double _estimateThisYear;
	public double _carryOverToCurrentYear;

	public CG1(final RothCalculator.CapitalGain cg0) {
	    super(cg0._name, _thisYear);
	    _cg0 = cg0;
	    _estimateThisYear = 0d;
	    _carryOverToCurrentYear = 0d;
	}

	public String getString() {
	    return String.format("CG1[%s], EstimateThisYear[$%.2f] Carryover to Current Year[$%.2f]", //
		    _name, _estimateThisYear, YearOfData.this._thisYear, _carryOverToCurrentYear);
	}

	@Override
	public String toString() {
	    return getString();
	}
    }

    public final RothCalculator _rothCalculator;
    public final int _thisYear;
    public final double _inflationFactor;
    public final double _investmentsFactor;
    public final double _standardDeduction;
    public final double _partBStandardPremium;
    public final Brackets[] _bracketsS;
    public final TP1[] _tp1s;

    public YearOfData(final RothCalculator rothCalculator, final int year) {
	_rothCalculator = rothCalculator;
	_thisYear = year;
	final int myIdx = _thisYear - _rothCalculator.getCurrentYear();
	final YearOfData pvsYear = myIdx == 0 ? null : _rothCalculator._yearsOfData[myIdx - 1];
	final int nBracketsS = RothCalculator._BracketsNames.length;
	final double inflationExpRate = _rothCalculator.getGrowthRate(RothCalculator._InflationIdx);
	final double deltaT = pvsYear == null ? _rothCalculator._remainderOfCurrentYear : 1d;
	_inflationFactor = Math.exp(inflationExpRate * deltaT);
	final double investmentsExpRate = _rothCalculator.getGrowthRate(RothCalculator._InvestmentIdx);
	_investmentsFactor = Math.exp(investmentsExpRate * deltaT);
	if (pvsYear == null) {
	    _standardDeduction = _rothCalculator._standardDeductionCurrentYear;
	    _partBStandardPremium = _rothCalculator._partBPremiumCurrentYear;
	    _bracketsS = _rothCalculator._bracketsS;
	} else {
	    _standardDeduction = pvsYear._standardDeduction * _inflationFactor;
	    _partBStandardPremium = pvsYear._partBStandardPremium * _inflationFactor;
	    _bracketsS = new Brackets[nBracketsS];
	    for (int k0 = 0; k0 < nBracketsS; ++k0) {
		final Brackets oldBrackets = pvsYear._bracketsS[k0];
		final Block newBracketsBlock = _rothCalculator.getNewBracketsBlock(oldBrackets, _thisYear);
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
	final RothCalculator.TaxPayer[] taxPayers = _rothCalculator._taxPayers;
	final int nTaxPayers = taxPayers.length;
	_tp1s = new TP1[nTaxPayers];
	for (int k = 0; k < nTaxPayers; ++k) {
	    _tp1s[k] = new TP1(taxPayers[k]);
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
