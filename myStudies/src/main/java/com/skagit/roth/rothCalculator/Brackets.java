package com.skagit.roth.rothCalculator;

import com.skagit.roth.workBookConcepts.Line;
import com.skagit.roth.workBookConcepts.WorkBookConcepts;
import com.skagit.util.NamedEntity;
import com.skagit.util.TypeOfDouble;

public class Brackets extends NamedEntity {

    public static class PerCentCeiling implements Cloneable {
	public double _perCent;
	public double _ceiling;

	public PerCentCeiling(final double perCent, final double ceiling) {
	    _perCent = perCent;
	    _ceiling = ceiling;
	}

	@Override
	public PerCentCeiling clone() {
	    try {
		return (PerCentCeiling) super.clone();
	    } catch (final CloneNotSupportedException e) {
	    }
	    return null;
	}

	public String getString() {
	    return String.format("[%s:%s]", //
		    TypeOfDouble.PER_CENT.format(_perCent, 1), TypeOfDouble.MONEY.format(_ceiling, 2));
	}

	@Override
	public String toString() {
	    return getString();
	}
    }

    public Brackets(final Brackets brackets) {
	super(brackets._name);
	final PerCentCeiling[] perCentCeilings = brackets._perCentCeilings;
	final int n = perCentCeilings.length;
	_perCentCeilings = new PerCentCeiling[n];
	for (int k = 0; k < n; ++k) {
	    _perCentCeilings[k] = perCentCeilings[k].clone();
	}
    }

    public final PerCentCeiling[] _perCentCeilings;

    public Brackets(final WorkBookConcepts workBookConcepts, final String bracketsName) {
	super(bracketsName);
	final String bracketsSheetName = WorkBookConcepts.getSheetName(WorkBookConcepts._BracketsSheetIdx);
	final Line[] lines = workBookConcepts.getBlock(bracketsSheetName, bracketsName)._lines;
	final int nLines = lines.length;
	_perCentCeilings = new PerCentCeiling[nLines];
	for (int k = 0; k < nLines; ++k) {
	    final Line line = lines[k];
	    final double dataD = line._data._d;
	    final double ceiling = dataD >= 0d ? dataD : Double.POSITIVE_INFINITY;
	    _perCentCeilings[k] = new PerCentCeiling(100d * line._header._d, ceiling);
	}
    }

    public String getString() {
	String s = _name;
	final int nCeilingPerCents = _perCentCeilings.length;
	for (int k = 0; k < nCeilingPerCents; ++k) {
	    s += String.format("\n%s", _perCentCeilings[k].getString());
	}
	return s;
    }

    @Override
    public String toString() {
	return getString();
    }

}