package com.skagit.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class MyStudiesDateUtils {
    final private static DateTimeFormatter _TimeFormatter = DateTimeFormatter.ofPattern("MMM-dd hh:mm:ss");
    final private static SimpleDateFormat _SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    final private static SimpleDateFormat _YearOnlySimpleDateFormat = new SimpleDateFormat("yyyy");
    TimeZone _ReferenceTimeZone = TimeZone.getTimeZone("Etc/UTC");

    public static long getDateDiff(final Date date0, final Date date1, final TimeUnit timeUnit) {
	return timeUnit.convert(date1.getTime() - date0.getTime(), TimeUnit.MILLISECONDS);
    }

    public static int getAPartOfADate(final Date date, final ChronoField chronoField) {
	final LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	return localDate.get(chronoField);
    }

    public static Date parseDate(final String date) {
	try {
	    return _SimpleDateFormat.parse(date);
	} catch (final ParseException e) {
	    return null;
	}
    }

    public static String getStringFromMillis(final long millis) {
	final Instant instant = Instant.ofEpochMilli(millis);
	final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
	return zonedDateTime.format(_TimeFormatter);
    }

    public static String getCurrentTimeString() {
	return getStringFromMillis(System.currentTimeMillis());
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
