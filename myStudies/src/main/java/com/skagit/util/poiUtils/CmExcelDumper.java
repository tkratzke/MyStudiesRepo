package com.skagit.util.poiUtils;

import java.io.File;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.skagit.util.complex.DimensionSpec;

public class CmExcelDumper extends ExcelDumper {

	public CmExcelDumper(File f, boolean clearOutDir) {
		super(f, clearOutDir);
	}

	/** Convenience constants. */
	final private static int _Re = DimensionSpec._Re;
	final private static int _Im = DimensionSpec._Im;

	/** Complex @@@ */
	/** Put in a single row. */
	public void createSheetCm(final XSSFWorkbook workBookX, final String coreSheetName, final double[][] a) {
		final double[] re = a[_Re];
		final double[] im = a[_Im];
		final int em = re.length;
		int iRow = 0;
		final XSSFWorkbook workBook = workBookX == null ? _defaultWorkBook : workBookX;
		final Sheet sheet = workBook.createSheet(coreSheetName);
		final Row row = sheet.createRow(iRow++);
		for (int i = 0; i < em; ++i) {
			final Cell cell = row.createCell(i);
			CmPoiUtils.setComplexValue(cell, re[i], im[i]);
		}
	}

	/** Put in a single table. */
	public void createSheetCm(final XSSFWorkbook workBookX, final String coreSheetName, final double[][][] a) {
		final double[][] re = a[_Re];
		final double[][] im = a[_Im];
		final int em = re.length;
		final int en = re[0].length;
		int iRow = 0;
		final XSSFWorkbook workBook = workBookX == null ? _defaultWorkBook : workBookX;
		final Sheet sheet = workBook.createSheet(coreSheetName);
		for (int i = 0; i < em; ++i) {
			final Row row = sheet.createRow(iRow++);
			for (int j = 0; j < en; ++j) {
				final Cell cell = row.createCell(j);
				CmPoiUtils.setComplexValue(cell, re[i][j], im[i][j]);
			}
		}
	}

	/**
	 * Put in a single sheet; the first index indicates which table in this sheet.
	 */
	public void createSheetCm(final XSSFWorkbook workBookX, final String coreSheetName, final double[][][][] a) {
		final double[][][] re = a[_Re];
		final double[][][] im = a[_Im];
		final int em = re.length;
		final int en1 = re[0].length;
		final int en2 = re[0][0].length;
		int iRow = 0;
		final XSSFWorkbook workBook = workBookX == null ? _defaultWorkBook : workBookX;
		final Sheet sheet = workBook.createSheet(coreSheetName);
		for (int i = 0; i < em; ++i) {
			if (i > 0) {
				++iRow;
			}
			for (int j1 = 0; j1 < en1; ++j1) {
				final Row row = sheet.createRow(iRow++);
				for (int j2 = 0; j2 < en2; ++j2) {
					final Cell cell = row.createCell(j2);
					CmPoiUtils.setComplexValue(cell, re[i][j1][j2], im[i][j1][j2]);
				}
			}
		}
	}

	/**
	 * Put in multiple sheets or even separate files. The first index indicates
	 * which sheet, and the second indicates which table within that sheet.
	 */
	public void createSheetCm(final XSSFWorkbook workBookX, final String coreSheetName, final double[][][][][] a) {
		final double[][][][] re = a[_Re];
		final double[][][][] im = a[_Im];
		final int em = re.length;
		final boolean buildSeparateWorkbook = em >= 4;
		final XSSFWorkbook workBook;
		if (buildSeparateWorkbook) {
			workBook = new XSSFWorkbook();
		} else {
			workBook = workBookX == null ? _defaultWorkBook : workBookX;
		}
		for (int i = 0; i < em; ++i) {
			final double[][][][] aI = new double[][][][] { re[i], im[i] };
			final String name = String.format("%s-%03d", coreSheetName, i);
			createSheetCm(workBook, name, aI);
		}
		if (buildSeparateWorkbook) {
			close(workBook, coreSheetName);
		}
	}

	/** complex float */

	/** complex float @@@ */
	/** Put in a single row. */
	public void createSheetCmFl(final XSSFWorkbook workBookX, final String coreSheetName, final float[][] a) {
		final float[] re = a[_Re];
		final float[] im = a[_Im];
		final int em = re.length;
		int iRow = 0;
		final XSSFWorkbook workBook = workBookX == null ? _defaultWorkBook : workBookX;
		final Sheet sheet = workBook.createSheet(coreSheetName);
		final Row row = sheet.createRow(iRow++);
		for (int i = 0; i < em; ++i) {
			final Cell cell = row.createCell(i);
			CmPoiUtils.setComplexValue(cell, re[i], im[i]);
		}
	}

	/** Put in a single table. */
	public void createSheetCmFl(final XSSFWorkbook workBookX, final String coreSheetName, final float[][][] a) {
		final float[][] re = a[_Re];
		final float[][] im = a[_Im];
		final int em = re.length;
		final int en = re[0].length;
		int iRow = 0;
		final XSSFWorkbook workBook = workBookX == null ? _defaultWorkBook : workBookX;
		final Sheet sheet = workBook.createSheet(coreSheetName);
		for (int i = 0; i < em; ++i) {
			final Row row = sheet.createRow(iRow++);
			for (int j = 0; j < en; ++j) {
				final Cell cell = row.createCell(j);
				CmPoiUtils.setComplexValue(cell, re[i][j], im[i][j]);
			}
		}
	}

	/**
	 * Put in a single sheet; the first index indicates which table in this sheet.
	 */
	public void createSheetCmFl(final XSSFWorkbook workBookX, final String coreSheetName, final float[][][][] a) {
		final float[][][] re = a[_Re];
		final float[][][] im = a[_Im];
		final int em = re.length;
		final int en1 = re[0].length;
		final int en2 = re[0][0].length;
		int iRow = 0;
		final XSSFWorkbook workBook = workBookX == null ? _defaultWorkBook : workBookX;
		final Sheet sheet = workBook.createSheet(coreSheetName);
		for (int i = 0; i < em; ++i) {
			if (i > 0) {
				++iRow;
			}
			for (int j1 = 0; j1 < en1; ++j1) {
				final Row row = sheet.createRow(iRow++);
				for (int j2 = 0; j2 < en2; ++j2) {
					final Cell cell = row.createCell(j2);
					CmPoiUtils.setComplexValue(cell, re[i][j1][j2], im[i][j1][j2]);
				}
			}
		}
	}

	/**
	 * Put in multiple sheets or even separate files. The first index indicates
	 * which sheet, and the second indicates which table within that sheet.
	 */
	public void createSheetCmFl(final XSSFWorkbook workBookX, final String coreSheetName, final float[][][][][] a) {
		final float[][][][] re = a[_Re];
		final float[][][][] im = a[_Im];
		final int em = re.length;
		final boolean buildSeparateWorkbook = em >= 4;
		final XSSFWorkbook workBook;
		if (buildSeparateWorkbook) {
			workBook = new XSSFWorkbook();
		} else {
			workBook = workBookX == null ? _defaultWorkBook : workBookX;
		}
		for (int i = 0; i < em; ++i) {
			final float[][][][] aI = new float[][][][] { re[i], im[i] };
			final String name = String.format("%s-%03d", coreSheetName, i);
			createSheetCmFl(workBook, name, aI);
		}
		if (buildSeparateWorkbook) {
			close(workBook, coreSheetName);
		}
	}

	@SuppressWarnings("unused")
	public static void oldMain(final String[] args) {
		final double[][] re = { { 1, 3, 5 }, { 7, 9, 11 } };
		final double[][] im = { { 2, 4, 6 }, { 8, 10, 12 } };
		//
		final double[][][] re3 = { { { 1, 3, 5 }, { 7, 9, 11 } }, { { 1, 3, 5 }, { 7, 9, 11 } } };
		final double[][][] im3 = { { { 2, 4, 6 }, { 8, 10, 12 } }, { { 2, 4, 6 }, { 8, 10, 12 } } };
		//
		//
		final double[][][][] re4 = { { { { 1, 3, 5 }, { 7, 9, 11 } }, { { 1, 3, 5 }, { 7, 9, 11 } } },
				{ { { 1, 3, 5 }, { 7, 9, 11 } }, { { 1, 3, 5 }, { 7, 9, 11 } } } };
		final double[][][][] im4 = { { { { 2, 4, 6 }, { 8, 10, 12 } }, { { 2, 4, 6 }, { 8, 10, 12 } } },
				{ { { 2, 4, 6 }, { 8, 10, 12 } }, { { 2, 4, 6 }, { 8, 10, 12 } } } };
		final double[][][][][] reIm4 = new double[][][][][] { re4, im4 };
		//
		try (final XSSFWorkbook workbook = new XSSFWorkbook()) {
			final boolean clearOutDir = true;
			final CmExcelDumper cmExcelDumper = new CmExcelDumper(new File("."), clearOutDir);
			cmExcelDumper.createSheetCm(workbook, "zample", reIm4);
			cmExcelDumper.close(workbook, "workbookName");
		} catch (final IOException e) {
		}
	}

}
