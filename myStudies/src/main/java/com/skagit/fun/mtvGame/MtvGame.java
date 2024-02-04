package com.skagit.fun.mtvGame;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Random;

import com.skagit.util.CombinatoricTools;
import com.skagit.util.PermutationTools;

public class MtvGame {

	public interface Clue {
		void process();
	}

	public class LittleClue implements Clue {
		public final int _a, _b;
		public final boolean _right;

		public LittleClue(final int a, final int b, final boolean right) {
			_a = a;
			_b = b;
			_right = right;
		}

		public LittleClue(final int a, final int b, final int answer) {
			_a = a;
			_b = b;
			final int nPairs = getNPairs();
			_right = PermutationTools.kPermAndKToEntry(nPairs, answer, a) == _b;
		}

		private LittleClue() {
			final int nPairs = getNPairs();
			int a = -1, b = -1;
			boolean right = false;
			int cum = 0;
			int kChosen = 0;
			for (int iPass = 0; iPass < 2; ++iPass) {
				for (int k0 = 0; k0 < nPairs; ++k0) {
					final BitSet aSide = _aSide[k0];
					final int nNbrs = aSide.cardinality();
					if (nNbrs == 1) {
						continue;
					}
					if (iPass == 0) {
						cum += nNbrs;
					} else {
						if (kChosen >= nNbrs) {
							kChosen -= nNbrs;
						} else {
							final int chosenNbr = nNbrs - kChosen - 1;
							int k1 = aSide.nextSetBit(0);
							for (int k2 = 0; k2 < chosenNbr;
									++k2, k1 = aSide.nextSetBit(k1 + 1)) {
							}
							a = k0;
							b = k1;
							if (_kPermAnswer >= 0) {
								final int aEntry = PermutationTools.kPermAndKToEntry(nPairs,
										_kPermAnswer, a);
								right = aEntry == b;
							} else {
								final int draw = _r.nextInt(nNbrs);
								right = draw == chosenNbr;
							}
							break;
						}
					}
				}
				kChosen = _r.nextInt(cum);
			}
			_a = a;
			_b = b;
			_right = right;
		}

		@Override
		public void process() {
			final int nPairs = getNPairs();
			if (_right) {
				if (!_aSide[_a].get(_b) || !_bSide[_b].get(_a)) {
					assert false : "Tried to assign a match to a Red Pair";
				}
				_aSide[_a].clear();
				_aSide[_a].set(_b);
				_bSide[_b].clear();
				_bSide[_b].set(_a);
				for (int k = 0; k < nPairs; ++k) {
					if (k != _a) {
						_aSide[k].clear(_b);
						_bSide[_b].clear(k);
					}
					if (k != _b) {
						_bSide[k].clear(_a);
						_aSide[_a].clear(k);
					}
				}
			} else {
				if ((_aSide[_a].cardinality() == 1 && _aSide[_a].get(_b)) ||
						(_bSide[_b].cardinality() == 1 && _bSide[_b].get(_a))) {
					assert false : "Tried to clear a Green Pair";
				}
				_aSide[_a].clear(_b);
				_bSide[_b].clear(_a);
			}
			updateBipartiteGraph();
			for (;;) {
				if (!updatePermsFromBipartiteGraph() || !updateBipartiteGraphFromPerms()) {
					return;
				}
			}
		}

		public String getString() {
			return String.format("[%d,%d:%c]", _a, _b, _right ? 'Y' : 'N');
		}

		@Override
		public String toString() {
			return getString();
		}
	}

	public class BigClue implements Clue {
		public final int _kPerm;
		public final int _nRight;

		public BigClue(final int kPerm, final int nRight) {
			_kPerm = kPerm;
			_nRight = nRight;
		}

		public BigClue(final int kPerm, final boolean dummy) {
			_kPerm = kPerm;
			_nRight = getNRight(_kPerm, _kPermAnswer);
		}

		private int getNRight(final int kPerm0, final int kPerm1) {
			final int nPairs = getNPairs();
			final int[] perm0 = PermutationTools.kPermToPerm(nPairs, kPerm0);
			final int[] perm1 = PermutationTools.kPermToPerm(nPairs, kPerm1);
			int nRight = 0;
			for (int k = 0; k < nPairs; ++k) {
				if (perm0[k] == perm1[k]) {
					++nRight;
				}
			}
			return nRight;
		}

		private BigClue() {
			final int nPossiblePerms = _possiblePerms.cardinality();
			final int kPerm0 = _r.nextInt(nPossiblePerms);
			int kPermIt0 = _possiblePerms.nextSetBit(0);
			for (int k = 0; k < kPerm0;
					++k, kPermIt0 = _possiblePerms.nextSetBit(kPermIt0 + 1)) {
			}
			_kPerm = kPermIt0;
			final int kPermAnswer;
			if (_kPermAnswer >= 0) {
				kPermAnswer = _kPermAnswer;
			} else {
				final int kPerm1 = _r.nextInt(nPossiblePerms);
				int kPermIt1 = _possiblePerms.nextSetBit(0);
				for (int k = 0; k < kPerm1;
						++k, kPermIt1 = _possiblePerms.nextSetBit(kPermIt1 + 1)) {
				}
				kPermAnswer = kPermIt1;
			}
			_nRight = getNRight(kPermIt0, kPermAnswer);
		}

		@Override
		public void process() {
			final int nPairs = getNPairs();
			final int nWrong0 = nPairs - _nRight;
			final int[] perm0 = PermutationTools.kPermToPerm(nPairs, _kPerm);
			NextPerm: for (int kPerm = _possiblePerms.nextSetBit(0); kPerm >= 0;
					kPerm = _possiblePerms.nextSetBit(kPerm + 1)) {
				int nRight = 0, nWrong = 0;
				for (int k = 0; k < nPairs; ++k) {
					if (PermutationTools.kPermAndKToEntry(nPairs, kPerm,
							k) == perm0[k]) {
						if (++nRight > _nRight) {
							_possiblePerms.clear(kPerm);
							continue NextPerm;
						}
					} else if (++nWrong > nWrong0) {
						_possiblePerms.clear(kPerm);
						continue NextPerm;
					}
				}
			}
			for (;;) {
				if (!updateBipartiteGraphFromPerms() || !updatePermsFromBipartiteGraph()) {
					return;
				}
			}
		}

		public String getString() {
			final int nPairs = getNPairs();
			final int[] perm = PermutationTools.kPermToPerm(nPairs, _kPerm);
			return String.format("%s: nRight[%d]",
					CombinatoricTools.getString(perm), _nRight);
		}

		@Override
		public String toString() {
			return getString();
		}
	}

	/** Redundantly store both sides of the bipartite graph. */
	private final Random _r;
	private final int _kPermAnswer;
	protected final BitSet[] _aSide;
	protected final BitSet[] _bSide;
	protected final BitSet _possiblePerms;

	public MtvGame(final int nPairs, final int kPermAnswer, final Random r) {
		_r = r;
		_kPermAnswer = kPermAnswer;
		final int nPerms = PermutationTools.factorial(nPairs);
		/** Initialize computations. */
		_aSide = new BitSet[nPairs];
		_bSide = new BitSet[nPairs];
		final BitSet bitSet = new BitSet(nPairs);
		bitSet.set(0, nPairs);
		for (int k = 0; k < nPairs; ++k) {
			_aSide[k] = (BitSet) bitSet.clone();
			_bSide[k] = (BitSet) bitSet.clone();
		}
		_possiblePerms = new BitSet(nPerms);
		_possiblePerms.set(0, nPerms);
	}

	public int getNPairs() {
		return _aSide.length;
	}

	public boolean isSolved() {
		final int nPairs = getNPairs();
		for (int a = 0; a < nPairs; ++a) {
			if (_aSide[a].cardinality() > 1) {
				return false;
			}
		}
		return true;
	}

	private boolean updateBipartiteGraph() {
		final int nPairs = getNPairs();
		final BitSet fixed = new BitSet(nPairs);
		boolean madeChange = false;
		for (boolean didSomething = true; didSomething;) {
			didSomething = false;
			for (int iPass = 0; iPass < 2; ++iPass) {
				final BitSet[] side = iPass == 0 ? _aSide : _bSide;
				for (int k = 0; k < nPairs; ++k) {
					if (side[k].cardinality() == 1) {
						fixed.set(k);
					}
				}
				final BitSet extnsn = (BitSet) fixed.clone();
				while (true) {
					final int nInExtnsn = extnsn.cardinality();
					final BitSet nbrs = new BitSet(nPairs);
					for (int k = extnsn.nextSetBit(0); k >= 0;
							k = extnsn.nextSetBit(k + 1)) {
						nbrs.or(side[k]);
					}
					if (nbrs.cardinality() <= nInExtnsn) {
						/** Outside of extnsn, nobody can be matched to any of nbrs. */
						for (int k = extnsn.nextClearBit(0); k < nPairs;
								k = extnsn.nextClearBit(k + 1)) {
							final int oldCrdnlty = side[k].cardinality();
							side[k].andNot(nbrs);
							if (side[k].cardinality() < oldCrdnlty) {
								madeChange = didSomething = true;
							}
						}
					}
					if (!PermutationTools.nextExtension(fixed, extnsn, nPairs)) {
						break;
					}
				}
			}
		}
		return madeChange;
	}

	private boolean updatePermsFromBipartiteGraph() {
		boolean madeChange = false;
		final int nPairs = getNPairs();
		for (int kPerm = _possiblePerms.nextSetBit(0); kPerm >= 0;
				kPerm = _possiblePerms.nextSetBit(kPerm + 1)) {
			for (int a = 0; a < nPairs; ++a) {
				final int b = PermutationTools.kPermAndKToEntry(nPairs, kPerm, a);
				/** In kPerm, a is matched to b. */
				if (!_aSide[a].get(b)) {
					/**
					 * Since b is not a possible match of a in _aSide, we eliminate
					 * kPerm.
					 */
					_possiblePerms.clear(kPerm);
					madeChange = true;
					break;
				}
			}
		}
		return madeChange;
	}

	private boolean updateBipartiteGraphFromPerms() {
		final int oldNEdges = getNEdgesInBipartiteGraph();
		final int nPairs = getNPairs();
		final BitSet[] aNots = new BitSet[nPairs];
		final BitSet[] bNots = new BitSet[nPairs];
		for (int k = 0; k < nPairs; ++k) {
			aNots[k] = new BitSet(nPairs);
			aNots[k].set(0, nPairs);
			bNots[k] = new BitSet(nPairs);
			bNots[k].set(0, nPairs);
		}
		for (int kPerm = _possiblePerms.nextSetBit(0); kPerm >= 0;
				kPerm = _possiblePerms.nextSetBit(kPerm + 1)) {
			final int[] perm = PermutationTools.kPermToPerm(nPairs, kPerm);
			for (int k = 0; k < nPairs; ++k) {
				aNots[k].clear(perm[k]);
				bNots[perm[k]].clear(k);
			}
		}
		for (int k = 0; k < nPairs; ++k) {
			_aSide[k].andNot(aNots[k]);
			_bSide[k].andNot(bNots[k]);
		}
		final int newNEdges = getNEdgesInBipartiteGraph();
		return newNEdges < oldNEdges;
	}

	protected int getNEdgesInBipartiteGraph() {
		final int nPairs = getNPairs();
		int nEdges = 0;
		for (int k = 0; k < nPairs; ++k) {
			nEdges += _aSide[k].cardinality();
		}
		return nEdges;
	}

	protected int[] getCounts() {
		final int nPairs = getNPairs();
		int nGreens = 0, nReds = 0, nYellows = 0;
		for (int k = 0; k < nPairs; ++k) {
			final int nNbrs = _aSide[k].cardinality();
			if (nNbrs == 1) {
				++nGreens;
			} else {
				nReds += nPairs - nNbrs;
				nYellows += nNbrs;
			}
		}
		final int nPossiblePerms = _possiblePerms.cardinality();
		return new int[] { nGreens, nReds, nYellows, nPossiblePerms };
	}

	public String getString() {
		final int nPairs = getNPairs();
		String s = "";
		for (int k = 0; k < nPairs; ++k) {
			if (k > 0) {
				s += "\n";
			}
			s += String.format("%d. %s", k, _aSide[k]);
		}
		final int[] counts = getCounts();
		final int nFloaters = nPairs - counts[0];
		final int check = (nFloaters * nPairs) - counts[1] - counts[2];
		s += String.format(
				"\nCounts:\t[GRY]=[%d,%d,%d] (Check(%d)), nPermsLeft[%d]",
				counts[0], counts[1], counts[2], check, counts[3]);
		if (counts[3] == 1) {
			final int kPerm = _possiblePerms.nextSetBit(0);
			final int[] perm = PermutationTools.kPermToPerm(nPairs, kPerm);
			s += String.format("(%s)", CombinatoricTools.getString(perm));
		}
		s += "\n";
		return s;
	}

	@Override
	public String toString() {
		return getString();
	}

	public static void main(final String[] args) {
		final Random r = new Random(19820813L);
		final int nPairs = 10;
		final int nTrials = 1000;
		final boolean fixAnswerAheadOfTime = true;
		final boolean printRounds = false;
		final ArrayList<int[]> resultsList = new ArrayList<>();
		for (int kTrial = 0; kTrial < nTrials; ++kTrial) {
			if (printRounds && kTrial > 0) {
				System.out.printf("\n\n");
			}
			final int kPermAnswer;
			if (fixAnswerAheadOfTime) {
				kPermAnswer = r.nextInt(PermutationTools.factorial(nPairs));
			} else {
				kPermAnswer = -1;
			}
			if (!printRounds) {
				System.out.printf("\nProblem # %d.", kTrial);
			}
			final MtvGame mtvGame = new MtvGame(nPairs, kPermAnswer, r);
			for (int kRound = 0;; ++kRound) {
				final int nRounds = kRound + 1;
				final LittleClue littleClue = mtvGame.new LittleClue();
				littleClue.process();
				if (printRounds) {
					if (kRound > 0) {
						System.out.printf("\n");
					}
					System.out.printf("Round # %d.\tLittleClue %s yields\n%s",
							nRounds, littleClue.getString(), mtvGame.getString());
				}
				if (mtvGame.isSolved()) {
					System.out.printf("Solved! %d round%s, after LittleClue", nRounds,
							nRounds == 0 ? "" : "s");
					while (resultsList.size() < nRounds + 1) {
						resultsList.add(new int[] { 0, 0 });
					}
					++resultsList.get(nRounds)[0];
					break;
				}
				final BigClue bigClue = mtvGame.new BigClue();
				bigClue.process();
				if (printRounds) {
					if (kRound > 0) {
						System.out.printf("\n");
					}
					System.out.printf("Round # %d.\tBigClue %s yields\n%s", nRounds,
							bigClue.getString(), mtvGame.getString());
				}
				if (mtvGame.isSolved()) {
					System.out.printf("Solved! %d rounds,   after BigClue.", nRounds);
					while (resultsList.size() < nRounds + 1) {
						resultsList.add(new int[] { 0, 0 });
					}
					++resultsList.get(nRounds)[1];
					break;
				}
			}
		}
		final int nResults = resultsList.size();
		System.out.printf("\n");
		for (int kRound = 0; kRound < nResults; ++kRound) {
			final int[] resultsOfRound = resultsList.get(kRound);
			final int nAfterLittle = resultsOfRound[0];
			final int nAfterBig = resultsOfRound[1];
			if (nAfterLittle == 0 && nAfterBig == 0) {
				continue;
			}
			System.out.printf("\n%d Rounds: nAfterLittle[%d] nAfterBig[%d]",
					kRound, nAfterLittle, nAfterBig);
		}
	}

}