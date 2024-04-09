package com.skagit.util.poiUtils;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.skagit.util.LsFormatter;

public class CmPoiUtils {

    public static void setComplexValue(final Cell cell, final double re, final double im) {
	if (im == 0) {
	    cell.setCellValue(re);
	} else {
	    cell.setCellFormula(String.format("COMPLEX(%f,%f)", re, im));
	}
    }

    public static void cmAutoFit(final Workbook workBook) {
	final int nSheets = workBook.getNumberOfSheets();
	for (int k = 0; k < nSheets; ++k) {
	    final Sheet sheet = workBook.getSheetAt(k);
	    cmAutoFit(sheet);
	}
    }

    private static void cmAutoFit(final Sheet sheet) {
	final TreeSet<Integer> colIndexes = new TreeSet<>();
	final TreeMap<Integer, int[]> minWidths = new TreeMap<>();
	final TreeSet<Integer> needMinWidths = new TreeSet<>();
	final Iterator<Row> rowIterator = sheet.rowIterator();
	while (rowIterator.hasNext()) {
	    final Row row = rowIterator.next();
	    final Iterator<Cell> cellIterator = row.cellIterator();
	    while (cellIterator.hasNext()) {
		final Cell cell = cellIterator.next();
		final int colIndex = cell.getColumnIndex();
		colIndexes.add(colIndex);
		int[] minWidth = minWidths.get(colIndex);
		if (minWidth == null) {
		    minWidth = new int[] { 0 };
		    minWidths.put(colIndex, minWidth);
		}
		final int thisWidth;
		final CellType cellType = cell.getCellType();
		if (cellType == CellType.FORMULA) {
		    final String formula = cell.getCellFormula();
		    int thisWidthX = -1;
		    if (formula.toUpperCase().startsWith("COMPLEX")) {
			final int lastLeftParen = formula.lastIndexOf('(');
			final int firstRightParen = formula.lastIndexOf(')');
			final String s = formula.substring(lastLeftParen + 1, firstRightParen);
			final String[] subFields = s.split("[,\\s]+");
			if (subFields != null && subFields.length == 2) {
			    double re = Double.NaN;
			    double im = Double.NaN;
			    try {
				re = Double.parseDouble(subFields[0]);
				im = Double.parseDouble(subFields[1]);
			    } catch (final NumberFormatException e) {
			    }
			    if (Double.isNaN(re) || Double.isNaN(im)) {
				thisWidthX = 25;
			    } else if (re == 0 && im == 0) {
				thisWidthX = 2;
			    } else if (re == 0) {
				thisWidthX = 10;
			    } else if (im == 0) {
				thisWidthX = 9;
			    } else {
				thisWidthX = 19;
			    }
			}
		    }
		    thisWidth = thisWidthX;
		    needMinWidths.add(colIndex);
		} else if (cellType == CellType.NUMERIC) {
		    final double d = cell.getNumericCellValue();
		    final String s = LsFormatter.StandardFormat(d);
		    thisWidth = s.length();
		} else if (cellType == CellType.STRING) {
		    final String s = cell.getStringCellValue();
		    thisWidth = s.length();
		} else {
		    thisWidth = 4;
		}
		minWidth[0] = Math.max(minWidth[0], thisWidth);
	    }
	}
	for (final int colIndex : colIndexes) {
	    if (needMinWidths.contains(colIndex)) {
		final int[] minWidth = minWidths.get(colIndex);
		sheet.setColumnWidth(colIndex, minWidth[0] * 256);
	    } else {
		sheet.autoSizeColumn(colIndex);
	    }
	}
    }
}
