package com.skagit.flashCardsGame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Scanner;

import com.skagit.util.BackupFileGetter;
import com.skagit.util.Statics;

public class TweakSoundFiles {

    public static void main(final String[] args) {
	final File cardsFilesDir = new File("TweakDir/cardsFiles");
	final File soundFilesDir = new File("TweakDir/soundFiles");
	processCardsFilesDir(cardsFilesDir);
	processSoundFilesDir(soundFilesDir);
    }

    public static void mainx(final String[] args) {
	final File cardsFile = new File("TweakDir/cardsFiles/Vietnamese.A.txt");
	processCardsFile(cardsFile);
    }

    public static void mainy(final String[] args) {
	final File soundFilesDir = new File("TweakDir/soundFiles/Tran0001-0040");
	processSoundFilesDir(soundFilesDir);
    }

    private static void processSoundFile(final File soundFile, final File newSoundFilesDir) {
	final String soundFileName = soundFile.getName();
	final int nChars = soundFileName.length();
	String newString = "";
	String numString = null;
	for (int k0 = 0; k0 < nChars; ++k0) {
	    final char c0 = soundFileName.charAt(k0);
	    if (numString == null) {
		if (Character.isDigit(c0)) {
		    numString = "" + c0;
		} else {
		    newString += c0;
		}
	    } else {
		/** numString is not null. */
		if (Character.isDigit(c0)) {
		    numString += c0;
		} else {
		    newString += String.format("%04d%c", Integer.parseInt(numString) - 1, c0);
		    numString = null;
		}
	    }
	}
	final File newSoundFile = new File(newSoundFilesDir, newString);
	Statics.copyNonDirectoryFile(soundFile, newSoundFile);
    }

    private static void processSoundFilesDir(final File soundFilesDir) {
	final File[] myFiles = soundFilesDir.listFiles(new FileFilter() {

	    @Override
	    public boolean accept(final File f) {
		return f.isFile() && f.getName().toLowerCase().endsWith(".aiff");
	    }
	});
	final int nMyFiles = myFiles == null ? 0 : myFiles.length;
	if (nMyFiles > 0) {
	    final File newSoundFilesDir = new File(soundFilesDir.getParentFile(), soundFilesDir.getName() + "New");
	    newSoundFilesDir.mkdir();
	    for (int k = 0; k < nMyFiles; ++k) {
		final File soundFile = myFiles[k];
		processSoundFile(soundFile, newSoundFilesDir);
	    }
	}
	final File[] mySubDirs = soundFilesDir.listFiles(new FileFilter() {

	    @Override
	    public boolean accept(final File f) {
		return f.isDirectory();
	    }
	});
	final int nMySubDirs = mySubDirs == null ? 0 : mySubDirs.length;
	for (int k = 0; k < nMySubDirs; ++k) {
	    final File subDir = mySubDirs[k];
	    processSoundFilesDir(subDir);
	}
    }

    private static void processCardsFile(final File cardsFile) {
	final File cardsFileNew = BackupFileGetter.getBackupFile(cardsFile, "." + Statics._CardsFileExtensionLc,
		/* nDigitsForCardsFileBackups= */1);
	try (//
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(cardsFile), "UTF-8"));
		Scanner fileSc = new Scanner(in); //
		PrintStream out = new PrintStream(cardsFileNew) //
	) {
	    if (in.markSupported()) {
		in.mark(1);
		if (in.read() != 0xFEFF) {
		    in.reset();
		}
	    }
	    while (fileSc.hasNext()) {
		final String nextLine = fileSc.nextLine();
		if (nextLine.isBlank()) {
		    continue;
		}
		final int nChars = nextLine.length();
		String newString = "";
		String numString = null;
		for (int k0 = 0; k0 < nChars; ++k0) {
		    final char c0 = nextLine.charAt(k0);
		    if (numString == null) {
			if (c0 == Statics._FileDelimiter && (k0 < nChars - 1)
				&& Character.isDigit(nextLine.charAt(k0 + 1))) {
			    numString = "";
			} else {
			    newString += c0;
			}
		    } else {
			/** numString is not null. */
			if (Character.isDigit(c0)) {
			    numString += c0;
			} else {
			    newString += String.format("%c%04d%c", Statics._FileDelimiter,
				    Integer.parseInt(numString) - 1, c0);
			    numString = null;
			}
		    }
		}
		out.println(newString);
	    }
	} catch (final IOException e) {
	    e.printStackTrace();
	}
    }

    private static void processCardsFilesDir(final File cardsFileDir) {
	final File[] myFiles = cardsFileDir.listFiles(new FileFilter() {

	    @Override
	    public boolean accept(final File f) {
		return f.isFile() && f.getName().toLowerCase().endsWith(".txt");
	    }
	});
	final int nMyFiles = myFiles == null ? 0 : myFiles.length;
	for (int k = 0; k < nMyFiles; ++k) {
	    final File cardsFile = myFiles[k];
	    processCardsFile(cardsFile);
	}
	final File[] mySubDirs = cardsFileDir.listFiles(new FileFilter() {

	    @Override
	    public boolean accept(final File f) {
		return f.isDirectory();
	    }
	});
	final int nMySubDirs = mySubDirs == null ? 0 : mySubDirs.length;
	for (int k = 0; k < nMySubDirs; ++k) {
	    final File subDir = mySubDirs[k];
	    processCardsFilesDir(subDir);
	}
    }

}
