package com.skagit.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.io.FilenameUtils;

public class FolderFlattener {

    final private static String _FlattenedDirName = "FlattenedDir";

    private static final HashMap<String, String> _ExtensionToSubDirName = new HashMap<>() {
	private static final long serialVersionUID = 1L;
	{
	    final String[][] mappings = { //
		    { "jpeg", "jpg" } //
	    };
	    final int nPairs = mappings.length;
	    for (int k0 = 0; k0 < nPairs; ++k0) {
		final String[] pair = mappings[k0];
		put(pair[0], pair[1]);
	    }
	}
    };

    static void process(final File flattenedDir, final File srcDir, final String prefix) {
	final File[] looseFiles = srcDir.listFiles(new FileFilter() {

	    @Override
	    public boolean accept(final File f) {
		return f.isFile();
	    }
	});
	if (looseFiles != null) {
	    final File[] initialExtensionSubDirs = flattenedDir.listFiles(new FileFilter() {
		@Override
		public boolean accept(final File dir) {
		    return dir.isDirectory();
		}
	    });
	    final ArrayList<File> extensionSubDirs = new ArrayList<>();
	    if (initialExtensionSubDirs != null) {
		extensionSubDirs.addAll(Arrays.asList(initialExtensionSubDirs));
	    }
	    for (final File f : looseFiles) {
		final String fName = f.getName();
		final String rawExtension = FilenameUtils.getExtension(fName);
		String referenceExtension = _ExtensionToSubDirName.get(rawExtension.toLowerCase());
		if (referenceExtension == null) {
		    referenceExtension = rawExtension;
		}
		File targetSubDir = null;
		for (final File extensionSubDir : extensionSubDirs) {
		    final String extensionSubDirName = extensionSubDir.getName();
		    final int extensionSubDirNameLen = extensionSubDirName.length();
		    if (extensionSubDirName.substring(0, extensionSubDirNameLen - 1)
			    .equalsIgnoreCase(referenceExtension)) {
			targetSubDir = extensionSubDir;
			break;
		    }
		}
		if (targetSubDir == null) {
		    targetSubDir = new File(flattenedDir, referenceExtension.toUpperCase() + 's');
		    targetSubDir.mkdir();
		    extensionSubDirs.add(targetSubDir);
		}
		final String newName = (prefix.length() > 0 ? (prefix + "-") : "") + f.getName();
		final File newF = new File(targetSubDir, newName);
		try {
		    Files.copy(f.toPath(), newF.toPath());
		} catch (final IOException e) {
		}
	    }
	}
	final File[] subDirs = srcDir.listFiles(new FileFilter() {

	    @Override
	    public boolean accept(final File f) {
		if (f.getName().equalsIgnoreCase(_FlattenedDirName)) {
		    return false;
		}
		return f.isDirectory();
	    }
	});
	if (subDirs != null) {
	    for (final File subDir : subDirs) {
		final String newPrefix = prefix + (prefix.length() > 0 ? "-" : "") + subDir.getName();
		process(flattenedDir, subDir, newPrefix);
	    }
	}
    }

    public static void main(final String[] args) {
	final File srcDir = new File(args[0]);
	final File flattenedDir = new File(srcDir, _FlattenedDirName);
	if (flattenedDir.mkdir()) {
	    process(flattenedDir, srcDir, /* prefix= */"");
	}
    }

}
