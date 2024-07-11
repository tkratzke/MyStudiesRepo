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
    final public double _currentValue;

    /** For Inherited IRA: */
    final public double _divisorForRmd;

    /** For Regular IRA: */
    final public double _ageOfFirstRmd;

    /** For Inherited IRA or Regular IRA: */
    final public Boolean _rmdAlreadyTaken;
    final public double _beginningBalance;

    /** For Post-Tax: */
    final public double _cash; // In $$
    final public double _perCentDueOfNonCashAtEoy; // Unavoidable, eg dividends (5%)
    final public double _annualPerCentGrowthInValueOfNonCash; // If you left it alone, what would it be (eg 8%)?
    final public double _annualPerCentGrowthInValueOfCash; // (Eg 3%)
    final public double _incomeGeneratedAndDirectedToCashAsPerCentOfNonCash; // (Eg 2%)
    final public double _incomeGeneratedAndReInvestedAsPerCentOfNonCash; // (Eg 4%)

    /** Roth has nothing special. */

    public Account0(final Owner0 owner0, final Block[] accountsBlocks, final String name) {
	super(name);
	_owner = owner0;
	final double growthRateProportion = WorkBookConcepts.getDouble(accountsBlocks, _name, "Growth Rate");
	_growthRate = new GrowthRate("Growth Rate", growthRateProportion);
	_currentValue = WorkBookConcepts.getDouble(accountsBlocks, _name, "Current Value");
	final double divisorForRmd = WorkBookConcepts.getDouble(accountsBlocks, _name, "Divisor, Current Year");
	if (divisorForRmd > 0d) {
	    _typeOfAccount = TypeOfAccount.INH_IRA;
	    _divisorForRmd = divisorForRmd;
	    _rmdAlreadyTaken = WorkBookConcepts.getBoolean(accountsBlocks, _name, "RMD Taken Current Year");
	    _beginningBalance = WorkBookConcepts.getDouble(accountsBlocks, _name, "Balance, Beginning of Current Year");
	    _ageOfFirstRmd = Double.NaN;
	    _cash = _perCentDueOfNonCashAtEoy = Double.NaN;
	    _annualPerCentGrowthInValueOfNonCash = _annualPerCentGrowthInValueOfCash = Double.NaN;
	    _incomeGeneratedAndDirectedToCashAsPerCentOfNonCash = Double.NaN;
	    _incomeGeneratedAndReInvestedAsPerCentOfNonCash = Double.NaN;
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
		_cash = _perCentDueOfNonCashAtEoy = Double.NaN;
		_annualPerCentGrowthInValueOfNonCash = _annualPerCentGrowthInValueOfCash = Double.NaN;
		_incomeGeneratedAndDirectedToCashAsPerCentOfNonCash = Double.NaN;
		_incomeGeneratedAndReInvestedAsPerCentOfNonCash = Double.NaN;
	    } else {
		final double cash = WorkBookConcepts.getDouble(accountsBlocks, _name, "Cash");
		if (Double.isFinite(cash)) {
		    _typeOfAccount = TypeOfAccount.POST_TAX;
		    _divisorForRmd = _ageOfFirstRmd = _beginningBalance = Double.NaN;
		    _rmdAlreadyTaken = null;
		    _cash = cash;
		    _perCentDueOfNonCashAtEoy = WorkBookConcepts.getDouble(accountsBlocks, _name,
			    "% Due of Non-Cash at EOY");
		    _annualPerCentGrowthInValueOfNonCash = WorkBookConcepts.getDouble(accountsBlocks, _name,
			    "Growth Rate Non-Cash");
		    _annualPerCentGrowthInValueOfCash = WorkBookConcepts.getDouble(accountsBlocks, _name,
			    "Growth Rate Cash");
		    _incomeGeneratedAndDirectedToCashAsPerCentOfNonCash = WorkBookConcepts.getDouble(accountsBlocks,
			    _name, "Income as % of Income Directed to Cash");
		    _incomeGeneratedAndReInvestedAsPerCentOfNonCash = WorkBookConcepts.getDouble(accountsBlocks, _name,
			    "Income as % of Income Re-Invested");
		} else {
		    _typeOfAccount = TypeOfAccount.ROTH;
		    _ageOfFirstRmd = Double.NaN;
		    _rmdAlreadyTaken = null;
		    _beginningBalance = Double.NaN;
		    _divisorForRmd = Double.NaN;
		    _cash = _perCentDueOfNonCashAtEoy = Double.NaN;
		    _annualPerCentGrowthInValueOfNonCash = _annualPerCentGrowthInValueOfCash = Double.NaN;
		    _incomeGeneratedAndDirectedToCashAsPerCentOfNonCash = Double.NaN;
		    _incomeGeneratedAndReInvestedAsPerCentOfNonCash = Double.NaN;
		}
	    }
	}
    }

    @Override
    public String getString() {
	String s = String.format("%s(%s), Growth Rate[%s], Balance[%s]", //
		_name, _typeOfAccount._english, //
		_growthRate.getString(), MyStudiesStringUtils.formatDollars(_currentValue) //
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
	    s += String.format(
		    "\n\tCash[%s], GeneralTaxes[%s], GrwthRtNon-Csh[%s], GrwthRtCsh[%s], IncmToCsh[%s], IncmReInv[%s]", //
		    MyStudiesStringUtils.formatDollars(_cash), //
		    MyStudiesStringUtils.formatPerCent(_perCentDueOfNonCashAtEoy, 0), //
		    MyStudiesStringUtils.formatPerCent(_annualPerCentGrowthInValueOfNonCash, 0), //
		    MyStudiesStringUtils.formatPerCent(_annualPerCentGrowthInValueOfCash, 0), //
		    MyStudiesStringUtils.formatPerCent(_incomeGeneratedAndDirectedToCashAsPerCentOfNonCash, 0), //
		    MyStudiesStringUtils.formatPerCent(_incomeGeneratedAndReInvestedAsPerCentOfNonCash, 0) //
	    );
	} else if (_typeOfAccount == TypeOfAccount.ROTH) {
	    /** There's no special data for a Roth. */
	}
	return s;
    }

}
