package com.skagit.roth;

public class Brackets {

    public static class PerCentCeiling {
	public final double _perCent;
	public final double _ceiling;

	public PerCentCeiling(final double perCent, final double ceiling) {
	    _perCent = perCent;
	    _ceiling = ceiling;
	}

	public String getString() {
	    return String.format("[%.1f%%:$%.2f]", _perCent, _ceiling);
	}

	@Override
	public String toString() {
	    return getString();
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
	    final double perCent = 100d * line._header._d;
	    final double dataD = line._data._d;
	    final double ceiling = dataD >= 0d ? dataD : Double.POSITIVE_INFINITY;
	    _perCentCeilings[k] = new PerCentCeiling(perCent, ceiling);
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
