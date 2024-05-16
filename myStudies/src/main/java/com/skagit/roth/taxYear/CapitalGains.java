package com.skagit.roth.taxYear;

import com.skagit.util.MyStudiesStringUtils;

public class CapitalGains {
    final static double _MinOutSum = -3000d;

    /** Note that the "in" fields are only for printing out getString() to debug. */
    final double _shIn, _ellIn, _cfShIn, _cfEllIn;
    final double _shOut, _ellOut, _cfShOut, _cfEllOut;

    /** Assumes shIn and ellIn have already been extrapolated. */
    public CapitalGains(final double shIn, final double ellIn, final double cfShIn, final double cfEllIn) {
	_shIn = shIn;
	_ellIn = ellIn;
	_cfShIn = cfShIn;
	_cfEllIn = cfEllIn;
	double cfShOut = _cfShIn;
	double shCgOut = _shIn;
	/** Short CF offsetting short. */
	final double offset0 = Math.min(-cfShOut, shCgOut);
	if (offset0 > 0d) {
	    shCgOut -= offset0;
	    cfShOut += offset0;
	}
	/** Long CF offsetting long. */
	double cfEllOut = _cfEllIn;
	double ellCgOut = _ellIn;
	final double offset1 = Math.min(-cfEllOut, ellCgOut);
	if (offset1 > 0d) {
	    ellCgOut -= offset1;
	    cfEllOut += offset1;
	}
	/** Long CF offsetting short. */
	final double offset2 = Math.min(-cfEllOut, shCgOut);
	if (offset2 > 0d) {
	    shCgOut -= offset2;
	    cfEllOut += offset2;
	}
	/** Short CF offsetting Long. */
	final double offset3 = Math.min(-cfShOut, ellCgOut);
	if (offset3 > 0d) {
	    ellCgOut -= offset3;
	    cfShOut += offset3;
	}
	_shOut = shCgOut;
	_ellOut = ellCgOut;
	_cfShOut = cfShOut;
	_cfEllOut = cfEllOut;
    }

    public String getString() {
	final String inS = String.format("[ShIn/ElIn/CfShIn/CfElIn]=[%s/%s/%s/%s]", //
		MyStudiesStringUtils.formatDollars(_shIn), //
		MyStudiesStringUtils.formatDollars(_ellIn), //
		MyStudiesStringUtils.formatDollars(_cfShIn), //
		MyStudiesStringUtils.formatDollars(_cfEllIn));
	final String outS = String.format("[ShOt/ElOt/CfShOt/CfElOt]=[%s/%s/%s/%s]", //
		MyStudiesStringUtils.formatDollars(_shOut), //
		MyStudiesStringUtils.formatDollars(_ellOut), //
		MyStudiesStringUtils.formatDollars(_cfShOut), //
		MyStudiesStringUtils.formatDollars(_cfEllOut));
	return String.format("%s ->\n%s", inS, outS);
    }

    @Override
    public String toString() {
	return getString();
    }

    public static void main(final String[] args) {
	final CapitalGains cg = new CapitalGains(1000d, 10000d, -4000d, -000d);
	System.out.println(cg.getString());
    }

}
