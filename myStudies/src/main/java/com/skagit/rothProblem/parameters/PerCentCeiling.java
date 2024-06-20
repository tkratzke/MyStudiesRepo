package com.skagit.rothProblem.parameters;

import com.skagit.util.MyStudiesStringUtils;

public class PerCentCeiling implements Cloneable {
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
		MyStudiesStringUtils.formatPerCent(_perCent, 1), MyStudiesStringUtils.formatDollars(_ceiling));
    }

    @Override
    public String toString() {
	return getString();
    }
}