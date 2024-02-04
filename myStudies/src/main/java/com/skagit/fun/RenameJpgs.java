package com.skagit.fun;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.apache.commons.io.FilenameUtils;

import com.skagit.util.Constants;
import com.skagit.util.StaticUtilities;
import com.skagit.util.myLogger.MyLogger;

public class RenameJpgs {

	final private static String _PictureFileExtension = "JPG";

	public static void renameNegativeFiles(final MyLogger logger,
			final File dir, final String newParentDirPath) {
		final String dirName = dir.getName();
		final String[] lines = getContentLines(dir);
		final File[] jpgFiles = getJpgFiles(dir);

		final int nLines = lines.length;
		final ArrayList<Integer> blockLengths = new ArrayList<>();
		for (int k = 0; k < nLines; ++k) {
			final String line = lines[k];
			final StringTokenizer stringTokenizer =
					new StringTokenizer(line, "\n\t\" $");

			while (stringTokenizer.hasMoreTokens()) {
				final String token = stringTokenizer.nextToken();
				try {
					final int blockLength = Integer.parseInt(token);
					blockLengths.add(blockLength);
				} catch (final Exception e) {
				}
			}
		}
		/** If there are no blocks, make a block of length 1 for each jpg. */
		int nBlocks = blockLengths.size();
		if (nBlocks == 0) {
			nBlocks = jpgFiles.length;
			for (int k0 = 0; k0 < nBlocks; ++k0) {
				blockLengths.add(1);
			}
		}
		int beginningOfBlock = 0;
		int counter = 0;
		for (int k0 = 0; k0 < nBlocks; ++k0) {
			final int blockLength = blockLengths.get(k0);
			for (int k1 = 0; k1 < blockLength; ++k1) {
				final File oldFile =
						jpgFiles[beginningOfBlock + blockLength - 1 - k1];
				final String newName0 = String.format("%s.%03d.%s", dirName,
						counter++, _PictureFileExtension);
				final String newName =
						StaticUtilities.legalizeWindowsFilename(newName0);
				System.out.printf("\n%s%c%s", oldFile.getName(),
						Constants._RightArrow, newName);
				oldFile.renameTo(new File(dir, newName));
			}
			beginningOfBlock += blockLength;
		}
		if (newParentDirPath != null) {
			/** Rename dir. */
			final File newParentDir = new File(newParentDirPath);
			for (int k = 0;; ++k) {
				final String prefix = String.format("%03d.", k);
				final File[] blockers = newParentDir.listFiles(new FileFilter() {

					@Override
					public boolean accept(final File f) {
						return f.getName().startsWith(prefix);
					}
				});
				if (blockers != null && blockers.length >= 1) {
					continue;
				}
				final String newDirName = String.format("%03d.%s", k, dirName);
				final File newDir = new File(newParentDirPath, newDirName);
				if (dir.renameTo(newDir)) {
					System.out.printf("\nDir %s moved to: %s.", dir.getPath(),
							newDir.getPath());
				} else {
					System.out.printf("\nDir %s failed to move to: %s.",
							dir.getPath(), newDir.getPath());
				}
			}
		}
	}

	private static void renameSlideFiles(final MyLogger logger,
			final File dir, final String newParentDirPath) {
		final String skipString = "!@#$%^&*()";
		final String dirName = dir.getName();
		final String[] lines = getContentLines(dir);
		final File[] jpgFiles = getJpgFiles(dir);

		final int nLines = lines.length;
		for (int k = 0, k1 = 0, sequenceNumber = 0; k < nLines; ++k) {
			final String newCoreName = lines[k];
			if (newCoreName.equals(skipString)) {
				++sequenceNumber;
				continue;
			}
			final String newName0 = String.format("%s.%02d.%s.%s", dirName,
					++sequenceNumber, newCoreName, _PictureFileExtension);
			final String newName =
					StaticUtilities.legalizeWindowsFilename(newName0);
			final File oldFile = jpgFiles[k1++];
			System.out.printf("\n%s%c%s", oldFile.getName(),
					Constants._RightArrow, newName);
			oldFile.renameTo(new File(dir, newName));
		}
		if (newParentDirPath != null) {
			/** Rename dir. */
			final File newParentDir = new File(newParentDirPath);
			final File newDir = new File(newParentDir, dirName);
			if (dir.renameTo(newDir)) {
				System.out.printf("\nDir %s moved to: %s.", dir.getPath(),
						newDir.getPath());
			} else {
				System.out.printf("\nDir %s failed to move to: %s.", dir.getPath(),
						newDir.getPath());
			}
		}
	}

	private static File[] getJpgFiles(final File dir) {
		return dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(final File f) {
				return FilenameUtils.getExtension(f.getName())
						.equalsIgnoreCase(_PictureFileExtension);
			}
		});
	}

	private static String[] getContentLines(final File dir) {
		final String contentFileExtension = "txt";
		final ArrayList<String> lineList = new ArrayList<>();
		try (Scanner scanner = new Scanner(
				new File(dir, dir.getName() + '.' + contentFileExtension))) {
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine().trim();
				if ((line.length() == 0) || (line.length() >= 2 && line.startsWith("!!"))) {
					continue;
				}
				lineList.add(line);
			}
		} catch (final IOException e) {
		}
		return lineList.toArray(new String[lineList.size()]);
	}

	public static void main(final String[] args) {
		final int nArgs = args.length;
		int iArg = 0;
		final char c = Character.toLowerCase(args[iArg++].charAt(0));
		final boolean useNegatives = c == 'n';
		final File dir = new File(args[iArg++]);
		final MyLogger logger = null;
		final String newDirPathParent;
		if (iArg < nArgs) {
			newDirPathParent = args[iArg++];
		} else {
			newDirPathParent = null;
		}
		if (useNegatives) {
			renameNegativeFiles(logger, dir, newDirPathParent);
		} else {
			renameSlideFiles(logger, dir, newDirPathParent);
		}
	}
}
