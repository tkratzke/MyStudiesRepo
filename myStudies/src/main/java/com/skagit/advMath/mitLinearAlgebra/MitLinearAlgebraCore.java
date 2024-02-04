package com.skagit.advMath.mitLinearAlgebra;

import java.util.Arrays;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

class MitLinearAlgebraCore {

	protected final static double _Threshold = 5.0e-11;

	protected final double _threshold;

	MitLinearAlgebraCore() {
		this(_Threshold);
	}

	MitLinearAlgebraCore(final double threshold) {
		_threshold = threshold > 0d ? threshold : _Threshold;
	}

	double getThreshold() {
		return _threshold;
	}

	/** Static routines for double[][]. */
	static int getNVectors(final double[][] vectors) {
		final int n = vectors == null ? 0 : vectors.length;
		/** The 1st null one indicates how many we really have. */
		for (int j = 0; j < n; ++j) {
			if (vectors[j] == null) {
				return j;
			}
		}
		return n;
	}

	static double[] getClmn(final double[][] a, final int j) {
		final int m = a == null ? 0 : a.length;
		final int n = (m == 0 || a[0] == null) ? 0 : a[0].length;
		if (j < 0 || j >= n) {
			return null;
		}
		final double[] clmn = new double[m];
		for (int i = 0; i < m; ++i) {
			clmn[i] = a[i][j];
		}
		return clmn;
	}

	static double[][] deepClone(final double[][] a) {
		if (a == null) {
			return null;
		}
		final int n = a.length;
		final double[][] clone = new double[n][];
		for (int i = 0; i < n; ++i) {
			clone[i] = a[i].clone();
		}
		return clone;
	}

	static double[][] transpose(final double[][] a) {
		final int m = a.length, n = a[0].length;
		final double[][] aT = new double[n][m];
		for (int i = 0; i < m; ++i) {
			for (int j = 0; j < n; ++j) {
				aT[j][i] = a[i][j];
			}
		}
		return aT;
	}

	/** getStrings. */
	static String getString(final double a, final int width,
			final int nSignificantDigits) {
		final double threshold = isZeroForPrinting(nSignificantDigits);
		return getStringWithThreshold(a, width, nSignificantDigits, threshold,
				/* doNotCheck= */false);
	}

	static String getString(final double[] a, final int width,
			final int nSignificantDigits) {
		final double threshold = isZeroForPrinting(nSignificantDigits);
		return getStringWithThreshold(a, width, nSignificantDigits, threshold,
				/* doNotCheck= */false);
	}

	static String getString(final double[][] a, final int width,
			final int nSignificantDigits) {
		final double threshold = isZeroForPrinting(nSignificantDigits);
		return getStringWithThreshold(a, width, nSignificantDigits, threshold,
				/* doNotCheck= */false);
	}

	static String getString(final double a) {
		return getString(a, 8, 3);
	}

	static String getString(final double[] a) {
		return getString(a, 8, 3);
	}

	static String getString(final double[][] a) {
		return getString(a, 8, 3);
	}

	/** Private, real work versions of getString's. */
	private static String getStringWithThreshold(final double a,
			final int widthIn, final int nSignificantDigitsIn,
			final double threshold, final boolean doNotCheck) {
		final int width, nSignificantDigits;
		if (!Double.isFinite(a)) {
			width = widthIn;
			nSignificantDigits = nSignificantDigitsIn;
		} else if (doNotCheck) {
			width = widthIn;
			nSignificantDigits = nSignificantDigitsIn;
		} else {
			final int maxWidth = getMaxWidthForInt(a, threshold);
			if (maxWidth >= 0) {
				width = maxWidth;
				nSignificantDigits = 0;
			} else {
				width = widthIn;
				nSignificantDigits = nSignificantDigitsIn;
			}
		}
		if (nSignificantDigits > 0) {
			/** Print it out as a double. */
			final String format =
					(width > 0 ? "%" + width + "." : "%.") + nSignificantDigits + "f";
			return String.format(format, a);
		}
		/** Print it out as an integer. */
		final String format = (width > 0 ? "%" + width : "%") + "d";
		return String.format(format, (int) Math.round(a));
	}

	private static String getStringWithThreshold(final double[] a,
			final int widthIn, final int nSignificantDigitsIn,
			final double threshold, final boolean doNotCheck) {
		final int n = a == null ? 0 : a.length;
		if (n == 0) {
			return "[]";
		}
		final int width, nSignificantDigits;
		if (doNotCheck) {
			width = widthIn;
			nSignificantDigits = nSignificantDigitsIn;
		} else {
			final int maxWidth = getMaxWidthForInt(a, threshold);
			if (maxWidth >= 0) {
				width = maxWidth;
				nSignificantDigits = 0;
			} else {
				width = widthIn;
				nSignificantDigits = nSignificantDigitsIn;
			}
		}
		final StringBuffer b = new StringBuffer("[");
		for (int i = 0; i < n; ++i) {
			if (i > 0) {
				if (nSignificantDigits > 0) {
					b.append(", ");
				} else {
					b.append(" ");
				}
			}
			b.append(getStringWithThreshold(a[i], width, nSignificantDigits,
					threshold, /* doNotCheck= */true));
		}
		b.append("]");
		return new String(b);
	}

	private static String getStringWithThreshold(final double[][] a,
			final int widthIn, final int nSignificantDigitsIn,
			final double threshold, final boolean doNotCheck) {
		final int m = a == null ? 0 : a.length;
		if (m == 0) {
			return "[[]]";
		}
		final int width, nSignificantDigits;
		if (doNotCheck) {
			width = widthIn;
			nSignificantDigits = nSignificantDigitsIn;
		} else {
			final int maxWidth = getMaxWidthForInt(a, threshold);
			if (maxWidth >= 0) {
				width = maxWidth;
				nSignificantDigits = 0;
			} else {
				width = widthIn;
				nSignificantDigits = nSignificantDigitsIn;
			}
		}
		final StringBuffer b = new StringBuffer("\n[");
		for (int k = 0; k < m; ++k) {
			b.append("\n " + getStringWithThreshold(a[k], width,
					nSignificantDigits, threshold, /* doNotCheck= */true));
		}
		b.append("\n]");
		return new String(b);
	}

	private static double isZeroForPrinting(final int nSignificantDigits) {
		return 5d * Math.pow(10d, -nSignificantDigits);
	}

	private final static int getMaxWidthForInt(final double a,
			final double threshold) {
		/** If it's not close to an integer, return "not an integer." */
		if (!Double.isFinite(a) || Math.abs(Math.round(a) - a) > threshold) {
			return -1;
		}
		/** It's close to an integer. If it's close to 0, give it width 2. */
		final int core = (int) Math.abs(Math.round(a));
		if (core == 0) {
			return 2;
		}
		/** If it's close to 10, give it 2 spaces plus the padding space. */
		final int unsigned = (int) Math.floor(Math.log10(core));
		return unsigned + (a < 0d ? 2 : 1);
	}

	private static int getMaxWidthForInt(final double[] a,
			final double threshold) {
		final int m = a == null ? 0 : a.length;
		int maxWidth = 0;
		for (int k = 0; k < m; ++k) {
			final double v = a[k];
			final int thisWidth = getMaxWidthForInt(v, threshold);
			if (thisWidth < 0) {
				return -1;
			}
			maxWidth = Math.max(maxWidth, thisWidth);
		}
		return maxWidth;
	}

	private static int getMaxWidthForInt(final double[][] a,
			final double threshold) {
		final int m = a == null ? 0 : a.length;
		int maxWidth = 0;
		for (int k = 0; k < m; ++k) {
			final double[] v = a[k];
			final int thisWidth = getMaxWidthForInt(v, threshold);
			if (thisWidth == -1) {
				return -1;
			}
			maxWidth = Math.max(maxWidth, thisWidth);
		}
		return maxWidth;
	}

	static void pointwiseSubtract(final double[][] mtx1,
			final double[][] mtx2) {
		final int m = Math.min(mtx1.length, mtx2.length);
		final int n = Math.min(mtx1[0].length, mtx2[0].length);
		for (int i = 0; i < m; ++i) {
			for (int j = 0; j < n; ++j) {
				mtx1[i][j] -= mtx2[i][j];
			}
		}
	}

	/** dotProduct and normalize. */
	static double dotProduct(final double[] a, final double[] b) {
		final int n = a.length;
		double dot = 0d;
		for (int i = 0; i < n; ++i) {
			dot += a[i] * b[i];
		}
		return dot;
	}

	/**
	 * Changes the input to something that has unit length and returns the
	 * length of the input.
	 */
	static double convertToUnitLength(final double[] vector) {
		final double magnitude = Math.sqrt(dotProduct(vector, vector));
		if (magnitude > 0d) {
			multiply(1d / magnitude, vector);
			return magnitude;

		}
		return 0d;
	}

	/** Multiply routines. */
	static void multiply(final double scalar, final double[] a) {
		final int n = a == null ? 0 : a.length;
		for (int i = 0; i < n; ++i) {
			a[i] *= scalar;
		}
	}

	static double[][] multiply(final double[][] a, final double[][] b) {
		final int m = a == null ? 0 : a.length;
		if (m == 0) {
			return new double[][] { {} };
		}
		final int n = (b == null || b.length == 0) ? 0 : b[0].length;
		if (n == 0) {
			return new double[][] { {} };
		}
		final int mn = a[0].length;
		if (mn == 0 || mn != b.length) {
			return null;
		}
		final double[][] result = new double[m][];
		for (int i = 0; i < m; ++i) {
			result[i] = new double[n];
			for (int j = 0; j < n; ++j) {
				result[i][j] = 0d;
				for (int k = 0; k < mn; ++k) {
					result[i][j] += a[i][k] * b[k][j];
				}
			}
		}
		return result;
	}

	static double[][] multiply(final double[][] a, final double[][] b,
			final double[][] c) {
		final int m = a == null ? 0 : a.length;
		if (m == 0) {
			return new double[][] { {} };
		}
		final int n = (c == null || c.length == 0) ? 0 : c[0].length;
		if (n == 0) {
			return new double[][] { {} };
		}
		final int middle1 = a[0].length;
		if (middle1 == 0) {
			return new double[][] { {} };
		}
		final int nBRows = b == null ? 0 : b.length;
		if (nBRows != middle1) {
			return null;
		}
		final int middle2 = (b == null || b.length == 0) ? 0 : b[0].length;
		final int nCRows = c == null ? 0 : c.length;
		if (nCRows != middle2) {
			return null;
		}
		final double[][] result = new double[m][n];
		for (int i = 0; i < m; ++i) {
			for (int j = 0; j < n; ++j) {
				result[i][j] = 0;
				for (int k1 = 0; k1 < middle1; ++k1) {
					for (int k2 = 0; k2 < middle2; ++k2) {
						result[i][j] += a[i][k1] * b[k1][k2] * c[k2][j];
					}
				}
			}
		}
		return result;
	}

	/**
	 * Only works for a positive semi-definite matrix. Hence, we call the
	 * incoming argument aTa.
	 */
	final static double[] getRealEigenvalues(final double[][] aTa) {
		final Matrix aTaMatrix = new Matrix(aTa);
		final EigenvalueDecomposition eig = aTaMatrix.eig();
		return eig.getRealEigenvalues();
	}

	/** Lookup and combinatorics. */
	static int getLubIndex(final int[] array, final int key) {
		final int j = Arrays.binarySearch(array, key);
		if (j < 0) {
			/**
			 * j = (-(insertionPoint) - 1). insertionPoint is the index of the
			 * first element greater than key, or n if all elements are less than
			 * key.
			 */
			final int insertionPoint = -(j + 1);
			return insertionPoint;
		}
		/** We have a match. Find the smallest one that is still a match. */
		for (int lubIndex = j - 1; lubIndex >= 0; --lubIndex) {
			if (array[lubIndex] < key) {
				return lubIndex + 1;
			}
		}
		return 0;
	}

	static int getLubIndex(final double[] array, final double key) {
		final int j = Arrays.binarySearch(array, key);
		if (j < 0) {
			/**
			 * j = (-(insertionPoint) - 1). insertionPoint is the index of the
			 * first element greater than key, or n if all elements are less than
			 * key.
			 */
			final int insertionPoint = -(j + 1);
			return insertionPoint;
		}
		/** We have a match. Find the smallest one that is still a match. */
		for (int lubIndex = j - 1; lubIndex >= 0; --lubIndex) {
			if (array[lubIndex] < key) {
				return lubIndex + 1;
			}
		}
		return 0;
	}

}
