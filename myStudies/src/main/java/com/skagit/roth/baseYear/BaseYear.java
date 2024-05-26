package com.skagit.roth.baseYear;

import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.skagit.roth.rothCalculator.RothCalculator;
import com.skagit.roth.taxYear.ParameterSet;
import com.skagit.roth.workBookConcepts.Field;
import com.skagit.roth.workBookConcepts.Line;
import com.skagit.roth.workBookConcepts.WorkBookConcepts;
import com.skagit.util.MyStudiesDateUtils;
import com.skagit.util.TypeOfDouble;

public class BaseYear {

    public final Date _baseDate;
    public final int _baseDateYear;
    public final double _perCentLeftOfBaseYear;

    public final ParameterSet _baseParameterSet;

    public final Owner0[] _owner0s;
    public final Account0[] _jointAccounts;

    public BaseYear(final RothCalculator rothCalculator) {
	final WorkBookConcepts workBookConcepts = rothCalculator._workBookConcepts;
	final String staticsSheetName = WorkBookConcepts.getSheetName(WorkBookConcepts._StaticsSheetIdx);
	_baseDate = workBookConcepts.getMiscellaneousData("Base Date").getDate();
	_baseDateYear = MyStudiesDateUtils.getAPartOfADate(_baseDate, ChronoField.YEAR);
	final Date thisJan1 = MyStudiesDateUtils.parseDate(String.format("%d-1-1", _baseDateYear));
	final Date nextJan1 = MyStudiesDateUtils.parseDate(String.format("%d-1-1", _baseDateYear + 1));
	final double d0 = MyStudiesDateUtils.getDateDiff(thisJan1, _baseDate, TimeUnit.DAYS);
	final double d1 = MyStudiesDateUtils.getDateDiff(thisJan1, nextJan1, TimeUnit.DAYS);
	_perCentLeftOfBaseYear = 100d * (d1 - d0) / d1;
	_baseParameterSet = new ParameterSet(rothCalculator, _baseDateYear, _baseDateYear);

	final Line[] ownerLines = workBookConcepts.getBlock(staticsSheetName, "Owners")._lines;
	final int nLines0 = ownerLines.length;
	_owner0s = new Owner0[nLines0];
	for (int k = 0; k < nLines0; ++k) {
	    _owner0s[k] = new Owner0(ownerLines[k]);
	}
	Arrays.sort(_owner0s);

	final ArrayList<Account0> jointList = new ArrayList<>();
	final Line[] allAccntDefnLines = workBookConcepts.getBlock(staticsSheetName, "Account Owners")._lines;
	final int nAccntDefns = allAccntDefnLines.length;
	for (int k = 0; k < nAccntDefns; ++k) {
	    final Line accntDefnLine = allAccntDefnLines[k];
	    final Field accntDefnLineData = accntDefnLine._data;
	    final String accountOwnerName = accntDefnLineData._s;
	    if (accountOwnerName == null || accountOwnerName.length() == 0) {
		/** This is a Joint account. */
		final String accntName = accntDefnLine._header._s;
		jointList.add(new Account0(accntName, workBookConcepts, /* owner= */null));
	    }
	}

	final int nJointAccounts = jointList.size();
	_jointAccounts = jointList.toArray(new Account0[nJointAccounts]);
	Arrays.sort(_jointAccounts);
    }

    public String getString() {
	String s = String.format("Base Yr[%d], Base Dt[%s], %%of BaseYr Left[%s]\n%s", //
		_baseDateYear, MyStudiesDateUtils.formatDateOnly(_baseDate), //
		TypeOfDouble.PER_CENT.format(_perCentLeftOfBaseYear, 2), //
		_baseParameterSet.getString() //
	);
	final int nOwner0s = _owner0s.length;
	for (int k = 0; k < nOwner0s; ++k) {
	    s += String.format("\n\n%d. %s", k, _owner0s[k].getString());
	}
	final Account0[] jointAccounts = _jointAccounts;
	final int nJointAccounts = jointAccounts.length;
	for (int k = 0; k < nJointAccounts; ++k) {
	    s += String.format("\n\n%s", jointAccounts[k].getString());
	}
	return s;
    }

    @Override
    public String toString() {
	return getString();
    }

}
