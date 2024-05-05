package com.skagit.roth;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class RothCalculator {

    final public static SimpleDateFormat _SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    final public static SimpleDateFormat _YearOnlySimpleDateFormat = new SimpleDateFormat("yyyy");

    public class TaxPayer {

	public class Ira implements Comparable<Ira> {

	    public final String _iraName;
	    public final int _age;
	    public final double _divisor;
	    public final double _amount;

	    public Ira(final String iraName, final int age, final double divisor, final double amount) {
		_iraName = iraName;
		_age = age;
		_divisor = divisor;
		_amount = amount;
	    }

	    @Override
	    public int compareTo(final Ira ira) {
		return _iraName.compareTo(ira._iraName);
	    }

	    public String getString() {
		String s = String.format("IRA[%s], $%.2f", _iraName, _amount);
		if (_age > 0) {
		    s += String.format(", RMD Age[%d]", _age);
		} else {
		    s += String.format(", RMD Divisor[%.1f]", _divisor);
		}
		return s;
	    }

	    @Override
	    public String toString() {
		return getString();
	    }

	}

	public final String _taxPayerName;
	public final Date _dateOfBirth;
	public final Ira[] _iras;

	public TaxPayer(final String taxPayerName, final Date dateOfBirth) {
	    _taxPayerName = taxPayerName;
	    _dateOfBirth = dateOfBirth;
	    final Line[] iraDefnLines = getIraDefns();
	    final int nIraDefns = iraDefnLines.length;
	    final ArrayList<Ira> iraList = new ArrayList<>();
	    for (int k = 0; k < nIraDefns; ++k) {
		final Line iraDefnLine = iraDefnLines[k];
		final Field iraDefnLineData = iraDefnLine._data;
		if (iraDefnLineData._s.equals(_taxPayerName)) {
		    final String iraName = iraDefnLine._header._s;
		    final int age = getIraAge(iraName);
		    final double divisor = getIraDivisor(iraName);
		    final double amount = getIraAmount(iraName);
		    iraList.add(new Ira(iraName, age, divisor, amount));
		}
	    }
	    _iras = iraList.toArray(new Ira[iraList.size()]);
	}

	public String getString() {
	    String s = String.format("TaxPayer[%s], DateOfBirth[%s]", _taxPayerName, FormatDate(_dateOfBirth));
	    final int nIras = _iras.length;
	    for (int k = 0; k < nIras; ++k) {
		s += "\n\t" + _iras[k].getString();
	    }
	    return s;
	}

	@Override
	public String toString() {
	    return getString();
	}
    }

    /** Poi concepts: */
    final public XSSFWorkbook _workBook;
    final public FormulaEvaluator _formulaEvaluator;
    final public XSSFSheet _staticSheet;
    final public XSSFSheet _firstYearSheet;
    /** Blocks of data: */
    final public Block[] _staticBlocks;
    final public Block[] _firstYearBlocks;
    /** Class representation: */
    public final Date _firstEndOfYear;
    public final Date _lastEndOfYear;
    public final TaxPayer[] _taxPayers;

    public RothCalculator(final XSSFWorkbook workBook, final String staticSheetName, final String firstYearSheetName) {
	_workBook = workBook;
	_formulaEvaluator = _workBook.getCreationHelper().createFormulaEvaluator();
	_staticSheet = _workBook.getSheet(staticSheetName);
	_firstYearSheet = _workBook.getSheet(firstYearSheetName);
	/** Read Sheets into Blocks. */
	_staticBlocks = sheetToBlocks(_staticSheet);
	_firstYearBlocks = sheetToBlocks(_firstYearSheet);
	/** Convert Blocks to class representation. */
	_firstEndOfYear = getFirstYear();
	_lastEndOfYear = getLastYear();
	final Block taxPayersBlock = getTaxPayersBlock();
	final Line[] lines = taxPayersBlock._lines;
	final int nLines = lines.length;
	_taxPayers = new TaxPayer[nLines];
	for (int k = 0; k < nLines; ++k) {
	    final Line line = lines[k];
	    _taxPayers[k] = new TaxPayer(line._header._s, line._data._date);
	}
    }

    private Block[] sheetToBlocks(final XSSFSheet sheet) {
	final int nMergedRegions = sheet.getNumMergedRegions();
	final ArrayList<CellRangeAddress> craList0 = new ArrayList<>();
	for (int k = 0; k < nMergedRegions; ++k) {
	    final CellRangeAddress cra = sheet.getMergedRegion(k);
	    final int nCells = cra.getNumberOfCells();
	    final int firstRow = cra.getFirstRow();
	    final int lastRow = cra.getLastRow();
	    final int firstClmn = cra.getFirstColumn();
	    if (nCells == 3 && firstRow == lastRow && firstClmn == 0) {
		craList0.add(cra);
	    }
	}
	Collections.sort(craList0, new Comparator<CellRangeAddress>() {

	    @Override
	    public int compare(final CellRangeAddress cra0, final CellRangeAddress cra1) {
		final int firstRow0 = cra0.getFirstRow();
		final int firstRow1 = cra1.getFirstRow();
		return firstRow0 < firstRow1 ? -1 : (firstRow0 > firstRow1 ? 1 : 0);
	    }
	});

	final int nCras0 = craList0.size();
	final ArrayList<CellRangeAddress> craList1 = new ArrayList<>();
	for (int k0 = 0; k0 < nCras0; ++k0) {
	    final CellRangeAddress cra = craList0.get(k0);
	    craList1.add(cra);
	    final int firstRow = cra.getFirstRow();
	    final XSSFCell cell = sheet.getRow(firstRow).getCell(cra.getFirstColumn());
	    if (cell.getStringCellValue().equals("End Data")) {
		break;
	    }
	}
	final int nCras1 = craList1.size();
	final CellRangeAddress[] javaInputHeaders = craList1.toArray(new CellRangeAddress[nCras1]);
	Arrays.sort(javaInputHeaders, new Comparator<CellRangeAddress>() {

	    @Override
	    public int compare(final CellRangeAddress cra0, final CellRangeAddress cra1) {
		final int firstRow0 = cra0.getFirstRow();
		final int firstRow1 = cra1.getFirstRow();
		return firstRow0 < firstRow1 ? -1 : (firstRow0 > firstRow1 ? 1 : 0);
	    }
	});
	final int nDataBlocks = nCras1 - 1;
	final Block[] blocks = new Block[nDataBlocks];
	for (int k = 0; k < nDataBlocks; ++k) {
	    final CellRangeAddress thisCra = javaInputHeaders[k];
	    final CellRangeAddress nextCra = javaInputHeaders[k + 1];
	    blocks[k] = new Block(this, sheet, thisCra, nextCra);
	}
	Arrays.sort(blocks);
	return blocks;
    }

    public String getString() {
	String s = String.format("From %s to %s.\nTax Payers:", _YearOnlySimpleDateFormat.format(_firstEndOfYear),
		_YearOnlySimpleDateFormat.format(_lastEndOfYear));
	final int nTaxPayers = _taxPayers.length;
	for (int k = 0; k < nTaxPayers; ++k) {
	    if (k > 0) {
		s += "\n";
	    }
	    s += String.format("\n%d. %s", k, _taxPayers[k].getString());
	}
	return s;
    }

    public String poiGetString() {
	String s = "Static Blocks:";
	final int nStaticBlocks = _staticBlocks.length;
	for (int k = 0; k < nStaticBlocks; ++k) {
	    if (k > 0) {
		s += "\n";
	    }
	    s += String.format("\n%d. %s", k, _staticBlocks[k]);
	}
	s += "\n\nFirst Year Blocks:";
	final int nFirstYearBlocks = _firstYearBlocks.length;
	for (int k = 0; k < nFirstYearBlocks; ++k) {
	    if (k > 0) {
		s += "\n";
	    }
	    s += String.format("\n%d. %s", k, _firstYearBlocks[k]);
	}
	return s;
    }

    @Override
    public String toString() {
	return getString();
    }

    public static String FormatDate(final Date date) {
	return _SimpleDateFormat.format(date);
    }

    private Date getFirstYear() {
	return getStaticBlock("First Year")._lines[0]._header._date;
    }

    private Date getLastYear() {
	return getStaticBlock("Last Year")._lines[0]._header._date;
    }

    public Block getTaxPayersBlock() {
	return getStaticBlock("Tax Payers");
    }

    public Line[] getIraDefns() {
	return getStaticBlock("IRAs")._lines;
    }

    public int getIraAge(final String iraName) {
	final Line[] lines = getStaticBlock("IRA RMD Ages")._lines;
	final int idx = Arrays.binarySearch(lines, new Line(iraName));
	if (idx < 0) {
	    return -1;
	}
	return (int) Math.round(lines[idx]._data._d);
    }

    public double getIraDivisor(final String iraName) {
	final Line[] lines = getStaticBlock("IRA RMD Divisors")._lines;
	final int idx = Arrays.binarySearch(lines, new Line(iraName));
	if (idx < 0) {
	    return Double.NaN;
	}
	return lines[idx]._data._d;
    }

    public double getIraAmount(final String iraName) {
	final Line[] lines = getStaticBlock("IRA Amounts")._lines;
	final int idx = Arrays.binarySearch(lines, new Line(iraName));
	if (idx < 0) {
	    return Double.NaN;
	}
	return lines[idx]._data._d;
    }

    private Block getStaticBlock(final String s) {
	return _staticBlocks[Arrays.binarySearch(_staticBlocks, new Block(s))];
    }

    public static void main(final String[] args) {
	final String filePath = args[0] + ".xlsx";
	final String staticSheetName = args[1];
	final String firstYearSheetName = args[2];
	try (XSSFWorkbook workBook = new XSSFWorkbook(new FileInputStream(filePath))) {
	    final RothCalculator rothCalculator = new RothCalculator(workBook, staticSheetName, firstYearSheetName);
	    System.out.print(rothCalculator.getString());
	} catch (final IOException e) {
	    e.printStackTrace();
	}
    }

}
