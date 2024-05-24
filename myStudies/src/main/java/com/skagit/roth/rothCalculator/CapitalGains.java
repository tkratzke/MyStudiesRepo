package com.skagit.roth.rothCalculator;

import com.skagit.util.TypeOfDouble;

public class CapitalGains {
    final double _maxCapitalGainsLoss;
    final double _shIn, _elIn, _shCfIn, _elCfIn;
    final double _shAfterOffset, _elAfterOffset, _shCfAfterOffset, _elCfAfterOffset;
    final double _shOt, _elOt, _shCfOt, _elCfOt;

    public CapitalGains(final double maxCapitalGainsLoss, final double shIn, final double elIn, final double cfShIn,
	    final double cfElIn) {
	_maxCapitalGainsLoss = maxCapitalGainsLoss;
	_shIn = shIn;
	_elIn = elIn;
	_shCfIn = cfShIn;
	_elCfIn = cfElIn;
	double shCfOt = _shCfIn;
	double shOt = _shIn;
	/** Short CF offsetting short. */
	final double offset0 = Math.min(-shCfOt, shOt);
	if (offset0 > 0d) {
	    shOt -= offset0;
	    shCfOt += offset0;
	}
	/** Long CF offsetting long. */
	double elCfOt = _elCfIn;
	double elOt = _elIn;
	final double offset1 = Math.min(-elCfOt, elOt);
	if (offset1 > 0d) {
	    elOt -= offset1;
	    elCfOt += offset1;
	}
	/** Long CF offsetting short. */
	final double offset2 = Math.min(-elCfOt, shOt);
	if (offset2 > 0d) {
	    shOt -= offset2;
	    elCfOt += offset2;
	}
	/** Short CF offsetting Long. */
	final double offset3 = Math.min(-shCfOt, elOt);
	if (offset3 > 0d) {
	    elOt -= offset3;
	    shCfOt += offset3;
	}
	_shAfterOffset = shOt;
	_elAfterOffset = elOt;
	_shCfAfterOffset = shCfOt;
	_elCfAfterOffset = elCfOt;

	/** Cap -shOt and -elOt as per _maxCapitalGAinsLoss. */
	final double minShOt = -_maxCapitalGainsLoss;
	if (shOt < minShOt) {
	    shCfOt -= (minShOt - shOt);
	    shOt = minShOt;
	}
	final double minElOt;
	if (shOt < 0d) {
	    minElOt = -_maxCapitalGainsLoss - shOt;
	} else {
	    minElOt = -_maxCapitalGainsLoss;
	}
	if (elOt < minElOt) {
	    elCfOt -= (minElOt - elOt);
	    elOt = minElOt;
	}
	_shOt = shOt;
	_elOt = elOt;
	_shCfOt = shCfOt;
	_elCfOt = elCfOt;
    }

    public String getString() {
	final String maxCapGainsLossS = String.format("MaxCapitalGainsLoss[%s]",
		TypeOfDouble.MONEY.format(_maxCapitalGainsLoss, 2));
	final String inS = String.format("In:   Shrt:Long[%s,%s]  ShrtCf:LongCf[%s:%s]", //
		TypeOfDouble.MONEY.format(_shIn, 2), //
		TypeOfDouble.MONEY.format(_elIn, 2), //
		TypeOfDouble.MONEY.format(_shCfIn, 2), //
		TypeOfDouble.MONEY.format(_elCfIn, 2));
	final String afterOffsetS = String.format("AO:   Shrt:Long[%s:%s]  ShrtCf:LongCf[%s:%s]", //
		TypeOfDouble.MONEY.format(_shAfterOffset, 2), //
		TypeOfDouble.MONEY.format(_elAfterOffset, 2), //
		TypeOfDouble.MONEY.format(_shCfAfterOffset, 2), //
		TypeOfDouble.MONEY.format(_elCfAfterOffset, 2));
	final String outS = String.format("Out:  Shrt:Long[%s:%s]  ShrtCf:LongCf[%s:%s]", //
		TypeOfDouble.MONEY.format(_shOt, 2), //
		TypeOfDouble.MONEY.format(_elOt, 2), //
		TypeOfDouble.MONEY.format(_shCfOt, 2), //
		TypeOfDouble.MONEY.format(_elCfOt, 2));
	return String.format("%s\n%s ->\n%s ->\n%s", maxCapGainsLossS, inS, afterOffsetS, outS);
    }

    @Override
    public String toString() {
	return getString();
    }

    /** For debugging. */
    private final static int _MaxCapGainsLoss = 3;

    private CapitalGains(final int[] shElshCfelCf) {
	this(_MaxCapGainsLoss, shElshCfelCf[0], shElshCfelCf[1], shElshCfelCf[2], shElshCfelCf[3]);
    }

    public static void main(final String[] args) {

	final int[][] cases = new int[][] { //
		{ -1, -5, 0, 0 }, //
		{ 2, -5, 0, 0 }, //
		{ 1, -5, -4, 0 }, //
		{ 1, -5, -4, 0 }, //
	};
	for (final int[] thisCase : cases) {
	    final CapitalGains cg = new CapitalGains(thisCase);
	    System.out.println(cg.getString());
	    System.out.println();
	}
    }

}
