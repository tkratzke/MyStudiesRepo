package com.skagit.roth.taxYear;

import java.time.temporal.ChronoField;

import com.skagit.roth.baseYear.Account0;
import com.skagit.roth.baseYear.Owner0;
import com.skagit.roth.rothCalculator.RothCalculator;
import com.skagit.util.MyStudiesDateUtils;
import com.skagit.util.NamedEntity;
import com.skagit.util.TypeOfDouble;

public class Account1 extends NamedEntity {

    public final Account0 _account0;
    public double _initialBalance;
    public double _finalBalance;
    public final double _rmd;

    public Account1(final Account0 account0, final RothCalculator rothCalculator, final int thisYear) {
	super(account0._name);
	_account0 = account0;
	final Owner0 owner0 = _account0._owner;
	final Account1 pvsAccount1 = rothCalculator.getAccount1(account0, thisYear - 1);
	if (pvsAccount1 == null) {
	    _initialBalance = account0._balanceBeginningOfBaseYear;
	} else {
	    _initialBalance = pvsAccount1._finalBalance;
	}
	if (owner0 == null) {
	    _rmd = 0d;
	} else {
	    final double divisor;
	    final int baseDateYear = rothCalculator._baseYear._baseDateYear;
	    final double baseDivisorForInhIra = account0._baseDivisorForInhIra;
	    if (baseDivisorForInhIra > 0d) {
		divisor = baseDivisorForInhIra + (thisYear - baseDateYear);
	    } else {
		final int yearOfBirth = MyStudiesDateUtils.getAPartOfADate(owner0._dateOfBirth, ChronoField.YEAR);
		final int age = yearOfBirth - thisYear;
		if (age < account0._ageOfRmd) {
		    divisor = Double.NaN;
		} else {
		    divisor = rothCalculator._lifeExpectancies[age];
		}
	    }
	    _rmd = divisor > 0d ? (_initialBalance / divisor) : 0d;
	}
	final double investmentsFactor = rothCalculator.getInvestmentsFactor(thisYear);
	_finalBalance = (investmentsFactor * (_initialBalance - _rmd));
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
