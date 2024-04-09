package com.skagit.flashCardsGame;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Statics {

    /**
     * <pre>
     *  Good list of characters.
     *  http://xahlee.info/comp/unicode_arrows.html
     * </pre>
     */
    final public static String _SpaceProxy = "%Sp%";
    final public static char _keyboardSymbol = '\u2328';
    final public static char _ClubSymbolChar = '\u2663';
    final public static char _CommentChar = '!';
    final public static char _DiamondSymbolChar = '\u2666';
    final public static char _EditPropertiesChar = '@';
    final public static char _EmptySetChar = '\u2205';
    final public static char _HeavyCheckChar = '\u2714';
    final public static char _HelpChar = 'H';
    final public static char _QuitChar = '!';
    final public static char _ReloadCardsChar = '$';
    final public static char _RestartQuizChar = '#';
    final public static char _ReturnChar = '\u23CE';
    final public static char _RtArrowChar = '\u2192';
    final public static char _RtArrowChar2 = '\u21a0';
    final public static char _SpadeSymbolChar = '\u2660';
    final public static char _LtArrowChar = '\u2190';
    final public static char _NoChar = 'N';
    final public static char _TabSymbolChar = '\u2409';
    final public static char _YesChar = 'Y';
    final public static char _FileDelimiter = '%';
    public static char[] _SpecialChars = { //
	    _keyboardSymbol, _ClubSymbolChar, _CommentChar, _DiamondSymbolChar, _EditPropertiesChar, _EmptySetChar,
	    _HeavyCheckChar, _HelpChar, _QuitChar, _ReloadCardsChar, _RestartQuizChar, _ReturnChar, _RtArrowChar,
	    _RtArrowChar2, _SpadeSymbolChar, _LtArrowChar, _NoChar, _TabSymbolChar, _YesChar, _FileDelimiter };

    final public static int _BlockSize = 10;
    final public static int _RoomLen = 10;
    final public static int _MaxLenForCardPart = 45;
    final public static int _MaxLineLen = 80;
    final public static int _MaxNFailsPerElement = 5;
    final public static int _NominalTabLen = 8;

    final public static String _SoundString, _PenString;
    static {
	final byte[] soundBytes = new byte[] { (byte) 0xF0, (byte) 0x9F, (byte) 0x94, (byte) 0x8A };
	_SoundString = new String(soundBytes, StandardCharsets.UTF_8);
	final byte[] penBytes = new byte[] { (byte) 0xF0, (byte) 0x9F, (byte) 0x96, (byte) 0x8A };
	_PenString = new String(penBytes, StandardCharsets.UTF_8);
    }
    final public static String _GameFileEndingLc = "-fcg.properties";
    final public static String _CardsFileExtensionLc = "txt";
    final public static String _CardsFileEndingLc = "." + _CardsFileExtensionLc;
    final public static String _SoundFilesDirEndingLc = "sound.files";
    final public static String _CommentString = Character.toString(_CommentChar) + ' ';
    final public static String _CountAsRightString = "Count as Right? ";
    final public static String _EndCardsString = "$$";
    /** Either of the following two FieldSeparators seems to work. */
    final public static String _FieldSeparator = "\\s*\\t\\s*";
    final public static String _FieldSeparator1 = "(\\s*\\t\\s*)+";
    final public static String _RegExForPunct = "[,.;:?!]+";
    final public static String _Sep1 = Character.toString(_ClubSymbolChar);
    final public static String _Sep2 = Character.toString(_DiamondSymbolChar) + " ";
    final public static String _HelpString = String.format(
	    "%c=\"Show this Message,\" %c=Quit, %c=Edit Properties, %c=Restart Quiz, " //
		    + "%c=Reload Cards, %c=\"Show-and-ask,\" %s=Next Line is Continuation",
	    _HelpChar, _QuitChar, _EditPropertiesChar, _RestartQuizChar, _ReloadCardsChar, _ReturnChar,
	    Character.toString(_TabSymbolChar) + _ReturnChar);
    final public static String _IndentString = String.format(String.format("%%-%ds", _NominalTabLen), "");
    final public static String _NoString = "No";
    final public static String _PrefaceForNewLine = _IndentString + _Sep1;
    final public static int _PrefaceForNewLineLen = _PrefaceForNewLine.length();
    final public static String _WhiteSpace = "\\s+";
    final public static String _YesString = "Yes";

    /** Derived ints. */
    final public static int _Sep1Len = _Sep1.length();
    final public static int _Sep2Len = _Sep2.length();
    final public static int _IndentLen = _IndentString.length();

    public static class YesNoResponse {
	final boolean _yesValue, _lastLineWasBlank;

	YesNoResponse(final Scanner sc, final boolean defaultYesValue) {
	    final char otherChar = defaultYesValue ? _NoChar : _YesChar;
	    final InputString inputString = new InputString(sc);
	    final String response = inputString._inputString;
	    final boolean lastLineWasBlank = inputString._lastLineWasBlank;
	    if (response.length() == 0) {
		_yesValue = defaultYesValue;
	    } else {
		_yesValue = Character.toUpperCase(response.charAt(0)) == otherChar ? !defaultYesValue : defaultYesValue;
	    }
	    _lastLineWasBlank = lastLineWasBlank;
	}
    }

    /**
     * <pre>
     * Good article on dealing with VN diacritics.
     * https://namnguyen1202.hashnode.dev/removing-vietnamese-diacritic-in-java
     * Note the use of a default ctor in an anonymous inherited class.
     * </pre>
     */
    private static final HashMap<Character, Character> _VnToEngCharMap = new HashMap<>() {
	private static final long serialVersionUID = 1L;
	{
	    final String[][] mappings = { //
		    { "áàảãạâấầẩẫậăắằẳẵặ", "a" }, //
		    { "đ", "d" }, //
		    { "éèẻẽẹêếềểễệ", "e" }, //
		    { "íìỉĩị", "i" }, //
		    { "óòỏõọôốồổỗộơớờởỡợ", "o" }, //
		    { "úùủũụưứừửữự", "u" }, //
		    { "ýỳỷỹỵ", "y" }, //
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

    final public static String StripVNDiacritics(final String s) {
	final StringBuilder sb = new StringBuilder(s);
	boolean didSomething = false;
	for (int k = 0; k < sb.length(); k++) {
	    final char c = sb.charAt(k);
	    final Character target = _VnToEngCharMap.get(c);
	    if (target != null && c != target) {
		sb.setCharAt(k, target);
		didSomething = true;
	    }
	}
	return didSomething ? sb.toString() : s;
    }

    public static String CleanWhiteSpace(final String s) {
	if (s == null) {
	    return "";
	}
	final String ss = s.trim().replaceAll(_WhiteSpace, " ");
	return ss.length() == s.length() ? s : ss;
    }

    public static String KillPunct(final String s) {
	if (s == null) {
	    return "";
	}
	final String ss = s.replaceAll(_RegExForPunct, "");
	return ss.length() == s.length() ? s : ss;
    }

    public static String getFullYesNoPrompt(final String prompt, final boolean defaultYesValue) {
	final char otherChar = defaultYesValue ? _NoChar : _YesChar;
	final String defaultString = defaultYesValue ? _YesString : _NoString;
	final String otherString = defaultYesValue ? _NoString : _YesString;
	return String.format("%s %c=%s,%c=%s: ", prompt, _ReturnChar, defaultString, otherChar, otherString);
    }

    /** Avoids having the same value consecutively. */
    public static void shuffleArray(final int[] ints, final Random r, int lastValue) {
	final int n = ints.length;
	for (int k0 = 0; k0 < n; ++k0) {
	    for (int nFails = 0; nFails <= _MaxNFailsPerElement; ++nFails) {
		final int vK0 = ints[k0];
		final int k1 = k0 + r.nextInt(n - k0);
		final int vK1 = ints[k1];
		if (vK1 != lastValue || nFails == _MaxNFailsPerElement) {
		    lastValue = ints[k0] = vK1;
		    ints[k1] = vK0;
		    break;
		}
	    }
	}
    }

    /**
     * Returns 0 if both null, 2 if neither is null, 1 if o0 is not null, and -1 if
     * o1 is not null.
     */
    public static int NullCompare(final Object o0, final Object o1) {
	if ((o0 == null) != (o1 == null)) {
	    return o0 == null ? -1 : 1;
	}
	if (o0 == null) {
	    return 0;
	}
	return 2;
    }

    public static String getSystemProperty(final String propertyName, final boolean useSpaceProxy) {
	final String rawProperty = System.getProperty(propertyName);
	if (rawProperty == null || !useSpaceProxy) {
	    return rawProperty;
	}
	final String replaced = rawProperty.replaceAll(_SpaceProxy, " ");
	return replaced;
    }

    public static String formatNow() {
	final String nowString = new SimpleDateFormat("M/d/yy h:mm:ss a").format(new Date(System.currentTimeMillis()));
	return nowString;
    }

    public static String getCanonicalPath(final File f) {
	try {
	    return f.getCanonicalPath();
	} catch (final IOException e) {
	} catch (final NullPointerException e) {
	}
	return null;
    }

    private static String turnSlashesAround(final String path0) {
	/** Turn all slashes to File.separator. */
	final String path1 = path0.replaceAll(Pattern.quote("/"), Matcher.quoteReplacement(File.separator));
	return path1.replaceAll(Pattern.quote("\\"), Matcher.quoteReplacement(File.separator));
    }

    /**
     * <pre>
     * 1. a/b/c.txt -> c
     * 2. a.txt     -> a
     * 3. a/b/c     -> c
     * 4. a/b/c/    -> ""
     * 5. a/b.c/d   -> d
     * 6. abcd      -> abcd
     * </pre>
     */
    public static String getBaseName(final String path0) {
	final int len0 = path0 == null ? 0 : path0.length();
	if (len0 == 0) {
	    return null;
	}
	/** Turn all slashes to File.separator. */
	final String path = turnSlashesAround(path0);
	final int len = path == null ? 0 : path.length();
	if (path.charAt(len - 1) == File.separatorChar) {
	    /** Case 4 above. */
	    return "";
	}
	final int lastSep = path.lastIndexOf(File.separatorChar);
	final int lastDot = path.lastIndexOf('.');
	if (lastSep >= 0 && lastDot >= 0) {
	    if (lastDot < lastSep) {
		/** Case 5 above */
		return path.substring(lastSep + 1, len);
	    } else {
		/** Cannot have lastDot == lastSep, so lastDot > lastSep, and we have Case 1. */
		return path.substring(lastSep + 1, lastDot);
	    }
	} else if (lastDot >= 0) {
	    /** Case 2. */
	    return path.substring(0, lastDot);
	} else if (lastSep >= 0) {
	    /** Case 3. */
	    return path.substring(lastSep + 1, len);
	}
	/** No separators or dots. */
	return path;
    }

    public static File getGameDir(final String gameDirString) {
	/**
	 * We try interpreting gameDirString as a directory by checking for it under,
	 * GamesDir, user.dir, and by itself.
	 */
	final File f0 = new File(DirsTracker.getGamesDir(), gameDirString);
	if (f0.isDirectory() && dirHasGameFile(f0)) {
	    return f0;
	}
	final File f1 = new File(DirsTracker.getUserDir(), gameDirString);
	if (f1.isDirectory() && dirHasGameFile(f1)) {
	    return f1;
	}
	final File f2 = new File(gameDirString);
	if (f2.isDirectory() && dirHasGameFile(f2)) {
	    return f2;
	}
	/**
	 * Could not interpret it as a directory. Try as a file. Files must end in
	 * _GameFileEndingLc
	 */
	if (gameDirString.toLowerCase().endsWith(_GameFileEndingLc)) {
	    final File f = new File(gameDirString);
	    final String fName0 = f.getName();
	    final String fName1 = fName0.substring(0, fName0.length() - _GameFileEndingLc.length());
	    final File parentDir = new File(gameDirString).getParentFile();
	    if (fName1.equalsIgnoreCase(parentDir.getName())) {
		return parentDir;
	    }
	}
	return null;
    }

    private static boolean dirHasGameFile(final File dir) {
	return getGameFileFromDirFile(dir) != null;
    }

    public static File getGameFileFromDirFile(final File dir) {
	if (dir == null) {
	    return null;
	}
	final String dName = dir.getName();
	final File gameFile = new File(dir, dName + _GameFileEndingLc);
	return gameFile.isFile() ? gameFile : null;
    }

    /**
     * <pre>
     * 1. a/b/c.txt -> a/b/c.txt
     * 2. a.txt     -> a.txt
     * 3. a/b/c     -> a/b/c.txt
     * 4. a/b/c/    -> .txt
     * 5. a/b.c/d   -> a/b.c/d.txt
     * 6. abcd      -> abcd.txt
     * </pre>
     */
    public static String forceExtension(final String path0, final String extension) {
	final String withDot = "." + extension;
	final int len0 = path0 == null ? 0 : path0.length();
	if (len0 == 0) {
	    return withDot;
	}
	/** Turn all slashes to File.separator. */
	final String path1 = path0.replaceAll(Pattern.quote("/"), Matcher.quoteReplacement(File.separator));
	final String path = path1.replaceAll(Pattern.quote("\\"), Matcher.quoteReplacement(File.separator));
	final int len = path.length();
	final int lastSep = path.lastIndexOf(File.separatorChar);
	if (lastSep == len - 1) {
	    return path + withDot;
	}
	final int lastDot = path.lastIndexOf('.');
	if (lastDot < 0) {
	    /** No dot. Tack on withDot. */
	    return path + withDot;
	}
	if (lastSep < lastDot) {
	    /** No Separator after lastDot. Tack on withDot after removing the extension. */
	    return path.substring(0, lastDot) + withDot;
	}
	/** Separator after last dot. Tack on withDot. */
	return path + withDot;
    }

    public static File getCardsFile(final File gameDir, final String cardsFileString0) {
	final int len = cardsFileString0 == null ? 0 : cardsFileString0.length();
	final String cardsFileString1;
	if (len == 0) {
	    cardsFileString1 = gameDir.getName() + "-Cards.txt";
	} else {
	    cardsFileString1 = cardsFileString0;
	}
	final String cardsFileString2 = turnSlashesAround(cardsFileString1);

	final File[] dirsToTry = new File[] { gameDir, DirsTracker.getCardFilesDir(), DirsTracker.getUserDir(),
		DirsTracker.getGamesDir() };
	/**
	 * <pre>
	 * Try:
	 * 1. as-is, if it ends with -cards.txt.
	 * 2. Strip the extension and the dot, and add -cards.txt
	 * 3. Add -cards.txt to it.
	 * </pre>
	 */
	for (int iPass = 0; iPass < 3; ++iPass) {
	    final String cardsFileString3;
	    if (iPass == 0) {
		if (cardsFileString2.toLowerCase().endsWith("-cards.txt")) {
		    cardsFileString3 = cardsFileString2;
		} else {
		    continue;
		}
	    } else if (iPass == 1) {
		final int lastDot = cardsFileString2.lastIndexOf('.');
		final int lastSlash = cardsFileString2.lastIndexOf(File.separatorChar);
		if (lastDot < 0 || lastDot < lastSlash) {
		    /** No extension to strip. */
		    continue;
		}
		cardsFileString3 = cardsFileString2.substring(0, lastDot - 1) + "-Cards.txt";
	    } else {
		cardsFileString3 = cardsFileString2 + "-Cards.txt";
	    }
	    for (final File dir : dirsToTry) {
		final File f = new File(dir, cardsFileString3);
		if (f.isFile()) {
		    return f;
		}
	    }
	}
	return null;
    }

    public static File getSoundFilesDir(final File gameDir, final String soundFilesString0) {
	final int len = soundFilesString0 == null ? 0 : soundFilesString0.length();
	final String soundFilesString1;
	if (len == 0) {
	    soundFilesString1 = gameDir.getName() + "-SoundFiles";
	} else {
	    soundFilesString1 = soundFilesString0;
	}
	final String soundFilesString2 = turnSlashesAround(soundFilesString1);

	final File[] dirsToTry = new File[] { gameDir, DirsTracker.getSoundFilesDirsDir(), DirsTracker.getUserDir(),
		DirsTracker.getGamesDir() };
	/**
	 * <pre>
	 * Try:
	 * 1. as-is, if it ends with -SoundFiles.
	 * 2. Strip the extension and the dot, and add -cards.txt
	 * 3. Add -cards.txt to it.
	 * </pre>
	 */
	for (int iPass = 0; iPass < 2; ++iPass) {
	    final String soundFilesString3;
	    if (iPass == 0) {
		if (soundFilesString2.toLowerCase().endsWith("-soundfiles")) {
		    soundFilesString3 = soundFilesString2;
		} else {
		    continue;
		}
	    } else {
		soundFilesString3 = soundFilesString2 + "-SoundFiles";
	    }
	    for (final File dir : dirsToTry) {
		final File f = new File(dir, soundFilesString3);
		if (f.isDirectory()) {
		    return f;
		}
	    }
	}
	return null;
    }

    public static void mainx(final String[] args) {
	if (false) {
	    final String[][] examples = { //
		    { "a/b/c/", "" }, //
		    { "a/b/c.txt", "c" }, //
		    { "a/b/c", "c" }, //
		    { "a/b.c/d", "d" }, //
		    { "a\\b\\c\\", "" }, //
		    { "a\\b\\c.txt", "c" }, //
		    { "a\\b\\c", "c" }, //
		    { "a\\b.c\\d", "d" }, //
		    { "a.txt", "a" }, //
		    { "abcd", "abcd" } //
	    };
	    final int nExamples = examples.length;
	    for (int k = 0; k < nExamples; ++k) {
		final String s0 = examples[k][0];
		final String s1 = examples[k][1];
		final String s2 = getBaseName(s0);
		System.out.printf("\n%b %s %s %s", s1.equals(s2), s0, s1, s2);
	    }
	} else if (false) {
	    /**
	     * <pre>
	     * 1. a/b/c.txt -> a/b/c.txt
	     * 2. a.txt     -> a.txt
	     * 3. a/b/c     -> a/b/c.txt
	     * 4. a/b/c/    -> .txt
	     * 5. a/b.c/d   -> a/b.c/d.txt
	     * 6. abcd      -> abcd.txt
	     * </pre>
	     */
	    final String[][] examples = { //
		    { "a/b/c/", "a\\b\\c\\.xxx" }, //
		    { "a/b/c.txt", "a\\b\\c.xxx" }, //
		    { "a.txt", "a.xxx" }, //
		    { "a/b/c", "a\\b\\c.xxx" }, //
		    { "a/b.c/d", "a\\b.c\\d.xxx" }, //
		    { "abcd", "abcd.xxx" } //
	    };
	    final int nExamples = examples.length;
	    for (int k = 0; k < nExamples; ++k) {
		final String s0 = examples[k][0];
		final String s1 = examples[k][1];
		final String s2 = forceExtension(s0, "xxx");
		System.out.printf("\n%b %s %s %s", s1.equals(s2), s0, s1, s2);
	    }
	}
    }

}
