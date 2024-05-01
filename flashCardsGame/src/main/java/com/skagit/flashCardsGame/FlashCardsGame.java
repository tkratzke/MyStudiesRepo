package com.skagit.flashCardsGame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.skagit.SimpleAudioPlayer;
import com.skagit.flashCardsGame.Statics.YesNoResponse;
import com.skagit.flashCardsGame.enums.ChangeType;
import com.skagit.flashCardsGame.enums.Clumping;
import com.skagit.flashCardsGame.enums.DiacriticsTreatment;
import com.skagit.flashCardsGame.enums.Mode;
import com.skagit.flashCardsGame.enums.PropertyPlus;

/**
 * <pre>
 * Interesting note on deleting remote branches from within Eclipse:
 * https://stackoverflow.com/questions/8625406/how-to-delete-a-branch-in-the-remote-repository-using-egit
 * </pre>
 */
public class FlashCardsGame {

    final private File _gameDir;
    final private File _gameFile;
    final private File _cardsFile;
    final private File _soundFilesDir;

    final private TreeMap<String, File> _allSoundFiles;
    final private TreeMap<String, String> _partToStem;

    final private Properties _properties;
    final private long _randomSeed;
    final private Mode _mode;
    final private boolean _silentMode;
    final private DiacriticsTreatment _diacriticsTreatment;
    final private Clumping _clumping;
    final private QuizGenerator _quizGenerator;

    private Card[] _cards;
    private QuizPlus _quizPlus;
    private boolean _needLineFeed;

    FlashCardsGame(final String gameDirString) {
	_needLineFeed = false;
	_gameDir = Statics.getGameDir(gameDirString);
	_gameFile = Statics.getGameFileFromDirFile(_gameDir);
	if (_gameFile == null) {
	    _cardsFile = _soundFilesDir = null;
	    _allSoundFiles = null;
	    _partToStem = null;
	    _properties = null;
	    _randomSeed = 0;
	    _mode = null;
	    _diacriticsTreatment = null;
	    _clumping = null;
	    _quizGenerator = null;
	    _silentMode = false;
	    return;
	}

	_properties = new Properties();
	try (InputStreamReader isr = new InputStreamReader(new FileInputStream(_gameFile), "UTF-8")) {
	    final Properties properties = new Properties();
	    properties.load(isr);
	    final int nPropertyPluses = PropertyPlus._Values.length;
	    for (int k = 0; k < nPropertyPluses; ++k) {
		final PropertyPlus propertyPlus = PropertyPlus._Values[k];
		final String key = propertyPlus._propertyName;
		final String validatedString = propertyPlus.getValidString(properties);
		_properties.put(key, validatedString);
	    }
	} catch (final IOException e) {
	}
	final String cardsFileString = PropertyPlus.CARDS_FILE.getValidString(_properties);
	_cardsFile = Statics.getCardsFile(_gameDir, cardsFileString);
	final String soundFilesDirString = PropertyPlus.SOUND_FILES_DIR.getValidString(_properties);
	_soundFilesDir = Statics.getSoundFilesDir(_gameDir, soundFilesDirString);
	reWritePropertiesFile();

	_diacriticsTreatment = DiacriticsTreatment
		.valueOf(PropertyPlus.DIACRITICS_TREATMENT.getValidString(_properties));
	_mode = Mode.valueOf(PropertyPlus.MODE.getValidString(_properties));
	_randomSeed = Integer.parseInt(PropertyPlus.RANDOM_SEED.getValidString(_properties));
	final String clumpingString = PropertyPlus.CLUMPING.getValidString(_properties);
	_clumping = Clumping.valueOf(clumpingString);
	_silentMode = Boolean.valueOf(PropertyPlus.SILENT_MODE.getValidString(_properties));

	_allSoundFiles = new TreeMap<>();
	_partToStem = new TreeMap<>();
	loadCards(/* announceCompleteDups= */true);
	if (_clumping != Clumping.NO_CLUMPING) {
	    final int nDups = announceAndClumpDuplicates();
	    if (nDups > 0) {
		if (_needLineFeed) {
		    System.out.println();
		}
		final String dupType = "Dup " + (_clumping == Clumping.BY_CLUE ? "Clues" : "Answers");
		System.out.println(String.format("n%s=%d.", dupType, nDups));
	    }
	}
	reWriteCardsFile();
	if (_mode == Mode.SWITCH) {
	    _quizGenerator = null;
	    return;
	}

	_quizGenerator = new QuizGenerator(_mode, _properties, _cards.length, _randomSeed);
	shuffleCards(_cards);
	_quizPlus = null;

	System.out.println();
	System.out.print(Statics._HelpString);
	System.out.println();
    }

    private void addToSoundFiles(final File mainDir) {
	/** Add sub-directories' soundFiles. */
	final File[] subDirs = mainDir.listFiles(new FileFilter() {

	    @Override
	    public boolean accept(final File f) {
		return f.isDirectory();
	    }
	});
	for (final File subDir : subDirs) {
	    addToSoundFiles(subDir);
	}
	/** Add mainDir's own sound files. */
	final File[] mainDirSoundFiles = mainDir.listFiles(new FileFilter() {

	    @Override
	    public boolean accept(final File f) {
		return SimpleAudioPlayer.validate(f);
	    }
	});
	for (final File f : mainDirSoundFiles) {
	    final String fName = f.getName();
	    final ArrayList<String> keyList = new ArrayList<>();
	    /** fName is a valid key. */
	    keyList.add(fName);
	    /** Strip the extension from fName to make stem, which is a valid key. */
	    final int lastDot = fName.lastIndexOf('.');
	    final String stem = lastDot >= 0 ? fName.substring(0, lastDot) : fName;
	    if (stem != fName) {
		keyList.add(stem);
	    }
	    final Matcher matcher = Pattern.compile("^\\d+\\-").matcher(stem);
	    if (matcher.find()) {
		final int stemLen = stem.length();
		final int start = matcher.start();
		final int end = matcher.end();
		/** The digits make up a valid key. */
		final String digitsString = stem.substring(start, end - 1);
		keyList.add(digitsString);
		_partToStem.put(digitsString, stem);
		if (end < stemLen) {
		    /** Everything in stem beyond the dash make up a valid key. */
		    final String beyondDashString = stem.substring(end, stemLen);
		    keyList.add(beyondDashString);
		    _partToStem.put(beyondDashString, stem);
		}
	    }
	    File parent = f.getParentFile();
	    String parentNamePlus = parent.getName() + File.separator;
	    for (int k = 0; !keyList.isEmpty();) {
		k %= keyList.size();
		final String thisKey = keyList.get(k);
		final File oldFile = _allSoundFiles.get(thisKey);
		if (oldFile == null) {
		    _allSoundFiles.put(thisKey, f);
		    keyList.remove(k);
		} else {
		    keyList.set(k, parentNamePlus + thisKey);
		    ++k;
		}
	    }
	    parent = parent.getParentFile();
	    parentNamePlus = parent.getName() + File.separator + parentNamePlus;
	}
    }

    private void loadCards(final boolean announceCompleteDups) {
	_allSoundFiles.clear();
	addToSoundFiles(_soundFilesDir);
	final TreeMap<Card, Card> cardMap = loadCardMap(announceCompleteDups);
	final int nCards = cardMap.size();
	_cards = cardMap.keySet().toArray(new Card[nCards]);
	Arrays.sort(_cards, Card._ByCardNumberOnly);
    }

    /**
     * <pre>
     * Interesting note on text files and the "right" way to do things:
     * https://stackoverflow.com/questions/17405165/first-character-of-the-reading-from-the-text-file-%C3%AF
     * </pre>
     */
    private TreeMap<Card, Card> loadCardMap(final boolean announceCompleteDups) {
	final TreeMap<Card, Card> cardMap = new TreeMap<>(Card._ByClueThenAnswer);
	try (final BufferedReader in = new BufferedReader(
		new InputStreamReader(new FileInputStream(_cardsFile), "UTF-8"))) {
	    /** Mark the stream at the beginning of the file. */
	    if (in.markSupported()) {
		/**
		 * Mark where we're at in the file (the beginning). The "1" is a "read ahead
		 * limit."
		 */
		in.mark(1);
		/**
		 * If the first character is NOT feff, go back to the beginning of the file. If
		 * it IS feff, ignore it and continue on.
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
			final boolean hasContinuation = nextLine.charAt(nextLine.length() - 1) == '\t';
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
		    final String lbdASide = lbd._aSide;
		    final String lbdBSide = lbd._bSide;
		    if (lbdASide == null && lbdBSide == null) {
			/** Essentially, nextLine is blank. */
			continue;
		    }
		    if (aSide == null) {
			aSide = lbdASide;
			bSide = lbdBSide;
		    } else {
			if (lbdASide.length() > 0) {
			    aSide += " " + lbdASide;
			}
			if (lbdBSide.length() > 0) {
			    bSide += " " + lbdBSide;
			}
		    }
		    if (!lbd._nextLineIsContinuation) {
			wrapUp(cardMap, aSide, bSide, commentLinesList, comment, announceCompleteDups);
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

    private void wrapUp(final TreeMap<Card, Card> cardMap, final String clueSide, final String answerSide,
	    final ArrayList<String> commentList, final String comment, final boolean announceCompleteDups) {
	if (comment != null && comment.length() > 0) {
	    commentList.add(comment);
	}
	final boolean switchSides = _mode == Mode.SWITCH;
	if (clueSide != null && answerSide != null && clueSide.length() > 0 && answerSide.length() > 0) {
	    final int nNewCommentLines = commentList.size();
	    final String[] newCommentLines = commentList.toArray(new String[nNewCommentLines]);
	    final Card newCard = new Card(switchSides, _allSoundFiles, _partToStem, cardMap.size(), clueSide,
		    answerSide, newCommentLines);
	    final Card oldCard = cardMap.get(newCard);
	    if (oldCard != null) {
		if (announceCompleteDups) {
		    System.out.println(
			    String.format("Merging Card #%d into #%d:", newCard._cardNumber, oldCard._cardNumber));
		    System.out.println(oldCard.getString());
		    System.out.println(newCard.getString());
		}
		if (nNewCommentLines > 0) {
		    final String[] oldCommentLines = oldCard._commentLines;
		    final int nOldCommentLines = oldCommentLines == null ? 0 : oldCommentLines.length;
		    final int nAllCommentLines = nOldCommentLines + nNewCommentLines;
		    final String[] allCommentLines = new String[nAllCommentLines];
		    if (nOldCommentLines > 0) {
			System.arraycopy(oldCommentLines, 0, allCommentLines, 0, nOldCommentLines);
		    }
		    System.arraycopy(newCommentLines, 0, allCommentLines, nOldCommentLines, nNewCommentLines);
		    oldCard._commentLines = allCommentLines;
		    commentList.clear();
		    return;
		}
	    }
	    cardMap.put(newCard, newCard);
	    commentList.clear();
	}
    }

    private int announceAndClumpDuplicates() {
	if (_clumping != Clumping.BY_CLUE && _clumping != Clumping.BY_ANSWER) {
	    return 0;
	}
	final boolean byClue = _clumping == Clumping.BY_CLUE;
	final Comparator<Card> comparator = byClue ? Card._ByClueSideOnly : Card._ByAnswerSideOnly;
	final int nCards = _cards.length;
	int nDups = 0;
	final String sideBeingChecked = byClue ? "Clue Side" : "Answer Side";
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
		System.out.println(String.format("Duplicate %s #%d:", sideBeingChecked, nDups++));
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
	    newCards[k1++] = card;
	    final ArrayList<Card> slaves = kingToSlaves.get(card);
	    final int nSlaves = slaves.size();
	    for (int k2 = 0; k2 < nSlaves; ++k2) {
		final Card slave = slaves.get(k2);
		slave._cardNumber = k1;
		newCards[k1++] = slave;
	    }
	}
	System.arraycopy(newCards, 0, _cards, 0, nCards);
	return nDups;
    }

    private void reWriteCardsFile() {
	final int nCards = _cards.length;
	if (nCards == 0) {
	    return;
	}
	final int nDigits = (int) (Math.log10(nCards) + 1d);
	final String intFormat = String.format("%%%dd.", nDigits);
	final String blankIntFormat = String.format("%%-%ds ", nDigits);
	final String blankIntString = String.format(blankIntFormat, "");
	final String cluePartFormat;
	{
	    int maxCluePartLen = 0;
	    for (final Card card : _cards) {
		final CardParts aParts = new CardParts(card.getFullString(/* clueSide= */true),
			Statics._MaxLenForCardPart);
		maxCluePartLen = Math.max(maxCluePartLen, aParts._maxLen);
	    }
	    cluePartFormat = String.format("%%-%ds", maxCluePartLen);
	}

	try (PrintWriter pw = new PrintWriter(_cardsFile)) {
	    boolean recentWasMultiLine = false;
	    for (int kCard = 0, nPrinted = 0; kCard < nCards; ++kCard) {
		final Card card = _cards[kCard];
		final CardParts clueParts = new CardParts(card.getFullString(/* clueSide= */true),
			Statics._MaxLenForCardPart);
		final CardParts answerParts = new CardParts(card.getFullString(/* clueSide= */false),
			Statics._MaxLenForCardPart);
		final int nClueParts = clueParts.size();
		final int nAnswerParts = answerParts.size();
		final int nDataParts = Math.max(nClueParts, nAnswerParts);
		final String[] commentLines = card._commentLines;
		final int nCommentLines = commentLines == null ? 0 : commentLines.length;
		final boolean isMultiLine = nCommentLines > 0 || nDataParts > 1;
		if (kCard > 0) {
		    if ((isMultiLine || recentWasMultiLine || (nPrinted % Statics._BlockSize == 0))) {
			pw.println();
		    }
		}
		recentWasMultiLine = isMultiLine;
		for (int kComment = 0; kComment < nCommentLines; ++kComment) {
		    final CommentParts commentParts = new CommentParts(commentLines[kComment], Statics._MaxLineLen);
		    for (int kCommentPart = 0; kCommentPart < commentParts.size(); ++kCommentPart) {
			pw.print(commentParts.get(kCommentPart));
			if (kCommentPart < commentParts.size() - 1) {
			    pw.print('\t');
			}
			pw.println();
		    }
		}
		for (int kDataPart = 0; kDataPart < nDataParts; ++kDataPart) {
		    final String cluePart = kDataPart < nClueParts ? clueParts.get(kDataPart) : null;
		    final String answerPart = kDataPart < nAnswerParts ? answerParts.get(kDataPart) : null;
		    if (kDataPart == 0) {
			pw.printf(intFormat, card._cardNumber);
		    } else {
			pw.print(blankIntString);
		    }
		    pw.print('\t');
		    pw.printf(answerPart != null ? cluePartFormat : "%s", cluePart != null ? cluePart : "");
		    if (answerPart != null) {
			pw.printf("\t%s", answerPart);
		    }
		    if (kDataPart < nDataParts - 1) {
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
	_quizGenerator.updateProperties(_properties);
    }

    void reWritePropertiesFile() {
	final Properties properties;
	final long seed = Long.parseLong(PropertyPlus.RANDOM_SEED.getValidString(_properties));
	if (seed < 0) {
	    properties = (Properties) _properties.clone();
	    final int topCardIdx0 = Integer.parseInt(PropertyPlus.TOP_CARD_INDEX.getValidString(_properties));
	    final int maxNNewWords = Integer.parseInt(PropertyPlus.NUMBER_OF_NEW_WORDS.getValidString(_properties));
	    final int maxNRecentWords = Integer
		    .parseInt(PropertyPlus.NUMBER_OF_RECENT_WORDS.getValidString(_properties));
	    final int topIndexCardIdx1 = Math.min(topCardIdx0, maxNNewWords + maxNRecentWords - 1);
	    properties.put(PropertyPlus.TOP_CARD_INDEX._propertyName, Long.toString(topIndexCardIdx1));
	} else {
	    properties = _properties;
	}
	try (PrintWriter pw = new PrintWriter(new FileOutputStream(_gameFile))) {
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

    final private String getTypeIPrompt(final int cardIdx, final boolean currentQuestionWasWrongAtLeastOnce) {
	final int cardNumber = _cards[cardIdx]._cardNumber;
	final int quizLen = _quizPlus.getCurrentQuizLen();
	if (_mode == Mode.STEP) {
	    return String.format("CrdIdx=%d,#%d", cardIdx, cardNumber);
	}
	String typeIPrompt = "";
	final int currentIdxInQuiz = _quizPlus.getCurrentIdxInQuiz();
	final boolean criticalQuizIdx = _quizPlus.isCriticalQuizIndex(currentIdxInQuiz);
	if (criticalQuizIdx) {
	    typeIPrompt += "*";
	}
	typeIPrompt += String.format("%d of %d(CrdIdx=%d,#%d)", currentIdxInQuiz + 1, quizLen, cardIdx, cardNumber);
	final int nRights = _quizPlus.getNRights();
	int nWrongs = _quizPlus.getNWrongs();
	int nTrials = nRights + nWrongs;
	if (criticalQuizIdx && currentQuestionWasWrongAtLeastOnce) {
	    ++nWrongs;
	    ++nTrials;
	}
	if (nRights > 0 || nWrongs > 0) {
	    final long successPerCent = Math.round((100d * nRights) / nTrials);
	    typeIPrompt += String.format(",(Rt:Wr=%d:%d SccRt=%d%%)", nRights, nWrongs, successPerCent);
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
	     * Currently, we have no properties of our own to edit, so we immediately turn
	     * it over to _quizGenerator.
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
	final long[] core = new long[] {};
	final long[] others = _quizGenerator.getPropertyValues();
	final int nCore = core.length;
	final int nOthers = others.length;
	final long[] array = new long[nCore + nOthers];
	System.arraycopy(core, 0, array, 0, nCore);
	System.arraycopy(others, 0, array, nCore, nOthers);
	return array;
    }

    final String getString() {
	final String s0 = String.format(//
		"GameFile     : %s\n" //
			+ "GameDir      : %s\n" //
			+ "CardsFile    : %s\n" //
			+ "SoundFilesDir: %s", //
		_gameFile == null ? "NULL" : _gameFile, //
		_gameDir == null ? "NULL" : _gameDir, //
		_cardsFile == null ? "NULL" : _cardsFile, //
		_soundFilesDir == null ? "NULL" : _soundFilesDir //
	);
	final String s1 = getString0();
	return s0 + "\n" + s1;
    }

    final String getString0() {
	String s = String.format("%s RandomSeed[%d]", //
		_cards != null ? String.format("(w/ %d cards)", _cards.length) : "", //
		_randomSeed);
	if (_quizGenerator != null) {
	    s += "\n " + _quizGenerator.getString();
	}
	return s;
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

	OUTSIDE_LOOP: for (boolean keepGoing = true; keepGoing;) {
	    /** Check for a _status change from _quizPlus. */
	    final QuizPlusTransition quizPlusTransition = _quizGenerator.getStatusChange(_mode, nCards, restarted,
		    _quizPlus);
	    _quizPlus = quizPlusTransition._newQuizPlus;
	    restarted = false;
	    final ChangeType changeType = quizPlusTransition._changeType;
	    if (ChangeType._NewQuizSet.contains(changeType)) {
		System.out.println();
		if (_needLineFeed) {
		    System.out.println();
		}
		if (ChangeType._ReallyNewQuizSet.contains(changeType)) {
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
	    final String clueStringPart = card.getStringPart(/* clueSide= */true);
	    final int clueStringPartLen = clueStringPart.length();
	    final boolean clueHasStringPart = clueStringPartLen > 0;
	    final String answerStringPart = card.getStringPart(/* clueSide= */false);
	    final int answerStringPartLen = answerStringPart.length();
	    final boolean answerHasStringPart = answerStringPartLen > 0;
	    final File answerSoundFile = card.getSoundFile(/* clueSide= */false);
	    final boolean answerHasSoundFile = answerSoundFile != null;
	    boolean wasWrongAtLeastOnce = false;
	    for (boolean gotItRight = false; !gotItRight;) {
		final String typeIPrompt = getTypeIPrompt(cardIdx, wasWrongAtLeastOnce);
		final int typeIPromptLen = typeIPrompt.length();
		final String terminalString;
		if (answerHasSoundFile && answerHasStringPart) {
		    terminalString = String.format("%s%s%c   ", Statics._Sep2, Statics._SoundString,
			    Statics._keyboardSymbol);
		} else if (answerHasSoundFile) {
		    terminalString = String.format("%s%s  ", Statics._Sep2, Statics._SoundString);
		} else if (answerHasStringPart) {
		    terminalString = String.format("%s%c  ", Statics._Sep2, Statics._keyboardSymbol);
		} else {
		    terminalString = null;
		}
		final int terminalStringLen = terminalString.length();
		final int len1 = typeIPromptLen + Statics._Sep1Len + clueStringPartLen + terminalStringLen
			+ Math.min(Statics._RoomLen, answerStringPartLen);
		final String[] clueFields = clueStringPart.split(Statics._WhiteSpace);
		final int nClueFields = clueFields.length;
		final String[] answerFields;
		if (!answerHasStringPart) {
		    answerFields = new String[0];
		} else {
		    answerFields = answerStringPart.split(Statics._WhiteSpace);
		}
		final int nAnswerFields = answerFields.length;
		_needLineFeed = _needLineFeed || len1 > Statics._MaxLineLen;
		if (_needLineFeed) {
		    System.out.println();
		}
		boolean longQuestion = false;

		/** Expose the clue. */
		card.playSoundFileIfPossible(_silentMode, /* clueSide= */true);
		if (len1 <= Statics._MaxLineLen) {
		    System.out.printf("%s%s%s%s", typeIPrompt, Statics._Sep1, clueStringPart, terminalString);
		} else {
		    System.out.println(typeIPrompt);
		    System.out.print(Statics._PrefaceForNewLine);
		    int nUsedOnCurrentLine = Statics._PrefaceForNewLineLen;
		    longQuestion = true;
		    /** Break up the clue. */
		    for (int k = 0; k < nClueFields; ++k) {
			final String clueField = clueFields[k];
			final int clueFieldLen = clueField.length();
			final boolean justStartedLine = nUsedOnCurrentLine == (k == 0 ? Statics._PrefaceForNewLineLen
				: Statics._IndentLen);
			if (justStartedLine) {
			    System.out.print(clueField);
			    nUsedOnCurrentLine += clueFieldLen;
			} else if (nUsedOnCurrentLine + 1 + clueFieldLen + Statics._RoomLen >= Statics._MaxLineLen) {
			    System.out.println();
			    System.out.print(Statics._IndentString);
			    System.out.print(clueField);
			    nUsedOnCurrentLine = Statics._IndentLen + clueFieldLen;
			} else {
			    System.out.printf(" %s", clueField);
			    nUsedOnCurrentLine += 1 + clueFieldLen;
			}
		    }
		    System.out.print(terminalString);
		}
		/** Get the response. */
		final InputString inputString = new InputString(sc);
		longQuestion = longQuestion || inputString._nLinesOfResponse > 1;
		final String responseStringPart = inputString._inputString;
		final int responseStringPartLen = responseStringPart.length();
		if (responseStringPartLen == 1) {
		    /**
		     * <pre>
		     * The response is a command.
		     * quit, restart, edit the properties, or fall through,
		     * letting this be a bona fide response (such as ở).
		     * </pre>
		     */
		    final char char0Uc = Character.toUpperCase(responseStringPart.charAt(0));
		    if (char0Uc == Statics._QuitChar) {
			System.out.print(Statics.getFullYesNoPrompt("Reallly quit?", true));
			final YesNoResponse yesNoResponse = new YesNoResponse(sc, /* defaultYesNo= */true);
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
			final String keyString = topCard.getFullString(/* clueSide= */true);
			loadCards(/* announceCompleteDups= */false);
			nCards = _cards.length;
			int tci = -1;
			for (int kCard = 0; kCard < nCards; ++kCard) {
			    final Card card1 = _cards[kCard];
			    if (keyString.compareToIgnoreCase(card1.getFullString(true)) == 0) {
				tci = kCard;
				break;
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

		if (_silentMode && (!clueHasStringPart || !answerHasStringPart)) {
		    System.out.println(Statics._Sep1
			    + "In Silent Mode, both Clue and Answer must have String Parts.  Automatically right."
			    + Statics._Sep2);
		    _needLineFeed = true;
		    gotItRight = true;
		    continue;
		}
		/** Must process a response to the clue. */
		card.playSoundFileIfPossible(_silentMode, /* clueSide= */false);
		if (responseStringPartLen == 0) {
		    int nUsedOnCurrentLine = 0;
		    /**
		     * User just wants a check so we have to print out the String part of the
		     * answer. Break it up.
		     */
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

		    if (_mode == Mode.STEP) {
			gotItRight = true;
			_needLineFeed = true;
		    } else {
			final boolean defaultYesValue = true;
			final String prompt = Statics.getFullYesNoPrompt(Statics._CountAsRightString, defaultYesValue);
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
		    }
		    continue;
		}
		/** User typed in a non-trivial response with at least two characters. */
		final ResponseEvaluator responseEvaluator = new ResponseEvaluator(sc, _diacriticsTreatment,
			answerStringPart, responseStringPart);
		final String[] diffStrings = responseEvaluator._diffStrings;
		gotItRight = responseEvaluator._gotItRight;
		if (diffStrings != null) {
		    /** Because diffStrings is not null, print out the answer, but break it up. */
		    int nUsedOnCurrentLine = 0;
		    for (int k = 0; k < nAnswerFields;) {
			final String answerField;
			if (gotItRight && k == 0) {
			    /** Combine the first answer field with a Heavy Check Mark. */
			    answerField = String.format("%c %s", Statics._HeavyCheckChar, answerFields[0]);
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
			fullYesNoPrompt = Statics.getFullYesNoPrompt(Statics._CountAsRightString, true);
		    } else {
			fullYesNoPrompt = Statics.getFullYesNoPrompt(Statics._CountAsRightString, false);
		    }
		    final int fullYesNoPromptLen = fullYesNoPrompt.length();
		    /** Put diffString and the prompt onto the current line if there's room. */
		    if (nUsedOnCurrentLine + 1 + fullDiffStringLen + Statics._Sep2Len + fullYesNoPromptLen
			    + Statics._RoomLen <= Statics._MaxLineLen) {
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
			    System.out.printf("%s%s%s%s", Statics._IndentString, fullDiffString, Statics._Sep2,
				    fullYesNoPrompt);
			} else {
			    /** Otherwise, separate the diffString and the prompt string. */
			    System.out.printf("%s%s", Statics._IndentString, fullDiffString);
			    System.out.println();
			    System.out.printf("%s%s", Statics._IndentString, fullYesNoPrompt);
			}
		    }
		    if (_mode == Mode.STEP) {
			gotItRight = true;
			_needLineFeed = true;
		    } else {
			final YesNoResponse yesNoResponse = new YesNoResponse(sc, gotItRight);
			gotItRight = yesNoResponse._yesValue;
			_needLineFeed = !yesNoResponse._lastLineWasBlank;
		    }
		} else {
		    /** diffStrings == null. Check for acceptable sound. */
		    _needLineFeed = longQuestion;
		    if (_mode == Mode.STEP) {
			gotItRight = true;
		    } else if (answerHasSoundFile) {
			final boolean defaultYesValue = true;
			final String fullPrompt = Statics.getFullYesNoPrompt("Strings Match.  Sound OK?",
				defaultYesValue);
			System.out.print(fullPrompt);
			final YesNoResponse yesNoResponse = new YesNoResponse(sc, defaultYesValue);
			gotItRight = yesNoResponse._yesValue;
			_needLineFeed = true;
		    }
		}
		wasWrongAtLeastOnce = wasWrongAtLeastOnce || !gotItRight;
	    }
	    _quizPlus.reactToRightResponse(wasWrongAtLeastOnce);
	}
    }

    public static void main(final String[] args) {
	System.out.printf("SoundString=%s, PenString=%s, IndentString=\"%s\", SpecialChars=\"%s\"",
		Statics._SoundString, Statics._PenString, Statics._IndentString, new String(Statics._SpecialChars));
	System.out.println();
	System.out.println(DirsTracker.getDirCasesFinderDirsString());
	final FlashCardsGame flashCardsGame = new FlashCardsGame(args[0]);
	System.out.println(flashCardsGame.getString());
	if (flashCardsGame._mode != Mode.SWITCH) {
	    System.out.println();
	    try (Scanner sc = new Scanner(System.in)) {
		flashCardsGame.mainLoop(sc);
	    } catch (final Exception e) {
		e.printStackTrace();
	    }
	}
    }

}
