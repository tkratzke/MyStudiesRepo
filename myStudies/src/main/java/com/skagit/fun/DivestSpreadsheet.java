package com.skagit.fun;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;
import java.util.TreeSet;

import com.skagit.util.CombinatoricTools;
import com.skagit.util.ShortNameFinder;
import com.skagit.util.StringUtilities;

public class DivestSpreadsheet {
    final static public int _MaxSheetNameLength = 31;
    final static private SimpleDateFormat _MyDateFormat = new SimpleDateFormat("yyyy-MM-dd_hhmmsszzz");

    public static void main(final String[] args) {

	/** Boilerplate for getting homeDir. */
	final int nArgs = args.length;
	int iArg = 0;
	final File homeDir;
	if (iArg == nArgs) {
	    final String homeDirPath = StringUtilities.getSystemProperty("user.dir", /* useSpaceProxy= */true);
	    homeDir = new File(homeDirPath);
	} else {
	    homeDir = new File(args[iArg++]);
	}
	if (!homeDir.isDirectory()) {
	    System.exit(33);
	}

	/** Get the DateFile for listing the item directories.. */
	@SuppressWarnings("unused")
	final File[] datedFiles = homeDir.listFiles(new FileFilter() {

	    @Override
	    public boolean accept(final File f) {
		final String fName = f.getName();
		if (!f.isFile() || !fName.toLowerCase().endsWith(".txt")) {
		    return false;
		}
		try {
		    final Date dateInName = _MyDateFormat.parse(fName.substring(0, fName.length() - ".txt".length()));
		    return true;
		} catch (final ParseException e) {
		}
		return false;
	    }
	});
	final String newDateFileName = _MyDateFormat.format(new Date()) + ".txt";
	final File dateFile = new File(homeDir, newDateFileName);

	/** Build the map from itemDir to sheetName. */
	final File[] itemDirs = homeDir.listFiles(new FileFilter() {

	    @Override
	    public boolean accept(final File f) {
		return f.isDirectory();
	    }
	});
	final int nItemDirs = itemDirs.length;
	final TreeMap<File, String> itemDirToShortName = ShortNameFinder.computeCaseDirToShortName(homeDir, itemDirs,
		/* abbreviate= */false);
	final TreeMap<File, String> itemDirToSheetName = new TreeMap<>();
	final TreeSet<String> sheetNames = new TreeSet<>();
	for (int k0 = 0; k0 < nItemDirs; ++k0) {
	    final File itemDir = itemDirs[k0];
	    final String shortName = itemDirToShortName.get(itemDir);
	    for (int k1 = 0;; ++k1) {
		final String sheetName = CombinatoricTools.alterString(shortName, _MaxSheetNameLength,
			/* uniquifiers= */"!@#$%^&", k1);
		if (sheetNames.add(sheetName)) {
		    itemDirToSheetName.put(itemDir, sheetName);
		    break;
		}
	    }
	}
	try (PrintStream printStream = new PrintStream(dateFile)) {
	    int k = 0;
	    for (final File dir : itemDirToShortName.keySet()) {
		final String shortName = itemDirToShortName.get(dir);
		final String sheetName = itemDirToSheetName.get(dir);
		printStream.printf("%03d dir[%s] short[%s] sheet[%s]\n", ++k, dir.getName(), shortName, sheetName);
	    }
	} catch (final IOException e) {
	}
    }

}
