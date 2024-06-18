package com.skagit.rothProblem.workBookConcepts;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.skagit.util.NamedEntity;

public class Block extends NamedEntity {

    final public Line[] _lines;

    private static String getNameOfBlock(final XSSFSheet sheet, final CellRangeAddress start) {
	final int firstRow = start.getFirstRow();
	final XSSFCell nameCell = sheet.getRow(firstRow).getCell(start.getFirstColumn());
	return NamedEntity.CleanWhiteSpace(nameCell.getStringCellValue());
    }

    public Block(final XSSFSheet sheet, final FormulaEvaluator formulaEvaluator, final CellRangeAddress start,
	    final CellRangeAddress end) {
	super(getNameOfBlock(sheet, start));
	final int firstRow = start.getFirstRow();
	final int dataRowStop = end.getFirstRow();
	final ArrayList<Line> lineList = new ArrayList<>();
	for (int kRow = firstRow + 1; kRow < dataRowStop; ++kRow) {
	    final int origIdxWithinBlock = kRow - (firstRow + 1);
	    final Line line = new Line(sheet, formulaEvaluator, origIdxWithinBlock, kRow);
	    if (line.isValid()) {
		lineList.add(line);
	    }
	}
	final int nLines = lineList.size();
	_lines = lineList.toArray(new Line[nLines]);
	Arrays.sort(_lines);
    }

    @Override
    public String getString() {
	String s = _name;
	final int nLines = _lines.length;
	for (int k = 0; k < nLines; ++k) {
	    s += "\n" + _lines[k].getString();
	}
	return s;
    }

    public Field getData(final String s) {
	final int idx = Arrays.binarySearch(_lines, new NamedEntity(s));
	if (idx < 0) {
	    return null;
	}
	return _lines[idx]._data;
    }

}
