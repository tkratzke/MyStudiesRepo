package com.skagit.roth.workBookConcepts;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class Line implements Comparable<Line> {

    final public XSSFSheet _sheet;
    final public int _origIdxWithinBlock;
    final public Field _header;
    final public Field _data;

    public static String getName(final XSSFSheet sheet, final FormulaEvaluator formulaEvaluator, final int kRow) {
	return new Field(formulaEvaluator, sheet.getRow(kRow).getCell(0))._s;
    }

    public Line(final XSSFSheet sheet, final FormulaEvaluator formulaEvaluator, final int origIdxWithinBlock,
	    final int kRow) {
	_sheet = sheet;
	_origIdxWithinBlock = origIdxWithinBlock;
	final XSSFRow row = sheet.getRow(kRow);
	if (row == null) {
	    _header = _data = null;
	    return;
	}
	_header = new Field(formulaEvaluator, sheet.getRow(kRow).getCell(0));
	_data = new Field(formulaEvaluator, sheet.getRow(kRow).getCell(1));
    }

    public Line(final String name) {
	_sheet = null;
	_origIdxWithinBlock = -1;
	_header = new Field(name);
	_data = null;
    }

    public boolean isValid() {
	if (_header != null && _header.hasData()) {
	    return true;
	}
	return _data != null && _data.hasData();
    }

    public String getString() {
	return String.format("%s: %s", _header.getString(), _data.getString());
    }

    @Override
    public String toString() {
	return getString();
    }

    @Override
    public int compareTo(final Line line) {
	final Field hisHeader = line == null ? null : line._header;
	if ((_header == null) != (hisHeader == null)) {
	    return _header == null ? -1 : 1;
	}
	if (_header == null) {
	    return 0;
	}
	return _header.compareTo(hisHeader);
    }

}
