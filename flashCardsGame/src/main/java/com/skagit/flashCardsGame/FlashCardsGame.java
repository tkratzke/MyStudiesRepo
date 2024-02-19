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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

public class FlashCardsGame {

	final static char _LtArrowChar = '\u2190';
	final static char _RtArrowChar = '\u2192';
	final static char _EmptySetChar = '\u2205';
	final static char _HeavyCheckChar = '\u2714';
	final static char _TabChar = '\u2409';

	final private static char _ReturnChar = '\u23CE';
	final private static char _QuitChar = '!';
	final private static char _EditPropertiesChar = '@';
	final private static char _RestartChar = '#';
	final private static char _YesChar = 'Y';
	final private static char _NoChar = 'N';
	final private static String _YesString = "Yes";
	final static String _NoString = "No";
	final static String _RegExForPunct = "[,.;:?!@#$%^&*]+";
	final private static int _LongLine = 100;
	final private static int _BlockSize = 10;

	final private static String _HelpString = String.format(
			"%c=\"Honor Mode,\" %c=Edit Properties, %c=Quit, %c=Restart Current Quiz, %s=Next Line is Continuation",
			_ReturnChar, _EditPropertiesChar, _QuitChar, _RestartChar,
			"" + _TabChar + _ReturnChar);

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
	final private static String _FieldSeparator = "\\s*\\t\\s*";
	@SuppressWarnings("unused")
	final private static String _FieldSeparator1 = "(\s*\t\s*)+";
	final private static String _PropertiesEnding = ".properties";
	final static String _WhiteSpace = "\s+";

	final private File _propertiesFile;
	final private Properties _properties;
	final private long _seed;
	final private boolean _quizIsA_B, _ignoreVnDiacritics;
	final private Card[] _cards;
	final private QuizGenerator _quizGenerator;
	private QuizPlus _quizPlus;
	private boolean _needLineFeed;

	FlashCardsGame(final Scanner sc, final String[] args) {
		_needLineFeed = false;
		final String propertiesFilePath = args[0];
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
		_ignoreVnDiacritics = PropertyPlusToBoolean(_properties,
				PropertyPlus.IGNORE_VN_DIACRITICS);
		_seed = PropertyPlusToLong(_properties, PropertyPlus.SEED);
		reWritePropertiesFile();
		final TreeMap<Card, Card> cardMap = loadCardMap();
		final int nCards = cardMap.size();
		_cards = cardMap.keySet().toArray(new Card[nCards]);
		Arrays.sort(_cards, Card._ByCardNumberOnly);
		final int[] dupCounts = announceAndClumpDuplicates();
		reWriteCardsFile();
		final int nADups = dupCounts[1];
		final int nBDups = dupCounts[0];
		if (nADups > 0 || nBDups > 0) {
			if (_needLineFeed) {
				System.out.println();
			}
			System.out.println(String.format("nADups=%d nBDups = %d.", nADups, nBDups));
		}
		_quizGenerator = new QuizGenerator(_properties, _cards.length, _seed);
		shuffleCards(_cards);
		_quizPlus = null;
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

	private TreeMap<Card, Card> loadCardMap() {
		final TreeMap<Card, Card> cardMap = new TreeMap<>(Card._ByAThenB);
		final File cardsFile = getCardsFile();
		try (final BufferedReader in = new BufferedReader(
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

			String aSide = "";
			String bSide = "";
			try (final Scanner fileSc = new Scanner(in)) {
				while (fileSc.hasNext()) {
					final String nextLine = fileSc.nextLine().stripTrailing();
					final String[] rawFields = nextLine.split(_FieldSeparator);
					final int nFields = rawFields.length;
					boolean haveSomething = false;
					for (int k = 0; k < nFields; ++k) {
						if (!KillPunct(rawFields[k]).isBlank()) {
							haveSomething = true;
							break;
						}
					}
					if (!haveSomething) {
						continue;
					}
					final String rawField0 = rawFields[0];
					final int cardNumber = stringToInt(rawField0);
					if (cardNumber >= 0) {
						if (nFields == 1) {
							/**
							 * A number and nothing else doesn't count for anything; even as a
							 * terminator of the previous Card.
							 */
							continue;
						}
						/**
						 * nextLine starts with a number and has at least one other field. Wrap up the
						 * old entry.
						 */
						wrapUp(cardMap, aSide, bSide);
						aSide = "";
						bSide = "";
						if (nFields >= 3) {
							aSide = augment(aSide, rawFields[1]);
							bSide = augment(bSide, rawFields[2]);
							continue;
						}
						/**
						 * nFields must be 2, and there is a number field. If after the number field,
						 * there are at least 2 tabs before the next field, this is b-side. Otherwise,
						 * it is a-side.
						 */
						int nTabs = 0;
						/** Count the tabs after field0 and before the next field. */
						for (int k = nextLine.indexOf(rawField0) + rawField0.length();; ++k) {
							final char c = nextLine.charAt(k);
							if (!Character.isWhitespace(c)) {
								break;
							}
							if (c == '\t') {
								if (++nTabs == 2) {
									break;
								}
							}
						}
						if (nTabs == 1) {
							aSide = augment(aSide, rawFields[1]);
						} else {
							bSide = augment(bSide, rawFields[1]);
						}
					} else {
						/**
						 * A continuation line. If there's nothing to continue, we skip it.
						 * Furthermore, nFields must be at least 2 because we demand a tab to get past
						 * the vacant number field.
						 */
						if (aSide.length() > 0 || bSide.length() > 0 || nFields < 2) {
							final String rawField1 = rawFields[1];
							if (nFields >= 3) {
								aSide = augment(aSide, rawField1);
								bSide = augment(bSide, rawFields[2]);
							} else {
								/**
								 * There is one field and it must be added to aSide or bSide. Count the
								 * tabs from the beginning of the line to the first field.
								 */
								int nTabs = 0;
								for (int k = 0;; ++k) {
									final char c = nextLine.charAt(k);
									if (!Character.isWhitespace(c)) {
										break;
									}
									if (c == '\t') {
										if (++nTabs == 2) {
											break;
										}
									}
								}
								if (nTabs == 1) {
									aSide = augment(aSide, rawField1);
								} else {
									bSide = augment(bSide, rawField1);
								}
							}
						}
					}
				}
				wrapUp(cardMap, aSide, bSide);
				aSide = "";
				bSide = "";
			} catch (final Exception e) {
			}
		} catch (final IOException e) {
		}
		return cardMap;
	}

	private static String augment(final String oldS, final String newS) {
		return CleanWhiteSpace(oldS + (oldS.length() > 0 ? " " : "") + newS);
	}

	private static void wrapUp(final TreeMap<Card, Card> cardMap, final String aSide,
			final String bSide) {
		if (aSide.length() > 0 && bSide.length() > 0) {
			final Card card = new Card(cardMap.size(), aSide, bSide);
			cardMap.put(card, card);
		}
	}

	static private int stringToInt(String s) {
		int len = s == null ? 0 : s.length();
		if (len == 0) {
			return -1;
		}
		s = s.trim();
		len = s.length();
		if (s.charAt(len - 1) == '.') {
			s = s.substring(0, len - 1);
		}
		try {
			return Integer.parseInt(s);
		} catch (final NumberFormatException e) {
			return -1;
		}
	}

	private int[] announceAndClumpDuplicates() {
		final int nCards = _cards.length;
		final int[] dupCounts = new int[]{0, 0};
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
					if (_needLineFeed) {
						System.out.println();
					}
					System.out.println(String.format("Duplicate %s #%d:", sideBeingChecked,
							dupCounts[1 - iPass]++));
					System.out.println(oldCard.getString());
					System.out.println(card.getString());
					_needLineFeed = true;
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
		return dupCounts;
	}

	private void reWriteCardsFile() {
		final int nCards = _cards.length;
		final int nDigits = (int) (Math.log10(nCards) + 1d);
		final String cardNumberFormat = String.format("%%%dd", nDigits);
		int maxASideLen = 0;
		for (final Card card : _cards) {
			maxASideLen = Math.max(maxASideLen, card._aParts._maxLen);
		}
		final String aPartFormat = String.format("%%-%ds", maxASideLen);
		final String blankNumberFormat = String.format("%%-%ds", nDigits);
		final String blankNumberString = String.format(blankNumberFormat, "");
		final File cardsFile = getCardsFile();
		try (PrintWriter pw = new PrintWriter(cardsFile)) {
			boolean recentWasMultiLine = false;
			for (int k0 = 0, nPrinted = 0; k0 < nCards; ++k0) {
				final Card card = _cards[k0];
				final ArrayList<String> aParts = card._aParts;
				final ArrayList<String> bParts = card._bParts;
				final int nAParts = aParts.size();
				final int nBParts = bParts.size();
				final int nParts = Math.max(nAParts, nBParts);
				if (k0 > 0) {
					if ((nParts > 1 || recentWasMultiLine || (nPrinted % _BlockSize == 1))) {
						pw.println();
					}
				}
				++nPrinted;
				recentWasMultiLine = nParts > 1;
				for (int k1 = 0; k1 < nParts; ++k1) {
					final String aPart = k1 < nAParts ? aParts.get(k1) : "";
					if (k1 == 0) {
						pw.printf(cardNumberFormat, card._cardNumber);
					} else {
						pw.print(blankNumberString);
					}
					pw.print("\t");
					pw.printf(aPartFormat, aPart);
					if (k1 < nBParts) {
						pw.print("\t");
						pw.print(bParts.get(k1));
					}
					pw.println();
				}
			}
		} catch (final FileNotFoundException e) {
		}
	}

	void updateProperties() {
		_properties.put(PropertyPlus.SEED._realName, Long.toString(_seed));
		_properties.put(PropertyPlus.QUIZ_TYPE._realName, Boolean.toString(_quizIsA_B));
		_properties.put(PropertyPlus.IGNORE_VN_DIACRITICS._realName,
				Boolean.toString(_ignoreVnDiacritics));
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

	final private String getTypeIPrompt(final int indexInCards) {
		String typeIPrompt = "";
		final int currentIndexInQuiz = _quizPlus.getCurrentIndexInQuiz();
		if (_quizPlus.isCriticalQuizIndex(currentIndexInQuiz)) {
			typeIPrompt += "*";
		}
		final int cardNumber = _cards[indexInCards]._cardNumber;
		final int quizLen = _quizPlus.getCurrentQuizLen();
		typeIPrompt += String.format("%d of %d(IIC=%d,#%d)", currentIndexInQuiz + 1, quizLen,
				indexInCards, cardNumber);
		final int nRights = _quizPlus.getNRights();
		final int nWrongs = _quizPlus.getNWrongs();
		final int nTrials = nRights + nWrongs;
		if (nTrials > 0) {
			final long successRateI = Math.round((100d * nRights) / nTrials);
			typeIPrompt += String.format(",(#Rt/Wr=%d/%d SccRt=%d%%)", nRights, nWrongs,
					successRateI);
		}
		return typeIPrompt;
	}

	final private String getTypeIIPrompt() {
		final String prompt = String.format("Enter: %c=Done, R=Restart same quiz ",
				_ReturnChar);
		return prompt + _quizGenerator.getTypeIIPrompt();
	}

	final private void modifyProperties(final Scanner sc) {
		for (;;) {
			if (_needLineFeed) {
				System.out.println();
			}
			final String prompt = getTypeIIPrompt();
			System.out.print(prompt);
			System.out.print(": ");
			final InputString inputString = new InputString(sc);
			final String myLine = inputString._inputString.toUpperCase();
			_needLineFeed = !inputString._lastLineWasBlank;
			if (myLine.length() == 0) {
				return;
			}
			final String[] fields = myLine.split(_WhiteSpace);
			/**
			 * Currently, we have no properties of our own to edit, so we immediately turn it
			 * over to _quizGenerator.
			 */
			_quizGenerator.modifySingleProperty(fields);
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
		return CleanWhiteSpace((String) properties.get(propertyPlus._realName));
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
		return String.format("%s: %c%c%c%s RandomSeed[%d] \n%s", //
				getCoreFilePath(), //
				_quizIsA_B ? 'A' : 'B', _RtArrowChar, _quizIsA_B ? 'B' : 'A', //
				_ignoreVnDiacritics ? //
						(" " + PropertyPlus.IGNORE_VN_DIACRITICS._comment) : //
						"", //
				_seed, _quizGenerator.getString());
	}

	@Override
	public String toString() {
		return getString();
	}

	final static private EnumSet<TypeOfChange> _NewQuizSet = EnumSet.of(//
			TypeOfChange.CRITICAL_ONLY_WIN, //
			TypeOfChange.MOVE_ON_WIN, //
			TypeOfChange.LOSS, //
			TypeOfChange.NOTHING_TO_SOMETHING, //
			TypeOfChange.PARAMETERS_CHANGED, //
			TypeOfChange.RESTART//
	);

	final static private EnumSet<TypeOfChange> _ReallyNewQuizSet = EnumSet.of(//
			TypeOfChange.MOVE_ON_WIN, //
			TypeOfChange.NOTHING_TO_SOMETHING, //
			TypeOfChange.PARAMETERS_CHANGED //
	);

	void mainLoop(final Scanner sc) {
		final int nCards = _cards.length;
		long[] oldValues = storeValues();
		_quizPlus = null;
		boolean restarted = false;
		boolean needHelpString = true;

		OUTSIDE_LOOP : for (boolean keepGoing = true; keepGoing;) {
			/** Check for a status change from _quizPlus. */
			final QuizPlusTransition quizPlusTransition = _quizGenerator.getStatusChange(nCards,
					restarted, _quizPlus);
			_quizPlus = quizPlusTransition._newQuizPlus;
			restarted = false;
			final TypeOfChange typeOfChange = quizPlusTransition._typeOfChange;
			if (_NewQuizSet.contains(typeOfChange)) {
				System.out.println();
				if (_needLineFeed) {
					System.out.println();
				}
				if (_ReallyNewQuizSet.contains(typeOfChange)) {
					/** For a really new one, add another lineFeed. */
					System.out.println();
					System.out.println(getString());
					needHelpString = true;
				} else {
					needHelpString = !needHelpString;
				}
				if (needHelpString) {
					System.out.println(_HelpString);
				}
				System.out.print(typeOfChange._reasonForChangeString);
				System.out.println(" " + quizPlusTransition._transitionString);
				_needLineFeed = true;
			}
			if (madeChangesFrom(oldValues)) {
				updateProperties();
				reWritePropertiesFile();
				oldValues = storeValues();
			}

			final int indexInCards = _quizPlus.getCurrentQuiz_IndexInCards();
			final Card card = _cards[indexInCards];
			final ArrayList<String> clueParts = _quizIsA_B ? card._aParts : card._bParts;
			final String clue = CleanWhiteSpace(_quizIsA_B ? card._fullASide : card._fullBSide);
			final String answer = CleanWhiteSpace(
					_quizIsA_B ? card._fullBSide : card._fullASide);
			final String typeIPrompt0 = getTypeIPrompt(indexInCards);
			final int len = typeIPrompt0.length() + 2 + clue.length() + 1 + 1 + answer.length();
			boolean wasWrongAtLeastOnce = false;
			for (boolean gotItRight = false; !gotItRight;) {
				if (_needLineFeed) {
					System.out.println();
				}
				final int clueListSize = clueParts.size();
				final boolean longQuestion;
				if (len >= _LongLine || clueListSize > 1) {
					System.out.println(typeIPrompt0);
					for (int k = 0; k < clueListSize; ++k) {
						System.out.print("\t" + clueParts.get(k));
						if (k < clueListSize - 1) {
							System.out.println();
						} else {
							System.out.print(' ');
						}
					}
					longQuestion = true;
				} else {
					System.out.print(typeIPrompt0 + "::" + clue + ": ");
					longQuestion = false;
				}
				final InputString inputString = new InputString(sc);
				final String response = inputString._inputString;
				if (response.length() == 0) {
					final String prompt = String.format("\t%c%s%c Get it right?", _RtArrowChar,
							answer, _LtArrowChar);
					final BooleanPair booleanPair = getYesNo(sc, prompt, true);
					gotItRight = booleanPair._returnValue;
					_needLineFeed = !booleanPair._lastLineWasBlank;
					wasWrongAtLeastOnce = wasWrongAtLeastOnce || !gotItRight;
					continue;
				}
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
				final ResponseEvaluator responseEvaluator = new ResponseEvaluator(sc,
						_ignoreVnDiacritics, card, _quizIsA_B, response);
				final String diffString = responseEvaluator._diffString;
				gotItRight = responseEvaluator._gotItRight;
				if (diffString != null) {
					if (longQuestion) {
						System.out.print('\t');
					}
					System.out.print(diffString);
					final String preface = longQuestion ? "\n\t" : " ";
					if (gotItRight) {
						final BooleanPair booleanPair = getYesNo(sc, preface + "Count as wrong?",
								false);
						gotItRight = !booleanPair._returnValue;
						_needLineFeed = !booleanPair._lastLineWasBlank;
					} else {
						final BooleanPair booleanPair = getYesNo(sc, preface + "Count as right?",
								false);
						gotItRight = booleanPair._returnValue;
						_needLineFeed = !booleanPair._lastLineWasBlank;
					}
					_needLineFeed = true;
				} else {
					_needLineFeed = longQuestion;
				}
				wasWrongAtLeastOnce = wasWrongAtLeastOnce || !gotItRight;
			}
			_quizPlus.reactToRightResponse(wasWrongAtLeastOnce);
		}
	}

	private static class BooleanPair {
		final boolean _returnValue, _lastLineWasBlank;

		private BooleanPair(final boolean returnValue, final boolean lastLineWasBlank) {
			_returnValue = returnValue;
			_lastLineWasBlank = lastLineWasBlank;
		}

	}
	private static BooleanPair getYesNo(final Scanner sc, final String prompt,
			final boolean defaultBoolean) {
		final char otherChar = defaultBoolean ? _NoChar : _YesChar;
		final String defaultString = defaultBoolean ? _YesString : _NoString;
		final String otherString = defaultBoolean ? _NoString : _YesString;
		System.out.printf("%s %c=%s,%c=%s: ", prompt, _ReturnChar, defaultString, otherChar,
				otherString);
		final InputString inputString = new InputString(sc);
		final String response = inputString._inputString;
		final boolean lastLineWasBlank = inputString._lastLineWasBlank;
		final boolean returnValue;
		if (response.length() == 0) {
			returnValue = defaultBoolean;
		} else {
			returnValue = Character.toUpperCase(response.charAt(0)) == otherChar
					? !defaultBoolean
					: defaultBoolean;
		}
		return new BooleanPair(returnValue, lastLineWasBlank);
	}

	static String CleanWhiteSpace(final String s) {
		if (s == null) {
			return "";
		}
		return s.trim().replaceAll(_WhiteSpace, " ");
	}

	static String KillPunct(final String field) {
		if (field == null) {
			return "";
		}
		return field.replaceAll(_RegExForPunct, "");
	}

	public static boolean StringEquals(final String s0, final String s1) {
		final byte[] bytes0 = s0.getBytes();
		final byte[] bytes1 = s1.getBytes();
		final int n0 = bytes0.length, n1 = bytes1.length;
		if (n0 != n1) {
			return false;
		}
		for (int k = 0; k < n0; ++k) {
			if (bytes0[k] != bytes1[k]) {
				return false;
			}
		}
		return true;
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
