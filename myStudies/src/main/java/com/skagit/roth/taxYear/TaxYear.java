package com.skagit.roth.taxYear;

import java.time.temporal.ChronoField;

import com.skagit.roth.Account;
import com.skagit.roth.Block;
import com.skagit.roth.Brackets;
import com.skagit.roth.Line;
import com.skagit.roth.RothCalculator;
import com.skagit.util.MyStudiesDateUtils;
import com.skagit.util.NamedEntity;
import com.skagit.util.TypeOfDouble;

public class TaxYear {

    public class Account1 extends NamedEntity {

	public final Account _account0;
	public double _initialBalance;
	public double _finalBalance;
	public final double _rmd;

	public Account1(final Account account0) {
	    super(account0._name, _thisYear);
	    _account0 = account0;
	    final RothCalculator.Owner owner0 = _account0._owner;
	    final Account1 pvsAccount1 = _rothCalculator.getAccount1(account0, _thisYear - 1);
	    if (pvsAccount1 == null) {
		_initialBalance = account0._balanceBeginningOfCurrentYear;
	    } else {
		_initialBalance = pvsAccount1._finalBalance;
	    }
	    if (owner0 == null) {
		_rmd = 0d;
	    } else {
		final double divisor;
		final int beginningYear = _rothCalculator.getCurrentYear();
		if (_account0._ageOfRmd < 0) {
		    divisor = account0._currentDivisor + (_thisYear - beginningYear);
		} else {
		    final int yearOfBirth = MyStudiesDateUtils.getAPartOfADate(owner0._dateOfBirth, ChronoField.YEAR);
		    final int age = yearOfBirth - _thisYear;
		    if (age < account0._ageOfRmd) {
			divisor = Double.NaN;
		    } else {
			divisor = _rothCalculator._lifeExpectancies[age];
		    }
		}
		_rmd = divisor > 0d ? (_initialBalance / divisor) : 0d;
	    }
	    _finalBalance = (_investmentsFactor * (_initialBalance - _rmd));
	}

	public String getString() {
	    String s = String.format("Account1[%s], InitBlnc[%s]", //
		    _name, //
		    TypeOfDouble.MONEY.format(_initialBalance, 2));
	    if (_rmd > 0d) {
		s += String.format(", RMD[%s]", TypeOfDouble.MONEY.format(_rmd, 2));
	    }
	    return s;
	}

	@Override
	public String toString() {
	    return getString();
	}
    }

    /**
     * We assume RMDs are taken at the beginning of each year, and Roth Conversions
     * are taken at the end of each year.
     */
    public class Owner1 extends NamedEntity {

	public class OutsideIncome1 extends NamedEntity {

	    public final RothCalculator.Owner.OutsideIncome _oi0;
	    public final double _amount;

	    public OutsideIncome1(final RothCalculator.Owner.OutsideIncome oi0) {
		super(oi0._name, _thisYear);
		_oi0 = oi0;
		if (_oi0._year > 0) {
		    /** Signals a "one-off" year. */
		    _amount = _thisYear == _oi0._year ? _oi0._amount : 0d;
		    return;
		}
		final OutsideIncome1 pvsOi1 = _rothCalculator.getOi1(_oi0, _thisYear - 1);
		if (pvsOi1 == null) {
		    _amount = _oi0._amount;
		} else {
		    _amount = _inflationFactor * pvsOi1._amount;
		}
	    }

	    public String getString() {
		return String.format("OutsideIncome1[%s], Amnt[%s]", _name, //
			TypeOfDouble.MONEY.format(_amount, 2));
	    }

	    @Override
	    public String toString() {
		return getString();
	    }
	}

	public final RothCalculator.Owner _owner0;
	public final double _ssa;
	public final Account1[] _myAccounts1;
	public final OutsideIncome1[] _oi1s;

	public Owner1(final RothCalculator.Owner owner0) {
	    super(owner0._name, _thisYear);
	    _owner0 = owner0;
	    final RothCalculator rothCalculator = _owner0.getRothCalculator();
	    final Owner1 _pvsOwner1 = rothCalculator.getOwner1(_owner0, _thisYear - 1);
	    _ssa = _pvsOwner1 == null ? _owner0._ssa : (_pvsOwner1._ssa * _inflationFactor);

	    final Account[] ira0s = _owner0._myAccnts;
	    final int nIras = ira0s.length;
	    _myAccounts1 = new Account1[nIras];
	    for (int k = 0; k < nIras; ++k) {
		_myAccounts1[k] = new Account1(ira0s[k]);
	    }
	    final RothCalculator.Owner.OutsideIncome[] oi0s = _owner0._outsideIncomes;
	    final int nOis = oi0s.length;
	    _oi1s = new OutsideIncome1[nOis];
	    for (int k = 0; k < nOis; ++k) {
		_oi1s[k] = new OutsideIncome1(oi0s[k]);
	    }
	}

	public String getString() {
	    String s = String.format("Owner1[%s], SSA[%s]", _name, //
		    TypeOfDouble.MONEY.format(_ssa, 2));
	    final int nIras = _myAccounts1.length;
	    for (int k = 0; k < nIras; ++k) {
		s += "\n\t" + _myAccounts1[k].getString();
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

    public final RothCalculator _rothCalculator;
    public final int _thisYear;
    public final double _inflationFactor;
    public final double _investmentsFactor;
    public final double _standardDeduction;
    public final double _partBStandardPremium;
    public final Brackets[] _bracketsS;
    public final Owner1[] _owner1s;
    public final KeyValues _keyValues;

    public TaxYear(final RothCalculator rothCalculator, final int year) {
	_rothCalculator = rothCalculator;
	_thisYear = year;
	final int myIdx = _thisYear - _rothCalculator.getCurrentYear();
	final TaxYear pvsYear = myIdx == 0 ? null : _rothCalculator._taxYears[myIdx - 1];
	final int nBracketsS = RothCalculator._BracketsNames.length;
	final double inflationExpRate = _rothCalculator._inflationGrowthRate._expGrowthRate;
	final double deltaT = pvsYear == null ? _rothCalculator._perCentLeftOfCurrentYear : 1d;
	_inflationFactor = Math.exp(inflationExpRate * deltaT);
	final double investmentsExpRate = _rothCalculator._investmentsGrowthRate._expGrowthRate;
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
	final RothCalculator.Owner[] taxPayers = _rothCalculator._owners;
	final int nTaxPayers = taxPayers.length;
	_owner1s = new Owner1[nTaxPayers];
	for (int k = 0; k < nTaxPayers; ++k) {
	    _owner1s[k] = new Owner1(taxPayers[k]);
	}

	_keyValues = new KeyValues(this);
    }

    public Block getNewBracketsBlock(final Brackets oldBrackets) {
	return _rothCalculator.getBlock(RothCalculator._SheetNames[RothCalculator._BracketsSheetIdx],
		oldBrackets._name + " " + _thisYear);
    }

    private final static boolean _DumpBracketsS = false;

    public String getString() {
	String s = String.format("Year %d, StdDdctn[%s] PrtBPrmm[%s]", //
		_thisYear, //
		TypeOfDouble.MONEY.format(_standardDeduction, 2), //
		_partBStandardPremium);
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
