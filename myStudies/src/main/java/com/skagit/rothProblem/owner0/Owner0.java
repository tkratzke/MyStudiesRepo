package com.skagit.rothProblem.owner0;

import java.util.ArrayList;
import java.util.Arrays;
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
	final int idx0 = Arrays.binarySearch(accountsBlocks, new NamedEntity("Owners and Social Security Incomes"));
	final Block block0 = accountsBlocks[idx0];
	final Line[] lines0 = block0._lines;
	final int idx1 = Arrays.binarySearch(lines0, new Line(_name));
	_currentSsa = lines0[idx1]._data._d;
	/** Search for the OutsideIncome0s. */
	final Block block1A = WorkBookConcepts.getBlock(accountsBlocks, "Owners and Outside Incomes");
	final Line[] lines1A = block1A._lines;
	final Block block1B = WorkBookConcepts.getBlock(accountsBlocks, "Outside Incomes and Amounts");
	final Line[] lines1B = block1B._lines;
	final Block block1C = WorkBookConcepts.getBlock(accountsBlocks, "Outside Incomes and Years");
	final Line[] lines1C = block1C._lines;
	final int[] loHi1A = WorkBookConcepts.getMatchingLineIdxs(_name, lines1A);
	final int lo1A = loHi1A[0], hi1A = loHi1A[1];
	final int nOutsideIncome0s = hi1A - lo1A;
	_outsideIncome0s = new OutsideIncome0[nOutsideIncome0s];
	for (int k = lo1A; k < hi1A; ++k) {
	    final String oiName = lines1A[k]._data._s;
	    final int[] loHi1B = WorkBookConcepts.getMatchingLineIdxs(oiName, lines1B);
	    final double oiAmount = lines1B[loHi1B[0]]._data._d;
	    final int[] loHi1C = WorkBookConcepts.getMatchingLineIdxs(oiName, lines1C);
	    final int oiYear;
	    if (loHi1C[1] > loHi1C[0]) {
		oiYear = (int) Math.round(lines1C[loHi1C[0]]._data._d);
	    } else {
		oiYear = -1;
	    }
	    _outsideIncome0s[k - lo1A] = new OutsideIncome0(this, oiName, oiAmount, oiYear);
	}
	_account0s = getAccount0s(accountsBlocks);
    }

    public Owner0(final RothProblem rothProblem, final Block[] accountsBlocks) {
	super(null);
	_rothProblem = rothProblem;
	_birthDate = null;
	_currentSsa = Double.NaN;
	_outsideIncome0s = new OutsideIncome0[0];
	_account0s = getAccount0s(accountsBlocks);
    }

    private Account0[] getAccount0s(final Block[] accountsBlocks) {
	final ArrayList<Account0> list = new ArrayList<>();
	final Block block2 = WorkBookConcepts.getBlock(accountsBlocks, "Owners and Accounts");
	final Line[] lines2 = block2._lines;
	final int[] loHi2 = WorkBookConcepts.getMatchingLineIdxs(_name, lines2);
	for (int k = loHi2[0]; k < loHi2[1]; ++k) {
	    final Line line = lines2[k];
	    final String accountName = line._data._s;
	    if (accountName != null && accountName.length() > 0) {
		list.add(new Account0(this, accountsBlocks, accountName));
	    }
	}
	return list.toArray(new Account0[list.size()]);
    }

    @Override
    public String getString() {
	String s = String.format("Owner0 = %s", _name == null ? "Joint Accounts" : _name);
	if (_birthDate != null) {
	    s += String.format(", %s", MyStudiesDateUtils.formatDateOnly(_birthDate));
	}
	if (Double.isFinite(_currentSsa)) {
	    s += String.format(", %s", MyStudiesStringUtils.formatDollars(_currentSsa));
	}
	s += ":";
	final int nOutsideIncome0s = _outsideIncome0s.length;
	if (nOutsideIncome0s > 0) {
	    s += "\nOutside Income0s:";
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
