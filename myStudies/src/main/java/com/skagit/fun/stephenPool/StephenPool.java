package com.skagit.fun.stephenPool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCreationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class StephenPool {

    final public static String _SheetName = "Input Data";
    final public static String _DateFormatStringI = "MMMM dd, yyyy h:mm a";
    final public static String _DateFormatStringO = "[$-en-US]MMM dd yyyy h:mm AM/PM;@";
    final public static SimpleDateFormat _MyDateFormat = new SimpleDateFormat(_DateFormatStringI);

    public static Date extractDate(final String dateString) throws ParseException {
	String dateString1 = null;
	try {
	    dateString1 = dateString.substring(dateString.indexOf(", ", 0) + ", ".length());
	} catch (final Exception e) {
	}
	return _MyDateFormat.parse(dateString1);
    }

    private final static String _HomeIndicator = "at ";

    private String[] getNameParts(final String cellValue) {
	if (cellValue == null) {
	    return null;
	}
	try {
	    final String[] returnValue = new String[2];
	    final String cellValueLc = cellValue.toLowerCase();
	    if (cellValueLc.startsWith(_HomeIndicator)) {
		returnValue[0] = cellValue.substring(0, _HomeIndicator.length());
		returnValue[1] = cellValue.substring(_HomeIndicator.length());
	    } else {
		returnValue[0] = null;
		returnValue[1] = cellValue;
	    }
	    return returnValue;
	} catch (final Exception e) {
	}
	return null;
    }

    private String[] getNameParts(final Row row, final int clmnNumber) {
	try {
	    final Cell cell = row.getCell(clmnNumber);
	    final String cellValue = cell.getStringCellValue();
	    return getNameParts(cellValue);
	} catch (final Exception e) {
	}
	return null;
    }

    final File _f;
    final HashSet<Team> _teams = new HashSet<>();
    final ArrayList<Game> _individualGames = new ArrayList<>();
    final TreeSet<ClusterOfGames> _clustersOfGames = new TreeSet<>();

    public StephenPool(final File f) {
	_f = f;
    }

    public void readInputSheet(final int nInMiddleToTake) {
	int kRow = 0;
	try (FileInputStream fis = new FileInputStream(_f)) {
	    try (final XSSFWorkbook xssfWorkBook = new XSSFWorkbook(fis)) {
		final XSSFSheet sheet = xssfWorkBook.getSheet(_SheetName);
		final int firstRowNumber = sheet.getFirstRowNum();
		final int lastRowNumber = sheet.getLastRowNum();
		final TreeMap<String, Integer> fieldNameToClmnNumber = new TreeMap<>();
		NEXT_ROW: for (kRow = firstRowNumber; kRow <= lastRowNumber; ++kRow) {
		    final Row row = sheet.getRow(kRow);
		    final int firstCellClmn = row.getFirstCellNum();
		    final int lastCellClmn = row.getLastCellNum();
		    /** If we have no fields, try to read this line as fields. */
		    if (fieldNameToClmnNumber.size() == 0) {
			for (int kClmn = firstCellClmn; kClmn <= lastCellClmn; ++kClmn) {
			    final Cell cell = row.getCell(kClmn);
			    if (cell == null) {
				continue;
			    }
			    try {
				final String cellString = row.getCell(kClmn).getStringCellValue();
				if (cellString == null || cellString.length() == 0) {
				    break;
				}
				fieldNameToClmnNumber.put(cellString, kClmn);
			    } catch (final Exception e) {
				fieldNameToClmnNumber.clear();
				continue NEXT_ROW;
			    }
			}
			continue;
		    }

		    /**
		     * To have a valid line, we need a date, an awayTeamName, a homeTeamName, an
		     * awayLine, and a homeLine.
		     */
		    Date date = null;
		    String awayTeamName = null, homeTeamName = null;
		    int awayLine = Integer.MIN_VALUE, homeLine = Integer.MIN_VALUE;
		    try {
			date = extractDate(row.getCell(fieldNameToClmnNumber.get("Date/Time")).getStringCellValue());
			String[] nameParts = getNameParts(row, fieldNameToClmnNumber.get("Favorite"));
			if (nameParts == null) {
			    continue;
			}
			if (nameParts[0] == null) {
			    awayTeamName = nameParts[1];
			} else {
			    homeTeamName = nameParts[1];
			}
			nameParts = getNameParts(row, fieldNameToClmnNumber.get("Underdog"));
			if (nameParts == null) {
			    continue;
			}
			if (nameParts[0] == null) {
			    awayTeamName = nameParts[1];
			} else {
			    homeTeamName = nameParts[1];
			}
			/** Read in away and home lines. */
			awayLine = (int) Math
				.round(row.getCell(fieldNameToClmnNumber.get("Away Line")).getNumericCellValue());
			homeLine = (int) Math
				.round(row.getCell(fieldNameToClmnNumber.get("Home Line")).getNumericCellValue());
		    } catch (final Exception e) {
			continue;
		    }
		    if (date == null || awayTeamName == null || awayTeamName.length() == 0 || homeTeamName == null
			    || homeTeamName.length() == 0 || awayLine == Integer.MIN_VALUE
			    || homeLine == Integer.MIN_VALUE) {
			continue;
		    }
		    final Team awayTeam = new Team(awayTeamName);
		    final Team homeTeam = new Team(homeTeamName);

		    /** Read in clusterName, teamAName, and set the boolean individualGame. */
		    String extra0String = null, extra1String = null;
		    boolean individualGame = true;
		    if (nInMiddleToTake <= 0) {
			try {
			    extra0String = row.getCell(fieldNameToClmnNumber.get("Extra-0")).getStringCellValue();
			    if (extra0String == null || extra0String.length() == 0) {
				continue;
			    }
			    individualGame = extra0String.equalsIgnoreCase("game");
			    if (!individualGame) {
				extra1String = row.getCell(fieldNameToClmnNumber.get("Extra-1")).getStringCellValue();
			    }
			} catch (final Exception e) {
			}
		    }
		    /** We can now build a game. */
		    final Game game = new Game(date, awayTeam, homeTeam, awayLine, homeLine, extra0String,
			    extra1String);

		    /** If it's a cluster,... */
		    if (nInMiddleToTake <= 0 && !individualGame) {
			final String clusterName = extra0String;
			final String teamAName = getNameParts(extra1String)[1];
			ClusterOfGames clusterOfGames = new ClusterOfGames(clusterName);
			if (!_clustersOfGames.add(clusterOfGames)) {
			    clusterOfGames = _clustersOfGames.floor(clusterOfGames);
			}
			final Team teamA = teamAName.equalsIgnoreCase(awayTeamName) ? awayTeam : homeTeam;
			clusterOfGames.addGameAndTeamA(game, teamA);
		    } else {
			_individualGames.add(game);
		    }
		}
	    } catch (final IOException e1) {
		e1.printStackTrace();
	    }
	} catch (final IOException e1) {
	    e1.printStackTrace();
	}
    }

    public void finishUp() {
	for (final ClusterOfGames c : _clustersOfGames) {
	    c.finishUp();
	}
	/** Compute the weights. */
	final int nIndGames = _individualGames.size();
	final int nClusters = _clustersOfGames.size();
	final int nOutputLines = nIndGames + nClusters;
	final OutputLine[] outputLines = new OutputLine[nOutputLines];
	int k;
	for (k = 0; k < nIndGames; ++k) {
	    outputLines[k] = _individualGames.get(k);
	}
	k = nIndGames;
	for (final ClusterOfGames c : _clustersOfGames) {
	    outputLines[k++] = c;
	}
	Arrays.sort(outputLines, new Comparator<OutputLine>() {

	    @Override
	    public int compare(final OutputLine oL0, final OutputLine oL1) {
		final double d = oL0.getProbFavoriteWins() - oL1.getProbFavoriteWins();
		if (d != 0d) {
		    return d > 0d ? -1 : 1;
		}
		return oL0.getHomeName().compareTo(oL1.getHomeName());
	    }
	});
	for (k = 0; k < nOutputLines; ++k) {
	    outputLines[k]._weight = nOutputLines - k;
	}
    }

    private int writeOutputRows(final XSSFSheet sheet, int iRow, final CellStyle dateStyle,
	    final Collection<? extends OutputLine> outputLines, final boolean writeBook) {
	for (final OutputLine outputLine : outputLines) {
	    final Row row = sheet.createRow(iRow++);
	    final int dateIndex = OutputLine.OutputColumn.DATE_TIME.ordinal();
	    final Cell dateCell = row.createCell(dateIndex);
	    dateCell.setCellValue(outputLine._date);
	    dateCell.setCellStyle(dateStyle);

	    final int awayIndex = OutputLine.OutputColumn.AWAY_NAME.ordinal();
	    final Cell awayCell = row.createCell(awayIndex);
	    awayCell.setCellValue(outputLine.getAwayName());
	    final int awayLineIndex = OutputLine.OutputColumn.AWAY_LINE.ordinal();
	    final Cell awayLineCell = row.createCell(awayLineIndex);
	    awayLineCell.setCellValue(outputLine._awayLine);
	    final int probAwayWinsIndex = OutputLine.OutputColumn.PROB_AWAY_WINS.ordinal();
	    final Cell probAwayWinsCell = row.createCell(probAwayWinsIndex);
	    probAwayWinsCell.setCellValue(outputLine._probAwayWins);

	    final int homeIndex = OutputLine.OutputColumn.HOME_NAME.ordinal();
	    final Cell homeCell = row.createCell(homeIndex);
	    homeCell.setCellValue(outputLine.getHomeName());
	    final int homeLineIndex = OutputLine.OutputColumn.HOME_LINE.ordinal();
	    final Cell homeLineCell = row.createCell(homeLineIndex);
	    homeLineCell.setCellValue(outputLine._homeLine);
	    final int probHomeWinsIndex = OutputLine.OutputColumn.PROB_HOME_WINS.ordinal();
	    final Cell probHomeWinsCell = row.createCell(probHomeWinsIndex);
	    probHomeWinsCell.setCellValue(outputLine._probHomeWins);

	    if (writeBook) {
		final int bookPickIndex = OutputLine.OutputColumn.BOOK_PICK.ordinal();
		final Cell bookPickCell = row.createCell(bookPickIndex);
		bookPickCell.setCellValue(outputLine.getFavoriteName());
		final int bookWeightIndex = OutputLine.OutputColumn.BOOK_WEIGHT.ordinal();
		final Cell bookWeightCell = row.createCell(bookWeightIndex);
		bookWeightCell.setCellValue(outputLine._weight);
	    }
	}
	return iRow;
    }

    public void writeOutputSheet(final String outputSheetName) {
	try (FileInputStream fis = new FileInputStream(_f)) {
	    try (final XSSFWorkbook xssfWorkbook = new XSSFWorkbook(fis)) {
		final XSSFCreationHelper creationHelper = xssfWorkbook.getCreationHelper();
		final CellStyle dateStyle = xssfWorkbook.createCellStyle();
		dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat(_DateFormatStringO));
		final XSSFSheet sheet;
		for (int k = -1;; ++k) {
		    final String sheetName = outputSheetName + (k < 0 ? "" : String.format("%02d", k));
		    final int sheetIndex = xssfWorkbook.getSheetIndex(sheetName);
		    if (sheetIndex < 0) {
			sheet = xssfWorkbook.createSheet(sheetName);
			break;
		    }
		}
		int iRow = 0;
		final Row headerRow = sheet.createRow(iRow++);
		final int nFields = OutputLine.OutputColumn._AllOutputColumns.length;
		for (int clmnIndex = 0; clmnIndex < nFields; ++clmnIndex) {
		    final String clmnName = OutputLine.OutputColumn._AllOutputColumns[clmnIndex]._displayString;
		    final Cell cell = headerRow.createCell(clmnIndex);
		    cell.setCellValue(clmnName);
		}
		iRow = writeOutputRows(sheet, iRow, dateStyle, _individualGames, /* writeBook= */true);
		iRow = writeOutputRows(sheet, iRow, dateStyle, _clustersOfGames, /* writeBook= */true);
		for (final ClusterOfGames clusterOfGames : _clustersOfGames) {
		    sheet.createRow(iRow++);
		    final Row row = sheet.createRow(iRow++);
		    final Cell titleCell = row.createCell(/* cellIndex= */0);
		    titleCell.setCellValue(clusterOfGames._clusterName);
		    iRow = writeOutputRows(sheet, iRow, dateStyle, Arrays.asList(clusterOfGames.getSortedGames()),
			    /* writeBook= */false);
		}
		try (final FileOutputStream fos = new FileOutputStream(_f)) {
		    xssfWorkbook.write(fos);
		} catch (final IOException e0) {
		}
	    } catch (final IOException e1) {
	    }
	} catch (final IOException e1) {
	}
    }

    public String getString() {
	String s = "Individual Games:";
	int k = 0;
	for (final Game g : _individualGames) {
	    s += String.format("\n%d.\t%s", ++k, g.getString());
	}
	for (final ClusterOfGames clusterOfGames : _clustersOfGames) {
	    s += "\n\n" + clusterOfGames.getString();
	}
	return s;
    }

    @Override
    public String toString() {
	return getString();
    }

    private void takeMiddle(int nInMiddleToTake) {
	final int nIndividualGames = _individualGames.size();
	final Game[] gamesArray = _individualGames.toArray(new Game[nIndividualGames]);
	Arrays.sort(gamesArray, new Comparator<>() {

	    @Override
	    public int compare(final Game game0, final Game game1) {
		final double probFavoriteWins0 = game0.getProbFavoriteWins();
		final double probFavoriteWins1 = game1.getProbFavoriteWins();
		if (probFavoriteWins0 != probFavoriteWins1) {
		    return probFavoriteWins0 < probFavoriteWins1 ? -1 : 1;
		}
		return game0.compareTo(game1);
	    }
	});
	double tightest = Double.NaN;
	int startForTightest = -1;
	nInMiddleToTake = Math.min(nIndividualGames, nInMiddleToTake);
	for (int k = 0;; ++k) {
	    if (k + nInMiddleToTake - 1 >= nIndividualGames) {
		break;
	    }
	    final double probFavoriteWins0 = gamesArray[k].getProbFavoriteWins();
	    final double probFavoriteWins1 = gamesArray[k + nInMiddleToTake - 1].getProbFavoriteWins();
	    final double diff = probFavoriteWins1 - probFavoriteWins0;
	    if (startForTightest < 0 || diff < tightest) {
		tightest = diff;
		startForTightest = k;
	    }
	}
	_individualGames.clear();
	_individualGames
		.addAll(Arrays.asList(gamesArray).subList(startForTightest, startForTightest + nInMiddleToTake));
	if (true) {
	} else {
	    _individualGames.sort(new Comparator<>() {

		@Override
		public int compare(final Game game0, final Game game1) {
		    return game0.compareTo(game1);
		}
	    });
	}
    }

    final public static String _ExcelFilePath = "StephenPoolSpreadsheets/StephenPool - Bowls.xlsx";

    public static void main(final String[] args) {
	final File f = new File(_ExcelFilePath);
	for (int iPass = 0; iPass < 2; ++iPass) {
	    final StephenPool stephenPool = new StephenPool(f);
	    final int nInMiddleToTake = iPass == 0 ? 0 : 16;
	    stephenPool.readInputSheet(nInMiddleToTake);
	    if (nInMiddleToTake > 0) {
		stephenPool.takeMiddle(nInMiddleToTake);
	    }
	    stephenPool.finishUp();
	    final String outputSheetName = nInMiddleToTake > 0 ? String.format("Middle %2d", nInMiddleToTake)
		    : "Standard";
	    stephenPool.writeOutputSheet(outputSheetName);
	}
    }

}
