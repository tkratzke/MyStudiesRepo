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

    public static String formatPerCent(final double perCent, final int nDigits) {
	if (Math.abs(perCent) < 0.005) {
	    return "0%";
	}
	final int dInt = (int) Math.round(perCent);
	if (dInt == perCent) {
	    return String.format("%,d%%", dInt);
	}
	return String.format("%,." + nDigits + "f%%", perCent);
    }

    public static String formatDollars(final double dollars) {
	final int cents = (int) Math.round(dollars * 100d);
	final int absCents = Math.abs(cents);
	final String s;
	if (absCents % 100 == 0) {
	    s = String.format("$%,d", absCents / 100);
	} else {
	    s = String.format("$%,.2f", absCents / 100d);
	}
	return (absCents == cents ? "" : "-") + s;
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
