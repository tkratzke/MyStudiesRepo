package com.skagit.roth;

public class Brackets {

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
	    return String.format("[%.1f%%:$%.2f]", _perCent, _ceiling);
	}

	@Override
	public String toString() {
	    return getString();
	}
    }

    public Brackets(final Brackets brackets) {
	_name = brackets._name;
	final PerCentCeiling[] perCentCeilings = brackets._perCentCeilings;
	final int n = perCentCeilings.length;
	_perCentCeilings = new PerCentCeiling[n];
	for (int k = 0; k < n; ++k) {
	    _perCentCeilings[k] = perCentCeilings[k].clone();
	}
    }

    public final String _name;
    public final PerCentCeiling[] _perCentCeilings;

    public Brackets(final RothCalculator rothCalculator, final String bracketsName) {
	_name = bracketsName;
	final String bracketsSheetName = RothCalculator.getSheetName(RothCalculator._BracketsIdx);
	final Line[] lines = rothCalculator.getBlock(bracketsSheetName, bracketsName)._lines;
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
	    s += String.format("\n%S", _perCentCeilings[k]);
	}
	return s;
    }

    @Override
    public String toString() {
	return getString();
    }

}
