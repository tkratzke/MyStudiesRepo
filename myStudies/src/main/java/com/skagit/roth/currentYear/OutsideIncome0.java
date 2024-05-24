package com.skagit.roth.currentYear;

import java.util.Arrays;

import com.skagit.roth.workBookConcepts.Line;
import com.skagit.roth.workBookConcepts.WorkBookConcepts;
import com.skagit.util.NamedEntity;
import com.skagit.util.TypeOfDouble;

public class OutsideIncome0 extends NamedEntity {

    public final Owner0 _owner0;
    public final double _amount;
    public final int _year;

    public OutsideIncome0(final String name, final WorkBookConcepts workBookConcepts, final Owner0 owner0) {
	super(name);
	_owner0 = owner0;
	final Line forLookUp = new Line(_name);
	final Line[] oi0BlockLines = workBookConcepts.getBlock(
		WorkBookConcepts.getSheetName(WorkBookConcepts._StaticsSheetIdx), "Outside Income Amount")._lines;
	final int idx0 = Arrays.binarySearch(oi0BlockLines, forLookUp);
	_amount = oi0BlockLines[idx0]._data._d;
	final Line[] oi1BlockLines = workBookConcepts.getBlock(
		WorkBookConcepts.getSheetName(WorkBookConcepts._StaticsSheetIdx), "Outside Income Year")._lines;
	final int idx1 = Arrays.binarySearch(oi1BlockLines, forLookUp);
	if (idx1 < 0) {
	    _year = -1;
	} else {
	    _year = (int) Math.round(oi1BlockLines[idx1]._data._d);
	}
    }

    public Owner0 getOwner() {
	return _owner0;
    }

    public String getString() {
	String s = String.format("OI[%s], Amnt[%s]", //
		_name, //
		TypeOfDouble.MONEY.format(_amount, 2));
	if (_year > 0) {
	    s += String.format(", Year[%d]", _year);
	}
	return s;
    }

    @Override
    public String toString() {
	return getString();
    }
}
