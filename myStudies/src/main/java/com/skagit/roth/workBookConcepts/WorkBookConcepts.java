package com.skagit.roth.workBookConcepts;

import java.util.Arrays;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.skagit.util.MyStudiesStringUtils;
import com.skagit.util.NamedEntity;

public class WorkBookConcepts {

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

    public static double getDouble(final Block[] blocks, final String blockName, final String lineHdrName) {
	final Block block = WorkBookConcepts.getBlock(blocks, blockName);
	if (block == null) {
	    return Double.NaN;
	}
	final Line[] lines = block._lines;
	final int[] loHi = WorkBookConcepts.getMatchingLineIdxs(lineHdrName, lines);
	if (loHi[1] != loHi[0] + 1) {
	    return Double.NaN;
	}
	return lines[loHi[0]]._data._d;
    }

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

    public static void main(final String[] args) {
	final int[] ints = { 1, 1, 3, 3, 3, 3, 4, 6, 6 };
	System.out.println(MyStudiesStringUtils.getString(getMatchingInts(2, ints)));
    }

}
