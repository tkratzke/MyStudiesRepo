package com.skagit.rothProblem.owner0;

import com.skagit.rothProblem.GrowthRate;
import com.skagit.rothProblem.workBookConcepts.Block;
import com.skagit.rothProblem.workBookConcepts.WorkBookConcepts;
import com.skagit.util.MyStudiesDateUtils;
import com.skagit.util.MyStudiesStringUtils;
import com.skagit.util.NamedEntity;

public class Account0 extends NamedEntity {

    final public TypeOfAccount _typeOfAccount;

    final public Owner0 _owner;

    /** For all: */
    final public GrowthRate _growthRate;
    final public double _balance;

    /** For Inherited IRA: */
    final public double _divisorForRmd;

    /** For Regular IRA: */
    final public double _ageOfFirstRmd;

    /** For Inherited IRA or Regular IRA: */
    final public Boolean _rmdAlreadyTaken;
    final public double _beginningBalance;

    /** For Post-Tax: */
    final public double _basis;
    final public double _perCentTaxExempt;
    final public double _perCentLongCg;
    final public double _perCentAsLongCg;
    final public double _perCentShortCg;
    final public double _perCentAsShortCg;
    final public double _taxExempt;
    final public double _longCg;
    final public double _asLongCg;
    final public double _shortCg;
    final public double _asShortCg;

    /** Roth has nothing special. */

    public Account0(final Owner0 owner0, final Block[] accountsBlocks, final String name) {
	super(name);
	_owner = owner0;
	final double growthRateProportion = WorkBookConcepts.getDouble(accountsBlocks, _name, "Growth Rate");
	_growthRate = new GrowthRate("Growth Rate", growthRateProportion);
	_balance = WorkBookConcepts.getDouble(accountsBlocks, _name, "Balance");
	final double divisorForRmd = WorkBookConcepts.getDouble(accountsBlocks, _name, "Divisor, Current Year");
	if (divisorForRmd > 0d) {
	    _typeOfAccount = TypeOfAccount.INH_IRA;
	    _divisorForRmd = divisorForRmd;
	    _rmdAlreadyTaken = WorkBookConcepts.getBoolean(accountsBlocks, _name, "RMD Taken Current Year");
	    _beginningBalance = WorkBookConcepts.getDouble(accountsBlocks, _name, "Balance, Beginning of Current Year");
	    _basis = Double.NaN;
	    _ageOfFirstRmd = Double.NaN;
	    _perCentTaxExempt = _perCentLongCg = _perCentAsLongCg = _perCentShortCg = _perCentAsShortCg = Double.NaN;
	    _taxExempt = _longCg = _asLongCg = _shortCg = _asShortCg = Double.NaN;
	} else {
	    final double ageOfFirstRmd = WorkBookConcepts.getDouble(accountsBlocks, _name, "Age of First RMD");
	    if (ageOfFirstRmd > 0d) {
		_typeOfAccount = TypeOfAccount.REG_IRA;
		_ageOfFirstRmd = ageOfFirstRmd;
		final int currentYear = MyStudiesDateUtils.getYear(_owner._rothProblem._currentDate);
		final int birthYear = MyStudiesDateUtils.getYear(_owner._birthDate);
		final int currentAge = currentYear - birthYear;
		if (currentAge >= _ageOfFirstRmd) {
		    _rmdAlreadyTaken = WorkBookConcepts.getBoolean(accountsBlocks, _name, "RMD Taken Current Year");
		    _beginningBalance = WorkBookConcepts.getDouble(accountsBlocks, _name,
			    "Balance, Beginning of Current Year");
		} else {
		    _rmdAlreadyTaken = null;
		    _beginningBalance = Double.NaN;
		}
		_divisorForRmd = Double.NaN;
		_basis = Double.NaN;
		_perCentTaxExempt = _perCentLongCg = _perCentAsLongCg = _perCentShortCg = _perCentAsShortCg = Double.NaN;
		_taxExempt = _longCg = _asLongCg = _shortCg = _asShortCg = Double.NaN;
	    } else {
		final double basis = WorkBookConcepts.getDouble(accountsBlocks, _name, "Basis");
		if (Double.isFinite(basis)) {
		    _typeOfAccount = TypeOfAccount.POST_TAX;
		    _basis = basis;
		    _perCentTaxExempt = WorkBookConcepts.getDouble(accountsBlocks, _name, "% Tax-Exempt") * 100d;
		    _perCentLongCg = WorkBookConcepts.getDouble(accountsBlocks, _name, "% Long-Term") * 100d;
		    _perCentAsLongCg = WorkBookConcepts.getDouble(accountsBlocks, _name, "% As Long-Term") * 100d;
		    _perCentShortCg = WorkBookConcepts.getDouble(accountsBlocks, _name, "% Short-Term") * 100d;
		    _perCentAsShortCg = WorkBookConcepts.getDouble(accountsBlocks, _name, "% As Short-Term") * 100d;
		    _taxExempt = WorkBookConcepts.getDouble(accountsBlocks, _name, "Tax-Exempt");
		    _longCg = WorkBookConcepts.getDouble(accountsBlocks, _name, "Long-Term");
		    _asLongCg = WorkBookConcepts.getDouble(accountsBlocks, _name, "As Long-Term");
		    _shortCg = WorkBookConcepts.getDouble(accountsBlocks, _name, "Short-Term");
		    _asShortCg = WorkBookConcepts.getDouble(accountsBlocks, _name, "As Short-Term");
		    _divisorForRmd = _ageOfFirstRmd = _beginningBalance = Double.NaN;
		    _rmdAlreadyTaken = null;
		} else {
		    _typeOfAccount = TypeOfAccount.ROTH;
		    _ageOfFirstRmd = Double.NaN;
		    _rmdAlreadyTaken = null;
		    _beginningBalance = Double.NaN;
		    _divisorForRmd = Double.NaN;
		    _basis = Double.NaN;
		    _perCentTaxExempt = _perCentLongCg = _perCentAsLongCg = _perCentShortCg = _perCentAsShortCg = Double.NaN;
		    _taxExempt = _longCg = _asLongCg = _shortCg = _asShortCg = Double.NaN;
		}
	    }
	}
    }

    @Override
    public String getString() {
	String s = String.format("%s(%s), Growth Rate[%s], Balance[%s]", //
		_name, _typeOfAccount._english, //
		_growthRate.getString(), MyStudiesStringUtils.formatDollars(_balance) //
	);
	if (_typeOfAccount == TypeOfAccount.INH_IRA) {
	    s += String.format("\n\tDivisorForRmd[%s], RmdAlreadyTaken[%b], BeginningBalance[%s]", //
		    MyStudiesStringUtils.formatOther(_divisorForRmd, 1), //
		    _rmdAlreadyTaken.booleanValue(), //
		    MyStudiesStringUtils.formatDollars(_beginningBalance) //
	    );
	} else if (_typeOfAccount == TypeOfAccount.REG_IRA) {
	    s += String.format("\n\tAgeOfFirstRmd[%s]", //
		    MyStudiesStringUtils.formatOther(_ageOfFirstRmd, 1) //
	    );
	    final int currentYear = MyStudiesDateUtils.getYear(_owner._rothProblem._currentDate);
	    final int birthYear = MyStudiesDateUtils.getYear(_owner._birthDate);
	    final int currentAge = currentYear - birthYear;
	    if (currentAge >= _ageOfFirstRmd) {
		s += String.format(", RmdAlreadyTaken[%b], BeginningBalance[%s]", //
			_rmdAlreadyTaken.booleanValue(), //
			MyStudiesStringUtils.formatDollars(_beginningBalance) //
		);
	    }
	} else if (_typeOfAccount == TypeOfAccount.POST_TAX) {
	    s += String.format("\n\tBasis[%s], TxExmpt[%s], Long[%s], asLong[%s], Short[%s], asShort[%s]", //
		    MyStudiesStringUtils.formatDollars(_basis), //
		    MyStudiesStringUtils.formatPerCent(_perCentTaxExempt, 0), //
		    MyStudiesStringUtils.formatPerCent(_perCentLongCg, 0), //
		    MyStudiesStringUtils.formatPerCent(_perCentAsLongCg, 0), //
		    MyStudiesStringUtils.formatPerCent(_perCentShortCg, 0), //
		    MyStudiesStringUtils.formatPerCent(_perCentAsShortCg, 0) //
	    );
	    if (Double.isFinite(_taxExempt) || Double.isFinite(_taxExempt) || Double.isFinite(_taxExempt)
		    || Double.isFinite(_taxExempt) || Double.isFinite(_taxExempt)) {
		s += String.format("\n\tTaxExmpt[%s], LongCgs[%s], AsLongCgs[%s], ShortCgs[%s], AsShortCGs[%s]", //
			MyStudiesStringUtils.formatDollars(_taxExempt), //
			MyStudiesStringUtils.formatDollars(_longCg), //
			MyStudiesStringUtils.formatDollars(_asLongCg), //
			MyStudiesStringUtils.formatDollars(_shortCg), //
			MyStudiesStringUtils.formatDollars(_asShortCg) //
		);
	    }
	} else if (_typeOfAccount == TypeOfAccount.ROTH) {
	    /** There's no special data for a Roth. */
	}
	return s;
    }

}
