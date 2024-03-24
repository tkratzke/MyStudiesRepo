package com.skagit.flashCardsGame;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import com.skagit.flashCardsGame.enums.ChangeType;

public class Statics {

	/**
	 * <pre>
	 *  Good list of characters.
	 *  http://xahlee.info/comp/unicode_arrows.html
	 * </pre>
	 */
	final static char _ClubSymbolChar = '\u2663';
	final static char _CommentChar = '!';
	final static char _DiamondSymbolChar = '\u2666';
	final static char _EditPropertiesChar = '@';
	final static char _EmptySetChar = '\u2205';
	final static char _HeavyCheckChar = '\u2714';
	final static char _HelpChar = 'H';
	final static char _QuitChar = '!';
	final static char _ReloadCardsChar = '$';
	final static char _RestartQuizChar = '#';
	final static char _ReturnChar = '\u23CE';
	final public static char _RtArrowChar = '\u2192';
	final static char _RtArrowChar2 = '\u21a0';
	final static char _SpadeSymbolChar = '\u2660';
	final static char _LtArrowChar = '\u2190';
	final static char _NoChar = 'N';
	final static char _TabSymbolChar = '\u2409';
	final static char _YesChar = 'Y';
	final static char _FileDelimiter = '%';
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

	final static int _BlockSize = 10;
	final static int _RoomLen = 10;
	final static int _MaxLenForCardPart = 35;
	final static int _MaxLineLen = 65;
	final static int _MaxNFailsPerElement = 5;
	final static int _NominalTabLen = 8;

	final static String _CommentString = "" + _CommentChar + ' ';
	final static String _CountAsRightString = "Count as Right? ";
	final static String _EndCardsString = "$$";
	/** Either of the following two FieldSeparators seems to work. */
	final static String _FieldSeparator = "\\s*\\t\\s*";
	final static String _FieldSeparator1 = "(\\s*\\t\\s*)+";
	final static String _RegExForPunct = "[,.;:?!]+";
	final static String _Sep1 = " " + _ClubSymbolChar;
	final static String _Sep2 = "" + _DiamondSymbolChar + " ";
	final static String _HelpString = String.format(
			"%c=\"Show this Message,\" %c=Quit, %c=Edit Properties, %c=Restart Quiz, %c=Reload Cards"
					+ "%c=\"Show-and-ask,\" %s=Next Line is Continuation",
			_HelpChar, _QuitChar, _EditPropertiesChar, _RestartQuizChar, _ReloadCardsChar,
			_ReturnChar, "" + _TabSymbolChar + _ReturnChar);
	final static String _IndentString = String
			.format(String.format("%%-%ds", _NominalTabLen), "");
	final static String _NoString = "No";
	final static String _PrefaceForNewLine = _IndentString + _Sep1;
	final static int _PrefaceForNewLineLen = _PrefaceForNewLine.length();
	final static String _PropertiesEnding = ".properties";
	final static String _WhiteSpace = "\\s+";
	final static String _YesString = "Yes";

	/** Derived ints. */
	final static int _Sep1Len = _Sep1.length();
	final static int _Sep2Len = _Sep2.length();
	final static int _IndentLen = _IndentString.length();

	static class YesNoResponse {
		final boolean _yesValue, _lastLineWasBlank;

		YesNoResponse(final Scanner sc, final boolean defaultYesValue) {
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

	final static EnumSet<ChangeType> _NewQuizSet = EnumSet.of(//
			ChangeType.CRITICAL_ONLY_WIN, //
			ChangeType.MOVE_ON_WIN, //
			ChangeType.LOSS, //
			ChangeType.NOTHING_TO_SOMETHING, //
			ChangeType.PARAMETERS_CHANGED, //
			ChangeType.RESTART//
	);

	final static EnumSet<ChangeType> _ReallyNewQuizSet = EnumSet.of(//
			ChangeType.MOVE_ON_WIN, //
			ChangeType.NOTHING_TO_SOMETHING, //
			ChangeType.PARAMETERS_CHANGED //
	);

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

	static String getFullYesNoPrompt(final String prompt, final boolean defaultYesValue) {
		final char otherChar = defaultYesValue ? _NoChar : _YesChar;
		final String defaultString = defaultYesValue ? _YesString : _NoString;
		final String otherString = defaultYesValue ? _NoString : _YesString;
		return String.format("%s %c=%s,%c=%s: ", prompt, _ReturnChar, defaultString,
				otherChar, otherString);
	}

	/** Avoids having the same value consecutively. */
	static void shuffleArray(final int[] ints, final Random r, int lastValue) {
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
	 * Returns 0 if both null, 2 if neither is null, 1 if o0 is not null, and -1 if o1 is
	 * not null.
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

}
