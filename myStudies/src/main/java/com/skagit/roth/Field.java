package com.skagit.roth;

import java.util.Comparator;
import java.util.Date;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class Field {

    final public static char _EmptySetChar = '\u2205';
    final public static String _EmptySetString = "" + '\u2205';

    public final CellType _cellType;
    public final Boolean _b;
    public final double _d;
    public final String _s;
    public final Date _date;

    public Field(final RothCalculator rothCalculator, final XSSFSheet sheet, final int kRow, final int kClmn) {
	this(rothCalculator, sheet.getRow(kRow).getCell(kClmn));
    }

    public static Comparator<Field> ByString = new Comparator<Field>() {

	@Override
	public int compare(final Field field0, final Field field1) {
	    if ((field0 == null) != (field1 == null)) {
		return field0 == null ? -1 : 1;
	    }
	    if (field0 == null) {
		return 0;
	    }
	    final String s0 = field0._s;
	    final String s1 = field1._s;
	    if ((s0 == null) != (s1 == null)) {
		return s0 == null ? -1 : 1;
	    }
	    if (s0 == null) {
		return 0;
	    }
	    return s0.compareTo(s1);
	}
    };

    /** For looking things up. */
    public Field(final String s) {
	_b = null;
	_d = Double.NaN;
	_s = s;
	_date = null;
	_cellType = CellType.STRING;
    }

    public Field(final RothCalculator rothCalculator, final XSSFCell cell) {
	if (cell == null) {
	    _b = null;
	    _d = Double.NaN;
	    _s = null;
	    _date = null;
	    _cellType = null;
	    return;
	}
	final CellType cellType = cell.getCellType();
	final FormulaEvaluator formulaEvaluator = rothCalculator._formulaEvaluator;
	switch (cellType) {
	case BLANK:
	    _b = null;
	    _d = Double.NaN;
	    _s = null;
	    _date = null;
	    _cellType = cellType;
	    return;
	case BOOLEAN:
	    _b = cell.getBooleanCellValue();
	    _d = Double.NaN;
	    _s = null;
	    _date = null;
	    _cellType = cellType;
	    return;
	case FORMULA:
	    final CellType cellType2 = formulaEvaluator.evaluateFormulaCell(cell);
	    switch (cellType2) {
	    case BOOLEAN:
		_b = formulaEvaluator.evaluate(cell).getBooleanValue();
		_d = Double.NaN;
		_s = null;
		_date = null;
		_cellType = CellType.BOOLEAN;
		return;
	    case NUMERIC:
		_b = null;
		_s = null;
		_cellType = CellType.NUMERIC;
		final double d = formulaEvaluator.evaluate(cell).getNumberValue();
		if (DateUtil.isCellDateFormatted(cell)) {
		    _d = Double.NaN;
		    _date = DateUtil.getJavaDate(d);
		} else {
		    _d = d;
		    _date = null;
		}
		return;
	    case STRING:
		_b = null;
		_d = Double.NaN;
		_s = formulaEvaluator.evaluate(cell).getStringValue();
		_date = null;
		_cellType = CellType.STRING;
		return;
	    default:
		_b = null;
		_d = Double.NaN;
		_s = null;
		_date = null;
		_cellType = CellType.ERROR;
		return;
	    }
	case NUMERIC:
	    final double d = cell.getNumericCellValue();
	    if (DateUtil.isCellDateFormatted(cell)) {
		_d = Double.NaN;
		_date = DateUtil.getJavaDate(d);
	    } else {
		_d = d;
		_date = null;
	    }
	    _b = null;
	    _s = null;
	    _cellType = CellType.NUMERIC;
	    return;
	case STRING:
	    _b = null;
	    _d = Double.NaN;
	    _s = cell.getStringCellValue();
	    _date = null;
	    _cellType = cellType;
	    return;
	case ERROR:
	case _NONE:
	default:
	    _b = null;
	    _d = Double.NaN;
	    _s = null;
	    _date = null;
	    _cellType = null;
	    return;
	}
    }

    public String getString() {
	if (_b != null) {
	    return _b.toString();
	} else if (Double.isFinite(_d)) {
	    return String.format("%.2f", _d);
	} else if (_s != null) {
	    return _s.length() == 0 ? _EmptySetString : _s;
	} else if (_date != null) {
	    return RothCalculator.FormatDate(_date);
	}
	return "NULL";
    }

    @Override
    public String toString() {
	return getString();
    }

}
