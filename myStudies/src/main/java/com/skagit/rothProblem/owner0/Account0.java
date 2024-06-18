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

    /** For Inherited IRA: */
    final public double _divisorForRmd;

    /** For Regular IRA: */
    final public double _ageOfFirstRmd;

    /** For Inherited IRA or Regular IRA: */
    final public Boolean _rmdAlreadyTaken;
    final public double _beginningBalance;

    /** For Post-Tax: */
    final public double _basis;
    final public double _perCentThatIsLong;
    final public Boolean _projectCapitalGains;
    final public double _taxExempt;
    final public double _shortTermCapitalGain;
    final public double _longTermCapitalGain;

    /** Roth has nothing special. */

    /** For all: */
    final public GrowthRate _growthRate;
    final public double _endingBalance;

    public Account0(final Owner0 owner0, final Block[] accountsBlocks, final String name) {
	super(name);
	_owner = owner0;
	final double divisorForRmd = WorkBookConcepts.getDouble(accountsBlocks,
		"Accounts and Divisors for Current Year", _name);
	if (divisorForRmd > 0d) {
	    _typeOfAccount = TypeOfAccount.INH_IRA;
	    _divisorForRmd = divisorForRmd;
	    _rmdAlreadyTaken = WorkBookConcepts.getBoolean(accountsBlocks, "Accounts and RMD Taken Current Year",
		    _name);
	    _beginningBalance = WorkBookConcepts.getDouble(accountsBlocks,
		    "Accounts and Balances Beginning of Current Year", _name);
	    _ageOfFirstRmd = _perCentThatIsLong = _basis = _taxExempt = _shortTermCapitalGain = _longTermCapitalGain = Double.NaN;
	    _projectCapitalGains = null;
	} else {
	    final double ageOfFirstRmd = WorkBookConcepts.getDouble(accountsBlocks, "Accounts and Ages of First RMD",
		    _name);
	    if (ageOfFirstRmd > 0d) {
		_typeOfAccount = TypeOfAccount.REG_IRA;
		_ageOfFirstRmd = ageOfFirstRmd;
		final int currentYear = MyStudiesDateUtils.getYear(_owner._rothProblem._currentDate);
		final int birthYear = MyStudiesDateUtils.getYear(_owner._birthDate);
		final int currentAge = currentYear - birthYear;
		if (currentAge >= _ageOfFirstRmd) {
		    _rmdAlreadyTaken = WorkBookConcepts.getBoolean(accountsBlocks,
			    "Accounts and RMD Taken Current Year", _name);
		    _beginningBalance = WorkBookConcepts.getDouble(accountsBlocks,
			    "Accounts and Balances Beginning of Current Year", _name);
		} else {
		    _rmdAlreadyTaken = null;
		    _beginningBalance = Double.NaN;
		}
		_divisorForRmd = _perCentThatIsLong = _basis = _taxExempt = _shortTermCapitalGain = _longTermCapitalGain = Double.NaN;
		_projectCapitalGains = null;
	    } else {
		final double basis = WorkBookConcepts.getDouble(accountsBlocks, "Accounts and Bases", _name);
		if (Double.isFinite(basis)) {
		    _typeOfAccount = TypeOfAccount.POST_TAX;
		    _basis = basis;
		    _perCentThatIsLong = WorkBookConcepts.getDouble(accountsBlocks, "Accounts and % That is Long",
			    _name);
		    _projectCapitalGains = WorkBookConcepts.getBoolean(accountsBlocks,
			    "Accounts and Project Capital Gains", _name);
		    _taxExempt = WorkBookConcepts.getDouble(accountsBlocks, _name, "Current Tax-Exempt");
		    _shortTermCapitalGain = WorkBookConcepts.getDouble(accountsBlocks, _name, "Current Short-Term");
		    _longTermCapitalGain = WorkBookConcepts.getDouble(accountsBlocks, _name, "Current Long-Term");
		    _divisorForRmd = _ageOfFirstRmd = _beginningBalance = Double.NaN;
		    _rmdAlreadyTaken = null;
		} else {
		    _typeOfAccount = TypeOfAccount.ROTH;
		    _divisorForRmd = _ageOfFirstRmd = _beginningBalance = _basis = _perCentThatIsLong = _taxExempt = _shortTermCapitalGain = _longTermCapitalGain = Double.NaN;
		    _rmdAlreadyTaken = _projectCapitalGains = null;
		}
	    }
	}
	final double growthRateProportion = WorkBookConcepts.getDouble(accountsBlocks, "Accounts and Growth Rates",
		_name);
	_growthRate = new GrowthRate("Growth Rate", growthRateProportion);
	_endingBalance = WorkBookConcepts.getDouble(accountsBlocks, "Accounts and Balances", _name);
    }

    @Override
    public String getString() {
	String s = String.format("%s(%s), Growth Rate[%s], Balance[%s]", //
		_name, _typeOfAccount._english, //
		_growthRate.getString(), MyStudiesStringUtils.formatDollars(_endingBalance) //
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
	    s += String.format("\n\tBasis[%s], %% Long[%s], ProjctCGs[%b]" + //
		    "\n\tTaxExmpt[%s], ShrtTrmCGs[%s], LngTrmCGs[%s]", //
		    MyStudiesStringUtils.formatDollars(_basis), //
		    MyStudiesStringUtils.formatPerCent(_perCentThatIsLong, 1), //
		    _projectCapitalGains.booleanValue(), //
		    MyStudiesStringUtils.formatDollars(_taxExempt), //
		    MyStudiesStringUtils.formatDollars(_shortTermCapitalGain), //
		    MyStudiesStringUtils.formatDollars(_longTermCapitalGain) //
	    );
	} else if (_typeOfAccount == TypeOfAccount.ROTH) {
	    /** There's no special data for a Roth. */
	}
	return s;
    }

}
