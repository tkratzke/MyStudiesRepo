package com.skagit.rothProblem;

import com.skagit.util.TypeOfDouble;

public class CapitalGains {
    final double _maxCapitalGainsLoss;
    final double _thisYearShortTermCg, _thisYearLongTermCg;
    final double _shortTermCarryForwardIn, _longTermCarryForwardIn;
    final double _shAfterOffset, _elAfterOffset, _shCfAfterOffset, _elCfAfterOffset;
    final double _ttlShortTermCg, _ttlLongTermCg;
    final double _shortTermCarryForwardOut, _longTermCarryForwardOut;

    public CapitalGains(final double maxCapitalGainsLoss, //
	    final double thisYearShortTermCg, final double thisYearLongTermCg, //
	    final double shortTermCarryForwardIn, final double longTermCarryForwardIn //
    ) {
	_maxCapitalGainsLoss = maxCapitalGainsLoss;
	_thisYearShortTermCg = thisYearShortTermCg;
	_thisYearLongTermCg = thisYearLongTermCg;
	_shortTermCarryForwardIn = shortTermCarryForwardIn;
	_longTermCarryForwardIn = longTermCarryForwardIn;
	double shCfOt = _shortTermCarryForwardIn;
	double shOt = _thisYearShortTermCg;
	/** Short CF offsetting short. */
	final double offset0 = Math.min(-shCfOt, shOt);
	if (offset0 > 0d) {
	    shOt -= offset0;
	    shCfOt += offset0;
	}
	/** Long CF offsetting long. */
	double elCfOt = _longTermCarryForwardIn;
	double elOt = _thisYearLongTermCg;
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
	_ttlShortTermCg = shOt;
	_ttlLongTermCg = elOt;
	_shortTermCarryForwardOut = shCfOt;
	_longTermCarryForwardOut = elCfOt;
    }

    public String getString() {
	final String maxCapGainsLossS = String.format("MaxCapitalGainsLoss[%s]",
		TypeOfDouble.MONEY.format(_maxCapitalGainsLoss, 2));
	final String inS = String.format("In:   Shrt:Long[%s,%s]  ShrtCf:LongCf[%s:%s]", //
		TypeOfDouble.MONEY.format(_thisYearShortTermCg, 2), //
		TypeOfDouble.MONEY.format(_thisYearLongTermCg, 2), //
		TypeOfDouble.MONEY.format(_shortTermCarryForwardIn, 2), //
		TypeOfDouble.MONEY.format(_longTermCarryForwardIn, 2));
	final String afterOffsetS = String.format("AO:   Shrt:Long[%s:%s]  ShrtCf:LongCf[%s:%s]", //
		TypeOfDouble.MONEY.format(_shAfterOffset, 2), //
		TypeOfDouble.MONEY.format(_elAfterOffset, 2), //
		TypeOfDouble.MONEY.format(_shCfAfterOffset, 2), //
		TypeOfDouble.MONEY.format(_elCfAfterOffset, 2));
	final String outS = String.format("Out:  Shrt:Long[%s:%s]  ShrtCf:LongCf[%s:%s]", //
		TypeOfDouble.MONEY.format(_ttlShortTermCg, 2), //
		TypeOfDouble.MONEY.format(_ttlLongTermCg, 2), //
		TypeOfDouble.MONEY.format(_shortTermCarryForwardOut, 2), //
		TypeOfDouble.MONEY.format(_longTermCarryForwardOut, 2));
	return String.format("%s\n%s ->\n%s ->\n%s", maxCapGainsLossS, inS, afterOffsetS, outS);
    }

    @Override
    public String toString() {
	return getString();
    }

    private CapitalGains(final int maxCapGainsLoss, final int[] shElshCfelCf) {
	this(maxCapGainsLoss, shElshCfelCf[0], shElshCfelCf[1], shElshCfelCf[2], shElshCfelCf[3]);
    }

    public static void mainx(final String[] args) {
	final int maxCapGainsLoss = 3;
	final int[][] cases = new int[][] { //
		{ -1, -5, 0, 0 }, //
		{ 2, -5, 0, 0 }, //
		{ 1, -5, -4, 0 }, //
		{ 1, -5, -4, 0 }, //
	};
	for (final int[] thisCase : cases) {
	    final CapitalGains cg = new CapitalGains(maxCapGainsLoss, thisCase);
	    System.out.println(cg.getString());
	    System.out.println();
	}
    }

}
