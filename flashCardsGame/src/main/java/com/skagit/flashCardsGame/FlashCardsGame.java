package com.skagit.flashCardsGame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

public class FlashCardsGame {
	static final char _RtArrow = '\u2192';
	static final char _LtArrow = '\u2190';
	static final char _RtLtArrow = '\u2194';
	static final char _UpArrow = '\u2191';
	static final char _DnArrow = '\u2193';
	static final char _EmptySet = '\u2205';
	static final char _ReturnSymbol = '\u23CE';
	static final char _EditPropertiesSymbol = '#';
	static final String _IntroString;

	private static final long _Seed;
	static {
		final BigInteger bigInteger = new BigInteger(
				"1953102919570208198208131985030219910105201301192015102720231229");
		_Seed = bigInteger.mod(new BigInteger(Long.toString(Long.MAX_VALUE))).longValue();
		_IntroString = String.format("(%c=\"Check Mode,\" %c=\"Edit Properties\")",
				_ReturnSymbol, _EditPropertiesSymbol);
	}

	private static final String _DefaultPropertiesPath = "Data/FlashCards";
	private static final String _EchoFileIndicator = "-echo";
	private static final String _DelimiterRegEx = "[\n\r\t]+";

	private final Properties _properties;
	private final File _propertiesFileForStoringEcho;

	boolean _quizIsA_B;

	File _cardsFile;
	final TreeMap<Card, Card> _cardMap;
	Card[] _cards;

	final QuizGenerator _quizGenerator;
	QuizPlus _quizPlus;

	FlashCardsGame(final Scanner sc, final String propertiesPath,
			final boolean cleanCardsOnly) {
		_cardMap = new TreeMap<Card, Card>(new Comparator<>() {

			@Override
			public int compare(final Card card0, final Card card1) {
				final int compareValue = card0._aSide.compareToIgnoreCase(card1._aSide);
				if (compareValue != 0) {
					return compareValue;
				}
				return card0._bSide.compareToIgnoreCase(card1._bSide);
			}
		});
		GetPropertiesReturn propertiesReturn = getPropertiesReturn(propertiesPath);
		if (propertiesReturn == null) {
			propertiesReturn = getPropertiesReturn(_DefaultPropertiesPath);
			if (propertiesReturn == null) {
				_properties = null;
				_propertiesFileForStoringEcho = null;
				_quizIsA_B = true;
				_quizGenerator = null;
				_quizPlus = null;
				return;
			}
		}
		_properties = propertiesReturn._properties;

		/** Get _quizType. */
		final String quizTypeProperty = keyToString(_properties, "Quiz.Type");
		_quizIsA_B = quizTypeProperty.length() == 0
				|| Character.toUpperCase(quizTypeProperty.charAt(0)) == 'A';
		_properties.put("Quiz.Type", _quizIsA_B ? "A_B" : "B_A");

		/** Clean out the directories. */
		final File propertiesFile = propertiesReturn._propertiesFile;
		if (getBooleanFromScanner(sc, "Clear \"Echo\" files", /* lastResort= */true,
				/* automaticYes= */cleanCardsOnly, /* automaticNo= */false)) {
			final Path parentPath = FileSystems.getDefault()
					.getPath(propertiesFile.getParentFile().getAbsolutePath());
			final File parentFile = parentPath.toFile();
			String[] fileNames = null;
			fileNames = parentFile.list(new FilenameFilter() {
				@Override
				public boolean accept(final File dirFile, final String fileName) {
					final File f = new File(dirFile, fileName);
					return f.isFile() && fileName.toLowerCase().contains(_EchoFileIndicator);
				}
			});
			final int nI = fileNames.length;
			for (int i = 0; i < nI; ++i) {
				final File fileToDie = new File(parentFile, fileNames[i]);
				fileToDie.delete();
			}
		}

		/** Write out _propertiesFile. */
		if (getBooleanFromScanner(sc, "Overwrite Properties File", /* lastResort= */true,
				/* automaticYes= */cleanCardsOnly, /* automaticNo= */false)) {
			_propertiesFileForStoringEcho = propertiesFile;
		} else {
			_propertiesFileForStoringEcho = getEchoFile(propertiesFile, ".properties");
		}
		writePropertiesEchoFile();

		/** Get the Cards. */
		String cardsFileString = keyToString(_properties, "Cards.File");
		if (cardsFileString.length() == 0) {
			cardsFileString = "Cards.txt";
		}
		_cardsFile = getCardsFile(cardsFileString);
		loadCards(cleanCardsOnly);
		_quizGenerator = new QuizGenerator(_properties, /* nCards= */_cards.length, _Seed);
		_quizPlus = null;
	}

	static private boolean getBooleanFromScanner(final Scanner sc, final String corePrompt,
			final boolean lastResort, final boolean automaticYes, final boolean automaticNo) {
		if (automaticYes) {
			return true;
		}
		if (automaticNo) {
			return false;
		}
		System.out.printf("%s (Y/N) (No Response = %c)", corePrompt, lastResort ? 'Y' : 'N');
		final String myLine = sc.nextLine().trim();
		final String[] fields = myLine.trim().split("\\s+");
		return fieldsToBoolean(fields, lastResort);
	}

	void myPrintln(final PrintWriter pw, final boolean cleanCardsOnly, final String s) {
		pw.println(s);
		if (cleanCardsOnly) {
			pw.println(s);
		}
	}

	void myPrint(final PrintWriter pw, final boolean cleanCardsOnly, final String s) {
		pw.print(s);
		if (cleanCardsOnly) {
			pw.print(s);
		}
	}

	private void writeCardsToDisk(final boolean cleanCardsOnly) {
		final int nCards = _cards.length;
		final int nDigits = (int) (Math.log10(nCards) + 1d);
		final String cardIndexFormat = String.format("%%%dd.", nDigits);
		int maxASideLen = 0;
		for (final Card card : _cards) {
			maxASideLen = Math.max(maxASideLen, card._aSide.trim().length());
		}
		final String aSideFormat = String.format("%%-%ds", maxASideLen + 1);
		try (PrintWriter pw = new PrintWriter(_cardsFile)) {
			for (int k = 0; k < nCards; ++k) {
				final Card card = _cards[k];
				final String cardIndexString = String.format(cardIndexFormat, card._cardIndex);
				final String aSideString = String.format(aSideFormat, card._aSide + ":");
				String comment = card._comment;
				boolean printedBlankLine = false;
				if (k > 0) {
					if (k % 5 == 0) {
						myPrintln(pw, cleanCardsOnly, "");
						printedBlankLine = true;
					}
					myPrintln(pw, cleanCardsOnly, "");
				}
				if (comment != null) {
					comment = comment.trim();
					if (comment.length() > 0) {
						if (k > 0 && !printedBlankLine) {
							myPrintln(pw, cleanCardsOnly, "");
						}
						myPrintln(pw, cleanCardsOnly, comment);
					}
				}
				final String s = String.format("%s\t%s\t%s", cardIndexString, aSideString,
						card._bSide);
				myPrint(pw, cleanCardsOnly, s);
			}
		} catch (final FileNotFoundException e) {
		}
	}

	private static class GetPropertiesReturn {
		Properties _properties;
		File _propertiesFile;
		GetPropertiesReturn(final Properties properties, final File propertiesFile) {
			_properties = properties;
			_propertiesFile = propertiesFile;
		}
	}

	private final static GetPropertiesReturn getPropertiesReturn(String path) {
		if (path == null) {
			return null;
		}
		if (!path.toLowerCase().endsWith(".properties")) {
			final int lastDotIndex = path.lastIndexOf('.');
			if (lastDotIndex < 0) {
				path += ".properties";
			} else {
				path = path.substring(0, lastDotIndex) + ".properties";
			}
		}
		final File propertiesFile = new File(path);
		final Properties properties = new Properties();

		try (BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream(propertiesFile), "UTF-8"))) {
			in.mark(1);
			if (in.read() != 0xFEFF) {
				in.reset();
			}
			properties.load(in);
			return new GetPropertiesReturn(properties, propertiesFile);
		} catch (final IOException e) {
		}
		return null;
	}

	private final static File getEchoFile(final File f, final String suffix) {
		final File dir = f.getParentFile();
		final String name = f.getName();
		final int lastDotIndex = name.lastIndexOf('.');
		final String core = name.substring(0, lastDotIndex);
		for (int k = -1;; ++k) {
			final String numberPart = k == -1 ? "" : String.format("%02d", k);
			final String name2 = String.format("%s%s%s%s", core, _EchoFileIndicator, numberPart,
					suffix);
			final File echoFile = new File(dir, name2);
			if (!echoFile.exists()) {
				return echoFile;
			}
		}
	}

	private final File getCardsFile(String cardsFilePath) {
		if (!cardsFilePath.toUpperCase().endsWith(".TXT")) {
			final int lastDotIndex = cardsFilePath.lastIndexOf('.');
			if (lastDotIndex < 0) {
				cardsFilePath += ".txt";
			} else {
				cardsFilePath = cardsFilePath.substring(0, lastDotIndex) + ".txt";
			}
		}
		final File cardsFile1 = new File(cardsFilePath);
		if (cardsFile1.isFile()) {
			return new File(cardsFilePath);
		}
		final Path dirPath = FileSystems.getDefault()
				.getPath(_propertiesFileForStoringEcho.getParentFile().getAbsolutePath());
		final Path relativeToPropertiesDir = dirPath.resolve(cardsFilePath);
		final File cardsFile2 = relativeToPropertiesDir.toFile();
		return cardsFile2.isFile() ? cardsFile2 : null;
	}

	/**
	 * <pre>
	 * Interesting note on text files and the "right" way to do things:
	 * https://stackoverflow.com/questions/17405165/first-character-of-the-reading-from-the-text-file-%C3%AF
	 * </pre>
	 */
	private void loadCards(final boolean cleanCardsOnly) {
		_cardMap.clear();
		try (BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream(_cardsFile), "UTF-8"))) {
			/** Mark the stream at the beginning of the file. */
			if (in.markSupported()) {
				/**
				 * Mark where we're at in the file (the beginning). The "1" is a "read ahead
				 * limit, and does not mean "byte # 1."
				 */
				in.mark(1);
				/**
				 * If the first character is NOT feff, go back to the beginning of the file. If it
				 * IS feff, ignore it and continue on.
				 */
				if (in.read() != 0xFEFF) {
					in.reset();
				}
			}
			try (final Scanner fileSc = new Scanner(in)) {
				String comment = "";
				while (fileSc.hasNext()) {
					final String line = fileSc.nextLine().trim();
					final String[] fields = line.split("\\s*\\t\\s*");
					final int nFields = fields.length;
					final int cardIndex = _cardMap.size();
					if (nFields < 2) {
						if (comment.length() == 0) {
							comment = line;
						} else {
							comment = comment + "\n" + line;
						}
						continue;
					}
					String fieldA = fields[nFields > 2 ? 1 : 0];
					String fieldB = fields[nFields > 2 ? 2 : 1];
					if (fieldA.endsWith(".") || fieldA.endsWith(":")) {
						fieldA = fieldA.substring(0, fieldA.length() - 1);
					}
					if (fieldB.endsWith(".") || fieldB.endsWith(":")) {
						fieldB = fieldB.substring(0, fieldB.length() - 1);
					}
					final String aSide = fieldA.trim();
					final String bSide = fieldB.trim();
					final Card newCard = new Card(cardIndex, aSide, bSide);
					comment = comment.trim();
					newCard._comment = comment;
					final Card oldCard = _cardMap.get(newCard);
					if (oldCard != null) {
						System.out.printf("\n\nMerging\n%s into\n%s", newCard.getString(),
								oldCard.getString());
						oldCard._comment = (oldCard._comment + "\n" + comment).trim();
					} else {
						_cardMap.put(newCard, newCard);
					}
					comment = "";
				}
			} catch (final Exception e) {
			}
		} catch (final IOException e) {
		}
		final int nCards = _cardMap.size();
		_cards = _cardMap.keySet().toArray(new Card[nCards]);
		Arrays.sort(_cards, new Comparator<>() {

			@Override
			public int compare(final Card card0, final Card card1) {
				if (card0._cardIndex < card1._cardIndex) {
					return -1;
				}
				return card0._cardIndex > card1._cardIndex ? 1 : 0;
			}
		});
		if (nCards > 0) {
			writeCardsToDisk(cleanCardsOnly);
			announceDuplicates();
		}
	}

	private void announceDuplicates() {
		final int nCards = _cards.length;
		for (int iPass = 0; iPass < 2; ++iPass) {
			final Comparator<Card> comparator = iPass == 0 ? Card._ForAtoB : Card._ForBtoA;
			final TreeMap<Card, Card> map = new TreeMap<>(comparator);
			for (int k = 0; k < nCards; ++k) {
				final Card card = _cards[k];
				final Card oldCard = map.put(card, card);
				if (oldCard != null) {
					System.out.printf("\n\nDuplicate %s:\n%s and \n%s",
							iPass == 0 ? "A-Side" : "B-Side", card.getString(), oldCard.getString());
				}
			}
		}
	}

	void updateProperties() {
		_properties.put("Quiz.Type", _quizIsA_B ? "A_B" : "B_A");
		_quizGenerator.updateProperties(_properties);
	}

	void writePropertiesEchoFile() {
		try (FileOutputStream fos = new FileOutputStream(_propertiesFileForStoringEcho)) {
			_properties.store(fos, /* comments= */null);
		} catch (final IOException e) {
		}
	}

	/** Avoids having the same value consecutively. */
	private final static int _MaxNFailsPerElement = 5;
	static void shuffleArray(final int[] ints, final Random r, int lastValue) {
		final int n = ints.length;
		for (int k = 0; k < n; ++k) {
			for (int nFails = 0; nFails <= _MaxNFailsPerElement; ++nFails) {
				final int vK = ints[k];
				final int i = k + r.nextInt(n - k);
				final int vI = ints[i];
				if (vI != lastValue || nFails == _MaxNFailsPerElement) {
					lastValue = ints[k] = vI;
					ints[i] = vK;
					break;
				}
			}
		}
	}

	final String getTypeIPrompt(final int cardIndex, final String clue) {
		return String.format("%s (Q=Quit): ", _quizPlus.getTypeIPrompt(cardIndex, clue));
	}

	final String getTypeIIPrompt() {
		final String prompt = String.format("\nEnter: %c=Done, Q=Quit, R=Restart same quiz ",
				_ReturnSymbol);
		return prompt + _quizGenerator.getTypeIIPrompt();
	}

	/** Returns true if we're asked to keep going (not quit). */
	boolean modifyProperties(final Scanner sc) {
		for (;;) {
			final String prompt = getTypeIIPrompt();
			System.out.printf("%s: ", prompt);
			final String myLine = sc.nextLine().toUpperCase().trim();
			final String[] fields = myLine.trim().split("\\s+");
			final String field0 = fields == null || fields.length == 0
					|| fields[0].length() == 0 ? "" : fields[0];
			final int field0Len = field0.length();
			if (field0Len == 0) {
				/** Done and keep going. */
				return true;
			} else if (field0Len == 1) {
				final char field00 = field0.charAt(0);
				if (field00 == 'Q') {
					/** Done and quit. */
					return false;
				} else if (field00 == 'R') {
					/** Simply restart the current quiz. */
					_quizPlus.resetForFullMode();
					continue;
				}
			} else {
				_quizGenerator.modifyFromFields(fields);
			}
		}
	}

	final boolean madeChangesFrom(final long[] oldValues) {
		final long[] newValues = storeValues();
		final int nValues = newValues.length;
		for (int k = 0; k < nValues; ++k) {
			if (newValues[k] != oldValues[k]) {
				return true;
			}
		}
		return false;
	}

	final long[] storeValues() {
		final long[] core = new long[]{};
		final long[] others = _quizGenerator.getPropertyValues();
		final int nCore = core.length, nOthers = others.length;
		final long[] array = new long[nCore + nOthers];
		System.arraycopy(core, 0, array, 0, nCore);
		System.arraycopy(others, 0, array, nCore, nOthers);
		return array;
	}

	static private String keyToString(final Properties properties, final String key) {
		return CleanString((String) properties.get(key));
	}

	static private String CleanString(final String s) {
		if (s == null) {
			return "";
		}
		return s.trim().replaceAll("\\s+", " ");
	}

	static int keyToInt(final Properties properties, final String key,
			final int lastResort) {
		final String s = keyToString(properties, key);
		final String[] fields = s.split("\s");
		return fieldsToInt(fields, lastResort);
	}

	static boolean keyToBoolean(final Properties properties, final String key,
			final boolean lastResort) {
		final String s = keyToString(properties, key);
		final String[] fields = s.split("\s");
		return fieldsToBoolean(fields, lastResort);
	}

	static TypeOfDecay keyToTypeOfDecay(final Properties properties, final String key,
			final TypeOfDecay lastResort) {
		final String s = keyToString(properties, key);
		final String[] fields = s.split("\s");
		return fieldsToTypeOfDecay(fields, lastResort);
	}

	static int keyToPercentI(final Properties properties, final String key,
			final int lastResort) {
		final String s = keyToString(properties, key);
		final String[] fields = s.split("\s");
		return fieldsToPercentI(fields, lastResort);
	}

	static int fieldsToInt(final String[] fields, final int lastResort) {
		for (final String field : fields) {
			try {
				return Integer.parseInt(field);
			} catch (final NumberFormatException e) {
			}
		}
		return lastResort;
	}

	static boolean fieldsToBoolean(final String[] fields, final boolean lastResort) {
		for (final String field : fields) {
			if (field == null || field.length() == 0) {
				continue;
			}
			final char field0 = field.toUpperCase().charAt(0);
			if (field0 == 'Y' || field0 == 'T') {
				return true;
			}
			if (field0 == 'N' || field0 == 'F') {
				return false;
			}
			try {
				return Boolean.parseBoolean(field);
			} catch (final NumberFormatException e) {
			}
		}
		return lastResort;
	}

	static TypeOfDecay fieldsToTypeOfDecay(final String[] fields,
			final TypeOfDecay lastResort) {
		for (final String field : fields) {
			for (final TypeOfDecay typeOfDecay : TypeOfDecay.values()) {
				if (field.equalsIgnoreCase(typeOfDecay.name())) {
					return typeOfDecay;
				}
			}
		}
		return lastResort;
	}

	static int fieldsToPercentI(final String[] fields, final int lastResort) {
		for (final String field : fields) {
			final int len = field.length();
			if (len < 2) {
				continue;
			}
			if (field.charAt(len - 1) != '%') {
				continue;
			}
			try {
				return Integer.parseInt(field.substring(0, len - 1));
			} catch (final NumberFormatException e) {
			}
		}
		return lastResort;
	}

	final String getString() {
		return String.format("%s, %s", _quizIsA_B ? "A_B" : "B_A",
				_quizGenerator.getString());
	}

	@Override
	public String toString() {
		return getString();
	}

	void mainLoop(final Scanner sc) {
		int nCards = _cards.length;
		_quizPlus = _quizGenerator.createNewQuizPlus(nCards);
		final String quizString = _quizPlus.getString();
		System.out.printf("\n%s\nInitial Quiz Summary: %s %s\n\n", getString(), quizString,
				_IntroString);
		long[] oldValues = storeValues();
		/** Main loop: */
		for (boolean keepGoing = true; keepGoing;) {
			nCards = _cards.length;
			final QuizPlusTransition quizPlusTransition = _quizGenerator.reactToQuizPlus(nCards,
					_quizPlus);
			switch (quizPlusTransition._typeOfChange) {
				case LOSS :
				case WIN :
					final String winLossString;
					if (quizPlusTransition._typeOfChange == TypeOfChange.LOSS) {
						winLossString = "Critical Only:";
					} else {
						if (quizPlusTransition._oldQuizPlus._criticalQuizIndicesOnly) {
							winLossString = "Re-do Original Quiz:";
						} else {
							winLossString = "Move on:";
						}
					}
					final String oldSummary = quizPlusTransition._oldQuizPlus.getSummaryString();
					final String newSummary = quizPlusTransition._newQuizPlus.getSummaryString();
					System.out.printf("\n%s %s %c %s %s\n", winLossString, oldSummary, _RtArrow,
							newSummary, _IntroString);
					break;
				case ADJUST :
					System.out.print("\nNew Quiz as per User Input.\n");
					break;
				case NULL :
					/** No win or loss, and no adjustments from the user. Do nothing. */
					break;
			}
			if (madeChangesFrom(oldValues)) {
				updateProperties();
				writePropertiesEchoFile();
				oldValues = storeValues();
			}
			_quizPlus = quizPlusTransition._newQuizPlus;
			final int cardIndex = _quizPlus.getCurrentQuizCardIndex();
			final Card card = _cards[cardIndex];
			final String clue = _quizIsA_B ? card._aSide : card._bSide;
			final String answer = _quizIsA_B ? card._bSide : card._aSide;
			final String typeIPrompt = getTypeIPrompt(cardIndex, clue);
			boolean gotRightResponse = false;
			int nWrongResponses = 0;
			boolean editProperties = false;
			for (;; ++nWrongResponses) {
				System.out.print(typeIPrompt);
				final String myLine = sc.nextLine();
				if (myLine.length() == 1 && myLine.charAt(0) == 'Q') {
					editProperties = true;
					keepGoing = false;
					break;
				}
				final String response = myLine.split(_DelimiterRegEx)[0].trim();
				if (response.length() > 0 && response.charAt(0) == _EditPropertiesSymbol) {
					editProperties = true;
					keepGoing = modifyProperties(sc);
					break;
				}
				if (response.length() == 0) {
					System.out.printf("::%s:: Did you get it right (%c or Y/N)?\n", answer,
							_ReturnSymbol);
					final String myLine2 = sc.nextLine();
					final boolean gotItRight = myLine2.length() == 0
							|| Character.toUpperCase(myLine2.charAt(0)) == 'Y';
					if (gotItRight) {
						gotRightResponse = true;
						break;
					} else {
						++nWrongResponses;
						System.out.println("Try again.");
					}
				} else {
					if (response.equalsIgnoreCase(answer)) {
						gotRightResponse = true;
						break;
					}
					++nWrongResponses;
					System.out.printf("::%s::\n\n", answer);
				}
			}
			if (editProperties && madeChangesFrom(oldValues)) {
				if (madeChangesFrom(oldValues)) {
					updateProperties();
					System.out.printf("\nProperties were manually adjusted to:\n%s\n", getString());
					writePropertiesEchoFile();
					oldValues = storeValues();
				}
			}
			if (gotRightResponse) {
				_quizPlus.reactToRightResponse(/* wasWrongAtLeastOnce= */nWrongResponses > 0);
			}
		}
	}

	public static void main(final String[] args) {
		try (Scanner sc = new Scanner(System.in)) {
			final String propertiesPath = args.length > 0 ? args[0] : null;
			final boolean cleanCardsOnly = args.length > 1
					&& args[1].toUpperCase().contains("CLEAN");
			final FlashCardsGame flashCardsGame = new FlashCardsGame(sc, propertiesPath,
					cleanCardsOnly);
			if (!cleanCardsOnly) {
				flashCardsGame.mainLoop(sc);
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
