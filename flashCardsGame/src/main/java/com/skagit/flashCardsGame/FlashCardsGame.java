package com.skagit.flashCardsGame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

public class FlashCardsGame {

	final static char _RtArrow = '\u2192';
	final static char _LtArrow = '\u2190';
	final static char _RtLtArrow = '\u2194';
	final static char _UpArrow = '\u2191';
	final static char _DnArrow = '\u2193';
	final static char _EmptySet = '\u2205';
	final static char _ReturnSymbol = '\u23CE';
	final static char _EditPropertiesSymbol = '#';
	final static String _IntroString;

	final private static long _Seed;
	static {
		final BigInteger bigInteger = new BigInteger(
				"1953102919570208198208131985030219910105201301192015102720231229");
		_Seed = bigInteger.mod(new BigInteger(Long.toString(Long.MAX_VALUE))).longValue();
		_IntroString = String.format("%c=\"Check Mode,\" %c=\"Edit Properties\"",
				_ReturnSymbol, _EditPropertiesSymbol);
	}
	/** Either of the following two FieldSeparators seems to work. */
	@SuppressWarnings("unused")
	final private static String _FieldSeparator0 = "\\s*\\t\\s*";
	final private static String _FieldSeparator = "(\s*\t\s*)+";
	final private static String _WhiteSpace = "\s+";
	final private static String _DefaultPropertiesFilePath = "Data/Tran";
	final private static String _PropertiesEnding = ".properties";

	final private File _propertiesFile;
	final private Properties _properties;
	boolean _quizIsA_B;
	Card[] _cards;
	final QuizGenerator _quizGenerator;
	QuizPlus _quizPlus;
	boolean _printedSomething;

	FlashCardsGame(final Scanner sc, final File propertiesFile) {
		_propertiesFile = propertiesFile;
		_properties = new Properties();
		try (
				InputStreamReader in = new InputStreamReader(new FileInputStream(_propertiesFile),
						"UTF-8")) {
			_properties.load(in);
		} catch (final IOException e) {
		}
		final String quizTypeProperty = keyToString(_properties, "Quiz.Type");
		_quizIsA_B = quizTypeProperty.length() == 0
				|| Character.toUpperCase(quizTypeProperty.charAt(0)) != 'B';
		reWritePropertiesFile();
		_printedSomething = false;
		loadCards();
		_quizGenerator = new QuizGenerator(_properties, _cards.length, _Seed);
		_quizGenerator.shuffleCards(_cards);
		_quizPlus = null;
	}

	private void writeCardsToDisk() {
		final int nCards = _cards.length;
		final int nDigits = (int) (Math.log10(nCards) + 1d);
		final String cardNumberFormat = String.format("%%%dd.", nDigits);
		int maxASideLen = 0;
		for (final Card card : _cards) {
			maxASideLen = Math.max(maxASideLen, card._aSide.length());
		}
		final String aSideFormat = String.format("%%-%ds", maxASideLen + 1);
		final File cardsFile = getCardsFile();
		try (PrintWriter pw = new PrintWriter(cardsFile)) {
			for (int k = 0; k < nCards; ++k) {
				final Card card = _cards[k];
				final String cardNumberString = String.format(cardNumberFormat, card._cardNumber);
				final String aSideString = String.format(aSideFormat, card._aSide + ":");
				String comment = card._comment;
				boolean printedBlankLine = false;
				if (k > 0) {
					if (k % 10 == 0) {
						pw.println();
						printedBlankLine = true;
					}
					pw.println();
				}
				if (comment != null) {
					comment = comment.trim();
					if (comment.length() > 0) {
						if (k > 0 && !printedBlankLine) {
							pw.println();
						}
						pw.println(comment);
					}
				}
				final String s = String.format("%s\t%s\t%s", cardNumberString, aSideString,
						card._bSide);
				pw.print(s);
			}
		} catch (final FileNotFoundException e) {
		}
	}

	private File getCardsFile() {
		final String propertiesFileName = _propertiesFile.getName();
		final File parentFile = _propertiesFile.getParentFile();
		final String cardsFileName = propertiesFileName.substring(0,
				propertiesFileName.length() - _PropertiesEnding.length()) + ".txt";
		return new File(parentFile, cardsFileName);
	}

	private String getCoreFilePath() {
		final String inputPath = _propertiesFile.toString();
		return inputPath.substring(0, inputPath.length() - _PropertiesEnding.length());
	}

	/**
	 * <pre>
	 * Interesting note on text files and the "right" way to do things:
	 * https://stackoverflow.com/questions/17405165/first-character-of-the-reading-from-the-text-file-%C3%AF
	 * </pre>
	 */

	private void loadCards() {
		final TreeMap<Card, Card> cardMap = new TreeMap<Card, Card>(Card._ByAThenB);
		final File cardsFile = getCardsFile();
		try (BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream(cardsFile), "UTF-8"))) {
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
					final String[] fields = line.split(_FieldSeparator);
					final int nFields = fields.length;
					final int cardNumber = cardMap.size();
					if (nFields < 2) {
						if (comment.length() == 0) {
							comment = line;
						} else {
							comment = comment + "\n" + line;
						}
						continue;
					}
					String aSide = CleanString(fields[nFields > 2 ? 1 : 0]);
					String bSide = CleanString(fields[nFields > 2 ? 2 : 1]);
					if (aSide.endsWith(".") || aSide.endsWith(":")) {
						aSide = aSide.substring(0, aSide.length() - 1);
					}
					if (bSide.endsWith(".") || bSide.endsWith(":")) {
						bSide = bSide.substring(0, bSide.length() - 1);
					}
					final Card newCard = new Card(cardNumber, aSide, bSide);
					comment = comment.trim();
					newCard._comment = comment;
					final Card oldCard = cardMap.get(newCard);
					if (oldCard != null) {
						if (_printedSomething) {
							System.out.printf("\n\n");
						}
						_printedSomething = true;
						System.out.printf("Merging\n%s into\n%s", newCard.getString(),
								oldCard.getString());
						oldCard._comment = (oldCard._comment + "\n" + comment).trim();
					} else {
						cardMap.put(newCard, newCard);
					}
					comment = "";
				}
			} catch (final Exception e) {
			}
		} catch (final IOException e) {
		}
		final int nCards = cardMap.size();
		_cards = cardMap.keySet().toArray(new Card[nCards]);
		Arrays.sort(_cards, Card._ByIndexOnly);
		announceAndClumpDuplicates();
		writeCardsToDisk();
	}

	private void announceAndClumpDuplicates() {
		final int nCards = _cards.length;
		for (int iPass = 0; iPass < 2; ++iPass) {
			/** If _quizIsA_B, we want to do the A-side second. */
			final Comparator<Card> comparator;
			final String sideBeingChecked;
			if (iPass == 1) {
				comparator = _quizIsA_B ? Card._ByASideOnly : Card._ByBSideOnly;
				sideBeingChecked = _quizIsA_B ? "A-Side" : "B-Side";
			} else {
				comparator = _quizIsA_B ? Card._ByBSideOnly : Card._ByASideOnly;
				sideBeingChecked = _quizIsA_B ? "B-Side" : "A-Side";
			}
			final TreeMap<Card, ArrayList<Card>> kingToSlaves = new TreeMap<>(comparator);
			for (int k = 0; k < nCards; ++k) {
				final Card card = _cards[k];
				final ArrayList<Card> slaves = kingToSlaves.get(card);
				if (slaves != null) {
					final Card oldCard = kingToSlaves.ceilingKey(card);
					if (_printedSomething) {
						System.out.printf("\n\n");
					}
					_printedSomething = true;
					System.out.printf("Duplicate %s:\n%s\n%s", sideBeingChecked,
							oldCard.getString(), card.getString());
					slaves.add(card);
					_cards[k] = null;
				} else {
					kingToSlaves.put(card, new ArrayList<>());
				}
			}
			final Card[] newCards = new Card[nCards];
			for (int k0 = 0, k1 = 0; k0 < nCards; ++k0) {
				final Card card = _cards[k0];
				if (card == null) {
					/** The card formerly at k0 is now somebody's slave. */
					continue;
				}
				card._cardNumber = k1;
				newCards[k1] = card;
				++k1;
				final ArrayList<Card> slaves = kingToSlaves.get(card);
				final int nSlaves = slaves.size();
				for (int k2 = 0; k2 < nSlaves; ++k2) {
					final Card slave = slaves.get(k2);
					slave._cardNumber = k1;
					newCards[k1] = slave;
					++k1;
				}
			}
			System.arraycopy(newCards, 0, _cards, 0, nCards);
		}
	}

	void updateProperties() {
		_properties.put("Quiz.Type", _quizIsA_B ? "A_B" : "B_A");
		_quizGenerator.updateProperties(_properties);
	}

	void reWritePropertiesFile() {
		try (FileOutputStream fos = new FileOutputStream(_propertiesFile)) {
			_properties.store(fos, /* comments= */null);
		} catch (final IOException e) {
		}
	}

	/** Avoids having the same value consecutively. */
	final private static int _MaxNFailsPerElement = 5;
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

	final String getTypeIPrompt(final int indexInCards, final String clue) {
		String s = "";
		final int currentIndexInQuiz = _quizPlus.getCurrentIndexInQuiz();
		if (_quizPlus.isCriticalQuizIndex(currentIndexInQuiz)) {
			s += "*";
		}
		final int cardNumber = _cards[indexInCards]._cardNumber;
		final int quizLen = _quizPlus.getCurrentQuizLen();
		s += String.format("%d of %d(IIC=%d,#%d)", currentIndexInQuiz + 1, quizLen,
				indexInCards, cardNumber);
		final int nRights = _quizPlus.getNRights();
		final int nWrongs = _quizPlus.getNWrongs();
		final int nTrials = nRights + nWrongs;
		if (nTrials > 0) {
			final long successRateI = Math.round((100d * nRights) / nTrials);
			s += String.format(",(#Rt/Wr=%d/%d SccRt=%d%%)", nRights, nWrongs, successRateI);
		}
		return s + String.format(" (Q=Quit): %s ", clue);
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
			final String myLine = readLine(sc).toUpperCase().trim();
			final String[] fields = myLine.trim().split(_WhiteSpace);
			final String field0 = (fields == null || fields.length == 0
					|| fields[0].length() == 0) ? "" : fields[0];
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

	private static String keyToString(final Properties properties, final String key) {
		return CleanString((String) properties.get(key));
	}

	static int keyToInt(final Properties properties, final String key,
			final int lastResort) {
		final String s = keyToString(properties, key);
		final String[] fields = s.split("\s");
		return fieldsToInt(fields, lastResort);
	}

	static long keyToLong(final Properties properties, final String key,
			final long lastResort) {
		final String s = keyToString(properties, key);
		final String[] fields = s.split("\s");
		return fieldsToLong(fields, lastResort);
	}

	static boolean keyToBoolean(final Properties properties, final String key,
			final boolean lastResort) {
		final String s = keyToString(properties, key);
		final String[] fields = s.split("\s");
		return fieldsToBoolean(fields, lastResort);
	}

	static QuizGenerator.TypeOfDecay keyToTypeOfDecay(final Properties properties,
			final String key, final QuizGenerator.TypeOfDecay lastResort) {
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

	static long fieldsToLong(final String[] fields, final long lastResort) {
		for (final String field : fields) {
			try {
				return Long.parseLong(field);
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

	static QuizGenerator.TypeOfDecay fieldsToTypeOfDecay(final String[] fields,
			final QuizGenerator.TypeOfDecay lastResort) {
		for (final String field : fields) {
			for (final QuizGenerator.TypeOfDecay typeOfDecay : QuizGenerator.TypeOfDecay
					.values()) {
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

	String readLine(final Scanner sc) {
		final String line = sc != null ? sc.nextLine() : System.console().readLine();
		return CleanString(line);
	}

	void mainLoop(final Scanner sc) {
		int nCards = _cards.length;
		_quizPlus = _quizGenerator.createNewQuizPlus(nCards);
		final String quizString = _quizPlus.getString();
		if (_printedSomething) {
			System.out.printf("\n\n");
		}
		_printedSomething = true;
		System.out.printf("%s\n%s\n\nInitial Quiz Summary (%s): %s\n\n", getString(),
				_IntroString, getCoreFilePath(), quizString);
		long[] oldValues = storeValues();
		/** Main loop: */
		for (boolean keepGoing = true; keepGoing;) {
			nCards = _cards.length;
			final QuizGenerator.QuizPlusTransition quizPlusTransition = _quizGenerator
					.reactToQuizPlus(nCards, _quizPlus);
			switch (quizPlusTransition._typeOfChange) {
				case LOSS :
				case WIN :
					final String winLossString;
					if (quizPlusTransition._typeOfChange == QuizGenerator.TypeOfChange.LOSS) {
						winLossString = "Critical Only:";
					} else {
						if (quizPlusTransition._oldQuizPlus._criticalQuizIndicesOnly) {
							winLossString = "Re-do Original Quiz:";
						} else {
							winLossString = String.format("Moving on within %s:", getCoreFilePath());
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
				reWritePropertiesFile();
				oldValues = storeValues();
			}
			_quizPlus = quizPlusTransition._newQuizPlus;
			final int indexInCards = _quizPlus.getCurrentQuiz_IndexInCards();
			final Card card = _cards[indexInCards];
			final String clue = _quizIsA_B ? card._aSide : card._bSide;
			final String answer = _quizIsA_B ? card._bSide : card._aSide;
			final String typeIPrompt = getTypeIPrompt(indexInCards, clue);
			boolean gotRightResponse = false;
			int nWrongResponses = 0;
			boolean editProperties = false;
			for (;; ++nWrongResponses) {
				System.out.print(typeIPrompt);
				final String response0 = readLine(sc);
				if (response0.length() == 1) {
					final char char0 = response0.charAt(0);
					if (char0 == 'Q') {
						editProperties = true;
						keepGoing = false;
						break;
					} else if (char0 == _EditPropertiesSymbol) {
						editProperties = true;
						keepGoing = modifyProperties(sc);
						break;
					}
				}
				if (response0.length() == 0) {
					System.out.printf("%s Did you get it right (%c or Y/N)?\n", answer,
							_ReturnSymbol);
					final String response1 = readLine(sc);
					final boolean gotItRight = response1.length() == 0
							|| Character.toUpperCase(response1.charAt(0)) == 'Y';
					if (gotItRight) {
						gotRightResponse = true;
						break;
					} else {
						++nWrongResponses;
						System.out.println("Try again.");
					}
				} else if (response0.equalsIgnoreCase(answer)) {
					gotRightResponse = true;
					break;
				}
				++nWrongResponses;
				System.out.printf("%s\n\n", answer);
			}
			if (editProperties && madeChangesFrom(oldValues)) {
				if (madeChangesFrom(oldValues)) {
					updateProperties();
					if (_printedSomething) {
						System.out.print("\n\n");
					}
					_printedSomething = true;
					System.out.printf("Properties were manually adjusted to:\n%s\n", getString());
					reWritePropertiesFile();
					oldValues = storeValues();
				}
			}
			if (gotRightResponse) {
				_quizPlus.reactToRightResponse(/* wasWrongAtLeastOnce= */nWrongResponses > 0);
			}
		}
	}

	static String CleanString(final String s) {
		if (s == null) {
			return "";
		}
		return s.trim().replaceAll(_WhiteSpace, " ");
	}

	public static void main(final String[] args) {
		final int nArgs = args.length;
		final String propertiesFilePath;
		if (nArgs > 0 && args[0].length() > 0) {
			propertiesFilePath = args[0];
		} else {
			propertiesFilePath = _DefaultPropertiesFilePath;
		}
		final File propertiesFile;
		if (propertiesFilePath.toLowerCase().endsWith(_PropertiesEnding)) {
			propertiesFile = new File(propertiesFilePath);
		} else {
			final int lastDotIndex = propertiesFilePath.lastIndexOf('.');
			if (lastDotIndex == -1) {
				propertiesFile = new File(propertiesFilePath + _PropertiesEnding);
			} else {
				propertiesFile = new File(
						propertiesFilePath.substring(0, lastDotIndex) + _PropertiesEnding);
			}
		}

		try (Scanner sc = new Scanner(System.in)) {
			final FlashCardsGame flashCardsGame = new FlashCardsGame(sc, propertiesFile);
			flashCardsGame.mainLoop(sc);
		}
	}

}
