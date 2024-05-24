package com.skagit.roth.workBookConcepts;

import java.util.Arrays;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.skagit.util.NamedEntity;

public class WorkBookConcepts {

    final public static String[] _SheetNames = { //
	    "Statics", //
	    "Brackets", //
	    "Investments", //
	    "Life Expectancies", // "
    };
    final public static int _StaticsSheetIdx = 0;
    final public static int _BracketsSheetIdx = 1;
    final public static int _InvestmentsSheetIdx = 2;
    final public static int _LifeExpectanciesSheetIdx = 3;

    public static String getSheetName(final int idx) {
	return _SheetNames[idx];
    }

    final public XSSFWorkbook _workBook;
    final public FormulaEvaluator _formulaEvaluator;
    final public SheetAndBlocks[] _sheetAndBlocksS;

    public WorkBookConcepts(final XSSFWorkbook workBook) {
	_workBook = workBook;
	_formulaEvaluator = _workBook.getCreationHelper().createFormulaEvaluator();
	/** Read Sheets into Blocks. */
	final int nSheets = _SheetNames.length;
	_sheetAndBlocksS = new SheetAndBlocks[nSheets];
	for (int k = 0; k < nSheets; ++k) {
	    _sheetAndBlocksS[k] = new SheetAndBlocks(this, _workBook.getSheet(_SheetNames[k]));
	}
	Arrays.sort(_sheetAndBlocksS);
    }

    public Block getBlock(final String sheetName, final String blockName) {
	final int sheetIdx = Arrays.binarySearch(_sheetAndBlocksS, new NamedEntity(sheetName));
	if (sheetIdx < 0) {
	    return null;
	}
	final Block[] blocks = _sheetAndBlocksS[sheetIdx]._blocks;
	final int blockIdx = Arrays.binarySearch(blocks, new NamedEntity(blockName));
	return blockIdx < 0 ? null : blocks[blockIdx];
    }

    public Line getLine(final String sheetName, final String blockName, final String s) {
	final Block block = getBlock(sheetName, blockName);
	if (block == null) {
	    return null;
	}
	final Line[] lines = block._lines;
	final int lineIdx = Arrays.binarySearch(lines, new Line(s));
	return lineIdx < 0 ? null : lines[lineIdx];
    }

    public Field getMiscellaneousData(final String s) {
	return getLine(_SheetNames[_StaticsSheetIdx], "Miscellaneous Data", s)._data;
    }

}
