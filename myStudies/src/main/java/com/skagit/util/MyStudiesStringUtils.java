package com.skagit.util;

import java.util.Arrays;

public class MyStudiesStringUtils {
    final private static String _WhiteSpace = "\\s+";

    private static int _MaxWidthForString = 80;

    public static String getString(final long[] longVector) {
	final int vecLen = longVector.length;
	String s = "[";
	for (int k = 0, sLen = s.length(); k < vecLen; ++k) {
	    final String ss = String.format("%d%c", longVector[k], (k == vecLen - 1 ? ']' : ','));
	    final int ssLen = ss.length();
	    if (sLen + ssLen > _MaxWidthForString) {
		s += "\n  ";
		sLen = 2;
	    }
	    s += ss;
	    sLen += ssLen;
	}
	return s;
    }

    public static String formatPerCent(final double d, final int nDigits) {
	if (Math.abs(d) < 0.005) {
	    return "0%";
	}
	final int dInt = (int) Math.round(d);
	if (dInt == d) {
	    return String.format("%,d%%", dInt);
	}
	return String.format("%,." + nDigits + "f%%", d);
    }

    public static String formatDollars(final double d) {
	if (Math.abs(d) < 0.005) {
	    return "$0";
	}
	final int dInt = (int) Math.round(d);
	final String s;
	if (dInt == d) {
	    s = String.format("%,d", dInt);
	} else {
	    s = String.format("%,.2f", d);
	}
	if (s.charAt(0) != '-') {
	    return "$" + s;
	}
	return "-$" + s.substring(1);
    }

    public static String formatOther(final double d, final int nDigits) {
	return String.format("%,." + nDigits + "f", d);
    }

    public static String getString(final int[] intVector) {
	return getString(intsToLongs(intVector));
    }

    public static long[] intsToLongs(final int[] intVector) {
	return Arrays.stream(intVector).mapToLong(i -> i).toArray();
    }

    public static String padRight(final String s, final int n) {
	return String.format("%1$-" + n + "s", s);
    }

    public static String padLeft(final String s, final int n) {
	return String.format("%1$" + n + "s", s);
    }

    public static String durationInSecondsToMinsSecsString(final int durationInSeconds, final int nDigitsForMinutes) {
	final int minutes = durationInSeconds / 60;
	final int seconds = durationInSeconds - (minutes * 60);
	final String format = "%0" + nDigitsForMinutes + "d:%02d";
	return String.format(format, minutes, seconds);
    }

    public static String CleanWhiteSpace(final String s) {
	if (s == null) {
	    return "";
	}
	final String ss = s.trim().replaceAll(_WhiteSpace, " ");
	return ss.length() == s.length() ? s : ss;
    }

}
