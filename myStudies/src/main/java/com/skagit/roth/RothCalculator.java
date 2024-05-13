package com.skagit.roth;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.skagit.roth.yearOfData.YearOfData;
import com.skagit.util.DateUtils;
import com.skagit.util.NamedEntity;

public class RothCalculator {

    final public static String[] _SheetNames = { //
	    "Statics", //
	    "Brackets", //
	    "Investments", //
	    "Life Expectancies", // "
    };
    final public static int _StaticsSheetIdx = 0;
    final public static int _BracketsSheetIdx = 1;
    final public static int _InvestmentsSheetIdx = 2;
    final public static int _LifeExpectanciesSheetIdx = 3;

    final public static String[] _BracketsNames = { //
	    "Tax Brackets", //
	    "Long Term Tax Rates", //
	    "Social Security AGI Tax Rates", //
	    "IRMAA Multipliers", //
    };

    final public static String[] _GrowthRateNames = { //
	    "Inflation", //
	    "Investments", //
    };
    final public static int _InflationGrowthRateIdx = 0;
    final public static int _InvestmentsGrowthRateIdx = 1;

    public class SheetAndBlocks extends NamedEntity {
	final XSSFSheet _sheet;
	final Block[] _blocks;

	public SheetAndBlocks(final XSSFSheet sheet) {
	    super(NamedEntity.CleanWhiteSpace(sheet.getSheetName()));
	    _sheet = sheet;
	    final int nMergedRegions = _sheet.getNumMergedRegions();
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
	    _blocks = new Block[nDataBlocks];
	    for (int k = 0; k < nDataBlocks; ++k) {
		final CellRangeAddress thisCra = javaInputHeaders[k];
		final CellRangeAddress nextCra = javaInputHeaders[k + 1];
		_blocks[k] = new Block(this, thisCra, nextCra);
	    }
	    Arrays.sort(_blocks);
	}

	public String getString() {
	    String s = _name;
	    final int nBlocks = _blocks == null ? 0 : _blocks.length;
	    for (int k = 0; k < nBlocks; ++k) {
		final Block block = _blocks[k];
		s += String.format("\n%s", block.getString());
	    }
	    return s;
	}

	@Override
	public String toString() {
	    return getString();
	}

	public RothCalculator getRothCalculator() {
	    return RothCalculator.this;
	}
    }

    public static String getSheetName(final int idx) {
	return _SheetNames[idx];
    }

    public class TaxPayer extends NamedEntity {

	public class Ira extends NamedEntity {

	    public final int _ageOfRmd;
	    public final double _currentDivisor;
	    public final double _balanceBeginningOfCurrentYear;
	    public final double _currentBalance;

	    public Ira(final String name) {
		super(name);
		final Line forLookUp = new Line(_name);
		final String staticsSheetName = getSheetName(_StaticsSheetIdx);
		final Line[] ageBlockLines = getBlock(staticsSheetName, "IRA Age of RMD")._lines;
		final int idx0 = Arrays.binarySearch(ageBlockLines, forLookUp);
		_ageOfRmd = idx0 < 0 ? 0 : (int) Math.round(ageBlockLines[idx0]._data._d);
		final Line[] divisorLines = getBlock(staticsSheetName, "IRA Divisor Current Year")._lines;
		final int idx2 = Arrays.binarySearch(divisorLines, forLookUp);
		_currentDivisor = idx2 < 0 ? Double.NaN : divisorLines[idx2]._data._d;
		final Line[] balance0Lines = getBlock(staticsSheetName, "IRA Balance Beginning of Current Year")._lines;
		final int idx3 = Arrays.binarySearch(balance0Lines, forLookUp);
		_balanceBeginningOfCurrentYear = balance0Lines[idx3]._data._d;
		final Line[] balance1Lines = getBlock(staticsSheetName, "IRA Current Balance")._lines;
		final int idx4 = Arrays.binarySearch(balance1Lines, forLookUp);
		_currentBalance = balance1Lines[idx4]._data._d;
	    }

	    public TaxPayer getOwner() {
		return TaxPayer.this;
	    }

	    public String getString() {
		String s = String.format("IRA[%s], Balance[$%.2f] CurrentBalance[$%.2f]", _name,
			_balanceBeginningOfCurrentYear, _currentBalance);
		if (_ageOfRmd > 0) {
		    s += String.format(", Age of RMD[%d]", _ageOfRmd);
		} else {
		    s += String.format(", CurrentDivisor[%.1f]", _currentDivisor);
		}
		return s;
	    }

	    @Override
	    public String toString() {
		return getString();
	    }
	}

	public class OutsideIncome extends NamedEntity {

	    public final double _amount;
	    public final int _year;

	    public OutsideIncome(final String name) {
		super(name);
		final Line forLookUp = new Line(_name);
		final Line[] oi0BlockLines = getBlock(getSheetName(_StaticsSheetIdx), "Outside Income Amount")._lines;
		final int idx0 = Arrays.binarySearch(oi0BlockLines, forLookUp);
		_amount = oi0BlockLines[idx0]._data._d;
		final Line[] oi1BlockLines = getBlock(getSheetName(_StaticsSheetIdx), "Outside Income Year")._lines;
		final int idx1 = Arrays.binarySearch(oi1BlockLines, forLookUp);
		if (idx1 < 0) {
		    _year = -1;
		} else {
		    _year = (int) Math.round(oi1BlockLines[idx1]._data._d);
		}
	    }

	    public TaxPayer getOwner() {
		return TaxPayer.this;
	    }

	    public String getString() {
		String s = String.format("OI[%s], Amount[$%.2f]", _name, _amount);
		if (_year > 0) {
		    s += String.format(", Year[%d]", _year);
		}
		return s;
	    }

	    @Override
	    public String toString() {
		return getString();
	    }
	}

	public final Date _dateOfBirth;
	public final double _ssa;
	public final Ira[] _iras;
	public final OutsideIncome[] _outsideIncomes;

	public TaxPayer(final Line line) {
	    super(line._header._s);
	    _dateOfBirth = line._data.getDate();
	    final String staticsSheetName = getSheetName(_StaticsSheetIdx);
	    final Line[] ssaLines = getBlock(staticsSheetName, "SSA Current Year")._lines;
	    final int idx = Arrays.binarySearch(ssaLines, new Line(_name));
	    _ssa = idx < 0 ? 0d : ssaLines[idx]._data._d;
	    final Line[] iraDefnLines = getBlock(staticsSheetName, "IRA")._lines;
	    final int nIraDefns = iraDefnLines.length;
	    final ArrayList<Ira> iraList = new ArrayList<>();
	    for (int k = 0; k < nIraDefns; ++k) {
		final Line iraDefnLine = iraDefnLines[k];
		final Field iraDefnLineData = iraDefnLine._data;
		if (iraDefnLineData._s.equals(_name)) {
		    final String iraName = iraDefnLine._header._s;
		    iraList.add(new Ira(iraName));
		}
	    }
	    _iras = iraList.toArray(new Ira[iraList.size()]);
	    final Line[] outsideIncomeDefnLines = getBlock(staticsSheetName, "Outside Income")._lines;
	    final int nOidls = outsideIncomeDefnLines.length;
	    final ArrayList<OutsideIncome> oiList = new ArrayList<>();
	    for (int k = 0; k < nOidls; ++k) {
		final Line oiLine = outsideIncomeDefnLines[k];
		final Field oiData = oiLine._data;
		if (oiData._s.equals(_name)) {
		    final String oiName = oiLine._header._s;
		    oiList.add(new OutsideIncome(oiName));
		}
	    }
	    _outsideIncomes = oiList.toArray(new OutsideIncome[oiList.size()]);
	    Arrays.sort(_outsideIncomes);
	}

	public String getString() {
	    String s = String.format("TP[%s], DateOfBirth[%s]", _name, DateUtils.formatDateOnly(_dateOfBirth));
	    if (_ssa > 0d) {
		s += String.format(" Current Year SSA[$%.2f]", _ssa);
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

	public RothCalculator getRothCalculator() {
	    return RothCalculator.this;
	}
    }

    public class CapitalGain extends NamedEntity {

	public final double _carryOverToCurrentYear;

	public CapitalGain(final Line cgLine) {
	    super(cgLine._header._s);
	    _carryOverToCurrentYear = cgLine._data._d;
	}

	public String getString() {
	    return String.format("Capital Gain[%s], Carryover to Current Year[$%.2f]", //
		    _name, _carryOverToCurrentYear);
	}

	@Override
	public String toString() {
	    return getString();
	}
    }

    public class GrowthRate extends NamedEntity {

	public final double _perCentGrowth;
	public final double _expGrowthRate;

	public GrowthRate(final Line grLine) {
	    super(grLine._header._s);
	    _perCentGrowth = grLine._data._d * 100d;
	    _expGrowthRate = Math.log(1d + _perCentGrowth / 100d);
	}

	public String getString() {
	    return String.format("Name[%s], PerCentGrowth[%.0f%%] ExpRate[%.4f]", //
		    _name, _perCentGrowth, _expGrowthRate);
	}

	@Override
	public String toString() {
	    return getString();
	}
    }

    /** Poi concepts: */
    final public XSSFWorkbook _workBook;
    final public FormulaEvaluator _formulaEvaluator;
    final public SheetAndBlocks[] _sheetAndBlocksS;
    /** Class representation: */
    public final Date _currentDate;
    public final int _finalYear;
    public final double _remainderOfCurrentYear;
    public final double _standardDeductionCurrentYear;
    public final double _partBPremiumCurrentYear;
    public final double[] _lifeExpectancies;
    public final TaxPayer[] _taxPayers;
    public final CapitalGain[] _capitalGains;
    public final GrowthRate[] _growthRates;
    public final Brackets[] _bracketsS;

    public final YearOfData[] _yearsOfData;

    public RothCalculator(final XSSFWorkbook workBook) {
	_workBook = workBook;
	_formulaEvaluator = _workBook.getCreationHelper().createFormulaEvaluator();
	/** Read Sheets into Blocks. */
	final int nSheets = _SheetNames.length;
	_sheetAndBlocksS = new SheetAndBlocks[nSheets];
	for (int k = 0; k < nSheets; ++k) {
	    _sheetAndBlocksS[k] = new SheetAndBlocks(_workBook.getSheet(_SheetNames[k]));
	}
	Arrays.sort(_sheetAndBlocksS);
	final String staticsSheetName = getSheetName(_StaticsSheetIdx);
	_currentDate = getBlock(staticsSheetName, "Current Date")._lines[0]._header.getDate();
	final int currentYear = getCurrentYear();
	_finalYear = (int) Math.round(getBlock(staticsSheetName, "Final Year")._lines[0]._header._d);
	_standardDeductionCurrentYear = getBlock(staticsSheetName,
		"Standard Deduction Current Year")._lines[0]._data._d;
	_partBPremiumCurrentYear = getBlock(staticsSheetName, "Part B Premium Current Year")._lines[0]._data._d;
	final Date thisJan1 = DateUtils.parseDate(String.format("%d-1-1", currentYear));
	final Date nextJan1 = DateUtils.parseDate(String.format("%d-1-1", currentYear + 1));
	final double d0 = DateUtils.getDateDiff(thisJan1, _currentDate, TimeUnit.DAYS);
	final double d1 = DateUtils.getDateDiff(thisJan1, nextJan1, TimeUnit.DAYS);
	_remainderOfCurrentYear = (d1 - d0) / d1;

	final Line[] taxPayerLines = getBlock(staticsSheetName, "Tax Payer")._lines;
	final int nLines0 = taxPayerLines.length;
	_taxPayers = new TaxPayer[nLines0];
	for (int k = 0; k < nLines0; ++k) {
	    _taxPayers[k] = new TaxPayer(taxPayerLines[k]);
	}
	Arrays.sort(_taxPayers);
	final Line[] capitalGainsCarryOverLines = getBlock(staticsSheetName,
		"Capital Gains Carryover to Current Year")._lines;
	final int nLines1 = capitalGainsCarryOverLines.length;
	_capitalGains = new CapitalGain[nLines1];
	for (int k = 0; k < nLines1; ++k) {
	    _capitalGains[k] = new CapitalGain(capitalGainsCarryOverLines[k]);
	}
	Arrays.sort(_capitalGains);
	final Line[] growthRateLines = getBlock(staticsSheetName, "Growth Rates")._lines;
	final int nLines2 = growthRateLines.length;
	_growthRates = new GrowthRate[nLines2];
	for (int k = 0; k < nLines2; ++k) {
	    _growthRates[k] = new GrowthRate(growthRateLines[k]);
	}
	Arrays.sort(_growthRates);
	final int nBracketsS = _BracketsNames.length;
	_bracketsS = new Brackets[nBracketsS];
	for (int k = 0; k < nBracketsS; ++k) {
	    _bracketsS[k] = new Brackets(this, _BracketsNames[k]);
	}
	Arrays.sort(_bracketsS);
	/** Read in the Life Expectancies. */
	final String lifeExpectanciesSheetName = getSheetName(_LifeExpectanciesSheetIdx);
	final Line[] lifeExpectancyLines = getBlock(lifeExpectanciesSheetName, "Expected Life Lengths")._lines;
	final int nLines3 = lifeExpectancyLines.length;
	_lifeExpectancies = new double[nLines3];
	for (int k = 0; k < nLines3; ++k) {
	    _lifeExpectancies[k] = lifeExpectancyLines[k]._data._d;
	}

	final int nYears = _finalYear - currentYear + 1;
	_yearsOfData = new YearOfData[nYears];
	for (int year = currentYear; year <= _finalYear; ++year) {
	    _yearsOfData[year - currentYear] = new YearOfData(this, year);
	}
    }

    public Block getBlock(final String sheetName, final String blockName) {
	final int sheetIdx = Arrays.binarySearch(_sheetAndBlocksS, new NamedEntity(sheetName));
	if (sheetIdx < 0) {
	    return null;
	}
	final Block[] theseBlocks = _sheetAndBlocksS[sheetIdx]._blocks;
	final int blockIdx = Arrays.binarySearch(theseBlocks, new NamedEntity(blockName));
	return blockIdx < 0 ? null : theseBlocks[blockIdx];
    }

    public Block getNewBracketsBlock(final Brackets oldBrackets, final int thisYear) {
	return getBlock(_SheetNames[_BracketsSheetIdx], oldBrackets._name + " " + thisYear);
    }

    public int getCurrentYear() {
	return DateUtils.getAPartOfADate(_currentDate, ChronoField.YEAR);
    }

    public double getGrowthRate(final int growthRateIdx) {
	final String growthRateName = _GrowthRateNames[growthRateIdx];
	final int idx = Arrays.binarySearch(_growthRates, new NamedEntity(growthRateName));
	return idx < 0 ? Double.NaN : _growthRates[idx]._expGrowthRate;
    }

    public String getString() {
	String s = String.format("Current Year[%d], Current Date[%s], Final Year[%d]", //
		getCurrentYear(), DateUtils.formatDateOnly(new Date(System.currentTimeMillis())), _finalYear);
	s += String.format(//
		"\nStandard Deduction Current Year[$%.2f], Medicare-B Standard Premium Current year[$%.2f]", //
		_standardDeductionCurrentYear, _partBPremiumCurrentYear //
	);
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
	final int nGrowthRates = _growthRates.length;
	for (int k = 0; k < nGrowthRates; ++k) {
	    if (k == 0) {
		s += "\n";
	    }
	    s += String.format("\n%d. %s", k, _growthRates[k].getString());
	}
	final int nBracketsS = _BracketsNames.length;
	for (int k = 0; k < nBracketsS; ++k) {
	    s += String.format("\n\n%s", _bracketsS[k]);
	}
	final int investmentSheetIdx = Arrays.binarySearch(_sheetAndBlocksS, new NamedEntity("Investments"));
	final Block[] investmentBlocks = _sheetAndBlocksS[investmentSheetIdx]._blocks;
	final int nInvestmentBlocks = investmentBlocks.length;
	for (int k = 0; k < nInvestmentBlocks; ++k) {
	    final Block block = investmentBlocks[k];
	    s += "\n\n" + block.getString();
	}
	final int nYearsOfData = _yearsOfData.length;
	for (int k = 0; k < nYearsOfData; ++k) {
	    final YearOfData yod = _yearsOfData[k];
	    s += "\n\n" + (yod == null ? "NULL" : yod.getString());
	}
	return s;
    }

    @Override
    public String toString() {
	return getString();
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

    public YearOfData.TP1 getTp1(final TaxPayer taxPayer, final int thisYear) {
	final int currentYear = getCurrentYear();
	if (thisYear < currentYear || thisYear > _finalYear) {
	    return null;
	}
	final YearOfData yearOfData = _yearsOfData[thisYear - currentYear];
	final YearOfData.TP1[] tp1s = yearOfData._tp1s;
	final int idx = Arrays.binarySearch(tp1s, new NamedEntity(taxPayer._name, thisYear));
	if (idx < 0) {
	    return null;
	}
	return yearOfData._tp1s[idx];
    }

    public YearOfData.TP1.IRA1 getIra1(final TaxPayer.Ira ira, final int thisYear) {
	final YearOfData.TP1 tp1 = getTp1(ira.getOwner(), thisYear);
	if (tp1 == null) {
	    return null;
	}
	final YearOfData.TP1.IRA1[] ira1s = tp1._ira1s;
	final int idx = Arrays.binarySearch(ira1s, new NamedEntity(ira._name, thisYear));
	if (idx < 0) {
	    return null;
	}
	return tp1._ira1s[idx];
    }

    public YearOfData.TP1.OI1 getOi1(final TaxPayer.OutsideIncome outsideIncome, final int thisYear) {
	final YearOfData.TP1 tp1 = getTp1(outsideIncome.getOwner(), thisYear);
	if (tp1 == null) {
	    return null;
	}
	final YearOfData.TP1.IRA1[] ira1s = tp1._ira1s;
	final int idx = Arrays.binarySearch(ira1s, new NamedEntity(outsideIncome._name, thisYear));
	if (idx < 0) {
	    return null;
	}
	return tp1._oi1s[idx];
    }

}
