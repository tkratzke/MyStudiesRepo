package com.skagit.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintStream;

public class DirsTracker {

    final private static String _GameDirsName = "gameDirs";
    final private static String _CardsFilesDirName = "cardsFiles";
    final private static String _SoundFileDirsDirName = "soundFiles";
    final private static String _LogDirName = "LogFiles";

    final public static File _UserDir;
    final public static File _LogDir;
    final public static File _DataDir;
    final public static File _GameDirsDir;
    final public static File _CardsFilesDir;
    final public static File _SoundFilesDirsDir;

    static {
	final String userDirPath = Statics.getSystemProperty("user.dir", /* useSpaceProxy= */true);
	_UserDir = new File(userDirPath);
	/**
	 * <pre>
	 * Unless overridden as a VM argument, _DataDir is set to _UserDir.
	 * </pre>
	 */
	final String dataDirPath = Statics.getSystemProperty("Data.Dir", /* useSpaceProxy= */true);
	if (dataDirPath == null) {
	    _DataDir = _UserDir;
	} else {
	    _DataDir = new File(dataDirPath);
	}
	_LogDir = new File(_DataDir, _LogDirName);
	if (!_LogDir.isDirectory()) {
	    _LogDir.mkdirs();
	}
	final boolean clearLogDir = Statics.getSystemProperty("ClearLogDir", /* useSpaceProxy= */true) == null;
	if (clearLogDir) {
	    final FileFilter filesToDeleteFilter = new FileFilter() {
		@Override
		public boolean accept(final File f) {
		    if (f.isDirectory()) {
			return false;
		    }
		    final String fNameLc = f.getName().toLowerCase();
		    return fNameLc.toLowerCase().endsWith(".log");
		}
	    };
	    final File[] filesToDelete = _LogDir.listFiles(filesToDeleteFilter);
	    if (filesToDelete != null) {
		for (final File f : filesToDelete) {
		    if (f.exists() && !f.delete()) {
			final String fPath = Statics.getCanonicalPath(f);
			logStream(System.err, "Could not delete " + fPath);
		    }
		}
	    }
	}
	if (_DataDir != null && _DataDir.isDirectory()) {
	    _GameDirsDir = new File(_DataDir, _GameDirsName);
	    _CardsFilesDir = new File(_DataDir, _CardsFilesDirName);
	    _SoundFilesDirsDir = new File(_DataDir, _SoundFileDirsDirName);
	} else {
	    _GameDirsDir = _CardsFilesDir = _SoundFilesDirsDir = null;
	}
    }

    public static void logStream(final PrintStream ps, final String s) {
	final String timeString = Statics.formatNow();
	ps.println(String.format("[%s]: %s", timeString, s));
    }

    public static File getLogFile(final String fileNameX) {
	final String baseName = Statics.getBaseName(fileNameX);
	String fName;
	for (int k = 0;; ++k) {
	    fName = null;
	    if (k == 0) {
		fName = baseName + ".log";
	    } else {
		fName = String.format("%s(%02d).log", baseName, k);
	    }
	    final File f = new File(_LogDir, fName);
	    if (f.isDirectory() || f.exists()) {
		continue;
	    }
	    try {
		if (!f.createNewFile()) {
		    continue;
		}
		final boolean success = f.canWrite();
		f.delete();
		if (!success) {
		    continue;
		}
	    } catch (final IOException e) {
		continue;
	    }
	    return f;
	}
    }

    public static String getDirCasesFinderDirsString() {
	String s = "Version 2024-09-04.A";
	s += "\nUser Dir: " + Statics.getCanonicalPath(_UserDir);
	s += "\nDataDir: " + Statics.getCanonicalPath(_DataDir);
	s += "\nLogDir: " + Statics.getCanonicalPath(_LogDir);
	s += "\nCardsFilesDir: " + Statics.getCanonicalPath(_CardsFilesDir);
	s += "\nSoundFilesDirsDir: " + Statics.getCanonicalPath(_SoundFilesDirsDir);
	s += "\nGameDirsDir: " + Statics.getCanonicalPath(_GameDirsDir);
	return s;
    }

}
