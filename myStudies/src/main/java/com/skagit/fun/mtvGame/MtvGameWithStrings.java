package com.skagit.fun.mtvGame;

import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

import com.skagit.util.CombinatoricTools;
import com.skagit.util.PermutationTools;

public class MtvGameWithStrings extends MtvGame {

    private final TreeMap<String, Integer> _stringToInt = new TreeMap<>();
    private final String[] _aStringAbbrevs, _bStringAbbrevs;
    private int _oldNPerms = -1;

    public MtvGameWithStrings(final String[][] pairs, final int kPerm0, final Random r) {
	super(pairs.length, kPerm0, r);
	final int nPairs = pairs.length;
	for (int k = 0; k < nPairs; ++k) {
	    final String[] pair = pairs[k];
	    _stringToInt.put(pair[0], k);
	    _stringToInt.put(pair[1], k);
	}
	_aStringAbbrevs = new String[nPairs];
	_bStringAbbrevs = new String[nPairs];
	final TreeSet<String> distinctStrings = new TreeSet<>();
	NextNToUse: for (int nToUse = 1;; ++nToUse) {
	    distinctStrings.clear();
	    for (int k = 0; k < nPairs; ++k) {
		final String[] pair = pairs[k];
		final String s0 = pair[0].substring(0, Math.min(nToUse, pair[0].length()));
		final String s1 = pair[1].substring(0, Math.min(nToUse, pair[1].length()));
		if (!distinctStrings.add(s0) || !distinctStrings.add(s1)) {
		    continue NextNToUse;
		}
		_aStringAbbrevs[k] = s0;
		_bStringAbbrevs[k] = s1;
	    }
	    break;
	}
	_oldNPerms = _possiblePerms.cardinality();
    }

    private LittleClue createLittleClue(final String[] pair, final boolean right) {
	final int nPairs = getNPairs();
	if (pair == null || pair.length != 2 || pair[0] == null || pair[1] == null) {
	    return null;
	}
	final Integer int0 = _stringToInt.get(pair[0]);
	final Integer int1 = _stringToInt.get(pair[1]);
	if (int0 == null || int0 < 0 || int0 >= nPairs || int1 == null || int1 < 0 || int1 >= nPairs) {
	    return null;
	}
	return new LittleClue(int0, int1, right);
    }

    private BigClue createBigClue(final String[][] pairs, final int nRight) {
	final int nPairs = getNPairs();
	if (pairs == null || pairs.length != nPairs || nRight < 0 || nRight > nPairs) {
	    return null;
	}
	for (int k = 0; k < nPairs; ++k) {
	    final String[] thisPair = pairs[k];
	    if (thisPair.length < 2 || thisPair[0] == null || thisPair[1] == null) {
		return null;
	    }
	    final Integer int0 = _stringToInt.get(thisPair[0]);
	    final Integer int1 = _stringToInt.get(thisPair[1]);
	    if (int0 == null || int1 == null) {
		return null;
	    }
	}
	final int[] perm = new int[nPairs];
	for (int k = 0; k < nPairs; ++k) {
	    final String[] thisPair = pairs[k];
	    final Integer int0 = _stringToInt.get(thisPair[0]);
	    final Integer int1 = _stringToInt.get(thisPair[1]);
	    perm[int0] = int1;
	}
	final int kPerm = PermutationTools.permToKPerm(perm);
	return new BigClue(kPerm, nRight);
    }

    @Override
    public String getString() {
	final int nPairs = getNPairs();
	String s = "";
	for (int k = 0; k < nPairs; ++k) {
	    if (k > 0) {
		s += "\n";
	    }
	    final String aString = _aStringAbbrevs[k];
	    final int nNbrs = _aSide[k].cardinality();
	    final String[] bStrings = new String[nNbrs];
	    for (int k1 = _aSide[k].nextSetBit(0), k2 = 0; k1 >= 0; k1 = _aSide[k].nextSetBit(k1 + 1), ++k2) {
		bStrings[k2] = _bStringAbbrevs[k1];
	    }
	    final String bString = CombinatoricTools.getString(bStrings);
	    s += String.format("%d. %s. %s", k, aString, bString);
	}
	final int[] counts = getCounts();
	s += String.format("\nCounts:\t[GRY]=[%d,%d,%d] nPermsLeft[%d]", counts[0], counts[1], counts[2], counts[3]);
	if (_oldNPerms > 0) {
	    final double proportion = counts[3] / (double) _oldNPerms;
	    s += String.format(" (percentage of old[%.3f])", 100d * proportion);
	}
	_oldNPerms = counts[3];
	if (_oldNPerms == 1) {
	    final int kPerm = _possiblePerms.nextSetBit(0);
	    final int[] perm = PermutationTools.kPermToPerm(nPairs, kPerm);
	    String ss = "{";
	    for (int k = 0; k < nPairs; ++k) {
		ss += k == 0 ? "" : " ";
		ss += _aStringAbbrevs[k] + ":" + _bStringAbbrevs[perm[k]];
	    }
	    ss += "}";
	    s += String.format("(%s)", ss);
	}
	s += "\n";
	return s;
    }

    private String getLittleClueString(final LittleClue littleClue) {
	final String aString = _aStringAbbrevs[littleClue._a];
	final String bString = _bStringAbbrevs[littleClue._b];
	return String.format("[%s,%s:%c]", aString, bString, littleClue._right ? 'Y' : 'N');
    }

    private String getBigClueString(final BigClue bigClue) {
	final int nPairs = getNPairs();
	final int[] perm = PermutationTools.kPermToPerm(nPairs, bigClue._kPerm);
	String ss = "{";
	for (int k = 0; k < nPairs; ++k) {
	    ss += k == 0 ? "" : " ";
	    ss += _aStringAbbrevs[k] + ":" + _bStringAbbrevs[perm[k]];
	}
	ss += "}";
	return String.format("%s: nRight[%d]", ss, bigClue._nRight);
    }

    public static void main(final String[] args) {
	final boolean fixAnswerAheadOfTime = true;
	final String[][] stringPairs = new String[][] { //
		{ "John", "Jacy" }, //
		{ "Dillon", "Jess" }, //
		{ "Chris", "Paige" }, //
		{ "Scali", "Simone" }, //
		{ "Adam", "Shanley" }, //
		{ "Wes", "Coleysia" }, //
		{ "Ethan", "Amber" }, //
		{ "Joey", "Brittany" }, //
		{ "Ryan", "Kayla" }, //
		{ "Dre", "Ashleigh" } //
	};
	final Random r = new Random(19820813);
	final int kPerm0;
	if (fixAnswerAheadOfTime) {
	    final int nPairs = stringPairs.length;
	    final int fact = PermutationTools.factorial(nPairs);
	    kPerm0 = r.nextInt(fact);
	} else {
	    kPerm0 = -1;
	}
	final MtvGameWithStrings mtvGameWithStrings = new MtvGameWithStrings(stringPairs, kPerm0, r);

	final Clue[][] rounds = new Clue[][] { //
		/* ROUND ONE */
		new Clue[] { //
			mtvGameWithStrings.createLittleClue(new String[] { "Chris", "Shanley" }, false), //
			mtvGameWithStrings.createBigClue(new String[][] { //
				{ "Wes", "Kayla" }, //
				{ "Ethan", "Shanley" }, //
				{ "Adam", "Brittany" }, //
				{ "Dre", "Jacy" }, //
				{ "John", "Simone" }, //
				{ "Chris", "Jess" }, //
				{ "Joey", "Paige" }, //
				{ "Scali", "Ashleigh" }, //
				{ "Ryan", "Amber" }, //
				{ "Dillon", "Coleysia" }, //
			}, 2) //
		}, //
		/* ROUND TWO */
		new Clue[] { mtvGameWithStrings.createLittleClue(new String[] { "Ethan", "Jess" }, false), //
			mtvGameWithStrings.createBigClue(new String[][] { //
				{ "John", "Jacy" }, //
				{ "Dillon", "Jess" }, //
				{ "Chris", "Paige" }, //
				{ "Scali", "Simone" }, //
				{ "Adam", "Shanley" }, //
				{ "Wes", "Coleysia" }, //
				{ "Ethan", "Amber" }, //
				{ "Joey", "Brittany" }, //
				{ "Ryan", "Kayla" }, //
				{ "Dre", "Ashleigh" }, //
			}, 4) //
		}, //
		/* ROUND THREE */
		new Clue[] { mtvGameWithStrings.createLittleClue(new String[] { "John", "Simone" }, false), //
			mtvGameWithStrings.createBigClue(new String[][] { //
				{ "Dillon", "Coleysia" }, //
				{ "John", "Jess" }, //
				{ "Ryan", "Kayla" }, //
				{ "Ethan", "Amber" }, //
				{ "Dre", "Ashleigh" }, //
				{ "Chris", "Simone" }, //
				{ "Adam", "Brittany" }, //
				{ "Scali", "Paige" }, //
				{ "Joey", "Shanley" }, //
				{ "Wes", "Jacy" }, //
			}, 2) //
		}, //
		/* ROUND FOUR */
		new Clue[] { mtvGameWithStrings.createLittleClue(new String[] { "Dillon", "Jess" }, false), //
			mtvGameWithStrings.createBigClue(new String[][] { //
				{ "Dillon", "Coleysia" }, //
				{ "Scali", "Paige" }, //
				{ "Wes", "Jess" }, //
				{ "Dre", "Simone" }, //
				{ "Ryan", "Brittany" }, //
				{ "Ethan", "Kayla" }, //
				{ "Adam", "Amber" }, //
				{ "Chris", "Ashleigh" }, //
				{ "John", "Shanley" }, //
				{ "Joey", "Jacy" }, //
			}, 2) //
		}, //
		/* ROUND FIVE */
		new Clue[] { mtvGameWithStrings.createLittleClue(new String[] { "Dre", "Ashleigh" }, false), //
			mtvGameWithStrings.createLittleClue(new String[] { "Dillon", "Coleysia" }, true), //
			mtvGameWithStrings.createBigClue(new String[][] { //
				{ "Dillon", "Coleysia" }, //
				{ "Ethan", "Amber" }, //
				{ "Joey", "Jess" }, //
				{ "Wes", "Kayla" }, //
				{ "John", "Jacy" }, //
				{ "Scali", "Simone" }, //
				{ "Adam", "Shanley" }, //
				{ "Chris", "Paige" }, //
				{ "Dre", "Brittany" }, //
				{ "Ryan", "Ashleigh" }, //
			}, 5) //
		}, //
		/* ROUND SIX */
		new Clue[] { mtvGameWithStrings.createLittleClue(new String[] { "Chris", "Paige" }, true), //
			mtvGameWithStrings.createBigClue(new String[][] { //
				{ "Dillon", "Coleysia" }, //
				{ "Chris", "Paige" }, //
				{ "John", "Jacy" }, //
				{ "Wes", "Kayla" }, //
				{ "Dre", "Shanley" }, //
				{ "Scali", "Brittany" }, //
				{ "Adam", "Ashleigh" }, //
				{ "Joey", "Simone" }, //
				{ "Ryan", "Jess" }, //
				{ "Ethan", "Amber", }, //
			}, 5), //
		}, //
		/* ROUND SEVEN */
		new Clue[] { mtvGameWithStrings.createLittleClue(new String[] { "Ryan", "Kayla" }, false), //
			mtvGameWithStrings.createBigClue(new String[][] { //
				{ "Dillon", "Coleysia" }, //
				{ "Chris", "Paige" }, //
				{ "Wes", "Kayla" }, //
				{ "Scali", "Jacy" }, //
				{ "Adam", "Shanley" }, //
				{ "Ethan", "Amber" }, //
				{ "Joey", "Jess" }, //
				{ "Ryan", "Ashleigh" }, //
				{ "John", "Brittany" }, //
				{ "Dre", "Simone" }, //
			}, 7) //
		}, //
		/* ROUND EIGHT */
		new Clue[] { mtvGameWithStrings.createLittleClue(new String[] { "Wes", "Kayla" }, true), //
			mtvGameWithStrings.createBigClue(new String[][] { //
				{ "Dillon", "Coleysia" }, //
				{ "Chris", "Paige" }, //
				{ "Wes", "Kayla" }, //
				{ "Ryan", "Jess" }, //
				{ "Dre", "Simone" }, //
				{ "John", "Jacy" }, //
				{ "Adam", "Shanley" }, //
				{ "Ethan", "Amber" }, //
				{ "Joey", "Brittany" }, //
				{ "Scali", "Ashleigh" }, //
			}, 8) //
		}, //
		/* ROUND NINE */
		new Clue[] { mtvGameWithStrings.createLittleClue(new String[] { "John", "Jacy" }, false), //
			mtvGameWithStrings.createBigClue(new String[][] { //
				{ "Dillon", "Coleysia" }, //
				{ "Chris", "Paige" }, //
				{ "Wes", "Kayla" }, //
				{ "Ethan", "Amber" }, //
				{ "Scali", "Jacy" }, //
				{ "Dre", "Simone" }, //
				{ "Ryan", "Jess" }, //
				{ "Adam", "Shanley" }, //
				{ "Joey", "Brittany" }, //
				{ "John", "Ashleigh" }, //
			}, 10) //
		} //
	};

	final int nRounds = rounds.length;
	boolean printedOneA = false;
	ROUND_LOOP: for (int kRound = 0; kRound < nRounds; ++kRound) {
	    final Clue[] cluesInRound = rounds[kRound];
	    final int nCluesInRound = cluesInRound == null ? 0 : cluesInRound.length;
	    for (int kClue = 0; kClue < nCluesInRound; ++kClue) {
		boolean printedOneB = false;
		final Clue clue = cluesInRound[kClue];
		clue.process();
		if (printedOneA) {
		    System.out.printf("\n");
		}
		if (printedOneB) {
		    System.out.printf("\n");
		}
		if (clue instanceof MtvGame.LittleClue) {
		    System.out.printf("Round %d, LittleClue %s yields\n%s", kRound + 1,
			    mtvGameWithStrings.getLittleClueString((LittleClue) clue), mtvGameWithStrings.getString());
		    printedOneA = printedOneB = true;
		} else if (clue instanceof BigClue) {
		    System.out.printf("Round %d, BigClue %s yields\n%s", kRound + 1,
			    mtvGameWithStrings.getBigClueString((BigClue) clue), mtvGameWithStrings.getString());
		    printedOneA = printedOneB = true;
		}
		if (mtvGameWithStrings.isSolved()) {
		    break ROUND_LOOP;
		}
	    }
	}
    }
}
