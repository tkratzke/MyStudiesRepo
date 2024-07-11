package com.skagit.rothProblem.taxYear;

import com.skagit.rothProblem.RothProblem;
import com.skagit.rothProblem.owner0.Account0;
import com.skagit.rothProblem.owner0.Owner0;
import com.skagit.util.MyStudiesDateUtils;

public class Account1 {
    public Account0 _account0;
    public final Account1 _pvsAccount1;
    public double _nonCashOut;

    public Account1(final Account0 account0) {
	_account0 = account0;
	_pvsAccount1 = null;
    }

    public Account1(final Account1 pvsAccount1) {
	_account0 = pvsAccount1._account0;
	_pvsAccount1 = pvsAccount1;
    }

    public Owner0 getOwner0() {
	return _account0._owner;
    }

    public RothProblem getRothProblem() {
	return getOwner0()._rothProblem;
    }

    private void completeTheYear() {
	final double p;
	final double nonCashIn;
	final RothProblem rothProblem = getRothProblem();
	if (_pvsAccount1 != null) {
	    p = 1d;
	    nonCashIn = _pvsAccount1._nonCashOut;
	} else {
	    p = MyStudiesDateUtils.getProportionOfYear(rothProblem._currentDate);
	    nonCashIn = _account0._beginningBalance;
	}
    }

}
