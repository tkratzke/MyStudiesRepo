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
    final public double _shortTermCarryForward;
    final public double _longTermCarryForward;
    final public double _livingExpenses;
    final public Owner0[] _owner0s;

    final public TaxYear[] _taxYears;

    public RothProblem(final XSSFWorkbook workBook, final String parametersSheetName, final String accountsSheetName) {
	final FormulaEvaluator formulaEvaluator = workBook.getCreationHelper().createFormulaEvaluator();
	final BlocksFromSheet blocksFromSheet = new BlocksFromSheet(workBook.getSheet(accountsSheetName),
		formulaEvaluator);
	final Block[] accountsBlocks = blocksFromSheet._blocks;
	/** Miscellaneous Data. */
	final String miscellaneousDataSheetName = "Miscellaneous Data";
	_currentDate = WorkBookConcepts.getDate(accountsBlocks, miscellaneousDataSheetName, "Current Date");
	_shortTermCarryForward = WorkBookConcepts.getDouble(accountsBlocks, miscellaneousDataSheetName,
		"Short-Term Carry Forward");
	_longTermCarryForward = WorkBookConcepts.getDouble(accountsBlocks, miscellaneousDataSheetName,
		"Long-Term Carry Forward");
	_livingExpenses = WorkBookConcepts.getDouble(accountsBlocks, miscellaneousDataSheetName, "Living Expenses");
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
	    final Owner0 owner0 = new Owner0(this, accountsBlocks, ownerName, birthDate);
	    ownerList.add(owner0);
	}
	_owner0s = ownerList.toArray(new Owner0[ownerList.size()]);
	Arrays.sort(_owner0s);

	final int currentYear = MyStudiesDateUtils.getYear(_currentDate);
	final Parameters parameters = new Parameters(workBook, formulaEvaluator, parametersSheetName);
	final int nTaxYears = parameters._finalYear - currentYear + 1;
	_taxYears = new TaxYear[nTaxYears];
	for (int k = 0; k < nTaxYears; ++k) {
	    if (k == 0) {
		_taxYears[k] = new TaxYear(this, parameters);
	    } else {
		_taxYears[k] = new TaxYear(_taxYears[k - 1]);
	    }
	}
    }

    public String getString() {
	String s = "";
	final int nTaxYears = _taxYears.length;
	for (int k = 0; k < nTaxYears; ++k) {
	    s += "\n\n" + _taxYears[k].getString();
	}
	s += String.format("\n\nNon-Parameters:\nShCf[%s] ElCf[%s] LvngExpnss[%s]", //
		MyStudiesStringUtils.formatDollars(_shortTermCarryForward), //
		MyStudiesStringUtils.formatDollars(_longTermCarryForward), //
		MyStudiesStringUtils.formatDollars(_livingExpenses));
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
	final String parametersSheetName = args[iArg++];
	final String accountsSheetName = args[iArg++];
	try (XSSFWorkbook workBook = new XSSFWorkbook(new FileInputStream(filePath))) {
	    final RothProblem rothProblem = new RothProblem(workBook, parametersSheetName, accountsSheetName);
	    System.out.println(rothProblem.getString());
	} catch (final IOException e) {
	    e.printStackTrace();
	}
    }

}
