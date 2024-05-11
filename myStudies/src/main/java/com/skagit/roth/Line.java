package com.skagit.roth;

import org.apache.poi.xssf.usermodel.XSSFSheet;

public class Line implements Comparable<Line> {

    public Field _header;
    public Field _data;

    /** For looking things up. */
    public Line(final String s) {
	_header = new Field(s);
	_data = null;
    }

    public Line(final RothCalculator.SheetAndBlocks sheetAndBlocks, final int kRow) {
	final XSSFSheet sheet = sheetAndBlocks._sheet;
	_header = new Field(sheetAndBlocks, sheet.getRow(kRow).getCell(0));
	_data = new Field(sheetAndBlocks, sheet.getRow(kRow).getCell(1));
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
	if (line == null) {
	    return 1;
	}
	return Field.ByString.compare(_header, line._header);
    }

}
