package com.skagit.roth;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class Line implements Comparable<Line> {

    final public Field _header;
    final public Field _data;

    public static String getName(final RothCalculator.SheetAndBlocks sheetAndBlocks, final int kRow) {
	final XSSFSheet sheet = sheetAndBlocks._sheet;
	return new Field(sheetAndBlocks, sheet.getRow(kRow).getCell(0))._s;
    }

    public Line(final RothCalculator.SheetAndBlocks sheetAndBlocks, final int kRow) {
	final XSSFSheet sheet = sheetAndBlocks._sheet;
	final XSSFRow row = sheet.getRow(kRow);
	if (row == null) {
	    _header = _data = null;
	    return;
	}
	_header = new Field(sheetAndBlocks, sheet.getRow(kRow).getCell(0));
	_data = new Field(sheetAndBlocks, sheet.getRow(kRow).getCell(1));
    }

    public Line(final String name) {
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
