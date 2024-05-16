package com.skagit.roth;

import com.skagit.util.NamedEntity;

public class GrowthRate extends NamedEntity {

    public final double _perCentGrowth;
    public final double _expGrowthRate;

    public GrowthRate(final String name, final double perCentGrowth) {
	super(name);
	_perCentGrowth = perCentGrowth;
	_expGrowthRate = Math.log(1d + _perCentGrowth / 100d);
    }

    public String getString() {
	return String.format("Name[%s], PerCentGrowth[%.0f%%] ExpRate[%.4f]", //
		_name, _perCentGrowth, _expGrowthRate);
    }

    @Override
    public String toString() {
	return getString();
    }
}
