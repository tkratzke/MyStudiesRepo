package com.skagit.rothProblem;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.skagit.rothProblem.owner0.Owner0;
import com.skagit.rothProblem.parameters.Parameters;
import com.skagit.rothProblem.taxYear.TaxYear;
import com.skagit.rothProblem.workBookConcepts.Block;
import com.skagit.rothProblem.workBookConcepts.BlocksFromSheet;
import com.skagit.rothProblem.workBookConcepts.Line;
import com.skagit.rothProblem.workBookConcepts.WorkBookConcepts;
import com.skagit.util.MyStudiesDateUtils;
import com.skagit.util.MyStudiesStringUtils;
import com.skagit.util.NamedEntity;

public class RothProblem {
    /** The spreadsheet supplies the parameters and the following data: */
    final public Date _currentDate;
    final public double _currentShCf;
    final public double _currentElCf;
    final public double _currentLivingExpenses;
    final public Owner0[] _owner0s;

    final public TaxYear[] _taxYears;

    public RothProblem(final XSSFWorkbook workBook, final String accountsSheetName) {
	final FormulaEvaluator formulaEvaluator = workBook.getCreationHelper().createFormulaEvaluator();
	final BlocksFromSheet blocksFromSheet = new BlocksFromSheet(workBook.getSheet(accountsSheetName),
		formulaEvaluator);
	final Block[] accountsBlocks = blocksFromSheet._blocks;
	/** Miscellaneous Data. */
	final String miscellaneousDataSheetName = "Miscellaneous Data";
	_currentDate = WorkBookConcepts.getDate(accountsBlocks, miscellaneousDataSheetName, "Current Date");
	_currentShCf = WorkBookConcepts.getDouble(accountsBlocks, miscellaneousDataSheetName,
		"Short-Term Carry Forward");
	_currentElCf = WorkBookConcepts.getDouble(accountsBlocks, miscellaneousDataSheetName,
		"Long-Term Carry Forward");
	_currentLivingExpenses = WorkBookConcepts.getDouble(accountsBlocks, miscellaneousDataSheetName,
		"Current Living Expenses");
	/** Read in the Owner0s and their Account0s. */
	final int idx0 = Arrays.binarySearch(accountsBlocks, new NamedEntity("Owners and Birth Dates"));
	final Block block0 = accountsBlocks[idx0];
	final Line[] lines0 = block0._lines;
	final ArrayList<Owner0> ownerList = new ArrayList<>();
	final int nLines0 = lines0.length;
	for (int k0 = 0; k0 < nLines0; ++k0) {
	    final Line line0 = lines0[k0];
	    final String ownerName = line0._header._s;
	    if (ownerName == null || ownerName.length() == 0) {
		continue;
	    }
	    final Date birthDate = line0._data.getDate();
	    if (birthDate == null) {
		continue;
	    }
	    final Owner0 owner0 = new Owner0(this, accountsBlocks, ownerName, birthDate);
	    ownerList.add(owner0);
	}
	ownerList.add(new Owner0(this, accountsBlocks));
	_owner0s = ownerList.toArray(new Owner0[ownerList.size()]);
	Arrays.sort(_owner0s);

	final int currentYear = MyStudiesDateUtils.getYear(_currentDate);
	final int nTaxYears = Parameters._CurrentYearParameters._finalYear - currentYear + 1;
	_taxYears = new TaxYear[nTaxYears];
	for (int k = 0; k < nTaxYears; ++k) {
	    if (k == 0) {
		_taxYears[k] = new TaxYear(this);
	    } else {
		_taxYears[k] = new TaxYear(this, _taxYears[k - 1]);
	    }
	}
    }

    public String getString() {
	String s = String.format("\nShCf[%s] ElCf[%s] LvngExpnss[%s]", //
		MyStudiesStringUtils.formatDollars(_currentShCf), //
		MyStudiesStringUtils.formatDollars(_currentElCf), //
		MyStudiesStringUtils.formatDollars(_currentLivingExpenses));
	final int nOwner0s = _owner0s.length;
	for (int k = 0; k < nOwner0s; ++k) {
	    s += "\n\n" + _owner0s[k].getString();
	}
	return s;
    }

    @Override
    public String toString() {
	return getString();
    }

    public static void main(final String[] args) {
	int iArg = 0;
	final String filePath = args[iArg++] + ".xlsx";
	final String accountsSheetName = args[iArg++];
	System.out.println(Parameters._CurrentYearParameters.getString());
	try (XSSFWorkbook workBook = new XSSFWorkbook(new FileInputStream(filePath))) {
	    final RothProblem rothProblem = new RothProblem(workBook, accountsSheetName);
	    System.out.println(rothProblem.getString());
	} catch (final IOException e) {
	    e.printStackTrace();
	}
    }

}
