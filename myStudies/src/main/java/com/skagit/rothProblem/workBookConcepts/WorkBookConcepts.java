package com.skagit.rothProblem.workBookConcepts;

import java.util.Arrays;
import java.util.Date;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.skagit.util.MyStudiesStringUtils;
import com.skagit.util.NamedEntity;

public class WorkBookConcepts {
    final public static int _NotAnInteger = Integer.MIN_VALUE;

    final public XSSFWorkbook _workBook;
    final FormulaEvaluator _formulaEvaluator;

    public WorkBookConcepts(final XSSFWorkbook workBook) {
	_workBook = workBook;
	_formulaEvaluator = _workBook.getCreationHelper().createFormulaEvaluator();
    }

    public static Block getBlock(final Block[] blocks, final String blockName) {
	final int idx = Arrays.binarySearch(blocks, new NamedEntity(blockName));
	return idx < 0 ? null : blocks[idx];
    }

    private static Field getField(final Block[] blocks, final String blockName, final String fieldName) {
	final Block block = WorkBookConcepts.getBlock(blocks, blockName);
	if (block == null) {
	    return null;
	}
	final Line[] lines = block._lines;
	final int[] loHi = WorkBookConcepts.getMatchingLineIdxs(fieldName, lines);
	if (loHi[1] != loHi[0] + 1) {
	    return null;
	}
	return lines[loHi[0]]._data;
    }

    public static Boolean getBoolean(final Block[] blocks, final String blockName, final String lineHdrName) {
	final Field field = getField(blocks, blockName, lineHdrName);
	return field == null ? null : field._b;
    }

    public static double getDouble(final Block[] blocks, final String blockName, final String lineHdrName) {
	final Field field = getField(blocks, blockName, lineHdrName);
	return field == null ? Double.NaN : field._d;
    }

    public static int getInt(final Block[] blocks, final String blockName, final String lineHdrName) {
	final double d = getDouble(blocks, blockName, lineHdrName);
	return Double.isFinite(d) ? (int) Math.round(d) : _NotAnInteger;
    }

    public static Date getDate(final Block[] blocks, final String blockName, final String lineHdrName) {
	final Field field = getField(blocks, blockName, lineHdrName);
	return field == null ? null : field.getDate();
    }

    public static int[] getMatchingLineIdxs(final String lineHderName, final Line[] lines) {
	final int nLines = lines.length;
	final Line line = new Line(lineHderName);
	final int idx = Arrays.binarySearch(lines, line);
	if (idx < 0) {
	    final int k = -idx - 1;
	    return new int[] { k, k };
	}
	int lo = idx;
	for (int k = idx - 1; k >= 0; --k) {
	    if (lines[k].compareTo(line) == 0) {
		lo = k;
	    }
	}
	int hi = idx + 1;
	for (int k = idx + 1; k < nLines; ++k) {
	    if (lines[k].compareTo(line) == 0) {
		hi = k + 1;
	    }
	}
	return new int[] { lo, hi };
    }

    /** For debugging: Uses the same algorithm, but with ints. */
    private static int[] getMatchingInts(final int i, final int[] ints) {
	final int nInts = ints.length;
	final int idx = Arrays.binarySearch(ints, i);
	if (idx < 0) {
	    final int k = -idx - 1;
	    return new int[] { k, k };
	}
	int lo = idx;
	for (int k = idx - 1; k >= 0; --k) {
	    if (ints[k] == i) {
		lo = k;
	    }
	}
	int hi = idx + 1;
	for (int k = idx + 1; k < nInts; ++k) {
	    if (ints[k] == i) {
		hi = k + 1;
	    }
	}
	return new int[] { lo, hi };
    }

    public static void mainx(final String[] args) {
	final int[] ints = { 1, 1, 3, 3, 3, 3, 4, 6, 6, 7 };
	System.out.println(MyStudiesStringUtils.getString(getMatchingInts(7, ints)));
    }

}
