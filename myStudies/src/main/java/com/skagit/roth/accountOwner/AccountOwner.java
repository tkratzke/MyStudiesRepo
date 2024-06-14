package com.skagit.roth.accountOwner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import com.skagit.roth.workBookConcepts.Block;
import com.skagit.roth.workBookConcepts.Line;
import com.skagit.roth.workBookConcepts.WorkBookConcepts;
import com.skagit.util.MyStudiesDateUtils;
import com.skagit.util.MyStudiesStringUtils;
import com.skagit.util.NamedEntity;

public class AccountOwner extends NamedEntity {

    public final Date _birthDate;
    public final double _currentSsa;
    public final OutsideIncome[] _outsideIncomes;
    public final Account[] _accounts;

    public AccountOwner(final Block[] accountsBlocks, final String name, final Date birthDate) {
	super(name);
	_birthDate = birthDate;
	final int idx0 = Arrays.binarySearch(accountsBlocks, new NamedEntity("Owners and Social Security Incomes"));
	final Block block0 = accountsBlocks[idx0];
	final Line[] lines0 = block0._lines;
	final int idx1 = Arrays.binarySearch(lines0, new Line(_name));
	_currentSsa = lines0[idx1]._data._d;
	/** Search for the OutsideIncomes. */
	final Block block1A = WorkBookConcepts.getBlock(accountsBlocks, "Owners and Outside Incomes");
	final Line[] lines1A = block1A._lines;
	final Block block1B = WorkBookConcepts.getBlock(accountsBlocks, "Outside Incomes and Amounts");
	final Line[] lines1B = block1B._lines;
	final Block block1C = WorkBookConcepts.getBlock(accountsBlocks, "Outside Incomes and Years");
	final Line[] lines1C = block1C._lines;
	final int[] loHi1A = WorkBookConcepts.getMatchingLineIdxs(_name, lines1A);
	final int lo1A = loHi1A[0], hi1A = loHi1A[1];
	final int nOutsideIncomes = hi1A - lo1A;
	_outsideIncomes = new OutsideIncome[nOutsideIncomes];
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
	    _outsideIncomes[k - lo1A] = new OutsideIncome(this, oiName, oiAmount, oiYear);
	}
	_accounts = getAccounts(accountsBlocks);
    }

    /** For the "owner" of the Joint Accounts. */
    public AccountOwner(final Block[] accountsBlocks) {
	super(null);
	_birthDate = null;
	_currentSsa = Double.NaN;
	_outsideIncomes = null;
	_accounts = getAccounts(accountsBlocks);
    }

    private Account[] getAccounts(final Block[] accountsBlocks) {
	final ArrayList<Account> accountList = new ArrayList<>();
	final Block block2 = WorkBookConcepts.getBlock(accountsBlocks, "Owners and Accounts");
	final Line[] lines2 = block2._lines;
	final int[] loHi2 = WorkBookConcepts.getMatchingLineIdxs(_name, lines2);
	for (int k = loHi2[0]; k < loHi2[1]; ++k) {
	    final Line line = lines2[k];
	    final String accountName = line._data._s;
	    if (accountName != null && accountName.length() > 0) {
		accountList.add(new Account(accountsBlocks, this, accountName));
	    }
	}
	return accountList.toArray(new Account[accountList.size()]);
    }

    @Override
    public String getString() {
	String s = null;
	if (_name != null) {
	    s = String.format("%s: %s, ssa[%s]", //
		    _name, //
		    MyStudiesDateUtils.formatDateOnly(_birthDate), //
		    MyStudiesStringUtils.formatDollars(_currentSsa) //
	    );
	    final int nOutsideAccounts = _outsideIncomes.length;
	    if (nOutsideAccounts > 0) {
		s += ", Outside Incomes:";
	    }
	    for (int k = 0; k < nOutsideAccounts; ++k) {
		s += String.format("\n%s", _outsideIncomes[k].getString());
	    }
	} else {
	    s = "Joint Accounts Owner:";
	}
	s += "\n\nAccounts:";
	final int nAccounts = _accounts.length;
	for (int k = 0; k < nAccounts; ++k) {
	    s += String.format("\n%s", _accounts[k].getString());
	}
	return s;
    }

}
