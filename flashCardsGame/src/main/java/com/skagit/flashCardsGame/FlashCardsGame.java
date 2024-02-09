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

	private static final String _DelimiterRegEx = "[\n\r\t]+";

	private final File _propertiesFile;
	private final Properties _properties;

	boolean _quizIsA_B;

	Card[] _cards;

	final QuizGenerator _quizGenerator;
	QuizPlus _quizPlus;

	FlashCardsGame(final Scanner sc, final String propertiesPath) {
		/** Load the properties. */
		_propertiesFile = new File(propertiesPath);
		_properties = new Properties();
		try (
				InputStreamReader in = new InputStreamReader(new FileInputStream(_propertiesFile),
						"UTF-8")) {
			_properties.load(in);
		} catch (final IOException e) {
		}

		/** Get _quizType. */
		final String quizTypeProperty = keyToString(_properties, "Quiz.Type");
		_quizIsA_B = quizTypeProperty.length() == 0
				|| Character.toUpperCase(quizTypeProperty.charAt(0)) == 'A';

		/** Write out _propertiesFile. */
		reWritePropertiesFile();

		loadCards();
		_quizGenerator = new QuizGenerator(_properties, /* nCards= */_cards.length, _Seed);
		_quizPlus = null;
	}

	void myPrintln(final PrintWriter pw, final String s) {
		pw.println(s);
	}

	void myPrint(final PrintWriter pw, final String s) {
		pw.print(s);
	}

	private void writeCardsToDisk() {
		final int nCards = _cards.length;
		final int nDigits = (int) (Math.log10(nCards) + 1d);
		final String cardIndexFormat = String.format("%%%dd.", nDigits);
		int maxASideLen = 0;
		for (final Card card : _cards) {
			maxASideLen = Math.max(maxASideLen, card._aSide.trim().length());
		}
		final String aSideFormat = String.format("%%-%ds", maxASideLen + 1);
		final File cardsFile = getCardsFile();
		try (PrintWriter pw = new PrintWriter(cardsFile)) {
			for (int k = 0; k < nCards; ++k) {
				final Card card = _cards[k];
				final String cardIndexString = String.format(cardIndexFormat, card._cardIndex);
				final String aSideString = String.format(aSideFormat, card._aSide + ":");
				String comment = card._comment;
				boolean printedBlankLine = false;
				if (k > 0) {
					if (k % 5 == 0) {
						myPrintln(pw, "");
						printedBlankLine = true;
					}
					myPrintln(pw, "");
				}
				if (comment != null) {
					comment = comment.trim();
					if (comment.length() > 0) {
						if (k > 0 && !printedBlankLine) {
							myPrintln(pw, "");
						}
						myPrintln(pw, comment);
					}
				}
				final String s = String.format("%s\t%s\t%s", cardIndexString, aSideString,
						card._bSide);
				myPrint(pw, s);
			}
		} catch (final FileNotFoundException e) {
		}
	}

	private File getCardsFile() {
		final String name = _propertiesFile.getName();
		final File parentFile = _propertiesFile.getParentFile();
		final String cardsFilePath = name.substring(0, name.length() - ".properties".length())
				+ ".txt";
		return new File(parentFile, cardsFilePath);
	}

	/**
	 * <pre>
	 * Interesting note on text files and the "right" way to do things:
	 * https://stackoverflow.com/questions/17405165/first-character-of-the-reading-from-the-text-file-%C3%AF
	 * </pre>
	 */
	private void loadCards() {
		/** Get the Cards. */
		final TreeMap<Card, Card> cardMap = new TreeMap<Card, Card>(new Comparator<>() {

			@Override
			public int compare(final Card card0, final Card card1) {
				final int compareValue = card0._aSide.compareToIgnoreCase(card1._aSide);
				if (compareValue != 0) {
					return compareValue;
				}
				return card0._bSide.compareToIgnoreCase(card1._bSide);
			}
		});
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
					final String[] fields = line.split("\\s*\\t\\s*");
					final int nFields = fields.length;
					final int cardIndex = cardMap.size();
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
					final Card oldCard = cardMap.get(newCard);
					if (oldCard != null) {
						System.out.printf("\n\nMerging\n%s into\n%s", newCard.getString(),
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
			announceAndClumpDuplicates();
			writeCardsToDisk();
		}
	}

	static final Comparator<Card> _CheckForADups = new Comparator<>() {

		@Override
		public int compare(final Card card0, final Card card) {
			return card0._aSide.compareTo(card._aSide);
		}
	};

	static final Comparator<Card> _CheckForBDups = new Comparator<>() {

		@Override
		public int compare(final Card card0, final Card card) {
			return card0._bSide.compareTo(card._bSide);
		}
	};

	private void announceAndClumpDuplicates() {
		final int nCards = _cards.length;
		for (int iPass = 0; iPass < 2; ++iPass) {
			final Comparator<Card> comparator = iPass == 0 ? _CheckForADups : _CheckForBDups;
			final TreeMap<Card, Card> map = new TreeMap<>(comparator);
			final TreeMap<Card, ArrayList<Card>> kings = new TreeMap<>(comparator);
			for (int k = 0; k < nCards; ++k) {
				final Card card = _cards[k];
				final Card oldCard = map.ceilingKey(card);
				if (oldCard != null && comparator.compare(oldCard, card) == 0) {
					System.out.printf("\n\nDuplicate %s:\n%s and \n%s",
							iPass == 0 ? "A-Side" : "B-Side", card.getString(), oldCard.getString());
					ArrayList<Card> slaves = kings.get(oldCard);
					if (slaves == null) {
						slaves = new ArrayList<>();
						kings.put(oldCard, slaves);
					}
					slaves.add(card);
					_cards[k] = null;
				}
			}
			final Card[] newCards = new Card[nCards];
			for (int k0 = 0, k1 = 0; k0 < nCards; ++k0) {
				final Card card = _cards[k0];
				if (card == null) {
					continue;
				}
				card._cardIndex = k1;
				newCards[k1] = card;
				++k1;
				final ArrayList<Card> slaves = kings.get(card);
				if (slaves != null) {
					final int nSlaves = slaves.size();
					for (int k2 = 0; k2 < nSlaves; ++k2) {
						final Card slave = slaves.get(k2);
						slave._cardIndex = k1;
						newCards[k1] = slave;
						++k1;
					}
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
				reWritePropertiesFile();
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
					reWritePropertiesFile();
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
			final String arg0 = args[0];
			final String propertiesPath;
			if (arg0.toLowerCase().endsWith(".properties")) {
				propertiesPath = arg0;
			} else {
				final int lastDotIndex = arg0.lastIndexOf('.');
				if (lastDotIndex == -1) {
					propertiesPath = arg0 + ".properties";
				} else {
					propertiesPath = arg0.substring(0, lastDotIndex) + ".properties";
				}
			}
			final FlashCardsGame flashCardsGame = new FlashCardsGame(sc, propertiesPath);
			flashCardsGame.mainLoop(sc);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
