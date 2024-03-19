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

import com.skagit.flashCardsGame.enums.ChangeType;
import com.skagit.flashCardsGame.enums.Clumping;
import com.skagit.flashCardsGame.enums.DiacriticsTreatment;
import com.skagit.flashCardsGame.enums.PropertyPlus;
import com.skagit.flashCardsGame.enums.QuizDirection;

public class FlashCardsGame {

	/**
	 * <pre>
	 *  Good list of characters.
	 *  http://xahlee.info/comp/unicode_arrows.html
	 * </pre>
	 */
	final static char _LtArrowChar = '\u2190';
	public final static char _RtArrowChar = '\u2192';
	final static char _RtArrowChar2 = '\u21a0';
	final static char _SpadeSymbolChar = '\u2660';
	final static char _ClubSymbolChar = '\u2663';
	final static char _DiamondSymbolChar = '\u2666';
	final static char _EmptySetChar = '\u2205';
	final static char _HeavyCheckChar = '\u2714';
	final static char _TabSymbolChar = '\u2409';

	final static char _CommentChar = '!';
	final static String _CommentString = "" + _CommentChar + ' ';
	final static String _EndCardsString = "$$";

	final static String _Indent = "   ";
	final static int _IndentLen = _Indent.length();
	final private static int _MaxLineLen = 65;

	final private static int _MaxLenForCardPart = 35;
	final private static int _BlockSize = 10;

	final private static String _Sep1 = " " + _ClubSymbolChar;
	final private static String _Sep2 = "" + _DiamondSymbolChar + " ";
	final private static int _Sep1Len = _Sep1.length();
	final private static int _Sep2Len = _Sep2.length();
	final private static String _PrefaceForNewLine = _Indent + _Sep1;
	final private static int _PrefaceForNewLineLen = _PrefaceForNewLine.length();
	final private static int _RoomLen = 10;
	final private static String _CountAsRight = "Count as Right? ";

	final static char _ReturnChar = '\u23CE';
	final private static char _HelpChar = 'H';
	final private static char _QuitChar = '!';
	final private static char _EditPropertiesChar = '@';
	final private static char _RestartQuizChar = '#';
	final private static char _ReloadCardsChar = '$';
	final private static char _YesChar = 'Y';
	final private static char _NoChar = 'N';
	final private static String _YesString = "Yes";
	final static String _NoString = "No";
	final static String _RegExForPunct = "[,.;:?!]+";

	/** Either of the following two FieldSeparators seems to work. */
	final static String _FieldSeparator = "\\s*\\t\\s*";
	@SuppressWarnings("unused")
	final private static String _FieldSeparator1 = "(\\s*\\t\\s*)+";
	final private static String _PropertiesEnding = ".properties";
	final static String _WhiteSpace = "\\s+";
	final static int _NominalTabLen = 5;

	static char[] _SpecialChars = { //
			_LtArrowChar, //
			_RtArrowChar, //
			_RtArrowChar2, //
			_SpadeSymbolChar, //
			_ClubSymbolChar, //
			_DiamondSymbolChar, //
			_EmptySetChar, //
			_HeavyCheckChar, //
			_TabSymbolChar, //
			_ReturnChar, //
			_HelpChar, //
			_QuitChar, //
			_EditPropertiesChar, //
			_RestartQuizChar, //
			_YesChar, //
			_NoChar //
	};

	final private static String _HelpString = String.format(
			"%c=\"Show this Message,\" %c=Quit, %c=Edit Properties, %c=Restart Quiz, %c=Reload Cards"
					+ "%c=\"Show-and-ask,\" %s=Next Line is Continuation",
			_HelpChar, _QuitChar, _EditPropertiesChar, _RestartQuizChar, _ReloadCardsChar,
			_ReturnChar, "" + _TabSymbolChar + _ReturnChar);

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

	/** Non-static fields. */
	final private File _propertiesFile;
	final private Properties _properties;

	final private long _randomSeed;
	final private QuizDirection _quizDirection;
	final private DiacriticsTreatment _diacriticsTreatment;
	final private Clumping _clumping;

	private Card[] _cards;
	final private QuizGenerator _quizGenerator;
	private QuizPlus _quizPlus;
	private boolean _needLineFeed;

	FlashCardsGame(final Scanner sc, final String[] args) {
		_needLineFeed = false;
		final String propertiesFilePath = args[0];
		if (propertiesFilePath.toLowerCase().endsWith(_PropertiesEnding)) {
			_propertiesFile = new File(propertiesFilePath);
		} else {
			final File f = new File(propertiesFilePath);
			final File p = f.getParentFile();
			final String name = f.getName();
			final int lastDotIndex = name.lastIndexOf('.');
			if (lastDotIndex == -1) {
				_propertiesFile = new File(propertiesFilePath + _PropertiesEnding);
			} else {
				_propertiesFile = new File(p,
						name.substring(0, lastDotIndex) + _PropertiesEnding);
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
				final String key = propertyPlus._propertyName;
				final String validatedString = propertyPlus.getValidString(properties.get(key));
				_properties.put(key, validatedString);
			}
		} catch (final IOException e) {
		}
		reWritePropertiesFile();

		final String typableString = PropertyPlus.QUIZ_DIRECTION.getValidString(_properties);
		_quizDirection = QuizDirection.get(typableString);
		_diacriticsTreatment = DiacriticsTreatment
				.valueOf(PropertyPlus.DIACRITICS_TREATMENT.getValidString(_properties));
		_randomSeed = Integer.parseInt(PropertyPlus.RANDOM_SEED.getValidString(_properties));
		final String clumpingString = PropertyPlus.CLUMPING.getValidString(_properties);
		_clumping = Clumping.valueOf(clumpingString);
		loadCards(/* announceCompleteDups= */true);
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
		_quizGenerator = new QuizGenerator(_properties, _cards.length, _randomSeed);
		shuffleCards(_cards);
		_quizPlus = null;
		System.out.println();
		System.out.print(_HelpString);
		System.out.println();
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
	 * Interesting note on deleting remote repositories from within Eclipse:
	 * https://stackoverflow.com/questions/8625406/how-to-delete-a-branch-in-the-remote-repository-using-egit
	 * </pre>
	 */

	/**
	 * <pre>
	 * Interesting note on text files and the "right" way to do things:
	 * https://stackoverflow.com/questions/17405165/first-character-of-the-reading-from-the-text-file-%C3%AF
	 * </pre>
	 */

	private TreeMap<Card, Card> loadCardMap(final boolean announceCompleteDups) {
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
				 * If the first character is NOT feff, go back to the beginning of the file. If it
				 * IS feff, ignore it and continue on.
				 */
				if (in.read() != 0xFEFF) {
					in.reset();
				}
			}

			String aSide = null;
			String bSide = null;
			final ArrayList<String> commentLinesList = new ArrayList<>();
			String comment = null;
			try (final Scanner fileSc = new Scanner(in)) {
				while (fileSc.hasNext()) {
					final String nextLine = fileSc.nextLine();
					final String trimmed = nextLine.trim();
					if (trimmed.isBlank()) {
						continue;
					}
					if (trimmed.equalsIgnoreCase(_EndCardsString)) {
						break;
					}
					final boolean newComment = trimmed.charAt(0) == _CommentChar;
					final boolean existingComment = comment != null;
					if (newComment || existingComment) {
						final boolean hasContinuation = nextLine
								.charAt(nextLine.length() - 1) == '\t';
						if (newComment) {
							if (existingComment) {
								commentLinesList.add(comment);
							}
							comment = trimmed.substring(1).trim();
							if (!hasContinuation) {
								commentLinesList.add(comment);
								comment = null;
							}
							continue;
						}
						/** This is a continuation of an existing comment. */
						comment += " " + trimmed;
						if (!hasContinuation) {
							commentLinesList.add(comment);
							comment = null;
						}
						continue;
					}

					/** Not a comment and not part of a comment. */
					final LineBreakDown lbd = new LineBreakDown(nextLine);
					if (lbd._aSide == null && lbd._bSide == null) {
						/** Essentially, nextLine is blank. */
						continue;
					}
					if (aSide == null) {
						aSide = lbd._aSide;
						bSide = lbd._bSide;
					} else {
						aSide += " " + lbd._aSide;
						bSide += " " + lbd._bSide;
					}
					if (!lbd._nextLineIsContinuation) {
						wrapUp(cardMap, aSide, bSide, commentLinesList, comment,
								announceCompleteDups);
						aSide = bSide = comment = null;
					}
				}
				wrapUp(cardMap, aSide, bSide, commentLinesList, comment, announceCompleteDups);
				aSide = bSide = comment = null;
			} catch (final Exception e) {
			}
		} catch (final IOException e) {
		}
		return cardMap;
	}

	private static void wrapUp(final TreeMap<Card, Card> cardMap, final String aSide,
			final String bSide, final ArrayList<String> commentList, final String strayComment,
			final boolean announceCompleteDups) {
		if (strayComment != null) {
			commentList.add(strayComment);
		}
		if (aSide != null && bSide != null && aSide.length() > 0 && bSide.length() > 0) {
			final int nNewCommentLines = commentList.size();
			final String[] newCommentLines = commentList.toArray(new String[nNewCommentLines]);
			final Card newCard = new Card(cardMap.size(), aSide, bSide, newCommentLines);
			final Card oldCard = cardMap.get(newCard);
			if (oldCard != null) {
				if (announceCompleteDups) {
					System.out.println(String.format("Merging Card #%d into #%d:",
							newCard._cardNumber, oldCard._cardNumber));
					System.out.println(oldCard.getString());
					System.out.println(newCard.getString());
				}
				if (nNewCommentLines > 0) {
					final String[] oldCommentLines = oldCard._commentLines;
					final int nOldCommentLines = oldCommentLines == null
							? 0
							: oldCommentLines.length;
					final int nAllCommentLines = nOldCommentLines + nNewCommentLines;
					final String[] allCommentLines = new String[nAllCommentLines];
					if (nOldCommentLines > 0) {
						System.arraycopy(oldCommentLines, 0, allCommentLines, 0, nOldCommentLines);
					}
					System.arraycopy(newCommentLines, 0, allCommentLines, nOldCommentLines,
							nNewCommentLines);
					oldCard._commentLines = allCommentLines;
					commentList.clear();
					return;
				}
			}
			cardMap.put(newCard, newCard);
			commentList.clear();
		}
	}

	private void loadCards(final boolean announceCompleteDups) {
		final TreeMap<Card, Card> cardMap = loadCardMap(announceCompleteDups);
		final int nCards = cardMap.size();
		_cards = cardMap.keySet().toArray(new Card[nCards]);
		Arrays.sort(_cards, Card._ByCardNumberOnly);
	}

	private int[] announceAndClumpDuplicates() {
		final int nCards = _cards.length;
		final int[] dupCounts = new int[]{0, 0};
		for (int iPass = 0; iPass < 2; ++iPass) {
			final Comparator<Card> comparator = iPass == 0
					? Card._ByASideOnly
					: Card._ByBSideOnly;
			final boolean makeSlave = iPass == 0
					? (_clumping == Clumping.A)
					: (_clumping == Clumping.B);
			final String sideBeingChecked = iPass == 0 ? "A-Side" : "B-Side";
			final TreeMap<Card, ArrayList<Card>> kingToSlaves = new TreeMap<>(comparator);
			for (int k = 0; k < nCards; ++k) {
				final Card card = _cards[k];
				final ArrayList<Card> slaves = kingToSlaves.get(card);
				if (slaves != null) {
					/** card has an existing king. */
					final Card oldCard = kingToSlaves.ceilingKey(card);
					if (_needLineFeed) {
						System.out.println();
					}
					System.out.println(String.format("Duplicate %s #%d:", sideBeingChecked,
							dupCounts[1 - iPass]++));
					System.out.println(oldCard.getString());
					System.out.println(card.getString());
					_needLineFeed = true;
					if (makeSlave) {
						slaves.add(card);
						_cards[k] = null;
					}
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
		if (nCards == 0) {
			return;
		}
		final int nDigits = (int) (Math.log10(nCards) + 1d);
		final String realNumberFormat = String.format("%%%dd.", nDigits);
		final String blankNumberFormat = String.format("%%-%ds ", nDigits);
		final String blankNumberString = String.format(blankNumberFormat, "");
		final String aPartFormat;
		{
			int max = 0;
			for (final Card card : _cards) {
				final Card.CardParts aParts = card.new CardParts(/* a= */true,
						_MaxLenForCardPart);
				max = Math.max(max, aParts._maxLen);
			}
			aPartFormat = String.format("%%-%ds", max);
		}

		final File cardsFile = getCardsFile();
		try (PrintWriter pw = new PrintWriter(cardsFile)) {
			boolean recentWasMultiLine = false;
			for (int k0 = 0, nPrinted = 0; k0 < nCards; ++k0) {
				final Card card = _cards[k0];
				final Card.CardParts aParts = card.new CardParts(true, _MaxLenForCardPart);
				final Card.CardParts bParts = card.new CardParts(false, _MaxLenForCardPart);
				final int nAParts = aParts.size();
				final int nBParts = bParts.size();
				final int nParts = Math.max(nAParts, nBParts);
				final String[] commentLines = card._commentLines;
				final int nCommentLines = commentLines == null ? 0 : commentLines.length;
				final boolean isMultiLine = nCommentLines > 0 || nParts > 1;
				if (k0 > 0) {
					if ((isMultiLine || recentWasMultiLine || (nPrinted % _BlockSize == 0))) {
						pw.println();
					}
				}
				recentWasMultiLine = isMultiLine;
				for (int k1 = 0; k1 < nCommentLines; ++k1) {
					final Card.CommentParts commentParts = card.new CommentParts(commentLines[k1],
							_MaxLineLen);
					for (int k2 = 0; k2 < commentParts.size(); ++k2) {
						pw.print(commentParts.get(k2));
						if (k2 < commentParts.size() - 1) {
							pw.print('\t');
						}
						pw.println();
					}
				}
				for (int k1 = 0; k1 < nParts; ++k1) {
					final String aPart = k1 < nAParts ? aParts.get(k1) : "";
					if (k1 == 0) {
						pw.printf(realNumberFormat, card._cardNumber);
					} else {
						pw.print(blankNumberString);
					}
					pw.print('\t');
					pw.printf(aPartFormat, aPart);
					if (k1 < nBParts) {
						pw.print('\t');
						pw.print(bParts.get(k1));
					}
					if (k1 < nParts - 1) {
						pw.print('\t');
					}
					pw.println();
				}
				++nPrinted;
			}
		} catch (final FileNotFoundException e) {
		}
	}

	void updateProperties() {
		_properties.put(PropertyPlus.RANDOM_SEED._propertyName, Long.toString(_randomSeed));
		_properties.put(PropertyPlus.QUIZ_DIRECTION._propertyName,
				_quizDirection._typableString);
		_properties.put(PropertyPlus.DIACRITICS_TREATMENT._propertyName,
				_diacriticsTreatment.name());
		_quizGenerator.updateProperties(_properties);
	}

	void reWritePropertiesFile() {
		final Properties properties;
		final long seed = Long
				.parseLong(PropertyPlus.RANDOM_SEED.getValidString(_properties));
		if (seed < 0) {
			properties = (Properties) _properties.clone();
			final int topCardIdx0 = Integer
					.parseInt(PropertyPlus.TOP_CARD_INDEX.getValidString(_properties));
			final int maxNNewWords = Integer
					.parseInt(PropertyPlus.NUMBER_OF_NEW_WORDS.getValidString(_properties));
			final int maxNRecentWords = Integer
					.parseInt(PropertyPlus.NUMBER_OF_RECENT_WORDS.getValidString(_properties));
			final int topIndexCardIdx1 = Math.min(topCardIdx0,
					maxNNewWords + maxNRecentWords - 1);
			properties.put(PropertyPlus.TOP_CARD_INDEX._propertyName,
					Long.toString(topIndexCardIdx1));
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
						pw.print(FlashCardsGame._CommentString + lines[k1]);
						pw.println();
					}
				}
				final String realPropertyName = propertyPlus._propertyName;
				pw.printf("%s=%s", realPropertyName, properties.get(realPropertyName));
				pw.println();
			}
		} catch (final IOException e) {
		}
	}

	private void shuffleCards(final Card[] cards) {
		if (_randomSeed == 0) {
			return;
		}
		final Random r = new Random();
		if (_randomSeed > 0) {
			r.setSeed(_randomSeed);
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

	final private String getTypeIPrompt(final int cardIdx,
			final boolean currentQuestionWasWrongAtLeastOnce) {
		String typeIPrompt = "";
		final int currentIdxInQuiz = _quizPlus.getCurrentIdxInQuiz();
		final boolean criticalQuizIdx = _quizPlus.isCriticalQuizIndex(currentIdxInQuiz);
		if (criticalQuizIdx) {
			typeIPrompt += "*";
		}
		final int cardNumber = _cards[cardIdx]._cardNumber;
		final int quizLen = _quizPlus.getCurrentQuizLen();
		typeIPrompt += String.format("%d of %d(CrdIdx=%d,#%d)", currentIdxInQuiz + 1, quizLen,
				cardIdx, cardNumber);
		final int nRights = _quizPlus.getNRights();
		int nWrongs = _quizPlus.getNWrongs();
		int nTrials = nRights + nWrongs;
		if (criticalQuizIdx && currentQuestionWasWrongAtLeastOnce) {
			++nWrongs;
			++nTrials;
		}
		if (nRights > 0 || nWrongs > 0) {
			final long successPerCent = Math.round((100d * nRights) / nTrials);
			typeIPrompt += String.format(",(Rt:Wr=%d:%d SccRt=%d%%)", nRights, nWrongs,
					successPerCent);
		}
		return typeIPrompt;
	}

	final private String getTypeIIPrompt() {
		final String prompt = String.format("Enter: %c=Done", _ReturnChar);
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
		final int nCore = core.length;
		final int nOthers = others.length;
		final long[] array = new long[nCore + nOthers];
		System.arraycopy(core, 0, array, 0, nCore);
		System.arraycopy(others, 0, array, nCore, nOthers);
		return array;
	}

	final String getString() {
		return String.format("%s(w/ %d cards): %s, %s, RandomSeed[%d] \n%s", //
				getCoreFilePath(), _cards.length, //
				_quizDirection._fancyString, //
				_diacriticsTreatment.name(), //
				_randomSeed, _quizGenerator.getString());
	}

	@Override
	public String toString() {
		return getString();
	}

	final static private EnumSet<ChangeType> _NewQuizSet = EnumSet.of(//
			ChangeType.CRITICAL_ONLY_WIN, //
			ChangeType.MOVE_ON_WIN, //
			ChangeType.LOSS, //
			ChangeType.NOTHING_TO_SOMETHING, //
			ChangeType.PARAMETERS_CHANGED, //
			ChangeType.RESTART//
	);

	final static private EnumSet<ChangeType> _ReallyNewQuizSet = EnumSet.of(//
			ChangeType.MOVE_ON_WIN, //
			ChangeType.NOTHING_TO_SOMETHING, //
			ChangeType.PARAMETERS_CHANGED //
	);

	void mainLoop(final Scanner sc) {
		int nCards = _cards.length;
		long[] oldValues = storeValues();
		_quizPlus = null;
		boolean restarted = false;

		OUTSIDE_LOOP : for (boolean keepGoing = true; keepGoing;) {
			/** Check for a status change from _quizPlus. */
			final QuizPlusTransition quizPlusTransition = _quizGenerator.getStatusChange(nCards,
					restarted, _quizPlus);
			_quizPlus = quizPlusTransition._newQuizPlus;
			restarted = false;
			final ChangeType changeType = quizPlusTransition._changeType;
			if (_NewQuizSet.contains(changeType)) {
				System.out.println();
				if (_needLineFeed) {
					System.out.println();
				}
				if (_ReallyNewQuizSet.contains(changeType)) {
					/** For a really new one, add another lineFeed. */
					System.out.println();
					System.out.println(getString());
				}
				System.out.print(changeType._reasonForChangeString);
				System.out.println(" " + quizPlusTransition._transitionString);
				_needLineFeed = true;
			}
			if (madeChangesFrom(oldValues)) {
				updateProperties();
				reWritePropertiesFile();
				oldValues = storeValues();
			}

			final int cardIdx = _quizPlus.getCurrentQuiz_CardIndex();
			final Card card = _cards[cardIdx];
			final String clue = CleanWhiteSpace(
					_quizDirection == QuizDirection.A_TO_B ? card._fullASide : card._fullBSide);
			final int clueLen = clue.length();
			final String answer = CleanWhiteSpace(
					_quizDirection == QuizDirection.A_TO_B ? card._fullBSide : card._fullASide);
			final int answerLen = answer.length();
			boolean wasWrongAtLeastOnce = false;
			for (boolean gotItRight = false; !gotItRight;) {
				final String typeIPrompt = getTypeIPrompt(cardIdx, wasWrongAtLeastOnce);
				final int typeIPromptLen = typeIPrompt.length();
				final int len1 = typeIPromptLen + _Sep1Len + clueLen + _Sep2Len
						+ Math.min(_RoomLen, answerLen);
				final String[] clueFields = clue.split(_WhiteSpace);
				final int nClueFields = clueFields.length;
				final String[] answerFields = answer.split(_WhiteSpace);
				final int nAnswerFields = answerFields.length;
				_needLineFeed = _needLineFeed || len1 > _MaxLineLen;
				if (_needLineFeed) {
					System.out.println();
				}
				boolean longQuestion = false;
				if (len1 <= _MaxLineLen) {
					System.out.printf("%s%s%s%s", typeIPrompt, _Sep1, clue, _Sep2);
				} else {
					System.out.println(typeIPrompt);
					System.out.print(_PrefaceForNewLine);
					int nUsedOnCurrentLine = _PrefaceForNewLineLen;
					longQuestion = true;
					/** Break up the clue. */
					for (int k = 0; k < nClueFields; ++k) {
						final String clueField = clueFields[k];
						final int clueFieldLen = clueField.length();
						final boolean justStartedLine = nUsedOnCurrentLine == (k == 0
								? _PrefaceForNewLineLen
								: _IndentLen);
						if (justStartedLine) {
							System.out.print(clueField);
							nUsedOnCurrentLine += clueFieldLen;
						} else if (nUsedOnCurrentLine + 1 + clueFieldLen + _RoomLen >= _MaxLineLen) {
							System.out.println();
							System.out.print(_Indent);
							System.out.print(clueField);
							nUsedOnCurrentLine = _IndentLen + clueFieldLen;
						} else {
							System.out.printf(" %s", clueField);
							nUsedOnCurrentLine += 1 + clueFieldLen;
						}
					}
					System.out.print(_Sep2);
				}
				final InputString inputString = new InputString(sc);
				longQuestion = longQuestion || inputString._nLinesOfResponse > 1;
				final String response = inputString._inputString;
				if (response.length() == 0) {
					int nUsedOnCurrentLine = 0;
					/** User just wants a check so we have to print out the answer. Break it up. */
					for (int k = 0; k < nAnswerFields;) {
						final String answerField = answerFields[k];
						final int answerFieldLen = answerField.length();
						if (nUsedOnCurrentLine == 0) {
							System.out.printf("%s%s", _Indent, answerField);
							nUsedOnCurrentLine = _IndentLen + answerFieldLen;
							++k;
						} else if (nUsedOnCurrentLine + 1 + answerFieldLen > _MaxLineLen) {
							System.out.println();
							nUsedOnCurrentLine = 0;
						} else {
							System.out.printf(" %s", answerField);
							++k;
						}
					}
					final boolean defaultYesValue = true;
					final String prompt = getFullYesNoPrompt(_CountAsRight, defaultYesValue);
					final int promptLen = prompt.length();
					if (nUsedOnCurrentLine + _Sep2Len + promptLen + _RoomLen <= _MaxLineLen) {
						System.out.printf("%s%s", _Sep2, prompt);
					} else {
						System.out.println();
						System.out.printf("%s%s%", _Indent, prompt);
					}
					final YesNoResponse yesNoResponse = new YesNoResponse(sc, defaultYesValue);
					gotItRight = yesNoResponse._yesValue;
					_needLineFeed = !yesNoResponse._lastLineWasBlank;
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
						System.out.print(getFullYesNoPrompt("Reallly quit?", true));
						final YesNoResponse yesNoResponse = new YesNoResponse(sc,
								/* defaultYesNo= */true);
						if (!yesNoResponse._yesValue) {
							continue OUTSIDE_LOOP;
						}
						keepGoing = false;
						return;
					} else if (char0Uc == _EditPropertiesChar) {
						modifyProperties(sc);
						continue OUTSIDE_LOOP;
					} else if (char0Uc == _RestartQuizChar) {
						_quizPlus.resetForFullMode();
						restarted = true;
						continue OUTSIDE_LOOP;
					} else if (char0Uc == _ReloadCardsChar) {
						final int oldTci = _quizGenerator._topCardIndex;
						final Card topCard = _cards[oldTci];
						final String keyString = _quizDirection == QuizDirection.A_TO_B
								? topCard._fullASide
								: topCard._fullBSide;
						loadCards(/* announceCompleteDups= */false);
						nCards = _cards.length;
						int tci = -1;
						for (int k = 0; k < nCards; ++k) {
							final Card card1 = _cards[k];
							if (_quizDirection == QuizDirection.A_TO_B) {
								if (keyString.compareToIgnoreCase(card1._fullASide) == 0) {
									tci = k;
									break;
								}
							} else {
								if (keyString.compareToIgnoreCase(card1._fullBSide) == 0) {
									tci = k;
									break;
								}
							}
						}
						_quizGenerator.reactToReloadOfCards(_cards.length, tci);
						continue OUTSIDE_LOOP;
					} else if (char0Uc == _HelpChar) {
						System.out.println();
						System.out.print(_HelpString);
						System.out.println();
						continue OUTSIDE_LOOP;
					}
				}
				final ResponseEvaluator responseEvaluator = new ResponseEvaluator(sc,
						_diacriticsTreatment, answer, response);
				final String[] diffStrings = responseEvaluator._diffStrings;
				gotItRight = responseEvaluator._gotItRight;
				if (diffStrings != null) {
					/** Because diffStrings is not null, print out the answer, but break it up. */
					int nUsedOnCurrentLine = 0;
					for (int k = 0; k < nAnswerFields;) {
						final String answerField;
						if (gotItRight && k == 0) {
							/** Combine the first answer field with a Heavy Check Mark. */
							answerField = String.format("%c %s", _HeavyCheckChar, answerFields[0]);
						} else {
							answerField = answerFields[k];
						}
						final int answerFieldLen = answerField.length();
						if (nUsedOnCurrentLine == 0) {
							System.out.printf("%s%s", _Indent, answerField);
							nUsedOnCurrentLine = _IndentLen + answerFieldLen;
							++k;
						} else if (nUsedOnCurrentLine + 1 + answerFieldLen > _MaxLineLen) {
							System.out.println();
							nUsedOnCurrentLine = 0;
						} else {
							System.out.printf(" %s", answerField);
							++k;
						}
					}
					/** And now the diffs; consolidate to a single String. */
					final int nDiffStrings = diffStrings.length;
					String fullDiffString = "";
					for (int k = 0; k < nDiffStrings; ++k) {
						fullDiffString += (k == 0 ? "" : " ") + diffStrings[k];
					}
					final int fullDiffStringLen = fullDiffString.length();
					final String fullYesNoPrompt;
					if (gotItRight) {
						fullYesNoPrompt = getFullYesNoPrompt(_CountAsRight, true);
					} else {
						fullYesNoPrompt = getFullYesNoPrompt(_CountAsRight, false);
					}
					final int fullYesNoPromptLen = fullYesNoPrompt.length();
					/** Put diffString and the prompt onto the current line if there's room. */
					if (nUsedOnCurrentLine + 1 + fullDiffStringLen + _Sep2Len + fullYesNoPromptLen
							+ _RoomLen <= _MaxLineLen) {
						System.out.printf(" %s%s%s", fullDiffString, _Sep2, fullYesNoPrompt);
					} else {
						/**
						 * Need a new line, but put diffString and the prompt onto a single line if
						 * there's room.
						 */
						System.out.println();
						if ( //
						_IndentLen + fullDiffStringLen + _Sep2Len + //
								fullYesNoPromptLen + _RoomLen <= //
								_MaxLineLen //
						) {
							System.out.printf("%s%s%s%s", _Indent, fullDiffString, _Sep2,
									fullYesNoPrompt);
						} else {
							/** Otherwise, separate the diffString and the prompt string. */
							System.out.printf("%s%s", _Indent, fullDiffString);
							System.out.println();
							System.out.printf("%s%s", _Indent, fullYesNoPrompt);
						}
					}
					final YesNoResponse yesNoResponse = new YesNoResponse(sc, gotItRight);
					gotItRight = yesNoResponse._yesValue;
					_needLineFeed = !yesNoResponse._lastLineWasBlank;
				} else {
					_needLineFeed = longQuestion;
				}
				wasWrongAtLeastOnce = wasWrongAtLeastOnce || !gotItRight;
			}
			_quizPlus.reactToRightResponse(wasWrongAtLeastOnce);
		}
	}

	private static String getFullYesNoPrompt(final String prompt,
			final boolean defaultYesValue) {
		final char otherChar = defaultYesValue ? _NoChar : _YesChar;
		final String defaultString = defaultYesValue ? _YesString : _NoString;
		final String otherString = defaultYesValue ? _NoString : _YesString;
		return String.format("%s %c=%s,%c=%s: ", prompt, _ReturnChar, defaultString,
				otherChar, otherString);
	}

	private static class YesNoResponse {
		private final boolean _yesValue, _lastLineWasBlank;

		private YesNoResponse(final Scanner sc, final boolean defaultYesValue) {
			final char otherChar = defaultYesValue ? _NoChar : _YesChar;
			final InputString inputString = new InputString(sc);
			final String response = inputString._inputString;
			final boolean lastLineWasBlank = inputString._lastLineWasBlank;
			if (response.length() == 0) {
				_yesValue = defaultYesValue;
			} else {
				_yesValue = Character.toUpperCase(response.charAt(0)) == otherChar
						? !defaultYesValue
						: defaultYesValue;
			}
			_lastLineWasBlank = lastLineWasBlank;
		}
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
		System.out.println(new String(_SpecialChars));
		try (Scanner sc = new Scanner(System.in)) {
			final FlashCardsGame flashCardsGame = new FlashCardsGame(sc, args);
			flashCardsGame.mainLoop(sc);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
