package com.skagit.rothProblem;

import com.skagit.util.MyStudiesStringUtils;
import com.skagit.util.NamedEntity;
import com.skagit.util.TypeOfDouble;

public class GrowthRate extends NamedEntity {

    public final double _perCent;
    public final double _expGrowthRate;

    public GrowthRate(final String name, final double proportion) {
	super(name);
	_perCent = proportion * 100d;
	_expGrowthRate = Math.log(1d + _perCent / 100d);
    }

    @Override
    public String getString() {
	return String.format("%s %s: ExpGrowthRate[%s]", //
		_name, //
		TypeOfDouble.PER_CENT.format(_perCent, 0), //
		MyStudiesStringUtils.formatOther(_expGrowthRate, 4));
    }
}
