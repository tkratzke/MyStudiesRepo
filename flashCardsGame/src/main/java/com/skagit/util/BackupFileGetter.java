package com.skagit.util;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BackupFileGetter {
    final static Pattern _Pattern = Pattern.compile("^(.*?)(\\d+)$");

    public static File getBackupFile(final File f, final String suffix, final int nDigits) {
	if (f == null || !f.isFile()) {
	    return null;
	}
	final File fParent = f.getParentFile();
	final String fName = f.getName();
	if (!fName.toLowerCase().endsWith(suffix.toLowerCase())) {
	    return null;
	}
	final int suffixLen = suffix.length();
	final int fNameLen = fName.length();
	final String s0 = fName.substring(0, fNameLen - suffixLen);
	final Matcher matcher = _Pattern.matcher(s0);
	String newFName = null;
	if (matcher.matches()) {
	    final String group1 = matcher.group(1);
	    final String group2 = matcher.group(2);
	    try {
		final int k = Integer.parseInt(group2);
		newFName = String.format("%s%0" + nDigits + "d%s", group1, k + 1, suffix);
	    } catch (final NumberFormatException e) {
	    }
	}
	if (newFName == null) {
	    newFName = String.format("%s$%0" + nDigits + "d%s", s0, 0, suffix);
	}
	final File newF = new File(fParent, newFName);
	if (!newF.exists()) {
	    return newF;
	}
	return getBackupFile(newF, suffix, nDigits);
    }

    public static void mainx(final String[] args) {
	final String fName = "AAA$000" + Statics._CardsFileEnding;
	System.out.println(fName + " "
		+ getBackupFile(new File(".", fName), Statics._CardsFileEnding, Statics._NDigitsForCardsFileBackups));
    }
}
