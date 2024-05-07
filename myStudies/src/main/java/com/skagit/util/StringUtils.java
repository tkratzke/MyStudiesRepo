package com.skagit.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;

public class StringUtils {
    final private static DateTimeFormatter _TimeFormatter = DateTimeFormatter.ofPattern("MMM-dd hh:mm:ss");
    final private static SimpleDateFormat _SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    final private static SimpleDateFormat _YearOnlySimpleDateFormat = new SimpleDateFormat("yyyy");
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

    public static String getString(final int[] intVector) {
	return getString(intsToLongs(intVector));
    }

    public static long[] intsToLongs(final int[] intVector) {
	return Arrays.stream(intVector).mapToLong(i -> i).toArray();
    }

    public static String getStringFromMillis(final long millis) {
	final Instant instant = Instant.ofEpochMilli(millis);
	final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
	return zonedDateTime.format(_TimeFormatter);
    }

    public static String getCurrentTimeString() {
	return getStringFromMillis(System.currentTimeMillis());
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

    public static Date getDateOnly(final Date date) {
	try {
	    return _SimpleDateFormat.parse(_SimpleDateFormat.format(date));
	} catch (final ParseException e) {
	}
	return null;
    }

    public static String formatYearOnly(final Date date) {
	return _YearOnlySimpleDateFormat.format(date);
    }

    public static String formatDateOnly(final Date date) {
	return _SimpleDateFormat.format(date);
    }

}
