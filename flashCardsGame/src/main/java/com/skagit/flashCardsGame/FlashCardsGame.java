package com.skagit.flashCardsGame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
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
	final static char _JulieModeSymbol = _ReturnSymbol;
	final static char _QuitSymbol = 'Q';
	final static char _EditPropertiesSymbol = 'E';
	final static char _RestartSymbol = 'R';
	final static char _B_ASymbol = 'B';
	final static String _IntroString = String.format(
			"%c=\"Julie Mode\", %c=Edit Properties, %c=Quit, %c=Restart Current Quiz",
			_JulieModeSymbol, _EditPropertiesSymbol, _QuitSymbol, _RestartSymbol);

	final static File _UserDirFile = new File(System.getProperty("user.dir"));

	/**
	 * <pre>
	 * Good article on dealing with VN diacritics.
	 * https://namnguyen1202.hashnode.dev/removing-vietnamese-diacritic-in-java
	 * </pre>
	 */
	final static String[][] _Replacements = { //
			{"[áàảãạâấầẩẫậăắằẳẵặ]", "a"}, //
			{"[ÁÀẢÃẠÂẤẦẨẪẬĂẮẰẲẴẶ]", "A"}, //
			{"[đ]", "d"}, //
			{"[Đ]", "D"}, //
			{"[éèẻẽẹêếềểễệ]", "e"}, //
			{"[ÉÈẺẼẸÊẾỀỂỄỆ]", "E"}, //
			{"[íìỉĩị]", "i"}, //
			{"[ÍÌỈĨỊ]", "I"}, //
			{"[óòỏõọôốồổỗộơớờởỡợ]", "o"}, //
			{"[ÓÒỎÕỌÔỐỒỔỖỘƠỚỜỢỠỢ]", "O"}, //
			{"[úùủũụưứừửữự]", "u"}, //
			{"[ÚÙỦŨỤƯỨỪỬỮỰ]", "U"}, //
			{"[ýỳỷỹỵ]", "y"}, //
			{"[ÝỲỶỸỴ]", "Y"}//
	};
	static final HashMap<Character, Character> _VnToEngCharMap;
	static {
		_VnToEngCharMap = new HashMap<>();
		final int nReplacePairs = _Replacements.length;
		for (int k0 = 0; k0 < nReplacePairs; ++k0) {
			final String[] pair = _Replacements[k0];
			final String vnChars = pair[0];
			final char engChar = pair[1].charAt(0);
			final int nVnChars = vnChars.length();
			for (int k1 = 0; k1 < nVnChars; ++k1) {
				_VnToEngCharMap.put(vnChars.charAt(k1), engChar);
			}
		}
	};

	final static String StripVNDiacritics(final String s0) {
		String s = s0;
		final int nReplacePairs = _Replacements.length;
		for (int k = 0; k < nReplacePairs; ++k) {
			final String[] pair = _Replacements[k];
			final String regEx = pair[0];
			final String replacement = pair[1];
			s = s.replaceAll(regEx, replacement);
		}
		return s;
	}

	final static String StripVNDiacritics2(final String s) {
		final StringBuilder sb = new StringBuilder(s);
		for (int k = 0; k < sb.length(); k++) {
			final char c = sb.charAt(k);
			final Character bigC = _VnToEngCharMap.get(c);
			if (bigC != null) {
				sb.setCharAt(k, bigC);
			}
		}
		return sb.toString();
	}

	/** Either of the following two FieldSeparators seems to work. */
	@SuppressWarnings("unused")
	final private static String _FieldSeparator0 = "\\s*\\t\\s*";
	final private static String _FieldSeparator = "(\s*\t\s*)+";
	final private static String _WhiteSpace = "\s+";
	final private static String _DefaultPropertiesFilePath = "Data/LingoDeer";
	final private static String _PropertiesEnding = ".properties";

	final private File _propertiesFile;
	final private Properties _properties;
	final private long _seed;
	boolean _quizIsA_B;
	boolean _ignoreDiacritics;
	Card[] _cards;
	final QuizGenerator _quizGenerator;
	QuizPlus _quizPlus;
	boolean _printedSomething;

	FlashCardsGame(final Scanner sc, final String[] args) {
		final int nArgs = args.length;
		final String propertiesFilePath;
		if (nArgs > 0 && args[0].length() > 0) {
			propertiesFilePath = args[0];
		} else {
			propertiesFilePath = _DefaultPropertiesFilePath;
		}
		if (propertiesFilePath.toLowerCase().endsWith(_PropertiesEnding)) {
			_propertiesFile = new File(propertiesFilePath);
		} else {
			final int lastDotIndex = propertiesFilePath.lastIndexOf('.');
			if (lastDotIndex == -1) {
				_propertiesFile = new File(propertiesFilePath + _PropertiesEnding);
			} else {
				_propertiesFile = new File(
						propertiesFilePath.substring(0, lastDotIndex) + _PropertiesEnding);
			}
		}
		_properties = new Properties();
		try (InputStreamReader isr = new InputStreamReader(
				new FileInputStream(_propertiesFile), "UTF-8")) {
			final Properties properties = new Properties();
			properties.load(isr);
			final int nPropertyPluses = PropertyPlus._Values.length;
			for (int k = 0; k < nPropertyPluses; ++k) {
				final PropertyPlus propertyPlus = PropertyPlus._Values[k];
				final String key = propertyPlus._realName;
				final Object o = properties.get(key);
				_properties.put(key, o == null ? propertyPlus._defaultStringValue : o);
			}
		} catch (final IOException e) {
		}

		_quizIsA_B = PropertyPlusToBoolean(_properties, PropertyPlus.QUIZ_TYPE);
		_ignoreDiacritics = PropertyPlusToBoolean(_properties,
				PropertyPlus.IGNORE_DIACRITICS);
		_seed = PropertyPlusToLong(_properties, PropertyPlus.SEED);
		reWritePropertiesFile();
		_printedSomething = false;
		loadCards();
		_quizGenerator = new QuizGenerator(_properties, _cards.length, _seed);
		shuffleCards(_cards);
		_quizPlus = null;
	}

	private void shuffleCards(final Card[] cards) {
		if (_seed == 0) {
			return;
		}
		final Random r = new Random();
		if (_seed > 0) {
			r.setSeed(_seed);
		}
		while (Math.abs(r.nextLong()) < Long.MAX_VALUE / 5) {
		}
		final int nCards = cards.length;
		for (int k = 0; k < nCards; ++k) {
			final int kk = k + r.nextInt(nCards - k);
			final Card card = cards[k];
			cards[k] = cards[kk];
			cards[kk] = card;
		}
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
		_properties.put(PropertyPlus.QUIZ_TYPE._realName, Boolean.toString(_quizIsA_B));
		_properties.put(PropertyPlus.IGNORE_DIACRITICS._realName,
				Boolean.toString(_ignoreDiacritics));
		_properties.put(PropertyPlus.SEED._realName, Long.toString(_seed));
		_quizGenerator.updateProperties(_properties);
	}

	void reWritePropertiesFile() {
		final Properties properties;
		final long seed = PropertyPlusToLong(_properties, PropertyPlus.SEED); //
		if (seed < 0) {
			properties = (Properties) _properties.clone();
			final int topIndexInCards0 = PropertyPlusToInt(properties, PropertyPlus.TCI);
			final int maxNNewWords = PropertyPlusToInt(properties, PropertyPlus.N_NEW_WORDS);
			final int maxNRecentWords = PropertyPlusToInt(properties,
					PropertyPlus.N_RECENT_WORDS);
			final int topIndexInCards1 = Math.min(topIndexInCards0,
					maxNNewWords + maxNRecentWords - 1);
			properties.put(PropertyPlus.TCI._realName, Long.toString(topIndexInCards1));
		} else {
			properties = _properties;
		}
		try (PrintWriter pw = new PrintWriter(new FileOutputStream(_propertiesFile))) {
			final int nPropertyPluses = PropertyPlus._Values.length;
			for (int k0 = 0; k0 < nPropertyPluses; ++k0) {
				final PropertyPlus propertyPlus = PropertyPlus._Values[k0];
				final String comment = propertyPlus._comment;
				if (comment != null && comment.length() > 0) {
					if (k0 > 0) {
						pw.println();
					}
					final String[] lines = comment.trim().split("\n");
					final int nLines = lines.length;
					for (int k1 = 0; k1 < nLines; ++k1) {
						pw.printf("! %s", lines[k1]);
						pw.println();
					}
				}
				final String realPropertyName = propertyPlus._realName;
				pw.printf("%s=%s", realPropertyName, properties.get(realPropertyName));
				pw.println();
			}
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
		return s + String.format(": %s ", clue);
	}

	final String getTypeIIPrompt() {
		final String prompt = String.format("Enter: %c=Done, R=Restart same quiz ",
				_ReturnSymbol);
		return prompt + _quizGenerator.getTypeIIPrompt();
	}

	void modifyProperties(final Scanner sc) {
		for (;;) {
			final String prompt = getTypeIIPrompt();
			System.out.printf("%s: ", prompt);
			final String myLine = readLine(sc).toUpperCase().trim();
			final String[] fields = myLine.trim().split(_WhiteSpace);
			final String field0 = (fields == null || fields.length == 0
					|| fields[0].length() == 0) ? "" : fields[0];
			final int field0Len = field0.length();
			if (field0Len == 0) {
				/** Done editing. */
				return;
			} else {
				_quizGenerator.modifySingleProperty(fields);
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

	private static String PropertyPlusToString(final Properties properties,
			final PropertyPlus propertyPlus) {
		return CleanString((String) properties.get(propertyPlus._realName));
	}

	static int PropertyPlusToInt(final Properties properties,
			final PropertyPlus propertyPlus) {
		final String s = PropertyPlusToString(properties, propertyPlus);
		final String[] fields = s.split(_WhiteSpace);
		final int defaultValue = Integer.parseInt(propertyPlus._defaultStringValue);
		return fieldsToInt(fields, defaultValue);
	}

	static long PropertyPlusToLong(final Properties properties,
			final PropertyPlus propertyPlus) {
		final String s = PropertyPlusToString(properties, propertyPlus);
		final String[] fields = s.split(_WhiteSpace);
		final long defaultValue = Long.parseLong(propertyPlus._defaultStringValue);
		return fieldsToLong(fields, defaultValue);
	}

	static boolean PropertyPlusToBoolean(final Properties properties,
			final PropertyPlus propertyPlus) {
		final String s = PropertyPlusToString(properties, propertyPlus);
		final String[] fields = s.split(_WhiteSpace);
		final String dsv = propertyPlus._defaultStringValue;
		final char char0 = (dsv == null || dsv.length() == 0)
				? 'F'
				: Character.toUpperCase(dsv.charAt(0));
		final boolean defaultValue = char0 == 'T' || char0 == 'Y';
		return fieldsToBoolean(fields, defaultValue);
	}

	static TypeOfDecay PropertyPlusToTypeOfDecay(final Properties properties,
			final PropertyPlus propertyPlus) {
		final String s = PropertyPlusToString(properties, propertyPlus);
		final String[] fields = s.split(_WhiteSpace);
		final TypeOfDecay defaultValue = TypeOfDecay
				.valueOf(propertyPlus._defaultStringValue);
		return fieldsToTypeOfDecay(fields, defaultValue);
	}

	static int PropertyPlusToPercentI(final Properties properties,
			final PropertyPlus propertyPlus) {
		final String s = PropertyPlusToString(properties, propertyPlus);
		final String[] fields = s.split(_WhiteSpace);
		final String defaultStringValue = propertyPlus._defaultStringValue;
		final int defaultValue = Integer
				.parseInt(defaultStringValue.substring(0, defaultStringValue.length() - 1));
		return fieldsToPercentI(fields, defaultValue);
	}

	static int fieldsToInt(final String[] fields, final int defaultValue) {
		for (final String field : fields) {
			try {
				return Integer.parseInt(field);
			} catch (final NumberFormatException e) {
			}
		}
		return defaultValue;
	}

	static long fieldsToLong(final String[] fields, final long defaultValue) {
		for (final String field : fields) {
			try {
				return Long.parseLong(field);
			} catch (final NumberFormatException e) {
			}
		}
		return defaultValue;
	}

	static boolean fieldsToBoolean(final String[] fields, final boolean defaultValue) {
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
		return defaultValue;
	}

	static TypeOfDecay fieldsToTypeOfDecay(final String[] fields,
			final TypeOfDecay defaultValue) {
		for (final String field : fields) {
			for (final TypeOfDecay typeOfDecay : TypeOfDecay._Values) {
				if (field.equalsIgnoreCase(typeOfDecay.name())) {
					return typeOfDecay;
				}
			}
		}
		return defaultValue;
	}

	static int fieldsToPercentI(final String[] fields, final int defaultValue) {
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
		return defaultValue;
	}

	final String getString() {
		return String.format("%s: %c%c%c%s RandomSeed[%d], %s", getCoreFilePath(),
				_quizIsA_B ? 'A' : 'B', _RtArrow, _quizIsA_B ? 'B' : 'A', //
				_ignoreDiacritics ? ",IgnoreDiacritics" : "", //
				_seed, _quizGenerator.getString());
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
		final int nCards = _cards.length;
		long[] oldValues = storeValues();
		_quizPlus = null;
		boolean restarted = false;
		OUTSIDE_LOOP : for (boolean keepGoing = true; keepGoing;) {
			/** Check for a status change from _quizPlus. */
			final QuizPlusTransition quizPlusTransition = _quizGenerator.getStatusChange(nCards,
					restarted, _quizPlus);
			_quizPlus = quizPlusTransition._newQuizPlus;
			restarted = false;
			final TypeOfChange typeOfChange = quizPlusTransition._typeOfChange;
			if (typeOfChange != TypeOfChange.NO_CHANGE) {
				if (_printedSomething) {
					System.out.println();
					System.out.println();
				}
				_printedSomething = true;
				System.out.println(getString());
				System.out.println(_IntroString);
				System.out.print(quizPlusTransition._reasonForChangeString);
				System.out.println(" " + quizPlusTransition._transitionString);
				System.out.println();
			}
			if (madeChangesFrom(oldValues)) {
				updateProperties();
				reWritePropertiesFile();
				oldValues = storeValues();
			}

			/** INSIDE_LOOP is to get an answer to a clue. */
			final int indexInCards = _quizPlus.getCurrentQuiz_IndexInCards();
			final Card card = _cards[indexInCards];
			final String clue = _quizIsA_B ? card._aSide : card._bSide;
			final String answer = _quizIsA_B ? card._bSide : card._aSide;
			final String typeIPrompt = getTypeIPrompt(indexInCards, clue);
			boolean gotItRight = false;
			int nWrongResponses = 0;
			INSIDE_LOOP : for (; !gotItRight; ++nWrongResponses) {
				System.out.print(typeIPrompt);
				final String response0 = readLine(sc);
				if (response0.length() == 1) {
					/** Either quit, restart, or edit the properties. */
					final char char0 = response0.charAt(0);
					if (char0 == _QuitSymbol) {
						keepGoing = false;
						break INSIDE_LOOP;
					} else if (char0 == _EditPropertiesSymbol) {
						modifyProperties(sc);
						if (madeChangesFrom(oldValues)) {
							continue OUTSIDE_LOOP;
						}
						continue INSIDE_LOOP;
					} else if (char0 == _RestartSymbol) {
						_quizPlus.resetForFullMode();
						restarted = true;
						continue OUTSIDE_LOOP;
					}
				}
				if (response0.length() == 0) {
					System.out.printf("%c%s%c Did you get it right (%c or Y/N)?\n", _RtArrow,
							answer, _LtArrow, _ReturnSymbol);
					final String response1 = readLine(sc);
					gotItRight = response1.length() == 0
							|| Character.toUpperCase(response1.charAt(0)) == 'Y';
				} else {
					final boolean gotItPartiallyWrong;
					if (_ignoreDiacritics) {
						final String response0Stripped = StripVNDiacritics(response0);
						final String answerStripped = StripVNDiacritics(answer);
						gotItRight = response0Stripped.equalsIgnoreCase(answerStripped);
						gotItPartiallyWrong = !response0.equalsIgnoreCase(answer);
					} else {
						gotItRight = response0.equalsIgnoreCase(answer);
						gotItPartiallyWrong = !gotItRight;
					}
					if (!gotItRight) {
						System.out.printf("%c%s%c\n\n", _RtArrow, answer, _LtArrow);
					} else if (gotItPartiallyWrong) {
						System.out.printf("NB: %s\n\n", answer);
					}
				}
				if (!gotItRight) {
					++nWrongResponses;
				}
				if (gotItRight) {
					_quizPlus.reactToRightResponse(/* wasWrongAtLeastOnce= */nWrongResponses > 0);
				}
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
		try (Scanner sc = new Scanner(System.in)) {
			final FlashCardsGame flashCardsGame = new FlashCardsGame(sc, args);
			flashCardsGame.mainLoop(sc);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
