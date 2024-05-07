package com.skagit.roth;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class Line implements Comparable<Line> {

    public final boolean _fidelity;
    public Field _header;
    public Field _data;

    /** For looking things up. */
    public Line(final String s) {
	_fidelity = false;
	_header = new Field(s);
	_data = null;
    }

    public Line(final RothCalculator.SheetAndBlocks sheetAndBlocks, final int kRow) {
	_fidelity = sheetAndBlocks._fidelity;
	final XSSFSheet sheet = sheetAndBlocks._sheet;
	_header = new Field(sheetAndBlocks, sheet.getRow(kRow).getCell(0));
	_data = new Field(sheetAndBlocks, sheet.getRow(kRow).getCell(1));
    }

    public boolean isValid() {
	if (_header == null && _data == null) {
	    return false;
	}
	boolean goodHeader = false;
	if (_header != null) {
	    final CellType cellType = _header._cellType;
	    goodHeader = cellType == CellType.STRING || cellType == CellType.BOOLEAN || cellType == CellType.NUMERIC;
	}
	boolean goodData = false;
	if (_data != null) {
	    final CellType cellType = _data._cellType;
	    goodData = cellType == CellType.STRING || cellType == CellType.BOOLEAN || cellType == CellType.NUMERIC;
	}
	return goodHeader || goodData;
    }

    public String getString() {
	return String.format("Hdr[%s] Dta[%s]", _header.getString(), _data.getString());
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
