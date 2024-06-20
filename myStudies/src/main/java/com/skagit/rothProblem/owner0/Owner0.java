package com.skagit.rothProblem.owner0;

import java.util.Date;

import com.skagit.rothProblem.RothProblem;
import com.skagit.rothProblem.workBookConcepts.Block;
import com.skagit.rothProblem.workBookConcepts.Line;
import com.skagit.rothProblem.workBookConcepts.WorkBookConcepts;
import com.skagit.util.MyStudiesDateUtils;
import com.skagit.util.MyStudiesStringUtils;
import com.skagit.util.NamedEntity;

public class Owner0 extends NamedEntity {

    public final RothProblem _rothProblem;
    public final Date _birthDate;
    public final double _currentSsa;
    public final OutsideIncome0[] _outsideIncome0s;
    public final Account0[] _account0s;

    public Owner0(final RothProblem rothProblem, final Block[] accountsBlocks, final String name,
	    final Date birthDate) {
	super(name);
	_rothProblem = rothProblem;
	_birthDate = birthDate;
	final double currentSsa = WorkBookConcepts.getDouble(accountsBlocks, _name, "Social Security Benefits");
	_currentSsa = Double.isFinite(currentSsa) ? currentSsa : 0d;
	final Block myMainBlock = WorkBookConcepts.getBlock(accountsBlocks, _name);
	final Line[] mainLines = myMainBlock._lines;

	final int[] loHi0 = WorkBookConcepts.getMatchingLineIdxs("Outside Income", mainLines);
	final int lo0 = loHi0[0], hi0 = loHi0[1];
	final int nOutsideIncome0s = hi0 - lo0;
	_outsideIncome0s = new OutsideIncome0[nOutsideIncome0s];
	for (int k = 0; k < nOutsideIncome0s; ++k) {
	    final String oiName = mainLines[lo0 + k]._data._s;
	    final double oiAmount = WorkBookConcepts.getDouble(accountsBlocks, oiName, "Amount");
	    final int oiYear = WorkBookConcepts.getInt(accountsBlocks, oiName, "Year");
	    _outsideIncome0s[k] = new OutsideIncome0(this, oiName, oiAmount, oiYear);
	}

	final int[] loHi1 = WorkBookConcepts.getMatchingLineIdxs("Account", mainLines);
	final int lo1 = loHi1[0], hi1 = loHi1[1];
	final int nAccount0s = hi1 - lo1;
	_account0s = new Account0[nAccount0s];
	for (int k = 0; k < nAccount0s; ++k) {
	    final String accountName = mainLines[lo1 + k]._data._s;
	    _account0s[k] = new Account0(this, accountsBlocks, accountName);
	}
    }

    @Override
    public String getString() {
	String s = String.format("Owner0 = %s", _name);
	if (_birthDate != null) {
	    s += String.format(", %s", MyStudiesDateUtils.formatDateOnly(_birthDate));
	}
	if (_currentSsa > 0d) {
	    s += String.format(", SSA[%s]", MyStudiesStringUtils.formatDollars(_currentSsa));
	}
	s += ":";
	final int nOutsideIncome0s = _outsideIncome0s.length;
	if (nOutsideIncome0s > 0) {
	    s += "\nOutsideIncome0s:";
	    for (int k = 0; k < nOutsideIncome0s; ++k) {
		s += String.format("\n\t%s", _outsideIncome0s[k].getString());
	    }
	}
	final int nAccount0s = _account0s.length;
	if (nAccount0s > 0) {
	    s += "\nAccount0s:";
	    for (int k = 0; k < nAccount0s; ++k) {
		s += String.format("\n%s", _account0s[k].getString());
	    }
	}
	return s;
    }
}
