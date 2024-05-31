package com.skagit.roth.baseYear;

import java.util.Arrays;

import com.skagit.roth.rothCalculator.InvestmentItem;
import com.skagit.roth.rothCalculator.InvestmentsEnum;
import com.skagit.roth.workBookConcepts.Block;
import com.skagit.roth.workBookConcepts.Line;
import com.skagit.roth.workBookConcepts.WorkBookConcepts;
import com.skagit.util.MyStudiesStringUtils;
import com.skagit.util.NamedEntity;
import com.skagit.util.TypeOfDouble;

public class Account0 extends NamedEntity {

    public final double _balanceBeginningOfBaseYear;
    public final double _baseBalance;
    /** If it's an IRA: */
    public final Owner0 _owner;
    /** If non-inherited IRA */
    public final double _ageOfRmd;
    /** If inherited IRA */
    public final double _baseDivisorForInhIra;
    /** If Joint: */
    public final InvestmentItem[] _investmentItems;

    public Account0(final String name, final WorkBookConcepts workBookConcepts, final Owner0 owner) {
	super(name);
	_owner = owner;
	final Line forLookUp = new Line(_name);
	final String staticsSheetName = WorkBookConcepts.getSheetName(WorkBookConcepts._StaticsSheetIdx);
	if (_owner != null) {
	    final Line[] ageBlockLines = workBookConcepts.getBlock(staticsSheetName, "Age of RMD")._lines;
	    final int idx0 = Arrays.binarySearch(ageBlockLines, forLookUp);
	    if (idx0 >= 0) {
		_ageOfRmd = ageBlockLines[idx0]._data._d;
		_baseDivisorForInhIra = Double.NaN;
	    } else {
		_ageOfRmd = Double.NaN;
		final Line[] divisorLines = workBookConcepts.getBlock(staticsSheetName,
			"Inherited IRA Divisor for Base Year")._lines;
		final int idx0a = Arrays.binarySearch(divisorLines, forLookUp);
		_baseDivisorForInhIra = divisorLines[idx0a]._data._d;
	    }
	    _investmentItems = null;
	} else {
	    /** This is a joint account. Read in the InvestmentItems. */
	    _ageOfRmd = _baseDivisorForInhIra = Double.NaN;
	    _investmentItems = new InvestmentItem[InvestmentsEnum._Values.length];
	    final int sheetIdx = Arrays.binarySearch(workBookConcepts._sheetAndBlocksS,
		    new NamedEntity(WorkBookConcepts._SheetNames[WorkBookConcepts._InvestmentsSheetIdx]));
	    final Block[] investmentBlocks = workBookConcepts._sheetAndBlocksS[sheetIdx]._blocks;
	    final int nInvestmentBlocks = investmentBlocks.length;
	    for (int k0 = 0; k0 < nInvestmentBlocks; ++k0) {
		final Block block = investmentBlocks[k0];
		final String blockName = block._name;
		if (blockName.contains(_name)) {
		    final Line[] investmentLines = block._lines;
		    final int nInvestmentLines = investmentLines.length;
		    for (int k1 = 0; k1 < InvestmentsEnum._Values.length; ++k1) {
			_investmentItems[k1] = new InvestmentItem(InvestmentsEnum._Values[k1], 0d);
		    }
		    for (int k2 = 0; k2 < nInvestmentLines; ++k2) {
			/** If two Lines have the same header, the second one trumps. */
			final Line line = investmentLines[k2];
			final String originalName = line._header._s;
			final double dollars = line._data._d;
			if (originalName != null && Double.isFinite(dollars)) {
			    final InvestmentsEnum investmentsEnum = InvestmentsEnum._ReverseMap.get(originalName);
			    if (investmentsEnum != null) {
				final int k3 = investmentsEnum.ordinal();
				_investmentItems[k3] = new InvestmentItem(investmentsEnum, //
					investmentLines[k2]._data._d);
			    }
			}
		    }
		}
	    }
	}
	final Line[] balance0Lines = workBookConcepts.getBlock(staticsSheetName,
		"Balance Beginning of Base Year")._lines;
	final int idx1 = Arrays.binarySearch(balance0Lines, forLookUp);
	_balanceBeginningOfBaseYear = balance0Lines[idx1]._data._d;
	final Line[] balance1Lines = workBookConcepts.getBlock(staticsSheetName, "Base Balance")._lines;
	final int idx2 = Arrays.binarySearch(balance1Lines, forLookUp);
	_baseBalance = balance1Lines[idx2]._data._d;
    }

    public String getString() {
	String s = String.format("ACCNT[%s]", _name);
	if (_owner != null) {
	    s += String.format(", OWNR[%s]", _owner._name);
	}
	s += String.format(", BlncBgnnngYr[%s] CrrntBlnc[%s]", //
		TypeOfDouble.MONEY.format(_balanceBeginningOfBaseYear, 2), //
		TypeOfDouble.MONEY.format(_baseBalance, 2));
	if (_ageOfRmd > 0d) {
	    s += String.format(", Age at RMD[%s]", MyStudiesStringUtils.formatOther(_ageOfRmd, 1));
	} else if (_baseDivisorForInhIra > 0d) {
	    s += String.format(", CrrntDvsr[%s]", //
		    MyStudiesStringUtils.formatOther(_baseDivisorForInhIra, 1));
	} else if (_investmentItems != null) {
	    s += "\nInvestmentItems";
	    final int nInvestmentItems = _investmentItems.length;
	    for (int k = 0; k < nInvestmentItems; ++k) {
		final InvestmentItem investmentItem = _investmentItems[k];
		s += String.format("\n%03d. %s", k, investmentItem.getString());
	    }
	}
	return s;
    }

    @Override
    public String toString() {
	return getString();
    }
}
