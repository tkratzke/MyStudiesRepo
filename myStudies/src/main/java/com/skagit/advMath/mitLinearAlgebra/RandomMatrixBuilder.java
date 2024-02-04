package com.skagit.advMath.mitLinearAlgebra;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

class RandomMatrixBuilder extends MitLinearAlgebraCore {

	final static int _LowNudge = -3;
	final static int _HighNudge = 3;
	final static int _LowMult = -2;
	final static int _HighMult = 2;

	final private Random _random;
	final private int _lowNudge, _highNudge;
	final private int _lowMult, _highMult;

	RandomMatrixBuilder(final long seed, final int lowNudge,
			final int highNudge, final int lowMult, final int highMult) {
		_random = new Random();
		_random.setSeed(seed);
		_lowNudge = lowNudge;
		_highNudge = highNudge;
		_lowMult = lowMult;
		_highMult = highMult;
	}

	RandomMatrixBuilder(final long seed) {
		this(seed, _LowNudge, _HighNudge, _LowMult, _HighMult);
	}

	RandomMatrixBuilder() {
		this(/* seed= */371L);
	}

	/**
	 * <pre>
	 * Returns a random m x n matrix with "new independent columns"
	 * specified by indClmnsArray.  If upperTriangular is true,
	 * then the returned matrix will satisfy the following:
	 *    1st non-zero in each row will be strictly after the
	 *    1st non-zero in the previous row.
	 * </pre>
	 */
	double[][] buildMatrix(final int mIn, final int nIn,
			final int[] indClmnsArrayIn, final boolean upperTriangular) {
		final int[] indClmnsArray;
		if (indClmnsArrayIn == null) {
			indClmnsArray = getInitial(Math.min(mIn, nIn));
		} else {
			indClmnsArray = indClmnsArrayIn.clone();
			Arrays.sort(indClmnsArray);
		}
		final int rank = indClmnsArray.length;
		final int maxSpec = indClmnsArray[rank - 1];
		final int m = Math.max(mIn, rank);
		final int n = Math.max(nIn, Math.max(rank, maxSpec + 1));

		final double[][] clmns = new double[n][];
		for (int j = 0; j < n; ++j) {
			final double[][] tempClmns = new double[j][];
			System.arraycopy(clmns, 0, tempClmns, 0, j);
			/** Fill in the dependent columns until the next independent one. */
			final int lubIdx = getLubIndex(indClmnsArray, j);
			final int lub =
					lubIdx == indClmnsArray.length ? n : indClmnsArray[lubIdx];
			for (; j < lub; ++j) {
				clmns[j] = getRandomLinearCombination(tempClmns, m);
			}
			if (j < n) {
				if (upperTriangular) {
					clmns[j] = getRandomLinearCombination(tempClmns, m);
					clmns[j][lubIdx] = randomNonZeroInt(_lowNudge, _highNudge);
				} else {
					clmns[j] = getRandomIndependentVector(tempClmns, m);
				}
			}
		}
		return transpose(clmns);
	}

	private double[][] getRandomOgMatrix(final int m) {
		final double[][] mtx0 = buildMatrix(m, m, /* indClmns= */getInitial(m),
				/* upperTriangular= */false);
		final Qr qrU = new Qr(mtx0);
		final double[][] q = qrU.getQ();
		final int[] perm = randomPermutation(m);
		final double[][] mtx = new double[m][];
		for (int k = 0; k < m; ++k) {
			mtx[k] = q[perm[k]];
		}
		return mtx;
	}

	double[][] getRandomMatrixFromSvd(final int m, final int n,
			final double[] sigmas) {
		final double[][] u = getRandomOgMatrix(m);
		final double[][] vT = getRandomOgMatrix(n);
		final int nSigmas = Math.min(m, sigmas.length);
		/** Form u x sigma, an m x n matrix. */
		final double[][] uSigma = new double[m][n];
		for (int j = 0; j < n; ++j) {
			/** Form the jth clmn of u x sigma. */
			final double sigma = j < nSigmas ? sigmas[j] : 0d;
			for (int i = 0; i < m; ++i) {
				uSigma[i][j] = sigma != 0d ? (u[i][j] * sigma) : 0d;
			}
		}
		return multiply(uSigma, vT);
	}

	double[] getRandomLinearCombination(final double[][] vectors,
			final int m) {
		final int n = getNVectors(vectors);
		final double[] vector = new double[m];
		Arrays.fill(vector, 0d);
		for (int j = 0; j < n; ++j) {
			final double coef = randomNonZeroInt(_lowMult, _highMult);
			for (int i = 0; i < m; ++i) {
				vector[i] += coef * vectors[j][i];
			}
		}
		return vector;
	}

	double[] getRandomIndependentVector(final double[][] vectors,
			final int m) {
		final int n = getNVectors(vectors);
		if (n == 0) {
			final double[] vector = new double[m];
			for (int i = 0; i < m; ++i) {
				vector[i] = randomNonZeroInt(_lowNudge, _highNudge);
			}
			return vector;
		}

		/**
		 * We're thinking of vectors as columns. rowReduce to find the pivots.
		 */
		final PaqLu paqLu = new PaqLu(transpose(vectors), PaqLu.Pivoting.Full);
		final int[][] pivotPairs = paqLu.getPivotPairs();

		/**
		 * If we have m pivots, we cannot get an independent column. After that
		 * check, record the pivot rows and build an array of columns to take a
		 * linear combination of.
		 */
		final int rank = pivotPairs.length;
		if (rank == m) {
			return null;
		}
		final BitSet pivotRows = new BitSet(m);
		final double[][] tempClmns = new double[rank][];
		for (int k = 0; k < rank; ++k) {
			pivotRows.set(pivotPairs[k][0]);
			tempClmns[k] = vectors[pivotPairs[k][1]];
		}

		/** Nudge the non-pivot rows. */
		final double[] clmn = getRandomLinearCombination(tempClmns, m);
		for (int i = pivotRows.nextClearBit(0); i < m;
				i = pivotRows.nextClearBit(i + 1)) {
			clmn[i] += randomNonZeroInt(_lowNudge, _highNudge);
		}
		return clmn;
	}

	private int randomNonZeroInt(final int low, final int high) {
		final int range = high - low + 1;
		final boolean forbidOnes = high > 1 || low < -1;
		int randomInt = 0;
		for (int k = 0; k < 10; ++k) {
			randomInt = _random.nextInt(range) + low;
			if (randomInt == 0) {
				continue;
			}
			if (!forbidOnes || randomInt > 1 || randomInt < -1) {
				return randomInt;
			}
		}
		return randomInt;
	}

	static int[] getInitial(final int n) {
		final int[] perm = new int[n];
		for (int i = 0; i < n; ++i) {
			perm[i] = i;
		}
		return perm;
	}

	private int[] randomPermutation(final int n) {
		final int[] perm = getInitial(n);
		for (int i = 0; i < n; ++i) {
			final int j = _random.nextInt(n - i);
			final int letterI = perm[i];
			perm[i] = perm[i + j];
			perm[i + j] = letterI;
		}
		return perm;
	}
}
