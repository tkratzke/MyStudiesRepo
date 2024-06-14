package com.skagit.roth.parameters;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.skagit.roth.workBookConcepts.Block;
import com.skagit.roth.workBookConcepts.BlocksFromSheet;
import com.skagit.roth.workBookConcepts.Line;
import com.skagit.util.MyStudiesDateUtils;
import com.skagit.util.MyStudiesStringUtils;
import com.skagit.util.NamedEntity;

public class Parameters {

    final public static String _ParametersSheetName = "Parameters";
    final public static Date _CurrentDate;
    final public static int _CurrentDateYear;
    final public static int _FinalYear;
    final public static double _StandardDeductionCurrentYear;
    final public static double _PartBStandardPremiumCurrentYear;
    final public static double _MaxCapitalGainsLossCurrentYear;
    final public static double _MedicareTaxThresholdCurrentYear;
    final public static double _AdditionalMedicareTaxPerCent;
    final public static double _AdditionalMedicareTaxOnInvestmentsPerCent;
    final public static GrowthRate _InflationGrowthRate;
    final public static GrowthRate _InvestmentsGrowthRate;
    final public static int _FirstLifeExpectancyAge;
    final public static double[] _LifeExpectancies;
    final public static TaxTable[] _TaxTables;

    static {
	FormulaEvaluator formulaEvaluator = null;
	BlocksFromSheet blocksFromSheet = null;
	Block[] blocks = null;
	final String filePath = System.getProperty("ParametersFile") + ".xlsx";
	try (XSSFWorkbook workBook = new XSSFWorkbook(new FileInputStream(filePath))) {
	    formulaEvaluator = workBook.getCreationHelper().createFormulaEvaluator();
	    blocksFromSheet = new BlocksFromSheet(workBook.getSheet(_ParametersSheetName), formulaEvaluator);
	    blocks = blocksFromSheet._blocks;
	} catch (final IOException e) {
	    e.printStackTrace();
	}
	/** Miscellaneous Data. */
	final int nBlocks = blocks.length;
	final BitSet usedBlocks = new BitSet(nBlocks);
	final int idx0 = Arrays.binarySearch(blocks, new NamedEntity("Miscellaneous Parameters"));
	usedBlocks.set(idx0);
	final Block block0 = blocks[idx0];
	final Line[] lines0 = block0._lines;
	final int idx0A = Arrays.binarySearch(lines0, new Line("Current Date"));
	_CurrentDate = lines0[idx0A]._data.getDate();
	_CurrentDateYear = MyStudiesDateUtils.getAPartOfADate(_CurrentDate, ChronoField.YEAR);
	final int idx0B = Arrays.binarySearch(lines0, new Line("Final Year"));
	_FinalYear = (int) Math.round(lines0[idx0B]._data._d);
	final int idx0C = Arrays.binarySearch(lines0, new Line("Standard Deduction Current Year"));
	_StandardDeductionCurrentYear = lines0[idx0C]._data._d;
	final int idx0D = Arrays.binarySearch(lines0, new Line("Part B Standard Premium Current Year"));
	_PartBStandardPremiumCurrentYear = lines0[idx0D]._data._d;
	final int idx0E = Arrays.binarySearch(lines0, new Line("Max Capital Gains Loss Current Year"));
	_MaxCapitalGainsLossCurrentYear = lines0[idx0E]._data._d;
	final int idx0F = Arrays.binarySearch(lines0, new Line("Medicare Tax Threshold Current Year"));
	_MedicareTaxThresholdCurrentYear = lines0[idx0F]._data._d;
	final int idx0G = Arrays.binarySearch(lines0, new Line("Additional Medicare Tax"));
	_AdditionalMedicareTaxPerCent = lines0[idx0G]._data._d * 100d;
	final int idx0H = Arrays.binarySearch(lines0, new Line("Additional Medicare Tax on Investments"));
	_AdditionalMedicareTaxOnInvestmentsPerCent = lines0[idx0H]._data._d * 100d;
	/** SWAGs. */
	final int idx1 = Arrays.binarySearch(blocks, new NamedEntity("SWAGs"));
	usedBlocks.set(idx1);
	final Block block1 = blocks[idx1];
	final Line[] lines1 = block1._lines;
	final String inflationString = "Inflation";
	final int idx1A = Arrays.binarySearch(lines1, new Line(inflationString));
	final double inflationProportion = lines1[idx1A]._data._d;
	_InflationGrowthRate = new GrowthRate(inflationString, inflationProportion);
	final String investmentsGrowthString = "Investments Growth Rate";
	final int idx1B = Arrays.binarySearch(lines1, new Line(investmentsGrowthString));
	final double investmentsGrowthRateProportion = lines1[idx1B]._data._d;
	_InvestmentsGrowthRate = new GrowthRate(investmentsGrowthString, investmentsGrowthRateProportion);
	/** Life Expectancies. */
	final int idx2 = Arrays.binarySearch(blocks, new NamedEntity("Life Expectancies"));
	usedBlocks.set(idx2);
	final Block block2 = blocks[idx2];
	final Line[] lines2 = block2._lines;
	final int nLines2 = lines2.length;
	_FirstLifeExpectancyAge = (int) Math.round(lines2[0]._header._d);
	_LifeExpectancies = new double[nLines2];
	for (int k = 0; k < nLines2; ++k) {
	    _LifeExpectancies[k] = lines2[k]._data._d;
	}
	/** Blocks that are not marked as used, are Tax Tables. */
	final int nTaxTables = nBlocks - usedBlocks.cardinality();
	_TaxTables = new TaxTable[nTaxTables];
	for (int k0 = usedBlocks.nextClearBit(0), k1 = 0; k0 < nBlocks; k0 = usedBlocks.nextClearBit(k0 + 1)) {
	    usedBlocks.set(k0);
	    _TaxTables[k1++] = new TaxTable(blocks[k0]);
	}
    }

    public static String getString() {
	String s = String.format("%s(%d) to %d", MyStudiesDateUtils.formatDateOnly(_CurrentDate), _CurrentDateYear,
		_FinalYear);
	s += String.format("\n\tStd Ded=%s", MyStudiesStringUtils.formatDollars(_StandardDeductionCurrentYear));
	s += String.format("\n\tPart B Std Prmm CrrntYr=%s.",
		MyStudiesStringUtils.formatDollars(_PartBStandardPremiumCurrentYear));
	s += String.format("\n\tMxCptlGnsLss=%s.", MyStudiesStringUtils.formatDollars(_MaxCapitalGainsLossCurrentYear));
	s += String.format("\n\tMdcrTxThrshld=%s.",
		MyStudiesStringUtils.formatDollars(_MedicareTaxThresholdCurrentYear));
	s += String.format("\n\tAddtnl MdcrTx=%s.",
		MyStudiesStringUtils.formatPerCent(_AdditionalMedicareTaxPerCent, 1));
	s += String.format("\n\tAddtnl MdcrTx On Invstmnts=%s.",
		MyStudiesStringUtils.formatPerCent(_AdditionalMedicareTaxOnInvestmentsPerCent, 1));
	s += String.format("\n\t%s", _InflationGrowthRate.getString());
	s += String.format("\n\t%s.", _InvestmentsGrowthRate.getString());
	s += String.format("\n\tFrstLfExpctncyAg=%s: ", MyStudiesStringUtils.formatOther(_FirstLifeExpectancyAge, 1));
	s += String.format("[");
	final int nLfExpctncies = _LifeExpectancies.length;
	for (int k = 0; k < nLfExpctncies; ++k) {
	    if (k > 0) {
		s += ",";
	    }
	    s += MyStudiesStringUtils.formatOther(_LifeExpectancies[k], 1);
	}
	s += String.format("]");

	final int nTaxTables = _TaxTables.length;
	for (int k = 0; k < nTaxTables; ++k) {
	    s += String.format("\n\n%s", _TaxTables[k].getString());
	}
	return s;
    }

    public static double getInflationMultiplier(final int year) {
	return _InflationGrowthRate.getMultiplier(year);
    }

    public static void main(final String[] args) {
	System.out.println(getString());
    }
}
