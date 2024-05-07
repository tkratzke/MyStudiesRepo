package com.skagit.roth;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.skagit.util.StringUtils;

public class Block implements Comparable<Block> {

    final boolean _fidelityBlock;
    final String _nameOfBlock;
    final Line[] _lines;

    public Block(final RothCalculator.SheetAndBlocks sheetAndBlocks, final CellRangeAddress start,
	    final CellRangeAddress end, final boolean fidelityBlock) {
	_fidelityBlock = fidelityBlock;
	final int firstRow = start.getFirstRow();
	final int dataRowStop = end.getFirstRow();
	final XSSFSheet sheet = sheetAndBlocks._sheet;
	final XSSFCell nameCell = sheet.getRow(firstRow).getCell(start.getFirstColumn());
	_nameOfBlock = StringUtils.CleanWhiteSpace(nameCell.getStringCellValue());
	final ArrayList<Line> lineList = new ArrayList<>();
	for (int kRow = firstRow + 1; kRow < dataRowStop; ++kRow) {
	    final Line line = new Line(sheetAndBlocks, kRow);
	    if (line.isValid()) {
		lineList.add(line);
	    }
	}
	final int nLines = lineList.size();
	_lines = lineList.toArray(new Line[nLines]);
	Arrays.sort(_lines);
    }

    /** For looking up a Block. */
    public Block(final String s) {
	_fidelityBlock = false;
	_nameOfBlock = s;
	_lines = null;
    }

    public String getString() {
	String s = _nameOfBlock;
	final int nLines = _lines.length;
	for (int k = 0; k < nLines; ++k) {
	    s += "\n" + _lines[k].getString();
	}
	return s;
    }

    @Override
    public String toString() {
	return getString();
    }

    @Override
    public int compareTo(final Block block) {
	if (block == null) {
	    return 1;
	}
	return _nameOfBlock.compareTo(block._nameOfBlock);
    }

    public Field getData(final String s) {
	final int idx = Arrays.binarySearch(_lines, new Field(s));
	if (idx < 0) {
	    return null;
	}
	return _lines[idx]._data;
    }

}
