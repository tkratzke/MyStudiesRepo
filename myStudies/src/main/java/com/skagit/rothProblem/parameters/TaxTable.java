package com.skagit.rothProblem.parameters;

import com.skagit.rothProblem.workBookConcepts.Block;
import com.skagit.rothProblem.workBookConcepts.Line;
import com.skagit.util.NamedEntity;

public class TaxTable extends NamedEntity {

    public final int _forYear;
    public final PerCentCeiling[] _perCentCeilings;

    public TaxTable(final int forYear, final Block block) {
	super(block._name);
	_forYear = forYear;
	final Line[] lines = block._lines;
	final int nLines = lines.length;
	_perCentCeilings = new PerCentCeiling[nLines];
	for (int k = 0; k < nLines; ++k) {
	    final Line line = lines[k];
	    final double dataD = line._data._d;
	    final double perCent = 100d * line._header._d;
	    final double ceiling = dataD >= 0d ? dataD : Double.POSITIVE_INFINITY;
	    _perCentCeilings[k] = new PerCentCeiling(perCent, ceiling);
	}
    }

    public TaxTable(final String name, final int forYear, final PerCentCeiling[] perCentCeilings) {
	super(name);
	_forYear = forYear;
	_perCentCeilings = perCentCeilings;
    }

    @Override
    public String getString() {
	String s = String.format("%s-%d", _name, _forYear);
	final int nCeilingPerCents = _perCentCeilings.length;
	for (int k = 0; k < nCeilingPerCents; ++k) {
	    s += String.format("\n%s", _perCentCeilings[k].getString());
	}
	return s;
    }

}
