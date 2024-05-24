package com.skagit.roth.currentYear;

import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.skagit.roth.rothCalculator.Brackets;
import com.skagit.roth.rothCalculator.InvestmentItem;
import com.skagit.roth.rothCalculator.InvestmentsEnum;
import com.skagit.roth.rothCalculator.RothCalculator;
import com.skagit.roth.workBookConcepts.Block;
import com.skagit.roth.workBookConcepts.Field;
import com.skagit.roth.workBookConcepts.Line;
import com.skagit.roth.workBookConcepts.WorkBookConcepts;
import com.skagit.util.MyStudiesDateUtils;
import com.skagit.util.NamedEntity;
import com.skagit.util.TypeOfDouble;

public class CurrentYear {

    public final Date _currentDate;
    public final double _standardDeductionCurrentYear;
    public final double _partBPremiumCurrentYear;
    public final double _maxCapitalGainsLossCurrentYear;
    public final double _medicareTaxThresholdCurrentYear;
    public final Brackets[] _bracketsCurrentYear;
    public final double _perCentLeftOfCurrentYear;

    public final Owner0[] _owner0s;
    public final Account0[] _jointAccounts;

    public CurrentYear(final WorkBookConcepts workBookConcepts) {
	/** Read in Brackets and Life Expectancies. */
	final int nBracketsS = RothCalculator._BracketsNames.length;
	_bracketsCurrentYear = new Brackets[nBracketsS];
	for (int k = 0; k < nBracketsS; ++k) {
	    _bracketsCurrentYear[k] = new Brackets(workBookConcepts, RothCalculator._BracketsNames[k]);
	}
	Arrays.sort(_bracketsCurrentYear);

	/**
	 * Read in the Statics sheet, defining the Owners, their Accounts (IRAs), and
	 * the Joint Accounts.
	 */
	final String staticsSheetName = WorkBookConcepts.getSheetName(WorkBookConcepts._StaticsSheetIdx);
	_currentDate = workBookConcepts.getMiscellaneousData("Current Date").getDate();
	final int currentYear = getCurrentYear();
	final Date thisJan1 = MyStudiesDateUtils.parseDate(String.format("%d-1-1", currentYear));
	final Date nextJan1 = MyStudiesDateUtils.parseDate(String.format("%d-1-1", currentYear + 1));
	final double d0 = MyStudiesDateUtils.getDateDiff(thisJan1, _currentDate, TimeUnit.DAYS);
	final double d1 = MyStudiesDateUtils.getDateDiff(thisJan1, nextJan1, TimeUnit.DAYS);
	_perCentLeftOfCurrentYear = 100d * (d1 - d0) / d1;
	_standardDeductionCurrentYear = workBookConcepts.getMiscellaneousData("Standard Deduction Current Year")._d;
	_partBPremiumCurrentYear = workBookConcepts.getMiscellaneousData("Part B Premium Current Year")._d;
	_maxCapitalGainsLossCurrentYear = workBookConcepts.getMiscellaneousData("Max Capital Gains Loss")._d;
	_medicareTaxThresholdCurrentYear = workBookConcepts.getMiscellaneousData("Medicare Tax Threshold")._d;

	final Line[] ownerLines = workBookConcepts.getBlock(staticsSheetName, "Owners")._lines;
	final int nLines0 = ownerLines.length;
	_owner0s = new Owner0[nLines0];
	for (int k = 0; k < nLines0; ++k) {
	    _owner0s[k] = new Owner0(ownerLines[k]);
	}
	Arrays.sort(_owner0s);

	final ArrayList<Account0> jointList = new ArrayList<>();
	final Line[] allAccntDefnLines = workBookConcepts.getBlock(staticsSheetName, "Account Owners")._lines;
	final int nAccntDefns = allAccntDefnLines.length;
	for (int k = 0; k < nAccntDefns; ++k) {
	    final Line accntDefnLine = allAccntDefnLines[k];
	    final Field accntDefnLineData = accntDefnLine._data;
	    final String accountOwnerName = accntDefnLineData._s;
	    if (accountOwnerName == null || accountOwnerName.length() == 0) {
		/** This is a Joint account. */
		final String accntName = accntDefnLine._header._s;
		jointList.add(new Account0(accntName, workBookConcepts, /* owner= */null));
	    }
	}

	/** Read in the Joint Accounts' Investment Information. */
	final int nJointAccounts = jointList.size();
	_jointAccounts = jointList.toArray(new Account0[nJointAccounts]);
	Arrays.sort(_jointAccounts);
	final int sheetIdx = Arrays.binarySearch(workBookConcepts._sheetAndBlocksS,
		new NamedEntity(WorkBookConcepts._SheetNames[WorkBookConcepts._InvestmentsSheetIdx]));
	final Block[] investmentBlocks = workBookConcepts._sheetAndBlocksS[sheetIdx]._blocks;
	final int nInvestmentBlocks = investmentBlocks.length;
	for (int k0 = 0; k0 < nJointAccounts; ++k0) {
	    final Account0 jointAccount = _jointAccounts[k0];
	    final String jointName = jointAccount._name;
	    final ArrayList<Block> myBlocks = new ArrayList<>();
	    for (int k1 = 0; k1 < nInvestmentBlocks; ++k1) {
		final Block block = investmentBlocks[k1];
		final String blockName = block._name;
		if (blockName.contains(jointName)) {
		    myBlocks.add(block);
		}
	    }
	    final int nMyBlocks = myBlocks.size();
	    for (int k2 = 0; k2 < nMyBlocks; ++k2) {
		final Block block = myBlocks.get(k2);
		final Line[] investmentLines = block._lines;
		final int nInvestmentLines = investmentLines.length;
		for (int k3 = 0; k3 < InvestmentsEnum._Values.length; ++k3) {
		    jointAccount._investmentItems[k3] = new InvestmentItem(InvestmentsEnum._Values[k3], //
			    0d);
		}
		for (int k5 = 0; k5 < nInvestmentLines; ++k5) {
		    /** If two Lines have the same header, the second one trumps. */
		    final Line line = investmentLines[k5];
		    final String originalName = line._header._s;
		    final double dollars = line._data._d;
		    if (originalName != null && Double.isFinite(dollars)) {
			final InvestmentsEnum investmentsEnum = InvestmentsEnum._ReverseMap.get(originalName);
			if (investmentsEnum != null) {
			    final int k3 = investmentsEnum.ordinal();
			    jointAccount._investmentItems[k3] = new InvestmentItem(investmentsEnum, //
				    investmentLines[k5]._data._d);
			}
		    }
		}
	    }
	}
    }

    public int getCurrentYear() {
	return MyStudiesDateUtils.getAPartOfADate(_currentDate, ChronoField.YEAR);
    }

    public String getString() {
	String s = String.format("Crrnt Yr[%d], Crrnt Dt[%s], %%of CrrntYr Left[%s], CrrntMxCGLoss[%s]", //
		getCurrentYear(), MyStudiesDateUtils.formatDateOnly(_currentDate), //
		TypeOfDouble.PER_CENT.format(_perCentLeftOfCurrentYear, 2), //
		TypeOfDouble.MONEY.format(_maxCapitalGainsLossCurrentYear, 2) //
	);
	s += String.format(//
		"\nStdDdctnCrrntYr[%s], MdcrPrtBPrmmCrrntYr[%s]", //
		TypeOfDouble.MONEY.format(_standardDeductionCurrentYear, 2), //
		TypeOfDouble.MONEY.format(_partBPremiumCurrentYear, 2));
	s += String.format(//
		"\nMdcrTxThrshldCrrntYr[%s]", //
		TypeOfDouble.MONEY.format(_medicareTaxThresholdCurrentYear, 2));
	final int nOwner0s = _owner0s.length;
	for (int k = 0; k < nOwner0s; ++k) {
	    s += String.format("\n\n%d. %s", k, _owner0s[k].getString());
	}
	final Account0[] jointAccounts = _jointAccounts;
	final int nJointAccounts = jointAccounts.length;
	for (int k = 0; k < nJointAccounts; ++k) {
	    s += String.format("\n\n%s", jointAccounts[k].getString());
	}
	final int nBracketsS = _bracketsCurrentYear.length;
	for (int k = 0; k < nBracketsS; ++k) {
	    s += String.format("\n\n%s", _bracketsCurrentYear[k]);
	}
	return s;
    }

    @Override
    public String toString() {
	return getString();
    }

}
