package com.skagit.roth;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.skagit.roth.taxYear.TaxYear;
import com.skagit.util.MyStudiesDateUtils;
import com.skagit.util.MyStudiesStringUtils;
import com.skagit.util.NamedEntity;
import com.skagit.util.TypeOfDouble;

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

    final public static String _InflationGrowthRateName = "Inflation";
    final public static String _InvestmentsGrowthRateName = "Investments";

    public class SheetAndBlocks extends NamedEntity {
	final XSSFSheet _sheet;
	final Block[] _blocks;

	public SheetAndBlocks(final XSSFSheet sheet) {
	    super(sheet.getSheetName());
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

    public class Account extends NamedEntity {

	public final Owner _owner;
	public final double _ageOfRmd;
	public final double _currentDivisor;
	public final double _balanceBeginningOfCurrentYear;
	public final double _currentBalance;

	public Account(final String name, final Owner owner) {
	    super(name);
	    _owner = owner;
	    final Line forLookUp = new Line(_name);
	    final String staticsSheetName = getSheetName(_StaticsSheetIdx);
	    if (_owner != null) {
		final Line[] ageBlockLines = getBlock(staticsSheetName, "Age of RMD")._lines;
		final int idx0 = Arrays.binarySearch(ageBlockLines, forLookUp);
		_ageOfRmd = idx0 < 0 ? 0d : ageBlockLines[idx0]._data._d;
		final Line[] divisorLines = getBlock(staticsSheetName, "Divisor Current Year")._lines;
		final int idx2 = Arrays.binarySearch(divisorLines, forLookUp);
		_currentDivisor = idx2 < 0 ? Double.NaN : divisorLines[idx2]._data._d;
	    } else {
		_ageOfRmd = 0d;
		_currentDivisor = Double.NaN;
	    }
	    final Line[] balance0Lines = getBlock(staticsSheetName, "Balance Beginning of Current Year")._lines;
	    final int idx3 = Arrays.binarySearch(balance0Lines, forLookUp);
	    _balanceBeginningOfCurrentYear = balance0Lines[idx3]._data._d;
	    final Line[] balance1Lines = getBlock(staticsSheetName, "Current Balance")._lines;
	    final int idx4 = Arrays.binarySearch(balance1Lines, forLookUp);
	    _currentBalance = balance1Lines[idx4]._data._d;
	}

	public String getString() {
	    String s = String.format("ACCNT[%s]", _name);
	    if (_owner != null) {
		s += String.format(", OWNR[%s]", _owner._name);
	    }
	    s += String.format(", BlncBgnnngYr[%s] CrrntBlnc[%s]", //
		    TypeOfDouble.MONEY.format(_balanceBeginningOfCurrentYear, 2), //
		    TypeOfDouble.MONEY.format(_currentBalance, 2));
	    if (_ageOfRmd > 0d) {
		s += String.format(", Age at RMD[%s]", MyStudiesStringUtils.formatOther(_ageOfRmd, 1));
	    } else if (_currentDivisor > 0d) {
		s += String.format(", CrrntDvsr[%s]", //
			MyStudiesStringUtils.formatOther(_currentDivisor, 1));
	    }
	    return s;
	}

	@Override
	public String toString() {
	    return getString();
	}
    }

    public class JointAccount extends Account {

	public JointAccount(final String name) {
	    super(name, null);
	}

	@Override
	public String getString() {
	    return super.getString();
	}

	@Override
	public String toString() {
	    return getString();
	}
    }

    public class Owner extends NamedEntity {

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

	    public Owner getOwner() {
		return Owner.this;
	    }

	    public String getString() {
		String s = String.format("OI[%s], Amnt[%s]", //
			_name, //
			TypeOfDouble.MONEY.format(_amount, 2));
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
	public final Account[] _myAccnts;
	public final OutsideIncome[] _outsideIncomes;

	public Owner(final Line line) {
	    super(line._header._s);
	    _dateOfBirth = line._data.getDate();
	    final String staticsSheetName = getSheetName(_StaticsSheetIdx);
	    final Line[] ssaLines = getBlock(staticsSheetName, "SSA")._lines;
	    final int idx = Arrays.binarySearch(ssaLines, new Line(_name));
	    _ssa = idx < 0 ? 0d : ssaLines[idx]._data._d;
	    final Line[] allAccntDefnLines = getBlock(staticsSheetName, "Account Owners")._lines;
	    final int nAccntDefns = allAccntDefnLines.length;
	    final ArrayList<Account> myAccntList = new ArrayList<>();
	    for (int k = 0; k < nAccntDefns; ++k) {
		final Line accntDefnLine = allAccntDefnLines[k];
		final Field accntDefnLineData = accntDefnLine._data;
		final String ownerName = accntDefnLineData._s;
		if (ownerName != null && ownerName.equals(_name)) {
		    final String accntName = accntDefnLine._header._s;
		    myAccntList.add(new Account(accntName, this));
		}
	    }
	    _myAccnts = myAccntList.toArray(new Account[myAccntList.size()]);
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
	    String s = String.format("Owner[%s], DtOfBrth[%s]", _name, MyStudiesDateUtils.formatDateOnly(_dateOfBirth));
	    if (_ssa > 0d) {
		s += String.format(" CrrntYrSsa[%s]", TypeOfDouble.MONEY.format(_ssa, 2));
	    }
	    final int nMyAccnts = _myAccnts.length;
	    for (int k = 0; k < nMyAccnts; ++k) {
		s += "\n\t" + _myAccnts[k].getString();
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

    /** Poi concepts: */
    final public XSSFWorkbook _workBook;
    final public FormulaEvaluator _formulaEvaluator;
    final public SheetAndBlocks[] _sheetAndBlocksS;
    /** Miscellaneous Data: */
    public final Date _currentDate;
    public final int _finalYear;
    public final double _standardDeductionCurrentYear;
    public final double _partBPremiumCurrentYear;
    public final double _maxCapitalGainsLoss;
    public final GrowthRate _inflationGrowthRate;
    public final GrowthRate _investmentsGrowthRate;
    public final double _perCentLong;
    public final double _perCentLeftOfCurrentYear;
    public final double _medicareTaxThreshold;
    public final double _additionalMedicareTaxPerCent;
    public final double _medicareTaxPerCentOnInvestments;

    public final double[] _lifeExpectancies;
    public final Brackets[] _bracketsS;

    public final Owner[] _owners;
    public final Account[] _jointAccounts;
    public final InvestmentItem[] _investmentItems;

    public final TaxYear[] _taxYears;

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
	_currentDate = getMiscellaneousData("Current Date").getDate();
	final int currentYear = getCurrentYear();
	final Date thisJan1 = MyStudiesDateUtils.parseDate(String.format("%d-1-1", currentYear));
	final Date nextJan1 = MyStudiesDateUtils.parseDate(String.format("%d-1-1", currentYear + 1));
	final double d0 = MyStudiesDateUtils.getDateDiff(thisJan1, _currentDate, TimeUnit.DAYS);
	final double d1 = MyStudiesDateUtils.getDateDiff(thisJan1, nextJan1, TimeUnit.DAYS);
	_perCentLeftOfCurrentYear = 100d * (d1 - d0) / d1;
	_finalYear = (int) Math.round(getMiscellaneousData("Final Year")._d);
	_standardDeductionCurrentYear = getMiscellaneousData("Standard Deduction Current Year")._d;
	_partBPremiumCurrentYear = getMiscellaneousData("Part B Premium Current Year")._d;
	_maxCapitalGainsLoss = getMiscellaneousData("Max Capital Gains Loss")._d;
	final double inflationProportion = getMiscellaneousData(_InflationGrowthRateName)._d;
	_inflationGrowthRate = new GrowthRate(_InflationGrowthRateName, inflationProportion);
	final double investmentsProportion = getMiscellaneousData(_InvestmentsGrowthRateName)._d;
	_investmentsGrowthRate = new GrowthRate(_InvestmentsGrowthRateName, investmentsProportion);
	_perCentLong = getMiscellaneousData("% that's Long")._d;
	_medicareTaxThreshold = getMiscellaneousData("Medicare Tax Threshold")._d;
	_additionalMedicareTaxPerCent = 100d * getMiscellaneousData("Additional Medicare Tax")._d;
	_medicareTaxPerCentOnInvestments = 100d * getMiscellaneousData("Additional Medicare Tax on Investments")._d;

	final Line[] ownerLines = getBlock(staticsSheetName, "Owners")._lines;
	final int nLines0 = ownerLines.length;
	_owners = new Owner[nLines0];
	for (int k = 0; k < nLines0; ++k) {
	    _owners[k] = new Owner(ownerLines[k]);
	}
	Arrays.sort(_owners);
	final ArrayList<Account> jointList = new ArrayList<>();
	final Line[] allAccntDefnLines = getBlock(staticsSheetName, "Account Owners")._lines;
	final int nAccntDefns = allAccntDefnLines.length;
	for (int k = 0; k < nAccntDefns; ++k) {
	    final Line accntDefnLine = allAccntDefnLines[k];
	    final Field accntDefnLineData = accntDefnLine._data;
	    final String accountOwnerName = accntDefnLineData._s;
	    if (accountOwnerName == null || accountOwnerName.length() == 0) {
		/** This is a Joint account. */
		final String accntName = accntDefnLine._header._s;
		jointList.add(new Account(accntName, null));
	    }
	}
	_jointAccounts = jointList.toArray(new Account[jointList.size()]);

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

	/** Build _investmentItems and check for duplicates. */
	final HashSet<String> labels = new HashSet<>();
	final int nInvestmentItems = InvestmentsEnum._Values.length;
	_investmentItems = new InvestmentItem[nInvestmentItems];
	final int sheetIdx = Arrays.binarySearch(_sheetAndBlocksS, new NamedEntity(_SheetNames[_InvestmentsSheetIdx]));
	final Block[] investmentBlocks = _sheetAndBlocksS[sheetIdx]._blocks;
	final int nInvestmentBlocks = investmentBlocks.length;
	for (int k0 = 0; k0 < nInvestmentBlocks; ++k0) {
	    final Block investmentBlock = investmentBlocks[k0];
	    final Line[] investmentLines = investmentBlock._lines;
	    final int nInvestmentLines = investmentLines.length;
	    for (int k1 = 0; k1 < nInvestmentLines; ++k1) {
		final Line investmentLine = investmentLines[k1];
		final String originalName = investmentLine._header._s;
		if (!labels.add(originalName)) {
		    System.err.println(String.format("%s from Block %s is a duplicate.", //
			    investmentLine.getString(), investmentBlock._name));
		} else {
		    final InvestmentsEnum investmentsEnum = InvestmentsEnum._ReverseMap.get(originalName);
		    _investmentItems[investmentsEnum.ordinal()] = new InvestmentItem(investmentsEnum, //
			    investmentLines[k1]._data._d);
		}
	    }
	}

	final int nYears = _finalYear - currentYear + 1;
	_taxYears = new TaxYear[nYears];
	for (int year = currentYear; year <= _finalYear; ++year) {
	    _taxYears[year - currentYear] = new TaxYear(this, year);
	}
    }

    public Block getBlock(final String sheetName, final String blockName) {
	final int sheetIdx = Arrays.binarySearch(_sheetAndBlocksS, new NamedEntity(sheetName));
	if (sheetIdx < 0) {
	    return null;
	}
	final Block[] blocks = _sheetAndBlocksS[sheetIdx]._blocks;
	final int blockIdx = Arrays.binarySearch(blocks, new NamedEntity(blockName));
	return blockIdx < 0 ? null : blocks[blockIdx];
    }

    public Line getLine(final String sheetName, final String blockName, final String s) {
	final Block block = getBlock(sheetName, blockName);
	if (block == null) {
	    return null;
	}
	final Line[] lines = block._lines;
	final int lineIdx = Arrays.binarySearch(lines, new Line(s));
	return lineIdx < 0 ? null : lines[lineIdx];
    }

    public Field getMiscellaneousData(final String s) {
	return getLine(_SheetNames[_StaticsSheetIdx], "Miscellaneous Data", s)._data;
    }

    public int getCurrentYear() {
	return MyStudiesDateUtils.getAPartOfADate(_currentDate, ChronoField.YEAR);
    }

    public String getString() {
	String s = String.format("Crrnt Yr[%d], Crrnt Dt[%s], %%of CrrntYr Left[%s], Fnl Yr[%d], CrrntMxCGLoss[%s]", //
		getCurrentYear(), MyStudiesDateUtils.formatDateOnly(_currentDate), //
		TypeOfDouble.PER_CENT.format(_perCentLeftOfCurrentYear, 2), //
		_finalYear, //
		TypeOfDouble.MONEY.format(_maxCapitalGainsLoss, 2) //
	);
	s += String.format(//
		"\nStdDdctnCrrntYr[%s], MdcrPrtBPrmmCrrntYr[%s], %% that's Long[%s]", //
		TypeOfDouble.MONEY.format(_standardDeductionCurrentYear, 2), //
		TypeOfDouble.MONEY.format(_partBPremiumCurrentYear, 2), //
		TypeOfDouble.PER_CENT.format(_perCentLong, 2));
	s += String.format(//
		"\nMdcrTxThrshld[%s], AddtnlMdcrTx[%s] MdcrTxPerCentOnInvstmnts[%s]", //
		TypeOfDouble.MONEY.format(_medicareTaxThreshold, 2), //
		TypeOfDouble.PER_CENT.format(_additionalMedicareTaxPerCent, 1),
		TypeOfDouble.PER_CENT.format(_medicareTaxPerCentOnInvestments, 1));
	final int nOwners = _owners.length;
	for (int k = 0; k < nOwners; ++k) {
	    s += String.format("\n\n%d. %s", k, _owners[k].getString());
	}
	final int nJointAccounts = _jointAccounts.length;
	for (int k = 0; k < nJointAccounts; ++k) {
	    if (k == 0) {
		s += "\n";
	    }
	    s += String.format("\n%s", _jointAccounts[k].getString());
	}
	s += String.format("\n\n%s", _inflationGrowthRate.getString());
	s += String.format("\n%s", _investmentsGrowthRate.getString());
	final int nBracketsS = _BracketsNames.length;
	for (int k = 0; k < nBracketsS; ++k) {
	    s += String.format("\n\n%s", _bracketsS[k]);
	}
	if (true) {
	    s += "\n\nInvestmentItems";
	    final int nInvestmentItems = _investmentItems.length;
	    for (int k = 0; k < nInvestmentItems; ++k) {
		s += String.format("\n%03d. %s", k, _investmentItems[k].getString());
	    }
	} else {
	    final int investmentSheetIdx = Arrays.binarySearch(_sheetAndBlocksS, new NamedEntity("Investments"));
	    final Block[] investmentBlocks = _sheetAndBlocksS[investmentSheetIdx]._blocks;
	    final int nInvestmentBlocks = investmentBlocks.length;
	    for (int k = 0; k < nInvestmentBlocks; ++k) {
		final Block block = investmentBlocks[k];
		s += "\n\n" + block.getString();
	    }
	}
	final int nYearsOfData = _taxYears.length;
	for (int k = 0; k < nYearsOfData; ++k) {
	    final TaxYear yod = _taxYears[k];
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

    public TaxYear.Owner1 getOwner1(final Owner owner, final int thisYear) {
	final int currentYear = getCurrentYear();
	if (thisYear < currentYear || thisYear > _finalYear) {
	    return null;
	}
	final TaxYear taxYear = _taxYears[thisYear - currentYear];
	final TaxYear.Owner1[] owner1s = taxYear._owner1s;
	final int idx = Arrays.binarySearch(owner1s, new NamedEntity(owner._name, thisYear));
	if (idx < 0) {
	    return null;
	}
	return taxYear._owner1s[idx];
    }

    public TaxYear.Account1 getAccount1(final Account account, final int thisYear) {
	final TaxYear.Owner1 owner1 = getOwner1(account._owner, thisYear);
	if (owner1 == null) {
	    return null;
	}
	final TaxYear.Account1[] account1s = owner1._myAccounts1;
	final int idx = Arrays.binarySearch(account1s, new NamedEntity(account._name, thisYear));
	if (idx < 0) {
	    return null;
	}
	return owner1._myAccounts1[idx];
    }

    public TaxYear.Owner1.OutsideIncome1 getOi1(final Owner.OutsideIncome outsideIncome, final int thisYear) {
	final TaxYear.Owner1 owner1 = getOwner1(outsideIncome.getOwner(), thisYear);
	if (owner1 == null) {
	    return null;
	}
	final TaxYear.Account1[] myAccount1s = owner1._myAccounts1;
	final int idx = Arrays.binarySearch(myAccount1s, new NamedEntity(outsideIncome._name, thisYear));
	if (idx < 0) {
	    return null;
	}
	return owner1._oi1s[idx];
    }

}
