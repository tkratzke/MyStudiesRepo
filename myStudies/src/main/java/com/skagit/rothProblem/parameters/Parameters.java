package com.skagit.rothProblem.parameters;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.skagit.rothProblem.GrowthRate;
import com.skagit.rothProblem.workBookConcepts.Block;
import com.skagit.rothProblem.workBookConcepts.BlocksFromSheet;
import com.skagit.rothProblem.workBookConcepts.Line;
import com.skagit.rothProblem.workBookConcepts.WorkBookConcepts;
import com.skagit.util.MyStudiesStringUtils;
import com.skagit.util.NamedEntity;

public class Parameters {

    final public static String _ParametersSheetName = "Current Year Parameters";
    final public static Parameters _CurrentYearParameters;
    static {
	_CurrentYearParameters = new Parameters();
    }

    final public int _currentYear;
    final public int _finalYear;
    final public double _standardDeduction;
    final public double _partBStandardPremium;
    final public double _maxCapitalGainsLoss;
    final public double _medicareTaxThreshold;
    final public TaxTable[] _taxTables;
    /** Constants. */
    final public double _additionalMedicareTaxPerCent;
    final public double _additionalMedicareTaxOnInvestmentsPerCent;
    final public GrowthRate _inflationGrowthRate;
    final public int _firstLifeExpectancyAge;
    final public double[] _lifeExpectancies;

    public Parameters() {
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
	final int nBlocks = blocks.length;
	/** Miscellaneous Data. */
	final BitSet usedBlocks = new BitSet(nBlocks);
	final String miscellaneousConstantsBlockName = "Miscellaneous Constants";
	final int idx0 = Arrays.binarySearch(blocks, new NamedEntity(miscellaneousConstantsBlockName));
	usedBlocks.set(idx0);
	_currentYear = (int) Math
		.round(WorkBookConcepts.getDouble(blocks, miscellaneousConstantsBlockName, "Current Year"));
	_finalYear = (int) Math
		.round(WorkBookConcepts.getDouble(blocks, miscellaneousConstantsBlockName, "Final Year"));
	_additionalMedicareTaxPerCent = WorkBookConcepts.getDouble(blocks, miscellaneousConstantsBlockName,
		"Additional Medicare Tax") * 100d;
	_additionalMedicareTaxOnInvestmentsPerCent = WorkBookConcepts.getDouble(blocks, miscellaneousConstantsBlockName,
		"Additional Medicare Tax on Investments") * 100d;
	final String inflationString = "Inflation";
	final double inflationProportion = WorkBookConcepts.getDouble(blocks, miscellaneousConstantsBlockName,
		inflationString);
	_inflationGrowthRate = new GrowthRate(inflationString, inflationProportion);
	/** Miscellaneous Dollar Amounts. */
	final String miscellaneousDollarAmountsBlockName = "Miscellaneous Dollar Amounts";
	final int idx1 = Arrays.binarySearch(blocks, new NamedEntity(miscellaneousDollarAmountsBlockName));
	usedBlocks.set(idx1);
	_standardDeduction = WorkBookConcepts.getDouble(blocks, miscellaneousDollarAmountsBlockName,
		"Standard Deduction");
	_partBStandardPremium = WorkBookConcepts.getDouble(blocks, miscellaneousDollarAmountsBlockName,
		"Part B Standard Premium");
	_maxCapitalGainsLoss = WorkBookConcepts.getDouble(blocks, miscellaneousDollarAmountsBlockName,
		"Max Capital Gains Loss");
	_medicareTaxThreshold = WorkBookConcepts.getDouble(blocks, miscellaneousDollarAmountsBlockName,
		"Medicare Tax Threshold");
	/** Life Expectancies. */
	final int idx2 = Arrays.binarySearch(blocks, new NamedEntity("Life Expectancies"));
	usedBlocks.set(idx2);
	final Block block2 = blocks[idx2];
	final Line[] lines2 = block2._lines;
	final int nLines2 = lines2.length;
	_firstLifeExpectancyAge = (int) Math.round(lines2[0]._header._d);
	_lifeExpectancies = new double[nLines2];
	for (int k = 0; k < nLines2; ++k) {
	    _lifeExpectancies[k] = lines2[k]._data._d;
	}
	/** Blocks that are not marked as used, are Tax Tables. */
	final int nTaxTables = nBlocks - usedBlocks.cardinality();
	_taxTables = new TaxTable[nTaxTables];
	for (int k0 = usedBlocks.nextClearBit(0), k1 = 0; k0 < nBlocks; k0 = usedBlocks.nextClearBit(k0 + 1)) {
	    usedBlocks.set(k0);
	    _taxTables[k1++] = new TaxTable(blocks[k0]);
	}
    }

    public String getString() {
	String s = String.format("Parameters: CurrentYear[%d] to FinalYear[%d]:", _currentYear, _finalYear);
	s += String.format("\nStd Ded=%s", MyStudiesStringUtils.formatDollars(_standardDeduction));
	s += String.format("\nPart B Std Prmm=%s.", MyStudiesStringUtils.formatDollars(_partBStandardPremium));
	s += String.format("\nMxCptlGnsLss=%s.", MyStudiesStringUtils.formatDollars(_maxCapitalGainsLoss));
	s += String.format("\nMdcrTxThrshld=%s.", MyStudiesStringUtils.formatDollars(_medicareTaxThreshold));
	s += String.format("\nAddtnl MdcrTx=%s.", MyStudiesStringUtils.formatPerCent(_additionalMedicareTaxPerCent, 1));
	s += String.format("\nAddtnl MdcrTx On Invstmnts=%s.",
		MyStudiesStringUtils.formatPerCent(_additionalMedicareTaxOnInvestmentsPerCent, 1));
	s += String.format("\n%s", _inflationGrowthRate.getString());
	s += String.format("\nFrstLfExpctncyAg=%s: ", MyStudiesStringUtils.formatOther(_firstLifeExpectancyAge, 1));
	s += String.format("[");
	final int nLfExpctncies = _lifeExpectancies.length;
	for (int k = 0; k < nLfExpctncies; ++k) {
	    if (k > 0) {
		s += ",";
	    }
	    s += MyStudiesStringUtils.formatOther(_lifeExpectancies[k], 1);
	}
	s += String.format("]");

	final int nTaxTables = _taxTables.length;
	for (int k = 0; k < nTaxTables; ++k) {
	    s += String.format("\n\n%s", _taxTables[k].getString());
	}
	return s;
    }

    @Override
    public String toString() {
	return getString();
    }

    public static void main(final String[] args) {
	System.out.println(_CurrentYearParameters.getString());
    }
}
