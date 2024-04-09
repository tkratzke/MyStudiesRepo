package com.skagit.advMath.codeComputer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.TreeMap;

public class CodeComputer {

    private static String _RightArrow = "->";

    /** We store Codes as non-numeric categorical data. */
    private static enum Code {
	EM, ZERO, ONE, TWO;
    }

    private static enum BuildUseIgnore {
	BUILD, USE, IGNORE
    }

    private static class NTrailsAndCodeW {
	private final int _nTrails;
	private final Code _codeW;

	private NTrailsAndCodeW(final int nTrails, final Code codeW) {
	    _nTrails = nTrails;
	    _codeW = codeW;
	}

	private String getString() {
	    return String.format("[%d,%c]", _nTrails, codeToChar(_codeW));
	}

	@Override
	public String toString() {
	    return getString();
	}
    }

    private static class FullReduction {
	final private ArrayList<Code> _codeList, _reducedCodeList;
	final private int _nTrailsLost;
	final private boolean _irreducible;

	private FullReduction(final ArrayList<Code> codeList, final boolean useOnly) {
	    _codeList = codeList == null ? new ArrayList<>() : new ArrayList<>(codeList);
	    final int nCodes = _codeList.size();
	    if (useOnly) {
		final NTrailsAndCodeW nTrailsAndCodeW = getFromIrreducibleCodeLists(_codeList);
		if (nTrailsAndCodeW != null) {
		    _reducedCodeList = _codeList;
		    _irreducible = true;
		    _nTrailsLost = 0;
		    return;
		}
	    }
	    _reducedCodeList = new ArrayList<>();
	    int nTrailsLost = 0;
	    for (int k = 0; k < nCodes; ++k) {
		final Code code = _codeList.get(k);
		nTrailsLost += addToReducedCodeListAndCountTrails(code);
	    }
	    _nTrailsLost = nTrailsLost;
	    _irreducible = _CodeListComparator.compare(_codeList, _reducedCodeList) == 0;
	}

	/**
	 * <pre>
	 * The reducing operations are:
	 * 1. ZERO is replaced by ONE-ONE.
	 * Hence, there are no ZEROs.
	 *
	 * 2. EM-EM, EM-TWO, TWO-EM, and TWO-TWO all are replaced by TWO.
	 * 3. ONE-EM-ONE is replaced by ONE-ONE.
	 * Hence, any EM is a neighbor of w.
	 *
	 * 4. ONE-ONE-ONE is replaced by ONE, losing one trail.
	 * Hence, there are at most two consecutive ONEs.
	 *
	 * The rest are all replaced by ONE-ONE-TWO or TWO-ONE-ONE.
	 * We choose ONE-ONE-TWO if the preceding code is ONE and
	 * TWO-ONE-ONE otherwise.
	 *
	 * 5. TWO-ONE-TWO is replaced by ONE-ONE-TWO or TWO-ONE-ONE.
	 * 6. TWO-ONE-ONE-TWO is replaced by ONE-ONE-TWO or TWO-ONE-ONE.
	 * Hence, there is at most one TWO.
	 *
	 * 7. TWO-ONE-ONE-EM and EM-ONE-ONE-TWO
	 *    are replaced by ONE-ONE-TWO or TWO-ONE-ONE.
	 * </pre>
	 */
	private int addToReducedCodeListAndCountTrails(final Code code) {
	    /** ZERO -> ONE-ONE. */
	    if (code == Code.ZERO) {
		int nTrails = addToReducedCodeListAndCountTrails(Code.ONE);
		nTrails += addToReducedCodeListAndCountTrails(Code.ONE);
		return nTrails;
	    }
	    final int nExistingCodes = _reducedCodeList.size();
	    /** Consecutive EMs or TWOs -> TWO. */
	    final Code oneBack = nExistingCodes >= 1 ? _reducedCodeList.get(nExistingCodes - 1) : null;
	    if ((code == Code.EM || code == Code.TWO) && (oneBack == Code.EM || oneBack == Code.TWO)) {
		if (oneBack == Code.EM) {
		    _reducedCodeList.remove(nExistingCodes - 1);
		    return addToReducedCodeListAndCountTrails(Code.TWO);
		}
		return 0;
	    }
	    /** ONE-EM-ONE -> ONE-ONE. */
	    final Code twoBack = nExistingCodes >= 2 ? _reducedCodeList.get(nExistingCodes - 2) : null;
	    if (twoBack == Code.ONE && oneBack == Code.EM && code == Code.ONE) {
		_reducedCodeList.remove(nExistingCodes - 1);
		return addToReducedCodeListAndCountTrails(Code.ONE);
	    }

	    /** ONE-ONE-ONE -> ONE, losing a trail. */
	    if (twoBack == Code.ONE && oneBack == Code.ONE && code == Code.ONE) {
		_reducedCodeList.remove(nExistingCodes - 1);
		return 1;
	    }

	    /** ONE-ONE-TWO and TWO-ONE-ONE -> ONE-ONE. */
	    if (twoBack == Code.ONE && oneBack == Code.ONE && code == Code.TWO) {
		return 0;
	    }
	    if (twoBack == Code.TWO && oneBack == Code.ONE && code == Code.ONE) {
		_reducedCodeList.remove(nExistingCodes - 1);
		_reducedCodeList.remove(nExistingCodes - 2);
		int nTrailsLost = 0;
		nTrailsLost += addToReducedCodeListAndCountTrails(Code.ONE);
		nTrailsLost += addToReducedCodeListAndCountTrails(Code.ONE);
		return nTrailsLost;
	    }

	    /** Get ready for all of the replacements by ONE-ONE-TWO or TWO-ONE-ONE. */
	    final Code threeBack = nExistingCodes >= 3 ? _reducedCodeList.get(nExistingCodes - 3) : null;
	    Code predecessor = null;
	    boolean replaceWithMagicTriple = false;

	    /** TWO-ONE-TWO -> ONE-ONE-TWO or TWO-ONE-ONE */
	    if (twoBack == Code.TWO && oneBack == Code.ONE && code == Code.TWO) {
		_reducedCodeList.remove(nExistingCodes - 1);
		_reducedCodeList.remove(nExistingCodes - 2);
		predecessor = threeBack;
		replaceWithMagicTriple = true;
	    } else {
		/**
		 * TWO-ONE-ONE-TWO, TWO-ONE-ONE-EM, and EM-ONE-ONE-TWO -> ONE-ONE-TWO or
		 * TWO-ONE-ONE.
		 */
		if (threeBack == Code.TWO && twoBack == Code.ONE && oneBack == Code.ONE && code == Code.TWO) {
		    replaceWithMagicTriple = true;
		} else if (threeBack == Code.TWO && twoBack == Code.ONE && oneBack == Code.ONE && code == Code.EM) {
		    replaceWithMagicTriple = true;
		} else if (threeBack == Code.EM && twoBack == Code.ONE && oneBack == Code.ONE && code == Code.TWO) {
		    replaceWithMagicTriple = true;
		}
		if (replaceWithMagicTriple) {
		    _reducedCodeList.remove(nExistingCodes - 1);
		    _reducedCodeList.remove(nExistingCodes - 2);
		    _reducedCodeList.remove(nExistingCodes - 3);
		    predecessor = nExistingCodes >= 4 ? _reducedCodeList.get(nExistingCodes - 4) : null;
		}
	    }

	    if (replaceWithMagicTriple) {
		int nTrailsLost = 0;
		if (predecessor == Code.ONE) {
		    nTrailsLost += addToReducedCodeListAndCountTrails(Code.ONE);
		    nTrailsLost += addToReducedCodeListAndCountTrails(Code.ONE);
		    nTrailsLost += addToReducedCodeListAndCountTrails(Code.TWO);
		} else {
		    nTrailsLost += addToReducedCodeListAndCountTrails(Code.TWO);
		    nTrailsLost += addToReducedCodeListAndCountTrails(Code.ONE);
		    nTrailsLost += addToReducedCodeListAndCountTrails(Code.ONE);
		}
		return nTrailsLost;
	    }
	    /** No reducing. Simply add to the list. */
	    _reducedCodeList.add(code);
	    return 0;
	}

	public boolean isIrreducible() {
	    return _irreducible;
	}
    }

    /** The following lines are the outputs of the ctor. */
    private final FullReduction _fullReduction;
    final private NTrailsAndCodeW _nTrailsAndCodeW;

    /** The ctor. */
    private CodeComputer(final ArrayList<Code> codeList, final BuildUseIgnore buildUseIgnore) {
	final boolean buildOrUse = buildUseIgnore == BuildUseIgnore.BUILD || buildUseIgnore == BuildUseIgnore.USE;
	_fullReduction = new FullReduction(codeList, buildOrUse);
	final ArrayList<Code> reducedCodeList = _fullReduction._reducedCodeList;
	final int nTrailsLost = _fullReduction._nTrailsLost;
	if (buildOrUse) {
	    final NTrailsAndCodeW lookedUp = getFromIrreducibleCodeLists(reducedCodeList);
	    if (lookedUp != null) {
		_nTrailsAndCodeW = new NTrailsAndCodeW(nTrailsLost + lookedUp._nTrails, lookedUp._codeW);
		return;
	    } else if (buildUseIgnore == BuildUseIgnore.USE) {
		/** Since we didn't find something, we have trouble. */
		_nTrailsAndCodeW = new NTrailsAndCodeW(-1, Code.EM);
		return;
	    }
	}
	final NTrailsAndCodeW forReduced = solveForReduced(buildUseIgnore);
	_nTrailsAndCodeW = new NTrailsAndCodeW(nTrailsLost + forReduced._nTrails, forReduced._codeW);
    }

    NTrailsAndCodeW solveForReduced(final BuildUseIgnore buildUseIgnore) {
	final ArrayList<Code> reducedCodeList = _fullReduction._reducedCodeList;
	final boolean build = buildUseIgnore == BuildUseIgnore.BUILD;
	final boolean use = buildUseIgnore == BuildUseIgnore.USE;
	if (build || use) {
	    final NTrailsAndCodeW lookedUp = _IrreducibleCodeLists.get(reducedCodeList);
	    if (lookedUp != null) {
		return lookedUp;
	    } else if (use) {
		return new NTrailsAndCodeW(-99, Code.EM);
	    }
	}
	final int nCodesInReducedList = reducedCodeList.size();
	final int nTrails;
	final Code codeW;
	/** Eliminate small cases. */
	if (nCodesInReducedList == 0) {
	    nTrails = 0;
	    codeW = Code.EM;
	} else if (nCodesInReducedList == 1) {
	    /** This is where Eulerian shows up. */
	    final Code codeZero = reducedCodeList.get(0);
	    nTrails = 1;
	    codeW = codeZero == Code.EM ? Code.TWO : codeZero;
	} else {
	    /** Count the ONEs before the first (and only, if it exists) TWO. */
	    int idxOfTWO = -1;
	    int nONEsBeforeTWO = 0;
	    for (int k = 0; k < nCodesInReducedList; ++k) {
		final Code code = reducedCodeList.get(k);
		if (code == Code.ONE) {
		    ++nONEsBeforeTWO;
		    continue;
		}
		if (code == Code.TWO) {
		    idxOfTWO = k;
		    break;
		}
	    }
	    if (idxOfTWO == -1) {
		/**
		 * Since there is no TWO, there are one or two ONEs. Hence, there is one more
		 * trail, and codeW is ONE if there is one ONE, and ZERO if there are two ONEs.
		 */
		nTrails = 1;
		codeW = nONEsBeforeTWO == 1 ? Code.ONE : Code.ZERO;
	    } else {
		/** Count the ONEs after the first (and only, which indeed exists) TWO. */
		int nONEsAfterTWO = 0;
		for (int k = idxOfTWO + 1; k < nCodesInReducedList; ++k) {
		    final Code code = reducedCodeList.get(k);
		    if (code == Code.ONE) {
			++nONEsAfterTWO;
		    }
		}
		final int nEarlyTrails = 1 + nONEsBeforeTWO / 2;
		final boolean earlyTrailToW = nONEsBeforeTWO % 2 == 0;
		/**
		 * The computation of nLateTrails does not have the "1 +" of the computation of
		 * nEarlyTrails, because we start with the end of the latest of the early
		 * trails.
		 */
		final int nLateTrails = nONEsAfterTWO / 2;
		final boolean lateTrailToW = nONEsAfterTWO % 2 == 0;
		/**
		 * With the booleans earlyTrailToW and lateTrailToW, we can determine what is
		 * necessary to represent the edges incident to w. We start by observing that if
		 * both earlyTrailToW and lateTrailToW are true, we splice the two trails to w
		 * together; note that if they are not the same trail, _codeW = ZERO and we
		 * reduce the number of trails. If they are the same trail, which is detected by
		 * nTrailsTotal = 1, then _codeW = TWO.
		 */
		if (earlyTrailToW && lateTrailToW) {
		    nTrails = nEarlyTrails + nLateTrails - 1;
		    codeW = Code.ZERO;
		} else if (!earlyTrailToW && !lateTrailToW) {
		    /** No trails to w. We'll need a new trail if w is bordered by a EM. */
		    final boolean firstIsEM = reducedCodeList.get(0) == Code.EM;
		    final boolean lastIsEM = reducedCodeList.get(nCodesInReducedList - 1) == Code.EM;
		    if (firstIsEM || lastIsEM) {
			nTrails = nEarlyTrails + nLateTrails + 1;
			codeW = Code.TWO;
		    } else {
			nTrails = nEarlyTrails + nLateTrails;
			codeW = Code.EM;
		    }
		} else {
		    /** We have exactly one trail to w. This yields _codeW = ONE. */
		    nTrails = nEarlyTrails + nLateTrails;
		    codeW = Code.ONE;
		}
	    }
	}
	final NTrailsAndCodeW nTrailsAndCodeW = new NTrailsAndCodeW(nTrails, codeW);
	if (build) {
	    putToIrreducibleCodeLists(reducedCodeList, nTrailsAndCodeW);
	}
	return nTrailsAndCodeW;
    }

    private static Comparator<ArrayList<Code>> _CodeListComparator = new Comparator<>() {

	@Override
	public int compare(final ArrayList<Code> codeList0, final ArrayList<Code> codeList1) {
	    final int len0 = codeList0.size();
	    final int len1 = codeList1.size();
	    if (len0 != len1) {
		return len0 < len1 ? -1 : 1;
	    }
	    final ArrayList<Code> codeList0R = new ArrayList<>(codeList0);
	    Collections.reverse(codeList0R);
	    final ArrayList<Code> lesser0 = _Lexicographic(codeList0, codeList0R) <= 0 ? codeList0 : codeList0R;
	    final ArrayList<Code> codeList1R = new ArrayList<>(codeList1);
	    Collections.reverse(codeList1R);
	    final ArrayList<Code> lesser1 = _Lexicographic(codeList1, codeList1R) <= 0 ? codeList1 : codeList1R;
	    return _Lexicographic(lesser0, lesser1);
	}
    };

    private static int _Lexicographic(final ArrayList<Code> codeList0, final ArrayList<Code> codeList1) {
	final int len0 = codeList0.size();
	for (int k = 0; k < len0; ++k) {
	    final Code code0 = codeList0.get(k);
	    final Code code1 = codeList1.get(k);
	    final int compareValue = code0.compareTo(code1);
	    if (compareValue != 0) {
		return compareValue;
	    }
	}
	return 0;
    }

    private static void putToIrreducibleCodeLists(final ArrayList<Code> codeList,
	    final NTrailsAndCodeW nTrailsAndCodeW) {
	_IrreducibleCodeLists.put(codeList, nTrailsAndCodeW);
    }

    private static NTrailsAndCodeW getFromIrreducibleCodeLists(final ArrayList<Code> codeList) {
	return _IrreducibleCodeLists.get(codeList);
    }

    private static ArrayList<ArrayList<Code>> getAllIrreducibleCodeLists() {
	return new ArrayList<>(_IrreducibleCodeLists.keySet());
    }

    /** Compute _IrreducibleCodeLists. */
    final static TreeMap<ArrayList<Code>, NTrailsAndCodeW> _IrreducibleCodeLists = new TreeMap<>(_CodeListComparator);
    static {
	ArrayList<ArrayList<Code>> oldCodeLists = new ArrayList<>();
	final ArrayList<Code> emptyCodeList = new ArrayList<>();
	final CodeComputer codeComputer0 = new CodeComputer(emptyCodeList, BuildUseIgnore.IGNORE);
	oldCodeLists.add(codeComputer0._fullReduction._reducedCodeList);

	for (;;) {
	    final int nOldCodeLists = oldCodeLists == null ? 0 : oldCodeLists.size();
	    if (nOldCodeLists == 0) {
		break;
	    }

	    final ArrayList<ArrayList<Code>> newCodeLists = new ArrayList<>();
	    for (int k = 0; k < nOldCodeLists; ++k) {
		final ArrayList<Code> oldCodeListClone = new ArrayList<>(oldCodeLists.get(k));
		for (final Code newCode : EnumSet.allOf(Code.class)) {
		    oldCodeListClone.add(newCode);
		    final FullReduction fullReduction = new FullReduction(oldCodeListClone, /* buildOrUse= */true);
		    if (fullReduction.isIrreducible()) {
			final ArrayList<Code> reducedCodeList = fullReduction._reducedCodeList;
			newCodeLists.add(reducedCodeList);
			/** The following "puts to" _IrreducibleCodeLists. */
			new CodeComputer(reducedCodeList, BuildUseIgnore.BUILD);
		    }
		    oldCodeListClone.remove(oldCodeListClone.size() - 1);
		}
	    }
	    final int nNewCodeLists = newCodeLists.size();
	    if (nNewCodeLists == 0) {
		break;
	    }
	    oldCodeLists = newCodeLists;
	}
    }

    /** Uninteresting utility functions. */
    static char codeToChar(final Code code) {
	if (code == null) {
	    return '*';
	}
	switch (code) {
	case EM:
	    return '.';
	case ZERO:
	    return '0';
	case ONE:
	    return '1';
	case TWO:
	    return '2';
	default:
	    return '*';
	}
    }

    static Code charToCode(final char c) {
	switch (c) {
	case '.':
	    return Code.EM;
	case '0':
	    return Code.ZERO;
	case '1':
	    return Code.ONE;
	case '2':
	    return Code.TWO;
	default:
	    return null;
	}
    }

    static ArrayList<Code> stringToCodeList(final String s) {
	final ArrayList<Code> codeList = new ArrayList<>();
	final int strLen = s == null ? 0 : s.length();
	for (int k = 0; k < strLen; ++k) {
	    final Code code = charToCode(s.charAt(k));
	    if (code != null) {
		codeList.add(code);
	    }
	}
	return codeList;
    }

    static String codeListToString(final ArrayList<Code> codeList, final boolean reverse) {
	final ArrayList<Code> codeListToPrint;
	if (reverse) {
	    final ArrayList<Code> reversed = new ArrayList<>(codeList);
	    Collections.reverse(reversed);
	    codeListToPrint = _Lexicographic(codeList, reversed) > 0 ? reversed : codeList;
	} else {
	    codeListToPrint = codeList;
	}
	final int nCodes = codeListToPrint.size();
	String s = "";
	for (int k = 0; k < nCodes; ++k) {
	    s += codeToChar(codeListToPrint.get(k));
	}
	return s;
    }

    private static String iCaseToString(final int[] lengths, final Code[] componentCodes, final int iCase) {
	final int nComponents = componentCodes.length;
	final int[] components = getComponents(lengths, iCase);
	String s = "";
	for (int k = 0; k < nComponents; ++k) {
	    s += String.format("%d", components[k]);
	}
	return s;
    }

    private String getString0() {
	final ArrayList<Code> codeList = _fullReduction._codeList;
	final ArrayList<Code> reducedCodeList = _fullReduction._reducedCodeList;
	if (_CodeListComparator.compare(codeList, reducedCodeList) != 0) {
	    return String.format("[%s]", codeListToString(reducedCodeList, /* reverse= */true));
	}
	return "";
    }

    private String getString1() {
	return String.format("nTrails=%d", _nTrailsAndCodeW._nTrails);
    }

    private String getString2() {
	return String.format("CodeW=%c", codeToChar(_nTrailsAndCodeW._codeW));
    }

    @Override
    public String toString() {
	return String.format("%s %s %s", getString0(), getString1(), getString2());
    }

    private static int[] getComponents(final int[] lengths, int iCase) {
	final int nLengths = lengths.length;
	final int[] components = new int[nLengths];
	for (int k = nLengths - 1; k >= 0; --k) {
	    final int length = lengths[k];
	    components[k] = iCase % length;
	    iCase /= length;
	}
	return components;
    }

    public static void main(final String[] args) {
	/** Compute miscCodeLists from miscCaseStrings. */
	final String[] miscTestStrings = { //
		".11211.", //
		".112", //
		"1212", //
		"222", //
		"1..121121..", //
		"121211212", //
		".121121..", //
		"111", //
		".121.", //
		"21211", //
		"21121.", //
		"211212", //
		"212" //
	};
	final ArrayList<ArrayList<Code>> miscTestCodeLists;
	final int nMiscTestCodeLists = miscTestStrings.length;
	miscTestCodeLists = new ArrayList<>();
	for (int k = 0; k < nMiscTestCodeLists; ++k) {
	    miscTestCodeLists.add(stringToCodeList(miscTestStrings[k]));
	}

	/** Compute and print out atMostOneTWO in a manual way. */
	int nIrreducibles = 0;
	final TreeMap<ArrayList<Code>, NTrailsAndCodeW> atMostOneTWO = new TreeMap<>(_CodeListComparator);
	final int[] lengths = { 2, 3, 2, 3, 2 };
	final Code[] componentCodes = { Code.EM, Code.ONE, Code.TWO, Code.ONE, Code.EM };
	final int nCases;
	{
	    int nCasesX = 1;
	    for (final int len : lengths) {
		nCasesX *= len;
	    }
	    nCases = nCasesX;
	}
	int maxStrLen1 = 0;
	int maxStrLen2 = 0;
	final int nDigitsA = (int) (Math.log10(nCases) + 1d);
	int nDigitsB = Integer.MIN_VALUE;
	for (int iPass = 0; iPass < 2; ++iPass) {
	    int kIrreducibles = 0;
	    atMostOneTWO.clear();
	    if (nIrreducibles > 0) {
		nDigitsB = (int) (Math.log10(nIrreducibles) + 1d);
	    }
	    for (int iCase = 1; iCase < nCases; ++iCase) {
		final int[] components = getComponents(lengths, iCase);
		final ArrayList<Code> codeList = new ArrayList<>();
		if (components[0] == 1) {
		    codeList.add(Code.EM);
		}
		if (components[1] > 0) {
		    codeList.add(Code.ONE);
		    if (components[1] == 2) {
			codeList.add(Code.ONE);
		    }
		}
		if (components[2] == 1) {
		    codeList.add(Code.TWO);
		}
		if (components[3] > 0) {
		    codeList.add(Code.ONE);
		    if (components[3] == 2) {
			codeList.add(Code.ONE);
		    }
		}
		if (components[4] == 1) {
		    codeList.add(Code.EM);
		}
		final FullReduction fullReduction = new FullReduction(codeList, /* buildOrUse= */false);
		final ArrayList<Code> reducedCodeList = fullReduction._reducedCodeList;
		final boolean irreducible = fullReduction.isIrreducible();
		;
		if (iPass == 0 && irreducible) {
		    ++nIrreducibles;
		}
		final CodeComputer codeComputer = new CodeComputer(codeList, BuildUseIgnore.IGNORE);
		final boolean isRegistered = atMostOneTWO.get(codeList) != null;
		if (irreducible && !isRegistered) {
		    final ArrayList<Code> reversed = new ArrayList<>(codeList);
		    Collections.reverse(reversed);
		    final ArrayList<Code> codeListToUse = _Lexicographic(codeList, reversed) <= 0 ? codeList : reversed;
		    atMostOneTWO.put(codeListToUse, codeComputer._nTrailsAndCodeW);
		}

		final String codeListString = codeListToString(codeList, /* reverse= */false);
		final String passNumbersString = iCaseToString(lengths, componentCodes, iCase);
		final String str1 = String.format("[%s](%s)%s", codeListString, passNumbersString,
			irreducible ? "*" : _RightArrow);
		final String str2;
		if (!irreducible) {
		    str2 = String.format("[%s]", codeListToString(reducedCodeList, /* reverse= */true));
		} else {
		    str2 = "";
		}
		if (iPass == 0) {
		    maxStrLen1 = Math.max(maxStrLen1, str1.length());
		    maxStrLen2 = Math.max(maxStrLen2, str2.length());
		    if (irreducible) {
			++nIrreducibles;
		    }
		} else {
		    if (iCase > 1) {
			System.out.println();
		    }
		    if (irreducible && !isRegistered) {
			System.out.printf("%" + nDigitsA + "d. %" + nDigitsB + "d. %s", iCase, ++kIrreducibles, str1);
		    } else {
			System.out.printf("%" + nDigitsA + "d. %" + nDigitsB + "s  %s", iCase, "", str1);
		    }
		    final int pad1 = maxStrLen1 - str1.length();
		    if (pad1 > 0) {
			System.out.printf("%" + pad1 + "s", "");
		    }
		    System.out.printf(" %s", str2);
		    final int pad2 = maxStrLen2 - str2.length();
		    if (pad2 > 0) {
			System.out.printf("%" + pad2 + "s", "");
		    }
		    System.out.printf(" %s %s", codeComputer.getString1(), codeComputer.getString2());
		}
	    }
	}

	/**
	 * _IrreducibleCodeLists, miscTestCodeLists, and atMostOneTWO are computed.
	 * Print out the results of computing CodeComputers for them.
	 */
	for (int iPass0 = 0; iPass0 < 3; ++iPass0) {
	    System.out.printf("\n\n");
	    if (iPass0 == 0) {
		System.out.printf("\n");
	    }
	    final boolean forIrreducibles = iPass0 == 0;
	    final boolean forAtMostOneTWO = iPass0 == 1;
	    final boolean forTestCases = iPass0 == 2;
	    int maxIntroStringLen = 0;
	    final ArrayList<ArrayList<Code>> codeLists;
	    final String caption;
	    if (forIrreducibles) {
		codeLists = getAllIrreducibleCodeLists();
		caption = "Irreducible CodeLists:";
	    } else if (forAtMostOneTWO) {
		codeLists = new ArrayList<>(atMostOneTWO.keySet());
		caption = "At Most One TWO:";
	    } else if (forTestCases) {
		codeLists = miscTestCodeLists;
		caption = "Miscellaneous Test CodeLists:";
	    } else {
		codeLists = null;
		caption = null;
	    }
	    final int nTestCodeLists = codeLists.size();
	    final String[] introStrings = new String[nTestCodeLists];
	    final int nDigits1 = (int) (Math.log10(nTestCodeLists) + 1d);
	    for (int k = 0; k < nTestCodeLists; ++k) {
		final ArrayList<Code> codeList = codeLists.get(k);
		introStrings[k] = String.format("%" + nDigits1 + "d. [%s]", //
			k + 1, codeListToString(codeList, /* reverse= */false));
		maxIntroStringLen = Math.max(maxIntroStringLen, introStrings[k].length());
	    }

	    int maxStringLen0 = 0, maxStringLen1 = 0, maxStringLen2 = 0;
	    for (int iPass1 = 0; iPass1 < 2; ++iPass1) {
		String coreFormat = null;
		if (iPass1 == 1) {
		    final String fIntro;
		    if (maxIntroStringLen > 0) {
			fIntro = "%-" + maxIntroStringLen + "s ";
		    } else {
			fIntro = "%s";
		    }
		    final String f0;
		    if (maxStringLen0 > 0) {
			f0 = "%-" + maxStringLen0 + "s ";
		    } else {
			f0 = "%s";
		    }
		    final String f1;
		    if (maxStringLen1 > 0) {
			f1 = "%-" + maxStringLen1 + "s ";
		    } else {
			f1 = "%s";
		    }
		    final String f2;
		    if (maxStringLen2 > 0) {
			f2 = "%-" + maxStringLen2 + "s";
		    } else {
			f2 = "%s";
		    }
		    coreFormat = fIntro + f0 + f1 + f2;
		}
		for (int nForThisLen = 0, k = 0; k < nTestCodeLists; ++k) {
		    final ArrayList<Code> codeList = codeLists.get(k);
		    final CodeComputer codeComputer = new CodeComputer(codeList, BuildUseIgnore.USE);
		    final String s0 = codeComputer.getString0();
		    final String s1 = codeComputer.getString1();
		    final String s2 = codeComputer.getString2();
		    if (iPass1 == 0) {
			maxStringLen0 = Math.max(maxStringLen0, s0.length());
			maxStringLen1 = Math.max(maxStringLen1, s1.length());
			maxStringLen2 = Math.max(maxStringLen2, s2.length());
		    } else {
			final String intro = introStrings[k];
			if (k == 0) {
			    System.out.printf((iPass0 == 0 ? "%s\n" : "\n%s\n") + coreFormat, caption, intro, s0, s1,
				    s2);
			} else {
			    System.out.printf("\n" + coreFormat, intro, s0, s1, s2);
			}
			++nForThisLen;
			if (forIrreducibles || forAtMostOneTWO) {
			    if (k == nTestCodeLists - 1 || codeList.size() != codeLists.get(k + 1).size()) {
				System.out.printf(" (%d)", nForThisLen);
				if (k < nTestCodeLists - 1) {
				    System.out.println();
				}
				nForThisLen = 0;
			    }
			} else if (k % 5 == 4 && k < nTestCodeLists - 1) {
			    System.out.println();
			}
		    }
		}
	    }
	}
    }
}
