package com.skagit.roth;

import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFCell;

import com.skagit.util.NamedEntity;
import com.skagit.util.TypeOfDouble;

public class Field implements Comparable<Field> {

    final public static char _EmptySetChar = '\u2205';
    final public static String _EmptySetString = "" + '\u2205';

    public final Boolean _b;
    public final TypeOfDouble _typeOfDouble;
    public final double _d;
    public final String _s;

    public Field(final RothCalculator.SheetAndBlocks sheetAndBlocks, final int kRow, final int kClmn) {
	this(sheetAndBlocks, sheetAndBlocks._sheet.getRow(kRow).getCell(kClmn));
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

    final private static EnumSet<CellType> _BadCellTypes = EnumSet.of(CellType._NONE, CellType.ERROR, CellType.BLANK);

    public Field(final RothCalculator.SheetAndBlocks sheetAndBlocks, final XSSFCell cell) {
	final CellType cellType = cell == null ? null : cell.getCellType();
	if (cellType == null || _BadCellTypes.contains(cellType)) {
	    _b = null;
	    _typeOfDouble = null;
	    _d = Double.NaN;
	    _s = null;
	    return;
	}
	final RothCalculator rothCalculator = sheetAndBlocks.getRothCalculator();
	final FormulaEvaluator formulaEvaluator = rothCalculator._formulaEvaluator;
	switch (cellType) {
	case BOOLEAN:
	    _b = cell.getBooleanCellValue();
	    _typeOfDouble = null;
	    _d = Double.NaN;
	    _s = null;
	    return;
	case FORMULA:
	    final CellType cellType2 = formulaEvaluator.evaluateFormulaCell(cell);
	    switch (cellType2) {
	    case BOOLEAN:
		_b = formulaEvaluator.evaluate(cell).getBooleanValue();
		_typeOfDouble = null;
		_d = Double.NaN;
		_s = null;
		return;
	    case NUMERIC:
		_b = null;
		_s = null;
		_typeOfDouble = getTypeOfDouble(cell);
		_d = formulaEvaluator.evaluate(cell).getNumberValue();
		return;
	    case STRING:
		_b = null;
		_s = NamedEntity.CleanWhiteSpace(formulaEvaluator.evaluate(cell).getStringValue());
		_d = Double.NaN;
		_typeOfDouble = null;
		return;
	    default:
		_b = null;
		_d = Double.NaN;
		_typeOfDouble = null;
		_s = null;
		return;
	    }
	case NUMERIC:
	    _b = null;
	    _typeOfDouble = getTypeOfDouble(cell);
	    _d = cell.getNumericCellValue();
	    _s = null;
	    return;
	case STRING:
	    _b = null;
	    _s = NamedEntity.CleanWhiteSpace(formulaEvaluator.evaluate(cell).getStringValue());
	    _d = Double.NaN;
	    _typeOfDouble = null;
	    return;
	default:
	    _b = null;
	    _typeOfDouble = null;
	    _d = Double.NaN;
	    _s = null;
	    return;
	}
    }

    public Field(final String s) {
	_b = null;
	_typeOfDouble = null;
	_d = Double.NaN;
	_s = s;
    }

    public boolean hasData() {
	return _s != null || _b != null || Double.isFinite(_d);
    }

    private static TypeOfDouble getTypeOfDouble(final XSSFCell cell) {
	if (DateUtil.isCellDateFormatted(cell)) {
	    return TypeOfDouble.DATE;
	} else {
	    final String formatString = cell.getCellStyle().getDataFormatString();
	    if (formatString.contains("%")) {
		return TypeOfDouble.PER_CENT;
	    }
	    if (formatString.contains("$")) {
		return TypeOfDouble.MONEY;
	    }
	}
	return TypeOfDouble.OTHER;
    }

    public String getString() {
	if (_b != null) {
	    return _b.toString();
	} else if (Double.isFinite(_d)) {
	    switch (_typeOfDouble) {
	    case DATE:
		return _typeOfDouble.format(_d, 0);
	    case MONEY:
	    case OTHER:
		return _typeOfDouble.format(_d, 2);
	    case PER_CENT:
		return _typeOfDouble.format(_d, 1);
	    default:
		break;

	    }
	} else if (_s != null) {
	    return _s.length() == 0 ? _EmptySetString : _s;
	}
	return "NULL";
    }

    @Override
    public String toString() {
	return getString();
    }

    public Date getDate() {
	return DateUtil.getJavaDate(_d);
    }

    @Override
    public int compareTo(final Field field) {
	if (field == null) {
	    return -1;
	}
	final String myS = _s, hisS = field._s;
	if ((myS == null) != (hisS == null)) {
	    return myS != null ? -1 : 1;
	}
	if (myS != null) {
	    return myS.compareTo(hisS);
	}
	if ((_typeOfDouble == null) != (field._typeOfDouble == null)) {
	    return _typeOfDouble != null ? -1 : 1;
	}
	if (_typeOfDouble != null) {
	    if (_d == field._d) {
		return 0;
	    }
	    return _d < field._d ? -1 : 1;
	}
	if ((_b == null) != (field._b == null)) {
	    return _b == null ? -1 : 1;
	}
	if (_b == null) {
	    return 0;
	}
	if (Boolean.valueOf(_b) == Boolean.valueOf(field._b)) {
	    return 0;
	}
	return Boolean.valueOf(_b) ? 1 : -1;
    }

}
