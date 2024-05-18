package com.skagit.roth;

import org.apache.poi.ss.usermodel.DateUtil;

import com.skagit.util.MyStudiesDateUtils;
import com.skagit.util.MyStudiesStringUtils;

public enum TypeOfDouble {
    DATE, MONEY, PER_CENT, OTHER;

    public String format(final double d) {
	if (this == DATE) {
	    return MyStudiesDateUtils.formatDateOnly(DateUtil.getJavaDate(d));
	}
	switch (this) {
	case DATE:
	    return MyStudiesDateUtils.formatDateOnly(DateUtil.getJavaDate(d));
	case MONEY:
	    return MyStudiesStringUtils.formatDollars(d);
	case OTHER:
	    return MyStudiesStringUtils.formatOther(d, 2);
	case PER_CENT:
	    return MyStudiesStringUtils.formatPerCent(d);
	default:
	}
	return null;
    }

}
