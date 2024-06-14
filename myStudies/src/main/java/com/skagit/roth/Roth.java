package com.skagit.roth;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.skagit.roth.accountOwner.AccountOwner;
import com.skagit.roth.parameters.Parameters;
import com.skagit.roth.workBookConcepts.Block;
import com.skagit.roth.workBookConcepts.BlocksFromSheet;
import com.skagit.roth.workBookConcepts.Line;
import com.skagit.util.MyStudiesStringUtils;
import com.skagit.util.NamedEntity;

public class Roth {
    final public AccountOwner[] _accountOwners;
    final public double _currentShCf;
    final public double _currentElCf;
    final public double _livingExpenses;

    public Roth(final XSSFWorkbook workBook, final String accountsSheetName) {
	final FormulaEvaluator formulaEvaluator = workBook.getCreationHelper().createFormulaEvaluator();
	final BlocksFromSheet blocksFromSheet = new BlocksFromSheet(workBook.getSheet(accountsSheetName),
		formulaEvaluator);
	final Block[] accountsBlocks = blocksFromSheet._blocks;
	final int idx0 = Arrays.binarySearch(accountsBlocks, new NamedEntity("Owners and Birth Dates"));
	final Block block0 = accountsBlocks[idx0];
	final Line[] lines0 = block0._lines;
	final ArrayList<AccountOwner> ownerList = new ArrayList<>();
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
	    final AccountOwner owner = new AccountOwner(accountsBlocks, ownerName, birthDate);
	    ownerList.add(owner);
	}
	ownerList.add(new AccountOwner(accountsBlocks));
	_accountOwners = ownerList.toArray(new AccountOwner[ownerList.size()]);
	Arrays.sort(_accountOwners);
	/** Carry Forwards: */
	final int cfIdx = Arrays.binarySearch(accountsBlocks, new NamedEntity("Current Carry Forwards"));
	final Block cfBlock = accountsBlocks[cfIdx];
	final Line[] cfLines = cfBlock._lines;
	final int nCfLines = cfLines.length;
	double sh = 0d, el = 0d;
	for (int k = 0; k < nCfLines; ++k) {
	    final Line cfLine = cfLines[k];
	    final String hdrLc = cfLine._header._s.toLowerCase();
	    final double d = cfLine._data._d;
	    if (hdrLc.contains("short")) {
		sh = d;
	    } else if (hdrLc.contains("long")) {
		el = d;
	    }
	}
	_currentShCf = sh;
	_currentElCf = el;
	/** Living Expenses: */
	final int lvngExpnssIdx = Arrays.binarySearch(accountsBlocks, new NamedEntity("Living Expenses"));
	final Block lvngExpnssBlock = accountsBlocks[lvngExpnssIdx];
	final Line[] lvngExpnssLines = lvngExpnssBlock._lines;
	final int nLvngExpnssLines = lvngExpnssLines.length;
	double livingExpenses = 0d;
	for (int k = 0; k < nLvngExpnssLines; ++k) {
	    final Line lvngExpnssLine = lvngExpnssLines[k];
	    final String hdrLc = lvngExpnssLine._header._s.toLowerCase();
	    final double d = lvngExpnssLine._data._d;
	    if (hdrLc.contains("annual")) {
		livingExpenses = d;
	    }
	}
	_livingExpenses = livingExpenses;
    }

    public String getString() {
	String s = "";
	final int nOwners = _accountOwners.length;
	for (int k = 0; k < nOwners; ++k) {
	    if (k > 0) {
		s += "\n\n";
	    }
	    s += _accountOwners[k].getString();
	}
	s += String.format("\n\nShCf[%s] ElCf[%s]", //
		MyStudiesStringUtils.formatDollars(_currentShCf), //
		MyStudiesStringUtils.formatDollars(_currentElCf) //
	);
	s += String.format("\n\nLvngExpnss[%s]", //
		MyStudiesStringUtils.formatDollars(_livingExpenses) //
	);
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
	System.out.println(Parameters.getString());
	try (XSSFWorkbook workBook = new XSSFWorkbook(new FileInputStream(filePath))) {
	    final Roth roth = new Roth(workBook, accountsSheetName);
	    System.out.println(roth.getString());
	} catch (final IOException e) {
	    e.printStackTrace();
	}
    }

}
