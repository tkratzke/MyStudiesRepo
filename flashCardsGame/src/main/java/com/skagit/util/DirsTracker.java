package com.skagit.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintStream;

public class DirsTracker {

    final private static File _UserDir;
    final private static File _RunDir;
    final private static String _GamesFilesDirName = "gameFiles";
    final private static String _CardsFilesDirName = "cardsFiles";
    final private static String _SoundFileDirsDirName = "soundFiles";
    final private static String _LogDirName = "LogFiles";
    final private static File _GameFilesDir;
    final private static File _CardsFilesDir;
    final private static File _SoundFilesDirsDir;
    final private static File _LogDir;

    static {
	final String userDirPath = Statics.getSystemProperty("user.dir", /* useSpaceProxy= */true);
	_UserDir = new File(userDirPath);
	_RunDir = computeRunDir();
	_LogDir = new File(_RunDir, _LogDirName);
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
	if (_RunDir != null && _RunDir.isDirectory()) {
	    _GameFilesDir = new File(_RunDir, _GamesFilesDirName);
	    _CardsFilesDir = new File(_RunDir, _CardsFilesDirName);
	    _SoundFilesDirsDir = new File(_RunDir, _SoundFileDirsDirName);
	} else {
	    _GameFilesDir = _CardsFilesDir = _SoundFilesDirsDir = null;
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

    private static File computeRunDir() {
	/**
	 * <pre>
	 * We compute _RunDir here, and it might be called within the static initializer.
	 * Unless overridden as a VM argument, _RunDir is set to _UserDir.
	 * </pre>
	 */
	final String userDirPath = Statics.getSystemProperty("user.dir", /* useSpaceProxy= */true);
	final File userDir = new File(userDirPath);
	/** Unless overridden as a VM argument, _RunDir is set to _UserDir. */
	final String runDirPath = Statics.getSystemProperty("run.dir", /* useSpaceProxy= */true);
	if (runDirPath == null) {
	    return userDir;
	}
	final File runDir = new File(runDirPath);
	if (runDir.exists() && runDir.isDirectory()) {
	    return runDir;
	}
	return null;
    }

    public static File getUserDir() {
	return _UserDir;
    }

    public static File getLogDir() {
	return _LogDir;
    }

    public static File getRunDir() {
	if (_RunDir != null) {
	    return _RunDir;
	}
	return computeRunDir();
    }

    public static File getGameFilesDir() {
	return _GameFilesDir;
    }

    public static File getCardsFilesDir() {
	return _CardsFilesDir;
    }

    public static File getSoundFilesDirsDir() {
	return _SoundFilesDirsDir;
    }

    public static String getDirCasesFinderDirsString() {
	String s = "Version 2024-08-05.B";
	s += "\nUser Dir: " + Statics.getCanonicalPath(getUserDir());
	s += "\nRunDir: " + Statics.getCanonicalPath(getRunDir());
	s += "\nLogDir: " + Statics.getCanonicalPath(getLogDir());
	s += "\nCardsFilesDir: " + Statics.getCanonicalPath(getCardsFilesDir());
	s += "\nSoundFilesDirsDir: " + Statics.getCanonicalPath(getSoundFilesDirsDir());
	s += "\nGameFilesDir: " + Statics.getCanonicalPath(getGameFilesDir());
	return s;
    }

}