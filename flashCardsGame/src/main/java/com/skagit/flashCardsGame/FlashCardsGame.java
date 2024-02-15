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

	final static char _LtArrow = '\u2190';
	final static char _RtArrow = '\u2192';
	final static char _EmptySetChar = '\u2205';
	final static char _CheckSymbol = '\u2714';

	final private static char _ReturnChar = '\u23CE';
	final private static char _QuitChar = 'Q';
	final private static char _EditPropertiesChar = 'E';
	final private static char _YesChar = 'Y';
	final private static char _NoChar = 'N';
	final private static char _RestartChar = 'R';

	/**
	 * <pre>
	 * Good article on dealing with VN diacritics.
	 * https://namnguyen1202.hashnode.dev/removing-vietnamese-diacritic-in-java
	 * </pre>
	 */
	static final HashMap<Character, Character> _VnToEngCharMap = new HashMap<>() {
		private static final long serialVersionUID = 1L;
		{
			final String[][] mappings = { //
					{"áàảãạâấầẩẫậăắằẳẵặ", "a"}, //
					{"đ", "d"}, //
					{"éèẻẽẹêếềểễệ", "e"}, //
					{"íìỉĩị", "i"}, //
					{"óòỏõọôốồổỗộơớờởỡợ", "o"}, //
					{"úùủũụưứừửữự", "u"}, //
					{"ýỳỷỹỵ", "y"}, //
			};
			final int nPairs = mappings.length;
			for (int k0 = 0; k0 < nPairs; ++k0) {
				final String[] pair = mappings[k0];
				final char engChar = pair[1].charAt(0);
				final char engCharUc = Character.toUpperCase(engChar);
				final String vnChars = pair[0];
				final int nVnChars = vnChars.length();
				for (int k1 = 0; k1 < nVnChars; ++k1) {
					final char vnChar = vnChars.charAt(k1);
					final char vnCharUc = Character.toUpperCase(vnChar);
					put(vnChar, engChar);
					put(vnCharUc, engCharUc);
				}
			}
		}
	};

	final static String StripVNDiacritics(final String s) {
		final StringBuilder sb = new StringBuilder(s);
		for (int k = 0; k < sb.length(); k++) {
			final char c = sb.charAt(k);
			final Character target = _VnToEngCharMap.get(c);
			if (target != null) {
				sb.setCharAt(k, target);
			}
		}
		return sb.toString();
	}

	/** Either of the following two FieldSeparators seems to work. */
	@SuppressWarnings("unused")
	final private static String _FieldSeparator0 = "\\s*\\t\\s*";
	final private static String _FieldSeparator = "(\s*\t\s*)+";
	final static String _WhiteSpace = "\s+";
	final private static String _DefaultPropertiesFilePath = "Data/LingoDeer";
	final private static String _PropertiesEnding = ".properties";

	final private File _propertiesFile;
	final private Properties _properties;
	final private long _seed;
	boolean _quizIsA_B, _ignoreDiacritics;
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
		announceAndClumpDuplicates();
		writeCardsToDisk();
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
				 * If the first character is NOT_HONOR_MODE feff, go back to the beginning of the
				 * file. If it IS feff, ignore it and continue on.
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
							System.out.println();
							System.out.println();
						}
						System.out.println("Merging");
						System.out.println("newCard.getString()" + " into");;
						System.out.print(oldCard.getString());
						_printedSomething = true;
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
						System.out.println();
						System.out.println();
					}
					System.out.println(String.format("Duplicate %s:", sideBeingChecked));
					System.out.println(oldCard.getString());
					System.out.print(card.getString());
					_printedSomething = true;
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
		_properties.put(PropertyPlus.SEED._realName, Long.toString(_seed));
		_properties.put(PropertyPlus.QUIZ_TYPE._realName, Boolean.toString(_quizIsA_B));
		_properties.put(PropertyPlus.IGNORE_DIACRITICS._realName,
				Boolean.toString(_ignoreDiacritics));
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
				_ReturnChar);
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
			if (myLine.length() == 0 && field0Len == 0) {
				/** Done editing. */
				return;
			} else if (field0Len == 0) {
				continue;
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
		return CleanString(sc.nextLine());
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
				/** A new quiz is worth a double line feed. */
				if (_printedSomething) {
					System.out.println();
					System.out.println();
				}
				_printedSomething = true;
				System.out.println(getString());
				System.out.println(getIntroString());
				System.out.print(quizPlusTransition._reasonForChangeString);
				System.out.println(" " + quizPlusTransition._transitionString);
				/** An extra line feed before the first question of this quiz. */
				System.out.println();
			}
			if (madeChangesFrom(oldValues)) {
				updateProperties();
				reWritePropertiesFile();
				oldValues = storeValues();
			}

			final int indexInCards = _quizPlus.getCurrentQuiz_IndexInCards();
			final Card card = _cards[indexInCards];
			final String clue = CleanString(_quizIsA_B ? card._aSide : card._bSide);
			final String answer = CleanString(_quizIsA_B ? card._bSide : card._aSide);
			final String typeIPrompt = getTypeIPrompt(indexInCards, clue);
			boolean gotItRight = false;
			int nWrongResponses = 0;
			for (; !gotItRight; ++nWrongResponses) {
				System.out.print(typeIPrompt);
				final String response = readLine(sc);
				final HonorResult honorResult;
				String honorResponse = null;
				if (response.length() == 0) {
					System.out.printf("%c%s%c Did you get it right (%c=Yes, %c=No)? ", _RtArrow,
							answer, _LtArrow, _ReturnChar, _NoChar);
					honorResponse = readLine(sc);
					final boolean haveBlankHonorResponse = honorResponse.length() == 0;
					if (!haveBlankHonorResponse
							&& Character.toUpperCase(honorResponse.charAt(0)) == _NoChar) {
						honorResult = HonorResult.WRONG;
					} else {
						honorResult = HonorResult.RIGHT;
					}
				} else {
					honorResult = HonorResult.NOT_HONOR_MODE;
					if (response.length() == 1) {
						/**
						 * quit, restart, edit the properties, or fall through, letting this be a bona
						 * fide response (such as ở).
						 */
						final char char0Uc = Character.toUpperCase(response.charAt(0));
						if (char0Uc == _QuitChar) {
							keepGoing = false;
							return;
						} else if (char0Uc == _EditPropertiesChar) {
							modifyProperties(sc);
							continue OUTSIDE_LOOP;
						} else if (char0Uc == _RestartChar) {
							_quizPlus.resetForFullMode();
							restarted = true;
							continue OUTSIDE_LOOP;
						}
					}
				}
				final DiffReport diffReport = new DiffReport(honorResult, _ignoreDiacritics,
						response, answer);
				final String diffString = diffReport._diffString;
				gotItRight = diffReport._gotItRight;
				if (diffString != null) {
					System.out.print(diffString);
					if (gotItRight && !diffReport._gotItExactlyRight) {
						System.out.printf(".  Count as wrong? %c=No,%c=Yes ", _ReturnChar, _YesChar);
						/**
						 * There's an automatic line feed in readLine so this block of code does not
						 * provide a line feed.
						 */
						final String countAsWrongResponse = readLine(sc);
						final boolean emptyLine = countAsWrongResponse.length() == 0;
						final boolean countAsWrong = !emptyLine
								&& Character.toUpperCase(countAsWrongResponse.charAt(0)) == _YesChar;
						if (countAsWrong) {
							gotItRight = false;
						}
					} else {
						/**
						 * There is no automatic line feed and we have a diffString. We must provide a
						 * line feed.
						 */
						System.out.println();
					}
					/** With a diffString, we need an extra line before the next question. */
					System.out.println();
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

	String getIntroString() {
		return String.format(
				"%c=\"Honor Mode,\" %c=Edit Properties, %c=Quit, %c=Restart Current Quiz",
				_ReturnChar, _EditPropertiesChar, _QuitChar, _RestartChar);
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
