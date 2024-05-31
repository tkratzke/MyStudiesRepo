package com.skagit.roth.workBookConcepts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.skagit.util.NamedEntity;

public class SheetAndBlocks extends NamedEntity {
    final public WorkBookConcepts _workBookConcepts;
    final XSSFSheet _sheet;
    final public Block[] _blocks;

    public SheetAndBlocks(final WorkBookConcepts workBookConcepts, final XSSFSheet sheet) {
	super(sheet.getSheetName());
	_workBookConcepts = workBookConcepts;
	_sheet = sheet;
	final int nMergedRegions = _sheet.getNumMergedRegions();
	final ArrayList<CellRangeAddress> craList0 = new ArrayList<>();
	for (int k = 0; k < nMergedRegions; ++k) {
	    final CellRangeAddress cra = sheet.getMergedRegion(k);
	    final int firstRow = cra.getFirstRow();
	    final int lastRow = cra.getLastRow();
	    final int firstClmn = cra.getFirstColumn();
	    final int lastClmn = cra.getLastColumn();
	    if (firstRow == lastRow && firstClmn == 0 && lastClmn == 1) {
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
	final CellRangeAddress[] craArray = craList1.toArray(new CellRangeAddress[nCras1]);
	Arrays.sort(craArray, new Comparator<CellRangeAddress>() {

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
	    final CellRangeAddress thisCra = craArray[k];
	    final CellRangeAddress nextCra = craArray[k + 1];
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
}
