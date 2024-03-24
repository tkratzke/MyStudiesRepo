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
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

import com.skagit.flashCardsGame.Statics.YesNoResponse;
import com.skagit.flashCardsGame.enums.ChangeType;
import com.skagit.flashCardsGame.enums.Clumping;
import com.skagit.flashCardsGame.enums.DiacriticsTreatment;
import com.skagit.flashCardsGame.enums.PropertyPlus;
import com.skagit.flashCardsGame.enums.QuizDirection;

/**
 * <pre>
 * Interesting note on deleting remote repositories from within Eclipse:
 * https://stackoverflow.com/questions/8625406/how-to-delete-a-branch-in-the-remote-repository-using-egit
 * </pre>
 */
public class FlashCardsGame {

	/** Non-static fields. */
	final private File _propertiesFile;
	final private File _soundFilesDir;
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
		if (propertiesFilePath.toLowerCase().endsWith(Statics._PropertiesEnding)) {
			_propertiesFile = new File(propertiesFilePath);
		} else {
			final File f = new File(propertiesFilePath);
			final File p = f.getParentFile();
			final String name = f.getName();
			final int lastDotIndex = name.lastIndexOf('.');
			if (lastDotIndex == -1) {
				_propertiesFile = new File(propertiesFilePath + Statics._PropertiesEnding);
			} else {
				_propertiesFile = new File(p,
						name.substring(0, lastDotIndex) + Statics._PropertiesEnding);
			}
		}
		final File propertiesDir = _propertiesFile.getParentFile();
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
		/** SOUND_FILES_DIR is kind of special. */
		final PropertyPlus sfdPP = PropertyPlus.SOUND_FILES_DIR;
		final String soundFilesString = sfdPP.getValidString(_properties);
		if (soundFilesString.length() > 0) {
			_soundFilesDir = propertiesDir;
		} else {
			final File dir = new File(soundFilesString);
			_soundFilesDir = dir.isDirectory() ? dir : propertiesDir;
			_properties.put(sfdPP._propertyName, _soundFilesDir.toString());
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
		System.out.print(Statics._HelpString);
		System.out.println();
	}

	private File getCardsFile() {
		final String propertiesFileName = _propertiesFile.getName();
		final File parentFile = _propertiesFile.getParentFile();
		final String cardsFileName = propertiesFileName.substring(0,
				propertiesFileName.length() - Statics._PropertiesEnding.length()) + ".txt";
		return new File(parentFile, cardsFileName);
	}

	private String getCoreFilePath() {
		final String inputPath = _propertiesFile.toString();
		return inputPath.substring(0,
				inputPath.length() - Statics._PropertiesEnding.length());
	}

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
				 * limit."
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
					if (trimmed.equalsIgnoreCase(Statics._EndCardsString)) {
						break;
					}
					final boolean newComment = trimmed.charAt(0) == Statics._CommentChar;
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
			final String bSide, final ArrayList<String> commentList, final String comment,
			final boolean announceCompleteDups) {
		if (comment != null && comment.length() > 0) {
			commentList.add(comment);
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
				final CardParts aParts = new CardParts(card._aSideString,
						Statics._MaxLenForCardPart);
				max = Math.max(max, aParts._maxLen);
			}
			aPartFormat = String.format("%%-%ds", max);
		}

		final File cardsFile = getCardsFile();
		try (PrintWriter pw = new PrintWriter(cardsFile)) {
			boolean recentWasMultiLine = false;
			for (int k0 = 0, nPrinted = 0; k0 < nCards; ++k0) {
				final Card card = _cards[k0];
				final CardParts aParts = new CardParts(card._aSideString,
						Statics._MaxLenForCardPart);
				final CardParts bParts = new CardParts(card._bSideString,
						Statics._MaxLenForCardPart);
				final int nAParts = aParts.size();
				final int nBParts = bParts.size();
				final int nParts = Math.max(nAParts, nBParts);
				final String[] commentLines = card._commentLines;
				final int nCommentLines = commentLines == null ? 0 : commentLines.length;
				final boolean isMultiLine = nCommentLines > 0 || nParts > 1;
				if (k0 > 0) {
					if ((isMultiLine || recentWasMultiLine
							|| (nPrinted % Statics._BlockSize == 0))) {
						pw.println();
					}
				}
				recentWasMultiLine = isMultiLine;
				for (int k1 = 0; k1 < nCommentLines; ++k1) {
					final CommentParts commentParts = new CommentParts(commentLines[k1],
							Statics._MaxLineLen);
					for (int k2 = 0; k2 < commentParts.size(); ++k2) {
						pw.print(commentParts.get(k2));
						if (k2 < commentParts.size() - 1) {
							pw.print('\t');
						}
						pw.println();
					}
				}
				for (int k1 = 0; k1 < nParts; ++k1) {
					final String aPart = k1 < nAParts ? aParts.get(k1) : null;
					final String bPart = k1 < nBParts ? bParts.get(k1) : null;
					if (k1 == 0) {
						pw.printf(realNumberFormat, card._cardNumber);
					} else {
						pw.print(blankNumberString);
					}
					pw.print('\t');
					if (aPart != null) {
						pw.printf(bPart != null ? aPartFormat : "%s", aPart);
					}
					if (bPart != null) {
						pw.printf("\t%s", bPart);
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
						pw.print(Statics._CommentString + lines[k1]);
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
		for (int k0 = 0; k0 < nCards; ++k0) {
			final int k1 = k0 + r.nextInt(nCards - k0);
			final Card card = cards[k0];
			cards[k0] = cards[k1];
			cards[k1] = card;
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
		final String prompt = String.format("Enter: %c=Done", Statics._ReturnChar);
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
			final String[] fields = myLine.split(Statics._WhiteSpace);
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

	void mainLoop(final Scanner sc) {
		int nCards = _cards.length;
		long[] oldValues = storeValues();
		_quizPlus = null;
		boolean restarted = false;

		OUTSIDE_LOOP : for (boolean keepGoing = true; keepGoing;) {
			/** Check for a _status change from _quizPlus. */
			final QuizPlusTransition quizPlusTransition = _quizGenerator.getStatusChange(nCards,
					restarted, _quizPlus);
			_quizPlus = quizPlusTransition._newQuizPlus;
			restarted = false;
			final ChangeType changeType = quizPlusTransition._changeType;
			if (Statics._NewQuizSet.contains(changeType)) {
				System.out.println();
				if (_needLineFeed) {
					System.out.println();
				}
				if (Statics._ReallyNewQuizSet.contains(changeType)) {
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
			final String clue = Statics.CleanWhiteSpace(
					_quizDirection == QuizDirection.A_TO_B ? card._aSideString : card._bSideString);
			final int clueLen = clue.length();
			final String answer = Statics.CleanWhiteSpace(
					_quizDirection == QuizDirection.A_TO_B ? card._bSideString : card._aSideString);
			final int answerLen = answer.length();
			boolean wasWrongAtLeastOnce = false;
			for (boolean gotItRight = false; !gotItRight;) {
				final String typeIPrompt = getTypeIPrompt(cardIdx, wasWrongAtLeastOnce);
				final int typeIPromptLen = typeIPrompt.length();
				final int len1 = typeIPromptLen + Statics._Sep1Len + clueLen + Statics._Sep2Len
						+ Math.min(Statics._RoomLen, answerLen);
				final String[] clueFields = clue.split(Statics._WhiteSpace);
				final int nClueFields = clueFields.length;
				final String[] answerFields = answer.split(Statics._WhiteSpace);
				final int nAnswerFields = answerFields.length;
				_needLineFeed = _needLineFeed || len1 > Statics._MaxLineLen;
				if (_needLineFeed) {
					System.out.println();
				}
				boolean longQuestion = false;
				if (len1 <= Statics._MaxLineLen) {
					System.out.printf("%s%s%s%s", typeIPrompt, Statics._Sep1, clue, Statics._Sep2);
				} else {
					System.out.println(typeIPrompt);
					System.out.print(Statics._PrefaceForNewLine);
					int nUsedOnCurrentLine = Statics._PrefaceForNewLineLen;
					longQuestion = true;
					/** Break up the clue. */
					for (int k = 0; k < nClueFields; ++k) {
						final String clueField = clueFields[k];
						final int clueFieldLen = clueField.length();
						final boolean justStartedLine = nUsedOnCurrentLine == (k == 0
								? Statics._PrefaceForNewLineLen
								: Statics._IndentLen);
						if (justStartedLine) {
							System.out.print(clueField);
							nUsedOnCurrentLine += clueFieldLen;
						} else if (nUsedOnCurrentLine + 1 + clueFieldLen
								+ Statics._RoomLen >= Statics._MaxLineLen) {
							System.out.println();
							System.out.print(Statics._IndentString);
							System.out.print(clueField);
							nUsedOnCurrentLine = Statics._IndentLen + clueFieldLen;
						} else {
							System.out.printf(" %s", clueField);
							nUsedOnCurrentLine += 1 + clueFieldLen;
						}
					}
					System.out.print(Statics._Sep2);
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
							System.out.printf("%s%s", Statics._IndentString, answerField);
							nUsedOnCurrentLine = Statics._IndentLen + answerFieldLen;
							++k;
						} else if (nUsedOnCurrentLine + 1 + answerFieldLen > Statics._MaxLineLen) {
							System.out.println();
							nUsedOnCurrentLine = 0;
						} else {
							System.out.printf(" %s", answerField);
							++k;
						}
					}
					final boolean defaultYesValue = true;
					final String prompt = Statics.getFullYesNoPrompt(Statics._CountAsRightString,
							defaultYesValue);
					final int promptLen = prompt.length();
					if (nUsedOnCurrentLine + Statics._Sep2Len + promptLen
							+ Statics._RoomLen <= Statics._MaxLineLen) {
						System.out.printf("%s%s", Statics._Sep2, prompt);
					} else {
						System.out.println();
						System.out.printf("%s%s%", Statics._IndentString, prompt);
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
					if (char0Uc == Statics._QuitChar) {
						System.out.print(Statics.getFullYesNoPrompt("Reallly quit?", true));
						final YesNoResponse yesNoResponse = new YesNoResponse(sc,
								/* defaultYesNo= */true);
						if (!yesNoResponse._yesValue) {
							continue OUTSIDE_LOOP;
						}
						keepGoing = false;
						return;
					} else if (char0Uc == Statics._EditPropertiesChar) {
						modifyProperties(sc);
						continue OUTSIDE_LOOP;
					} else if (char0Uc == Statics._RestartQuizChar) {
						_quizPlus.resetForFullMode();
						restarted = true;
						continue OUTSIDE_LOOP;
					} else if (char0Uc == Statics._ReloadCardsChar) {
						final int oldTci = _quizGenerator._topCardIndex;
						final Card topCard = _cards[oldTci];
						final String keyString = _quizDirection == QuizDirection.A_TO_B
								? topCard._aSideString
								: topCard._bSideString;
						loadCards(/* announceCompleteDups= */false);
						nCards = _cards.length;
						int tci = -1;
						for (int k = 0; k < nCards; ++k) {
							final Card card1 = _cards[k];
							if (_quizDirection == QuizDirection.A_TO_B) {
								if (keyString.compareToIgnoreCase(card1._aSideString) == 0) {
									tci = k;
									break;
								}
							} else {
								if (keyString.compareToIgnoreCase(card1._bSideString) == 0) {
									tci = k;
									break;
								}
							}
						}
						_quizGenerator.reactToReloadOfCards(_cards.length, tci);
						continue OUTSIDE_LOOP;
					} else if (char0Uc == Statics._HelpChar) {
						System.out.println();
						System.out.print(Statics._HelpString);
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
							answerField = String.format("%c %s", Statics._HeavyCheckChar,
									answerFields[0]);
						} else {
							answerField = answerFields[k];
						}
						final int answerFieldLen = answerField.length();
						if (nUsedOnCurrentLine == 0) {
							System.out.printf("%s%s", Statics._IndentString, answerField);
							nUsedOnCurrentLine = Statics._IndentLen + answerFieldLen;
							++k;
						} else if (nUsedOnCurrentLine + 1 + answerFieldLen > Statics._MaxLineLen) {
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
						fullYesNoPrompt = Statics.getFullYesNoPrompt(Statics._CountAsRightString,
								true);
					} else {
						fullYesNoPrompt = Statics.getFullYesNoPrompt(Statics._CountAsRightString,
								false);
					}
					final int fullYesNoPromptLen = fullYesNoPrompt.length();
					/** Put diffString and the prompt onto the current line if there's room. */
					if (nUsedOnCurrentLine + 1 + fullDiffStringLen + Statics._Sep2Len
							+ fullYesNoPromptLen + Statics._RoomLen <= Statics._MaxLineLen) {
						System.out.printf(" %s%s%s", fullDiffString, Statics._Sep2, fullYesNoPrompt);
					} else {
						/**
						 * Need a new line, but put diffString and the prompt onto a single line if
						 * there's room.
						 */
						System.out.println();
						if ( //
						Statics._IndentLen + fullDiffStringLen + Statics._Sep2Len + //
								fullYesNoPromptLen + Statics._RoomLen <= //
								Statics._MaxLineLen //
						) {
							System.out.printf("%s%s%s%s", Statics._IndentString, fullDiffString,
									Statics._Sep2, fullYesNoPrompt);
						} else {
							/** Otherwise, separate the diffString and the prompt string. */
							System.out.printf("%s%s", Statics._IndentString, fullDiffString);
							System.out.println();
							System.out.printf("%s%s", Statics._IndentString, fullYesNoPrompt);
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

	public static void main(final String[] args) {
		System.out.println(new String(Statics._SpecialChars));
		try (Scanner sc = new Scanner(System.in)) {
			final FlashCardsGame flashCardsGame = new FlashCardsGame(sc, args);
			flashCardsGame.mainLoop(sc);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
