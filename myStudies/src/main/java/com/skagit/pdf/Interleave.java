package com.skagit.pdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

public class Interleave {
	/**
	 * <pre>
	 * To use:
	 *   1. The 1st argument is the directory.
	 *   2. The 2nd argument is the filename within that directory of the result file.
	 *   3. The rest of arguments are pairs of file names within the directory.
	 * We require the same number of pages in the 2 files within a pair.
	 * We assume the 2nd file within a pair is reversed.
	 *
	 * For example:
	 *   H:\HPSCANS\Thesis Combined scan scan0001 scan0004 scan0005
	 * The directory is H:\HPSCANS\Thesis
	 * The result will be Combined.pdf
	 * The 1st pair is scan.pdf and scan0001.pdf, they have the same number
	 * of pages, and scan0001.pdf's pages are reversed.  E.g.:
	 * scan.pdf: pages 1,3,5
	 * scan0001.pdf: pages 6,4,2
	 * scan0004.pdf: pages 7,9
	 * scan0005.pdf: pages 10,8
	 *
	 * Note: On my scanner, the pages "dive headfirst" into the scanner."
	 *
	 * </pre>
	 *
	 */

	private static class FilePair {
		final File _inFile0;
		final File _inFile1;
		final File _outFile;

		FilePair(final File inFile0, final File inFile1, final Random r) {
			super();
			_inFile0 = inFile0;
			_inFile1 = inFile1;

			File outFile = null;
			try (PDDocument inDocument0 = PDDocument.load(_inFile0)) {
				final int nPages = inDocument0.getNumberOfPages();
				try (PDDocument inDocument1 = PDDocument.load(_inFile1)) {
					final int nPages1 = inDocument1.getNumberOfPages();
					if (nPages1 == nPages) {
						try (final PDDocument outDocument = new PDDocument()) {
							for (int k = 0; k < nPages; ++k) {
								final PDPage pdPage0 =
										outDocument.importPage(inDocument0.getPage(k));
								pdPage0.setAnnotations(null);
								final PDPage pdPage1 = outDocument
										.importPage(inDocument1.getPage(nPages - 1 - k));
								pdPage1.setAnnotations(null);
							}
							final String outDocumentName = String.format("%s_%03d_%s.pdf",
									FilenameUtils.getBaseName(_inFile0.getName()),
									Math.abs(r.nextLong()) % 1000,
									FilenameUtils.getBaseName(_inFile1.getName()));
							outFile = new File(_inFile0.getParentFile(), outDocumentName);
							outDocument.save(outFile);
						} catch (final IOException e) {
							System.err.printf("\nError 1 in FilePair ctor.\n");
							e.printStackTrace();
						}
					}
				} catch (final IOException e) {
					System.err.printf("\nError 2 in FilePair ctor.\n");
					e.printStackTrace();
				}
			} catch (final IOException e) {
				System.err.printf("\nError 3 in FilePair ctor.\n");
				e.printStackTrace();
			}
			_outFile = outFile;
		}

		final String getString() {
			final String inFile0String;
			if (_inFile0 != null && _inFile0.canRead()) {
				inFile0String = _inFile0.getAbsolutePath();
			} else {
				inFile0String = "BAD FILE 0";
			}
			final String inFile1String;
			if (_inFile1 != null && _inFile1.canRead()) {
				inFile1String = _inFile1.getAbsolutePath();
			} else {
				inFile1String = "BAD FILE 1";
			}
			final String outFileString;
			if (_outFile != null && _outFile.canRead()) {
				outFileString = _outFile.getAbsolutePath();
			} else {
				outFileString = "BAD Out File";
			}
			final String s = String.format("In0: %s\nIn1: %s\nOut: %s",
					inFile0String, inFile1String, outFileString);
			return s;
		}

		@Override
		final public String toString() {
			return getString();
		}
	}

	public static void main(final String[] args) {

		final int nArgs = args.length;
		int iArg = 0;

		final String dirPath = args[iArg++];
		final File dir = new File(dirPath);
		String outName = args[iArg++];
		if (!FilenameUtils.getExtension(outName).equalsIgnoreCase("pdf")) {
			outName = FilenameUtils.getBaseName(outName) +
					FilenameUtils.EXTENSION_SEPARATOR + "pdf";
		}

		final Random r = new Random();
		final int nFilePairs = (nArgs - iArg) / 2;
		final FilePair[] filePairs = new FilePair[nFilePairs];
		for (int k = 0; k < nFilePairs; ++k) {
			String file0Name = args[iArg++];
			if (!FilenameUtils.getExtension(file0Name).equalsIgnoreCase("pdf")) {
				file0Name = FilenameUtils.getBaseName(file0Name) +
						FilenameUtils.EXTENSION_SEPARATOR + "pdf";
			}
			String file1Name = args[iArg++];
			if (!FilenameUtils.getExtension(file1Name).equalsIgnoreCase("pdf")) {
				file1Name = FilenameUtils.getBaseName(file1Name) +
						FilenameUtils.EXTENSION_SEPARATOR + "pdf";
			}
			final File inFile0 = new File(dir, file0Name);
			final File inFile1 = new File(dir, file1Name);
			filePairs[k] = new FilePair(inFile0, inFile1, r);
		}

		final File outFile = new File(dir, outName);
		final PDFMergerUtility PDFmerger = new PDFMergerUtility();
		PDFmerger.setDestinationFileName(outFile.getAbsolutePath());
		for (int k = 0; k < nFilePairs; ++k) {
			try {
				final FilePair filePair = filePairs[k];
				PDFmerger.addSource(filePair._outFile);
			} catch (final FileNotFoundException e) {
			}
		}

		try {
			PDFmerger.mergeDocuments(null);
		} catch (final IOException e) {
		}

	}

}
