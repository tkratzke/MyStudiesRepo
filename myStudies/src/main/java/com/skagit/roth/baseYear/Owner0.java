package com.skagit.roth.baseYear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import com.skagit.roth.workBookConcepts.Field;
import com.skagit.roth.workBookConcepts.Line;
import com.skagit.roth.workBookConcepts.SheetAndBlocks;
import com.skagit.roth.workBookConcepts.WorkBookConcepts;
import com.skagit.util.MyStudiesDateUtils;
import com.skagit.util.NamedEntity;
import com.skagit.util.TypeOfDouble;

public class Owner0 extends NamedEntity {

    public final Date _dateOfBirth;
    public final double _ssIncome;
    public final Account0[] _iras;
    public final OutsideIncome0[] _outsideIncome0s;
    public final Account0 _rothAccount;

    public Owner0(final Line line) {
	super(line._header._s);
	_dateOfBirth = line._data.getDate();
	final SheetAndBlocks sheetAndBlocks = line._sheetAndBlocks;
	final WorkBookConcepts workBookConcepts = sheetAndBlocks._workBookConcepts;
	final String staticsSheetName = WorkBookConcepts.getSheetName(WorkBookConcepts._StaticsSheetIdx);
	final Line[] ssaLines = workBookConcepts.getBlock(staticsSheetName, "Social Security Income")._lines;
	final int idx = Arrays.binarySearch(ssaLines, new Line(_name));
	_ssIncome = idx < 0 ? 0d : ssaLines[idx]._data._d;
	final Line[] allAccntDefnLines = workBookConcepts.getBlock(staticsSheetName, "Account Owners")._lines;
	final int nAccntDefns = allAccntDefnLines.length;
	final ArrayList<Account0> myAccntList = new ArrayList<>();
	for (int k = 0; k < nAccntDefns; ++k) {
	    final Line accntDefnLine = allAccntDefnLines[k];
	    final Field accntDefnLineData = accntDefnLine._data;
	    final String ownerName = accntDefnLineData._s;
	    if (ownerName != null && ownerName.equals(_name)) {
		final String accntName = accntDefnLine._header._s;
		myAccntList.add(new Account0(accntName, workBookConcepts, this));
	    }
	}
	_iras = myAccntList.toArray(new Account0[myAccntList.size()]);
	final Line[] outsideIncomeDefnLines = workBookConcepts.getBlock(staticsSheetName, "Outside Income")._lines;
	final int nOidls = outsideIncomeDefnLines.length;
	final ArrayList<OutsideIncome0> oiList = new ArrayList<>();
	for (int k = 0; k < nOidls; ++k) {
	    final Line oiLine = outsideIncomeDefnLines[k];
	    final Field oiData = oiLine._data;
	    if (oiData._s.equals(_name)) {
		final String oiName = oiLine._header._s;
		oiList.add(new OutsideIncome0(oiName, workBookConcepts, this));
	    }
	}
	_outsideIncome0s = oiList.toArray(new OutsideIncome0[oiList.size()]);
	Arrays.sort(_outsideIncome0s);
	_rothAccount = new Account0("Roth", workBookConcepts, this);
    }

    public String getString() {
	String s = String.format("Owner[%s], DtOfBrth[%s]", _name, MyStudiesDateUtils.formatDateOnly(_dateOfBirth));
	if (_ssIncome > 0d) {
	    s += String.format(" CrrntYrSsa[%s]", TypeOfDouble.MONEY.format(_ssIncome, 2));
	}
	final int nMyAccnts = _iras.length;
	for (int k = 0; k < nMyAccnts; ++k) {
	    s += "\n\t" + _iras[k].getString();
	}
	final int nOutsideIncomes = _outsideIncome0s.length;
	for (int k = 0; k < nOutsideIncomes; ++k) {
	    s += "\n\t" + _outsideIncome0s[k].getString();
	}
	return s;
    }

    @Override
    public String toString() {
	return getString();
    }
}
