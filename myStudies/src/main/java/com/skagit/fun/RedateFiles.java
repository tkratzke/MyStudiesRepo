package com.skagit.fun;

import java.io.File;
import java.io.FileFilter;

public class RedateFiles {
	public static void main(final String[] args) {
		int iArg = 0;
		final File dir = new File(args[iArg++]);
		processDir(dir);
	}

	private static void processDir(final File dir) {
		final File[] members = dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(final File f) {
				if (f.isDirectory()) {
					return true;
				}
				final String fName = f.getName();
				final String[] components = fName.split("-|\\.");
				if (components.length == 4) {
					try {
						final int year = Integer.parseInt(components[2]);
						final int month = Integer.parseInt(components[0]);
						final int day = Integer.parseInt(components[1]);
						return year > 0 && month > 0 && day > 0;
					} catch (final NumberFormatException e) {
						return false;
					}
				}
				return false;
			}
		});

		final int nMembers = members.length;
		for (int k = 0; k < nMembers; ++k) {
			final File member = members[k];
			if (member.isDirectory()) {
				processDir(member);
			} else {
				final String memberName = member.getName();
				final String[] components = memberName.split("-|\\.");
				if (components.length == 4) {
					int year = Integer.parseInt(components[2]);
					final int month = Integer.parseInt(components[0]);
					final int day = Integer.parseInt(components[1]);
					if (year < 100) {
						year += 2000;
					}
					final String newName = String.format("%04d-%02d-%02d.%s", year,
							month, day, components[3]);
					member.renameTo(new File(member.getParentFile(), newName));
				}
			}
		}

	}

}
