package com.skagit.rothProblem.parameters;

import java.util.ArrayList;
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

    final public int _thisYear;
    final public int _finalYear;
    final public double _standardDeduction;
    final public double _partBStandardPremium;
    final public double _maxCgLoss;
    final public double _medicareTaxThreshold;
    final public TaxTable[] _taxTables;
    /** Constants. */
    final public double _additionalMedicareTaxPerCent;
    final public double _additionalMedicareTaxOnInvestmentsPerCent;
    final public GrowthRate _inflationGrowthRate;
    final public int _firstLifeExpectancyAge;
    final public double[] _lifeExpectancies;

    public Parameters(final XSSFWorkbook workBook, final FormulaEvaluator formulaEvaluator,
	    final String parametersSheetName) {
	final BlocksFromSheet blocksFromSheet = new BlocksFromSheet(workBook.getSheet(parametersSheetName),
		formulaEvaluator);
	final Block[] blocks = blocksFromSheet._blocks;
	final int nBlocks = blocks.length;
	/** Miscellaneous Data. */
	final BitSet usedBlocks = new BitSet(nBlocks);
	final String miscellaneousConstantsBlockName = "Miscellaneous Constants";
	final int idx0 = Arrays.binarySearch(blocks, new NamedEntity(miscellaneousConstantsBlockName));
	usedBlocks.set(idx0);
	_thisYear = (int) Math
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
	_maxCgLoss = WorkBookConcepts.getDouble(blocks, miscellaneousDollarAmountsBlockName, "Max Capital Gains Loss");
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
	    _taxTables[k1++] = new TaxTable(_thisYear, blocks[k0]);
	}
    }

    /**
     * Creates a new set of Parameters for the year after parameters._thisYear. This
     * is NOT a copy ctor.
     */
    public Parameters(final Parameters parameters) {
	_thisYear = parameters._thisYear + 1;
	_finalYear = parameters._finalYear;
	_inflationGrowthRate = parameters._inflationGrowthRate;
	final double inflationFactor = Math.exp(_inflationGrowthRate._expGrowthRate);
	_standardDeduction = parameters._standardDeduction * inflationFactor;
	_partBStandardPremium = parameters._partBStandardPremium * inflationFactor;
	_maxCgLoss = parameters._maxCgLoss * inflationFactor;
	_medicareTaxThreshold = parameters._medicareTaxThreshold * inflationFactor;
	_additionalMedicareTaxPerCent = parameters._additionalMedicareTaxPerCent;
	_additionalMedicareTaxOnInvestmentsPerCent = parameters._additionalMedicareTaxOnInvestmentsPerCent;
	_firstLifeExpectancyAge = parameters._firstLifeExpectancyAge;
	_lifeExpectancies = parameters._lifeExpectancies;
	final TaxTable[] taxTables = parameters._taxTables;
	final int nTaxTables = taxTables.length;
	final BitSet usedTaxTables = new BitSet(nTaxTables);
	final ArrayList<TaxTable> list = new ArrayList<>();
	for (int k0 = usedTaxTables.nextClearBit(0); k0 < nTaxTables; k0 = usedTaxTables.nextClearBit(k0 + 1)) {
	    final TaxTable taxTable = taxTables[k0];
	    final String name = taxTable._name;
	    final PerCentCeiling[] perCentCeilings = taxTable._perCentCeilings;
	    final int nPerCentCeilings = perCentCeilings.length;
	    final PerCentCeiling[] newPerCentCeilings = new PerCentCeiling[nPerCentCeilings];
	    final TaxTable nextTaxTable = k0 < (nTaxTables - 1) ? taxTables[k0 + 1] : null;
	    if (nextTaxTable != null && nextTaxTable._name.equals(name + " " + _thisYear)) {
		usedTaxTables.set(k0 + 1);
		final PerCentCeiling[] nextPerCentCeilings = nextTaxTable._perCentCeilings;
		final int nNextPerCentCeilings = nextPerCentCeilings.length;
		if (nNextPerCentCeilings == nPerCentCeilings) {
		    for (int k1 = 0; k1 < nPerCentCeilings; ++k1) {
			newPerCentCeilings[k1] = new PerCentCeiling(nextPerCentCeilings[k1]._perCent,
				perCentCeilings[k1]._ceiling * inflationFactor);
		    }
		    usedTaxTables.set(k0);
		}
	    }
	    if (!usedTaxTables.get(k0)) {
		for (int k1 = 0; k1 < nPerCentCeilings; ++k1) {
		    newPerCentCeilings[k1] = new PerCentCeiling(perCentCeilings[k1]._perCent,
			    perCentCeilings[k1]._ceiling * inflationFactor);
		}
		usedTaxTables.set(k0);
	    }
	    list.add(new TaxTable(name, _thisYear, newPerCentCeilings));
	}
	_taxTables = list.toArray(new TaxTable[list.size()]);
	Arrays.sort(_taxTables);

    }

    public String getString() {
	String s = String.format("Parameters: CurrentYear[%d] to FinalYear[%d]:", _thisYear, _finalYear);
	s += String.format("\nStd Ded=%s", MyStudiesStringUtils.formatDollars(_standardDeduction));
	s += String.format("\nPart B Std Prmm=%s.", MyStudiesStringUtils.formatDollars(_partBStandardPremium));
	s += String.format("\nMxCptlGnsLss=%s.", MyStudiesStringUtils.formatDollars(_maxCgLoss));
	s += String.format("\nMdcrTxThrshld=%s.", MyStudiesStringUtils.formatDollars(_medicareTaxThreshold));
	s += String.format("\nAddtnl MdcrTx=%s.", MyStudiesStringUtils.formatPerCent(_additionalMedicareTaxPerCent, 1));
	s += String.format("\nAddtnl MdcrTx On Invstmnts=%s.",
		MyStudiesStringUtils.formatPerCent(_additionalMedicareTaxOnInvestmentsPerCent, 1));
	s += String.format("\n%s", _inflationGrowthRate.getString());
	s += String.format("\nLife Expectancies:");
	final int nLfExpctncies = _lifeExpectancies.length;
	for (int k = 0; k < nLfExpctncies; ++k) {
	    s += (k % 8 == 0) ? "\n\t" : ",";
	    s += String.format("(%s:%s)", MyStudiesStringUtils.formatOther(_firstLifeExpectancyAge + k, 0),
		    MyStudiesStringUtils.formatOther(_lifeExpectancies[k], 1));
	}
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
}
