package com.skagit.util;

import org.apache.poi.ss.usermodel.DateUtil;

public enum TypeOfDouble {
    DATE, MONEY, PER_CENT, OTHER;

    public String format(final double d, final int nDigits) {
	if (this == DATE) {
	    return MyStudiesDateUtils.formatDateOnly(DateUtil.getJavaDate(d));
	}
	switch (this) {
	case DATE:
	    return MyStudiesDateUtils.formatDateOnly(DateUtil.getJavaDate(d));
	case MONEY:
	    return MyStudiesStringUtils.formatDollars(d);
	case OTHER:
	    return MyStudiesStringUtils.formatOther(d, nDigits);
	case PER_CENT:
	    return MyStudiesStringUtils.formatPerCent(d, nDigits);
	default:
	}
	return null;
    }

}
