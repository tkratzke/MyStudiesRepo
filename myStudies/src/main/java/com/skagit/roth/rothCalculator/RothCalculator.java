package com.skagit.roth.rothCalculator;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.skagit.roth.currentYear.Account0;
import com.skagit.roth.currentYear.CurrentYear;
import com.skagit.roth.currentYear.OutsideIncome0;
import com.skagit.roth.currentYear.Owner0;
import com.skagit.roth.taxYear.Account1;
import com.skagit.roth.taxYear.OutsideIncome1;
import com.skagit.roth.taxYear.Owner1;
import com.skagit.roth.taxYear.TaxYear;
import com.skagit.roth.workBookConcepts.Line;
import com.skagit.roth.workBookConcepts.WorkBookConcepts;
import com.skagit.util.NamedEntity;
import com.skagit.util.TypeOfDouble;

public class RothCalculator {

    final public static String[] _BracketsNames = { //
	    "Tax Brackets", //
	    "Long Term Tax Rates", //
	    "Social Security AGI Tax Rates", //
	    "IRMAA Multipliers", //
    };

    final public static String _InflationGrowthRateName = "Inflation";
    final public static String _InvestmentsGrowthRateName = "Investments";

    final public WorkBookConcepts _workBookConcepts;
    final public CurrentYear _currentYear;
    final public int _finalYear;

    public final double _additionalMedicareTaxPerCent;
    public final double _medicareTaxPerCentOnInvestments;
    public final GrowthRate _inflationGrowthRate;
    public final GrowthRate _investmentsGrowthRate;
    public final double _perCentLong;

    public final double[] _lifeExpectancies;
    public final TaxYear[] _taxYears;

    public RothCalculator(final XSSFWorkbook workBook) {
	_workBookConcepts = new WorkBookConcepts(workBook);
	_currentYear = new CurrentYear(_workBookConcepts);
	final String lifeExpectanciesSheetName = WorkBookConcepts
		.getSheetName(WorkBookConcepts._LifeExpectanciesSheetIdx);
	final Line[] lifeExpectancyLines = _workBookConcepts.getBlock(lifeExpectanciesSheetName,
		"Expected Life Lengths")._lines;
	final int nLines3 = lifeExpectancyLines.length;
	_lifeExpectancies = new double[nLines3];
	for (int k = 0; k < nLines3; ++k) {
	    _lifeExpectancies[k] = lifeExpectancyLines[k]._data._d;
	}
	_finalYear = (int) Math.round(_workBookConcepts.getMiscellaneousData("Final Year")._d);

	final double inflationProportion = _workBookConcepts
		.getMiscellaneousData(RothCalculator._InflationGrowthRateName)._d;
	_inflationGrowthRate = new GrowthRate(RothCalculator._InflationGrowthRateName, inflationProportion);
	final double investmentsProportion = _workBookConcepts
		.getMiscellaneousData(RothCalculator._InvestmentsGrowthRateName)._d;
	_investmentsGrowthRate = new GrowthRate(RothCalculator._InvestmentsGrowthRateName, investmentsProportion);
	_perCentLong = _workBookConcepts.getMiscellaneousData("% that's Long")._d;
	_additionalMedicareTaxPerCent = 100d * _workBookConcepts.getMiscellaneousData("Additional Medicare Tax")._d;
	_medicareTaxPerCentOnInvestments = 100d
		* _workBookConcepts.getMiscellaneousData("Additional Medicare Tax on Investments")._d;

	final int currentYearYear = _currentYear.getCurrentYear();
	final int nYears = _finalYear - currentYearYear + 1;
	_taxYears = new TaxYear[nYears];
	for (int year = currentYearYear; year <= _finalYear; ++year) {
	    _taxYears[year - currentYearYear] = new TaxYear(this, year);
	}
    }

    public String getString() {
	String s = String.format("Fnl Yr[%d]", _finalYear);
	s += String.format("\n%% that's Long[%s]", TypeOfDouble.PER_CENT.format(_perCentLong, 2));
	s += String.format(//
		"\nAddtnlMdcrTx[%s] MdcrTxPerCentOnInvstmnts[%s]", //
		TypeOfDouble.PER_CENT.format(_additionalMedicareTaxPerCent, 1),
		TypeOfDouble.PER_CENT.format(_medicareTaxPerCentOnInvestments, 1));
	s += String.format("\n%s", _inflationGrowthRate.getString());
	s += String.format("\n%s", _investmentsGrowthRate.getString());
	s += "\n\n" + _currentYear.getString();
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

    public Owner1 getOwner1(final Owner0 owner, final int thisYear) {
	final int currentYearYear = _currentYear.getCurrentYear();
	if (thisYear < currentYearYear || thisYear > _finalYear) {
	    return null;
	}
	final TaxYear taxYear = _taxYears[thisYear - currentYearYear];
	final Owner1[] owner1s = taxYear._owner1s;
	final int idx = Arrays.binarySearch(owner1s, new NamedEntity(owner._name, thisYear));
	if (idx < 0) {
	    return null;
	}
	return taxYear._owner1s[idx];
    }

    public Account1 getAccount1(final Account0 account0, final int thisYear) {
	final Owner1 owner1 = getOwner1(account0._owner, thisYear);
	if (owner1 == null) {
	    return null;
	}
	final Account1[] account1s = owner1._myAccounts1;
	final int idx = Arrays.binarySearch(account1s, new NamedEntity(account0._name, thisYear));
	if (idx < 0) {
	    return null;
	}
	return owner1._myAccounts1[idx];
    }

    public OutsideIncome1 getOi1(final OutsideIncome0 outsideIncome, final int thisYear) {
	final Owner1 owner1 = getOwner1(outsideIncome.getOwner(), thisYear);
	if (owner1 == null) {
	    return null;
	}
	final Account1[] myAccount1s = owner1._myAccounts1;
	final int idx = Arrays.binarySearch(myAccount1s, new NamedEntity(outsideIncome._name, thisYear));
	if (idx < 0) {
	    return null;
	}
	return owner1._oi1s[idx];
    }

}
