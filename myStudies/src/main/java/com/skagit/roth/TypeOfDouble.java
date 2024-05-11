package com.skagit.roth;

import org.apache.poi.ss.usermodel.DateUtil;

import com.skagit.util.DateUtils;

public enum TypeOfDouble {
    DATE(null), MONEY("$%.2f"), PER_CENT("%.1f%%"), OTHER("%.2f");

    final String _formatString;

    TypeOfDouble(final String formatString) {
	_formatString = formatString;
    }

    String format(final double d) {
	if (this == DATE) {
	    return DateUtils.formatDateOnly(DateUtil.getJavaDate(d));
	}
	return String.format(_formatString, d);
    }

}
