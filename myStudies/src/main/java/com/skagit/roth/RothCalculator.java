package com.skagit.roth;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
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

    final public static String[] _SheetNames = { "Statics", "Brackets First Year", "Fidelity" };
    final public static SimpleDateFormat _SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    final public static SimpleDateFormat _YearOnlySimpleDateFormat = new SimpleDateFormat("yyyy");

    public class TaxPayer {

	public class Ira implements Comparable<Ira> {

	    public final String _name;
	    public final int _ageOfRmd;
	    public final int _firstYear;
	    public final double _currentDivisor;
	    public final double _balance0;
	    public final double _currentBalance;

	    public Ira(final String iraName) {
		_name = iraName;
		final Line forLookUp = new Line(_name);
		final Line[] ageBlockLines = getBlock(0, "IRA Age of RMD")._lines;
		final int idx0 = Arrays.binarySearch(ageBlockLines, forLookUp);
		_ageOfRmd = idx0 < 0 ? 0 : (int) Math.round(ageBlockLines[idx0]._data._d);
		final Line[] firstYearLines = getBlock(0, "IRA First Year")._lines;
		final int idx1 = Arrays.binarySearch(firstYearLines, forLookUp);
		_firstYear = idx1 < 0 ? 0 : (int) Math.round(firstYearLines[idx1]._data._d);
		final Line[] divisorLines = getBlock(0, "IRA Divisor First Year")._lines;
		final int idx2 = Arrays.binarySearch(divisorLines, forLookUp);
		_currentDivisor = idx2 < 0 ? Double.NaN : divisorLines[idx2]._data._d;
		final Line[] balance0Lines = getBlock(0, "IRA Balance First Year")._lines;
		final int idx3 = Arrays.binarySearch(balance0Lines, forLookUp);
		_balance0 = balance0Lines[idx3]._data._d;
		final Line[] balance1Lines = getBlock(0, "IRA Current Balance")._lines;
		final int idx4 = Arrays.binarySearch(balance1Lines, forLookUp);
		_currentBalance = balance1Lines[idx4]._data._d;
	    }

	    @Override
	    public int compareTo(final Ira ira) {
		return _name.compareTo(ira._name);
	    }

	    public String getString() {
		String s = String.format("IRA[%s], Balance0[$%.2f] CurrentBalance[$%.2f]", _name, _balance0,
			_currentBalance);
		if (_ageOfRmd > 0) {
		    s += String.format(", Age of RMD[%d]", _ageOfRmd);
		} else {
		    s += String.format(", FirstYear[%d] DivisorForFirstYear[%.1f]", _firstYear, _currentDivisor);
		}
		return s;
	    }

	    @Override
	    public String toString() {
		return getString();
	    }

	}

	public class OutsideIncome implements Comparable<OutsideIncome> {

	    public final String _name;
	    public final double _amountFirstYear;

	    public OutsideIncome(final String oiName) {
		_name = oiName;
		final Line forLookUp = new Line(_name);
		final Line[] oiBlockLines = getBlock(0, "Outside Income First Year")._lines;
		final int idx0 = Arrays.binarySearch(oiBlockLines, forLookUp);
		_amountFirstYear = oiBlockLines[idx0]._data._d;
	    }

	    @Override
	    public int compareTo(final OutsideIncome outsideIncome) {
		return _name.compareTo(outsideIncome._name);
	    }

	    public String getString() {
		return String.format("Outside Income[%s], Amount First Year[$%.2f]", _name, _amountFirstYear);
	    }

	    @Override
	    public String toString() {
		return getString();
	    }
	}

	public final String _taxPayerName;
	public final Date _dateOfBirth;
	public final double _firstYearSsa;
	public final Ira[] _iras;
	public final OutsideIncome[] _outsideIncomes;

	public TaxPayer(final Line line) {
	    _taxPayerName = line._header._s;
	    _dateOfBirth = line._data._date;
	    final Line[] ssaLines = getBlock(0, "SSA First Year")._lines;
	    final int idx = Arrays.binarySearch(ssaLines, new Line(_taxPayerName));
	    _firstYearSsa = idx < 0 ? 0d : ssaLines[idx]._data._d;
	    final Line[] iraDefnLines = getBlock(0, "IRA")._lines;
	    final int nIraDefns = iraDefnLines.length;
	    final ArrayList<Ira> iraList = new ArrayList<>();
	    for (int k = 0; k < nIraDefns; ++k) {
		final Line iraDefnLine = iraDefnLines[k];
		final Field iraDefnLineData = iraDefnLine._data;
		if (iraDefnLineData._s.equals(_taxPayerName)) {
		    final String iraName = iraDefnLine._header._s;
		    iraList.add(new Ira(iraName));
		}
	    }
	    _iras = iraList.toArray(new Ira[iraList.size()]);
	    final Line[] outsideIncomeDefnLines = getBlock(0, "Outside Income")._lines;
	    final int nOidls = outsideIncomeDefnLines.length;
	    final ArrayList<OutsideIncome> oiList = new ArrayList<>();
	    for (int k = 0; k < nOidls; ++k) {
		final Line oiLine = outsideIncomeDefnLines[k];
		final Field oiData = oiLine._data;
		if (oiData._s.equals(_taxPayerName)) {
		    final String oiName = oiLine._header._s;
		    oiList.add(new OutsideIncome(oiName));
		}
	    }
	    _outsideIncomes = oiList.toArray(new OutsideIncome[oiList.size()]);
	}

	public String getString() {
	    String s = String.format("TaxPayer[%s], DateOfBirth[%s]", _taxPayerName, FormatDate(_dateOfBirth));
	    if (_firstYearSsa > 0d) {
		s += String.format(" FirstYearSSA[$%.2f]", _firstYearSsa);
	    }
	    final int nIras = _iras.length;
	    for (int k = 0; k < nIras; ++k) {
		s += "\n\t" + _iras[k].getString();
	    }
	    final int nOutsideIncomes = _outsideIncomes.length;
	    for (int k = 0; k < nOutsideIncomes; ++k) {
		s += "\n\t" + _outsideIncomes[k].getString();
	    }
	    return s;
	}

	@Override
	public String toString() {
	    return getString();
	}
    }

    public class CapitalGain implements Comparable<CapitalGain> {

	public final String _name;
	public final double _carryOverFirstYear;

	public CapitalGain(final Line cgLine) {
	    _name = cgLine._header._s;
	    _carryOverFirstYear = cgLine._data._d;
	}

	@Override
	public int compareTo(final CapitalGain capitalGain) {
	    return _name.compareTo(capitalGain._name);
	}

	public String getString() {
	    return String.format("Capital Gain[%s], Carryover First Year[$%.2f]", _name, _carryOverFirstYear);
	}

	@Override
	public String toString() {
	    return getString();
	}
    }

    /** Poi concepts: */
    final public XSSFWorkbook _workBook;
    final public FormulaEvaluator _formulaEvaluator;
    final public XSSFSheet[] _sheets;
    /** Blocks of data: */
    final public Block[][] _blocksS;
    /** Class representation: */
    public final Date _firstEndOfYear;
    public final Date _lastEndOfYear;
    public final Date _currentDate;
    public final double _standardDeductionFirstYear;
    public final TaxPayer[] _taxPayers;
    public final CapitalGain[] _capitalGains;

    public RothCalculator(final XSSFWorkbook workBook) {
	_workBook = workBook;
	_formulaEvaluator = _workBook.getCreationHelper().createFormulaEvaluator();
	/** Read Sheets into Blocks. */
	final int nSheets = _SheetNames.length;
	_sheets = new XSSFSheet[nSheets];
	_blocksS = new Block[nSheets][];
	for (int k = 0; k < nSheets; ++k) {
	    _sheets[k] = _workBook.getSheet(_SheetNames[k]);
	    _blocksS[k] = sheetToBlocks(_sheets[k]);
	}
	_firstEndOfYear = getBlock(0, "First Year")._lines[0]._header._date;
	_lastEndOfYear = getBlock(0, "Last Year")._lines[0]._header._date;
	_standardDeductionFirstYear = getBlock(0, "Standard Deduction First Year")._lines[0]._data._d;
	Date currentDate = null;
	try {
	    currentDate = _SimpleDateFormat.parse(_SimpleDateFormat.format(new Date())); //
	} catch (final ParseException e) {
	}
	_currentDate = currentDate;

	final Line[] taxPayerLines = getBlock(0, "Tax Payer")._lines;
	final int nLines0 = taxPayerLines.length;
	_taxPayers = new TaxPayer[nLines0];
	for (int k = 0; k < nLines0; ++k) {
	    _taxPayers[k] = new TaxPayer(taxPayerLines[k]);
	}
	final Line[] capitalGainsCarryOverFirstYearLines = getBlock(0, "Capital Gains Carryover First Year")._lines;
	final int nLines1 = capitalGainsCarryOverFirstYearLines.length;
	_capitalGains = new CapitalGain[nLines1];
	for (int k = 0; k < nLines1; ++k) {
	    _capitalGains[k] = new CapitalGain(capitalGainsCarryOverFirstYearLines[k]);
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
	final String firstYearString = _YearOnlySimpleDateFormat.format(_firstEndOfYear);
	final String currentDateString = _SimpleDateFormat.format(new Date(System.currentTimeMillis()));
	final String lastYearString = _YearOnlySimpleDateFormat.format(_lastEndOfYear);
	String s = String.format("First Year[%s], Current Date[%s], Last Year[%s]\nStandard Deduction First Year[%.2f]",
		firstYearString, currentDateString, lastYearString, _standardDeductionFirstYear);
	final int nTaxPayers = _taxPayers.length;
	for (int k = 0; k < nTaxPayers; ++k) {
	    s += String.format("\n\n%d. %s", k, _taxPayers[k].getString());
	}
	final int nCapitalGains = _capitalGains.length;
	for (int k = 0; k < nCapitalGains; ++k) {
	    if (k == 0) {
		s += "\n";
	    }
	    s += String.format("\n%d. %s", k, _capitalGains[k].getString());
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

    private Block getBlock(final int idx, final String blockName) {
	final Block[] theseBlocks = _blocksS[idx];
	return theseBlocks[Arrays.binarySearch(theseBlocks, new Block(blockName))];
    }

    public static void main(final String[] args) {
	final String filePath = args[0] + ".xlsx";
	try (XSSFWorkbook workBook = new XSSFWorkbook(new FileInputStream(filePath))) {
	    final RothCalculator rothCalculator = new RothCalculator(workBook);
	    System.out.print(rothCalculator.getString());
	} catch (final IOException e) {
	    e.printStackTrace();
	}
    }

}