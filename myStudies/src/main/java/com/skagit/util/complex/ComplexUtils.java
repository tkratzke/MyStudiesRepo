package com.skagit.util.complex;

import java.util.ArrayList;

import com.skagit.util.NumericalRoutines;

public class ComplexUtils {
	final public static float _ThresholdFl = 1.0e-6f;
	final public static double _ThresholdRe = 1.0e-10;

	/** Using Reflection to find dims of a double array. @@@ */
	public static int[] getDims(final Object array) {
		final ArrayList<Integer> dimList = new ArrayList<>();
		Object[] objects = null;
		try {
			objects = (Object[]) array;
		} catch (final ClassCastException e) {
			try {
				final double[] doubles = (double[]) Class.forName("[D").cast(array);
				return new int[] {
						doubles.length
				};
			} catch (final ClassNotFoundException e1) {
			}
		}
		for (;;) {
			final Object o = objects[0];
			try {
				dimList.add(objects.length);
			} catch (final ClassCastException e1) {
				try {
					final double[] doubles = (double[]) Class.forName("[D").cast(o);
					dimList.add(doubles.length);
					break;
				} catch (final ClassNotFoundException e2) {
					try {
						final int[] ints = (int[]) Class.forName("[I").cast(o);
						dimList.add(ints.length);
						break;
					} catch (final ClassNotFoundException e3) {
					}
				}
			}
		}
		final int nDims = dimList.size();
		final int[] returnValue = new int[nDims];
		for (int k = 0; k < nDims; ++k) {
			returnValue[k] = dimList.get(k);
		}
		return returnValue;
	}

	public static int[] getDimsFl(final Object array) {
		final ArrayList<Integer> dimList = new ArrayList<>();
		Object[] objects = null;
		try {
			objects = (Object[]) array;
		} catch (final ClassCastException e) {
			try {
				final float[] floats = (float[]) Class.forName("[F").cast(array);
				return new int[] {
						floats.length
				};
			} catch (final ClassNotFoundException e1) {
			}
		}
		for (;;) {
			final Object o = objects[0];
			try {
				dimList.add(objects.length);
			} catch (final ClassCastException e1) {
				try {
					final float[] floats = (float[]) Class.forName("[F").cast(o);
					dimList.add(floats.length);
					break;
				} catch (final ClassNotFoundException e2) {
					try {
						final int[] ints = (int[]) Class.forName("[I").cast(o);
						dimList.add(ints.length);
						break;
					} catch (final ClassNotFoundException e3) {
					}
				}
			}
		}
		final int nDims = dimList.size();
		final int[] returnValue = new int[nDims];
		for (int k = 0; k < nDims; ++k) {
			returnValue[k] = dimList.get(k);
		}
		return returnValue;
	}

	/** pointwiseOperate. @@@ */
	public static void pointwiseOperate(final double[][][][][][] a,
			final double[][][][][][] b,
			final NumericalRoutines.Operation operation) {
		generalPointwiseOperateRe(a, b, operation);
	}

	public static void pointwiseOperate(final double[][][][][] a,
			final double[][][][][] b, final NumericalRoutines.Operation operation) {
		generalPointwiseOperateRe(a, b, operation);
	}

	public static void pointwiseOperate(final double[][][][] a,
			final double[][][][] b, final NumericalRoutines.Operation operation) {
		generalPointwiseOperateRe(a, b, operation);
	}

	public static void pointwiseOperate(final double[][][] a,
			final double[][][] b, final NumericalRoutines.Operation operation) {
		generalPointwiseOperateRe(a, b, operation);
	}

	public static void pointwiseOperate(final double[][] a, final double[][] b,
			final NumericalRoutines.Operation operation) {
		generalPointwiseOperateRe(a, b, operation);
	}

	public static void pointwiseOperate(final double[] a, final double[] b,
			final NumericalRoutines.Operation operation) {
		generalPointwiseOperateRe(a, b, operation);
	}

	static void generalPointwiseOperateRe(final Object a, final Object b,
			final NumericalRoutines.Operation operation) {
		final int[] dims = getDims(a);
		final int nDims = dims.length;
		final int dim = dims[0];
		if (nDims == 1) {
			final double[] aArray = (double[]) a;
			final double[] bArray = (double[]) b;
			for (int k = 0; k < dim; ++k) {
				if (operation == NumericalRoutines.Operation.ADD) {
					aArray[k] += bArray[k];
				} else if (operation == NumericalRoutines.Operation.SUBTRACT) {
					aArray[k] -= bArray[k];
				} else if (operation == NumericalRoutines.Operation.MULTIPLY) {
					aArray[k] *= bArray[k];
				} else if (operation == NumericalRoutines.Operation.DIVIDE) {
					if (bArray[k] != 0) {
						aArray[k] /= bArray[k];
					} else if (aArray[k] != 0) {
						aArray[k] = aArray[k] > 0 ? Double.MAX_VALUE : -Double.MAX_VALUE;
					}
				} else if (operation == NumericalRoutines.Operation.POWER) {
					aArray[k] = Math.pow(aArray[k], bArray[k]);
				} else if (operation == NumericalRoutines.Operation.COPY_TO) {
					bArray[k] = aArray[k];
				}
			}
		} else if (nDims == 2) {
			final double[][] aArray = (double[][]) a;
			final double[][] bArray = (double[][]) b;
			for (int k = 0; k < dim; ++k) {
				generalPointwiseOperateRe(aArray[k], bArray[k], operation);
			}
		} else if (nDims == 3) {
			final double[][][] aArray = (double[][][]) a;
			final double[][][] bArray = (double[][][]) b;
			for (int k = 0; k < dim; ++k) {
				generalPointwiseOperateRe(aArray[k], bArray[k], operation);
			}
		} else if (nDims == 4) {
			final double[][][][] aArray = (double[][][][]) a;
			final double[][][][] bArray = (double[][][][]) b;
			for (int k = 0; k < dim; ++k) {
				generalPointwiseOperateRe(aArray[k], bArray[k], operation);
			}
		} else if (nDims == 5) {
			final double[][][][][] aArray = (double[][][][][]) a;
			final double[][][][][] bArray = (double[][][][][]) b;
			for (int k = 0; k < dim; ++k) {
				generalPointwiseOperateRe(aArray[k], bArray[k], operation);
			}
		} else if (nDims == 6) {
			final double[][][][][][] aArray = (double[][][][][][]) a;
			final double[][][][][][] bArray = (double[][][][][][]) b;
			for (int k = 0; k < dim; ++k) {
				generalPointwiseOperateRe(aArray[k], bArray[k], operation);
			}
		}
	}

	public static void pointwiseOperate(final float[][][][][][] a,
			final float[][][][][][] b,
			final NumericalRoutines.Operation operation) {
		generalPointwiseOperateFl(a, b, operation);
	}

	public static void pointwiseOperate(final float[][][][][] a,
			final float[][][][][] b, final NumericalRoutines.Operation operation) {
		generalPointwiseOperateFl(a, b, operation);
	}

	public static void pointwiseOperate(final float[][][][] a,
			final float[][][][] b, final NumericalRoutines.Operation operation) {
		generalPointwiseOperateFl(a, b, operation);
	}

	public static void pointwiseOperate(final float[][][] a,
			final float[][][] b, final NumericalRoutines.Operation operation) {
		generalPointwiseOperateFl(a, b, operation);
	}

	public static void pointwiseOperate(final float[][] a, final float[][] b,
			final NumericalRoutines.Operation operation) {
		generalPointwiseOperateFl(a, b, operation);
	}

	public static void pointwiseOperate(final float[] a, final float[] b,
			final NumericalRoutines.Operation operation) {
		generalPointwiseOperateFl(a, b, operation);
	}

	private static void generalPointwiseOperateFl(final Object a,
			final Object b, final NumericalRoutines.Operation operation) {
		final int[] dims = getDimsFl(a);
		final int nDims = dims.length;
		final int dim = dims[0];
		if (nDims == 1) {
			final float[] aArray = (float[]) a;
			final float[] bArray = (float[]) b;
			for (int k = 0; k < dim; ++k) {
				if (operation == NumericalRoutines.Operation.ADD) {
					aArray[k] += bArray[k];
				} else if (operation == NumericalRoutines.Operation.SUBTRACT) {
					aArray[k] -= bArray[k];
				} else if (operation == NumericalRoutines.Operation.MULTIPLY) {
					aArray[k] *= bArray[k];
				} else if (operation == NumericalRoutines.Operation.DIVIDE) {
					aArray[k] /= bArray[k];
				} else if (operation == NumericalRoutines.Operation.POWER) {
					aArray[k] = (float) Math.pow(aArray[k], bArray[k]);
				} else if (operation == NumericalRoutines.Operation.COPY_TO) {
					bArray[k] = aArray[k];
				}
			}
		} else if (nDims == 2) {
			final float[][] aArray = (float[][]) a;
			final float[][] bArray = (float[][]) b;
			for (int k = 0; k < dim; ++k) {
				generalPointwiseOperateFl(aArray[k], bArray[k], operation);
			}
		} else if (nDims == 3) {
			final float[][][] aArray = (float[][][]) a;
			final float[][][] bArray = (float[][][]) b;
			for (int k = 0; k < dim; ++k) {
				generalPointwiseOperateFl(aArray[k], bArray[k], operation);
			}
		} else if (nDims == 4) {
			final float[][][][] aArray = (float[][][][]) a;
			final float[][][][] bArray = (float[][][][]) b;
			for (int k = 0; k < dim; ++k) {
				generalPointwiseOperateFl(aArray[k], bArray[k], operation);
			}
		} else if (nDims == 5) {
			final float[][][][][] aArray = (float[][][][][]) a;
			final float[][][][][] bArray = (float[][][][][]) b;
			for (int k = 0; k < dim; ++k) {
				generalPointwiseOperateFl(aArray[k], bArray[k], operation);
			}
		} else if (nDims == 6) {
			final float[][][][][][] aArray = (float[][][][][][]) a;
			final float[][][][][][] bArray = (float[][][][][][]) b;
			for (int k = 0; k < dim; ++k) {
				generalPointwiseOperateFl(aArray[k], bArray[k], operation);
			}
		}
	}

	/** ApplyMask. @@@ */
	public static void applyMask(final double[][][] a,
			final double[][][] mask) {
		generalapplyMask(a, mask);
	}

	public static void applyMask(final double[][][][] a,
			final double[][][][] mask) {
		generalapplyMask(a, mask);
	}

	private static void generalapplyMask(final Object a, final Object mask) {
		final int[] dims = getDims(a);
		final int nDims = dims.length;
		final int dim = dims[0];
		if (nDims == 1) {
			final double[] aArray = (double[]) a;
			final double[] maskArray = (double[]) mask;
			for (int k = 0; k < dim; ++k) {
				aArray[k] *= maskArray[k];
			}
		} else if (nDims == 2) {
			final double[][] aArray = (double[][]) a;
			final double[][] maskArray = (double[][]) mask;
			for (int k = 0; k < dim; ++k) {
				generalapplyMask(aArray[k], maskArray[k]);
			}
		} else if (nDims == 3) {
			final double[][][] aArray = (double[][][]) a;
			final double[][][] maskArray = (double[][][]) mask;
			for (int k = 0; k < dim; ++k) {
				generalapplyMask(aArray[k], maskArray[k]);
			}
		} else if (nDims == 4) {
			final double[][][][] aArray = (double[][][][]) a;
			final double[][][][] maskArray = (double[][][][]) mask;
			for (int k = 0; k < dim; ++k) {
				generalapplyMask(aArray[k], maskArray[k]);
			}
		} else if (nDims == 5) {
			final double[][][][][] aArray = (double[][][][][]) a;
			final double[][][][][] maskArray = (double[][][][][]) mask;
			for (int k = 0; k < dim; ++k) {
				generalapplyMask(aArray[k], maskArray[k]);
			}
		} else if (nDims == 6) {
			final double[][][][][][] aArray = (double[][][][][][]) a;
			final double[][][][][][] maskArray = (double[][][][][][]) mask;
			for (int k = 0; k < dim; ++k) {
				generalapplyMask(aArray[k], maskArray[k]);
			}
		}
	}

	/** Comparing two matrices. */
	public static double[] getCompareValuesRe(final double[][][][][][] a,
			final double[][][][][][] b, final double threshold) {
		return generalCompareValuesRe(a, b, threshold, null);
	}

	public static double[] getCompareValuesRe(final double[][][][][] a,
			final double[][][][][] b, final double threshold) {
		return generalCompareValuesRe(a, b, threshold, null);
	}

	public static double[] getCompareValuesRe(final double[][][][] a,
			final double[][][][] b, final double threshold) {
		return generalCompareValuesRe(a, b, threshold, null);
	}

	public static double[] getCompareValuesRe(final double[][][] a,
			final double[][][] b, final double threshold) {
		return generalCompareValuesRe(a, b, threshold, null);
	}

	public static double[] getCompareValuesRe(final double[][] a,
			final double[][] b, final double threshold) {
		return generalCompareValuesRe(a, b, threshold, null);
	}

	public static double[] getCompareValuesRe(final double[] a,
			final double[] b, final double threshold) {
		return generalCompareValuesRe(a, b, threshold, null);
	}

	private static double[] generalCompareValuesRe(final Object a,
			final Object b, final double threshold, double[] returnValues) {
		if (returnValues == null) {
			/** nZeroes, nTotal, minNonZero, max. */
			returnValues = new double[] {
					0, 0, Double.POSITIVE_INFINITY, 0
			};
		}
		final int[] dims = getDims(a);
		final int nDims = dims.length;
		final int dim = dims[0];
		if (nDims == 1) {
			final double[] aArray = (double[]) a;
			final double[] bArray = (double[]) b;
			for (int k = 0; k < dim; ++k) {
				final double abs = Math.abs(aArray[k] - bArray[k]);
				if (abs <= threshold) {
					returnValues[0] += 1.0;
				}
				returnValues[1] += 1.0;
				if (abs > 0.0 && abs < returnValues[2]) {
					returnValues[2] = abs;
				}
				returnValues[3] = Math.max(returnValues[3], abs);
			}
		} else if (nDims == 2) {
			final double[][] aArray = (double[][]) a;
			final double[][] bArray = (double[][]) b;
			for (int k = 0; k < dim; ++k) {
				generalCompareValuesRe(aArray[k], bArray[k], threshold, returnValues);
			}
		} else if (nDims == 3) {
			final double[][][] aArray = (double[][][]) a;
			final double[][][] bArray = (double[][][]) b;
			for (int k = 0; k < dim; ++k) {
				generalCompareValuesRe(aArray[k], bArray[k], threshold, returnValues);
			}
		} else if (nDims == 4) {
			final double[][][][] aArray = (double[][][][]) a;
			final double[][][][] bArray = (double[][][][]) b;
			for (int k = 0; k < dim; ++k) {
				generalCompareValuesRe(aArray[k], bArray[k], threshold, returnValues);
			}
		} else if (nDims == 5) {
			final double[][][][][] aArray = (double[][][][][]) a;
			final double[][][][][] bArray = (double[][][][][]) b;
			for (int k = 0; k < dim; ++k) {
				generalCompareValuesRe(aArray[k], bArray[k], threshold, returnValues);
			}
		} else if (nDims == 6) {
			final double[][][][][][] aArray = (double[][][][][][]) a;
			final double[][][][][][] bArray = (double[][][][][][]) b;
			for (int k = 0; k < dim; ++k) {
				generalCompareValuesRe(aArray[k], bArray[k], threshold, returnValues);
			}
		}
		return returnValues;
	}

	public static float[] getCompareValuesFl(final float[][][][][][] a,
			final float[][][][][][] b, final float threshold) {
		return generalCompareValuesFl(a, b, threshold, null);
	}

	public static float[] getCompareValuesFl(final float[][][][][] a,
			final float[][][][][] b, final float threshold) {
		return generalCompareValuesFl(a, b, threshold, null);
	}

	public static float[] getCompareValuesFl(final float[][][][] a,
			final float[][][][] b, final float threshold) {
		return generalCompareValuesFl(a, b, threshold, null);
	}

	public static float[] getCompareValuesFl(final float[][][] a,
			final float[][][] b, final float threshold) {
		return generalCompareValuesFl(a, b, threshold, null);
	}

	public static float[] getCompareValuesFl(final float[][] a,
			final float[][] b, final float threshold) {
		return generalCompareValuesFl(a, b, threshold, null);
	}

	public static float[] getCompareValuesFl(final float[] a, final float[] b,
			final float threshold) {
		return generalCompareValuesFl(a, b, threshold, null);
	}

	private static float[] generalCompareValuesFl(final Object a,
			final Object b, final float threshold, float[] returnValues) {
		if (returnValues == null) {
			/** nZeroes, nTotal, minNonZero, max. */
			returnValues = new float[] {
					0, 0, Float.POSITIVE_INFINITY, 0
			};
		}
		final int[] dims = getDimsFl(a);
		final int nDims = dims.length;
		final int dim = dims[0];
		if (nDims == 1) {
			final float[] aArray = (float[]) a;
			final float[] bArray = (float[]) b;
			for (int k = 0; k < dim; ++k) {
				final float abs = Math.abs(aArray[k] - bArray[k]);
				if (abs <= threshold) {
					returnValues[0] += 1.0;
				}
				returnValues[1] += 1.0;
				if (abs > 0.0 && abs < returnValues[2]) {
					returnValues[2] = abs;
				}
				returnValues[3] = Math.max(returnValues[3], abs);
			}
		} else if (nDims == 2) {
			final float[][] aArray = (float[][]) a;
			final float[][] bArray = (float[][]) b;
			for (int k = 0; k < dim; ++k) {
				generalCompareValuesFl(aArray[k], bArray[k], threshold, returnValues);
			}
		} else if (nDims == 3) {
			final float[][][] aArray = (float[][][]) a;
			final float[][][] bArray = (float[][][]) b;
			for (int k = 0; k < dim; ++k) {
				generalCompareValuesFl(aArray[k], bArray[k], threshold, returnValues);
			}
		} else if (nDims == 4) {
			final float[][][][] aArray = (float[][][][]) a;
			final float[][][][] bArray = (float[][][][]) b;
			for (int k = 0; k < dim; ++k) {
				generalCompareValuesFl(aArray[k], bArray[k], threshold, returnValues);
			}
		} else if (nDims == 5) {
			final float[][][][][] aArray = (float[][][][][]) a;
			final float[][][][][] bArray = (float[][][][][]) b;
			for (int k = 0; k < dim; ++k) {
				generalCompareValuesFl(aArray[k], bArray[k], threshold, returnValues);
			}
		} else if (nDims == 6) {
			final float[][][][][][] aArray = (float[][][][][][]) a;
			final float[][][][][][] bArray = (float[][][][][][]) b;
			for (int k = 0; k < dim; ++k) {
				generalCompareValuesFl(aArray[k], bArray[k], threshold, returnValues);
			}
		}
		return returnValues;
	}

	/** getConstArrays. */
	public static double[][][][][][] getConstArrayRe6(final int[] dims,
			final double c) {
		return (double[][][][][][]) generalGetConstArrayRe(dims, c);
	}

	public static double[][][][][] getConstArrayRe5(final int[] dims,
			final double c) {
		return (double[][][][][]) generalGetConstArrayRe(dims, c);
	}

	public static double[][][][] getConstArrayRe4(final int[] dims,
			final double c) {
		return (double[][][][]) generalGetConstArrayRe(dims, c);
	}

	public static double[][][] getConstArrayRe3(final int[] dims,
			final double c) {
		return (double[][][]) generalGetConstArrayRe(dims, c);
	}

	public static double[][] getConstArrayRe2(final int[] dims,
			final double c) {
		return (double[][]) generalGetConstArrayRe(dims, c);
	}

	public static double[] getConstArrayRe1(final int[] dims, final double c) {
		return (double[]) generalGetConstArrayRe(dims, c);
	}

	private static Object generalGetConstArrayRe(final int[] dims,
			final double c) {
		final int nDims = dims.length;
		final int dim = dims[0];
		if (nDims == 1) {
			final double[] a = new double[dim];
			for (int k = 0; k < dim; ++k) {
				a[k] = c;
			}
			return a;
		}
		final int[] newDims = new int[nDims - 1];
		for (int k = 0; k < nDims - 1; ++k) {
			newDims[k] = dims[k + 1];
		}
		if (nDims == 2) {
			final double[][] a = new double[dim][];
			for (int k = 0; k < dim; ++k) {
				a[k] = (double[]) generalGetConstArrayRe(newDims, c);
			}
			return a;
		} else if (nDims == 3) {
			final double[][][] a = new double[dim][][];
			for (int k = 0; k < dim; ++k) {
				a[k] = (double[][]) generalGetConstArrayRe(newDims, c);
			}
			return a;
		} else if (nDims == 4) {
			final double[][][][] a = new double[dim][][][];
			for (int k = 0; k < dim; ++k) {
				a[k] = (double[][][]) generalGetConstArrayRe(newDims, c);
			}
			return a;
		} else if (nDims == 5) {
			final double[][][][][] a = new double[dim][][][][];
			for (int k = 0; k < dim; ++k) {
				a[k] = (double[][][][]) generalGetConstArrayRe(newDims, c);
			}
			return a;
		} else if (nDims == 6) {
			final double[][][][][][] a = new double[dim][][][][][];
			for (int k = 0; k < dim; ++k) {
				a[k] = (double[][][][][]) generalGetConstArrayRe(newDims, c);
			}
			return a;
		}
		return null;
	}

	public static int[][][][][][] getConstArrayI6(final int[] dims,
			final int c) {
		return (int[][][][][][]) generalGetConstArrayI(dims, c);
	}

	public static int[][][][][] getConstArrayI5(final int[] dims, final int c) {
		return (int[][][][][]) generalGetConstArrayI(dims, c);
	}

	public static int[][][][] getConstArrayI4(final int[] dims, final int c) {
		return (int[][][][]) generalGetConstArrayI(dims, c);
	}

	public static int[][][] getConstArrayI3(final int[] dims, final int c) {
		return (int[][][]) generalGetConstArrayI(dims, c);
	}

	public static int[][] getConstArrayI2(final int[] dims, final int c) {
		return (int[][]) generalGetConstArrayI(dims, c);
	}

	public static int[] getConstArrayI1(final int[] dims, final int c) {
		return (int[]) generalGetConstArrayI(dims, c);
	}

	private static Object generalGetConstArrayI(final int[] dims, final int c) {
		final int nDims = dims.length;
		final int dim = dims[0];
		if (nDims == 1) {
			final int[] a = new int[dim];
			for (int k = 0; k < dim; ++k) {
				a[k] = c;
			}
			return a;
		}
		final int[] newDims = new int[nDims - 1];
		for (int k = 0; k < nDims - 1; ++k) {
			newDims[k] = dims[k + 1];
		}
		if (nDims == 2) {
			final int[][] a = new int[dim][];
			for (int k = 0; k < dim; ++k) {
				a[k] = (int[]) generalGetConstArrayI(newDims, c);
			}
			return a;
		} else if (nDims == 3) {
			final int[][][] a = new int[dim][][];
			for (int k = 0; k < dim; ++k) {
				a[k] = (int[][]) generalGetConstArrayI(newDims, c);
			}
			return a;
		} else if (nDims == 4) {
			final int[][][][] a = new int[dim][][][];
			for (int k = 0; k < dim; ++k) {
				a[k] = (int[][][]) generalGetConstArrayI(newDims, c);
			}
			return a;
		} else if (nDims == 5) {
			final int[][][][][] a = new int[dim][][][][];
			for (int k = 0; k < dim; ++k) {
				a[k] = (int[][][][]) generalGetConstArrayI(newDims, c);
			}
			return a;
		} else if (nDims == 6) {
			final int[][][][][][] a = new int[dim][][][][][];
			for (int k = 0; k < dim; ++k) {
				a[k] = (int[][][][][]) generalGetConstArrayI(newDims, c);
			}
			return a;
		}
		return null;
	}

	/** Scalar Operate. */
	public static void scalarOperate(final double[][][][][][] a,
			final double scalar, final NumericalRoutines.Operation operation) {
		generalScalarOperate(a, scalar, operation);
	}

	public static void scalarOperate(final double[][][][][] a,
			final double scalar, final NumericalRoutines.Operation operation) {
		generalScalarOperate(a, scalar, operation);
	}

	public static void scalarOperate(final double[][][][] a,
			final double scalar, final NumericalRoutines.Operation operation) {
		generalScalarOperate(a, scalar, operation);
	}

	public static void scalarOperate(final double[][][] a, final double scalar,
			final NumericalRoutines.Operation operation) {
		generalScalarOperate(a, scalar, operation);
	}

	public static void scalarOperate(final double[][] a, final double scalar,
			final NumericalRoutines.Operation operation) {
		generalScalarOperate(a, scalar, operation);
	}

	public static void scalarOperate(final double[] a, final double scalar,
			final NumericalRoutines.Operation operation) {
		generalScalarOperate(a, scalar, operation);
	}

	private static void generalScalarOperate(final Object a,
			final double scalar, final NumericalRoutines.Operation operation) {
		final int[] dims = getDims(a);
		final int nDims = dims.length;
		final int dim = dims[0];
		if (nDims == 1) {
			final double[] array = (double[]) a;
			for (int k = 0; k < dim; ++k) {
				if (operation == NumericalRoutines.Operation.ADD) {
					array[k] += scalar;
				} else if (operation == NumericalRoutines.Operation.SUBTRACT) {
					array[k] -= scalar;
				} else if (operation == NumericalRoutines.Operation.MULTIPLY) {
					array[k] *= scalar;
				} else if (operation == NumericalRoutines.Operation.DIVIDE) {
					array[k] /= scalar;
				} else if (operation == NumericalRoutines.Operation.POWER) {
					array[k] = Math.pow(array[k], scalar);
				} else if (operation == NumericalRoutines.Operation.SET) {
					array[k] = scalar;
				}
			}
		} else if (nDims == 2) {
			final double[][] array = (double[][]) a;
			for (int k = 0; k < dim; ++k) {
				scalarOperate(array[k], scalar, operation);
			}
		} else if (nDims == 3) {
			final double[][][] array = (double[][][]) a;
			for (int k = 0; k < dim; ++k) {
				scalarOperate(array[k], scalar, operation);
			}
		} else if (nDims == 4) {
			final double[][][][] array = (double[][][][]) a;
			for (int k = 0; k < dim; ++k) {
				scalarOperate(array[k], scalar, operation);
			}
		} else if (nDims == 5) {
			final double[][][][][] array = (double[][][][][]) a;
			for (int k = 0; k < dim; ++k) {
				scalarOperate(array[k], scalar, operation);
			}
		} else if (nDims == 6) {
			final double[][][][][][] array = (double[][][][][][]) a;
			for (int k = 0; k < dim; ++k) {
				scalarOperate(array[k], scalar, operation);
			}
		}
	}

	/** DeepClone. */
	public static double[][][][][][] deepClone(final double[][][][][][] a) {
		return (double[][][][][][]) generalDeepCloneRe(a);
	}

	public static double[][][][][] deepClone(final double[][][][][] a) {
		return (double[][][][][]) generalDeepCloneRe(a);
	}

	public static double[][][][] deepClone(final double[][][][] a) {
		final int n1 = a.length;
		final int n2 = a[0].length;
		final int n3 = a[0][0].length;
		final int n4 = a[0][0][0].length;
		final double[][][][] b = new double[n1][n2][n3][n4];
		for (int k1 = 0; k1 < n1; ++k1) {
			for (int k2 = 0; k2 < n2; ++k2) {
				for (int k3 = 0; k3 < n3; ++k3) {
					for (int k4 = 0; k4 < n4; ++k4) {
						b[k1][k2][k3][k4] = a[k1][k2][k3][k4];
					}
				}
			}
		}
		return b;
	}

	public static double[][][] deepClone(final double[][][] a) {
		return (double[][][]) generalDeepCloneRe(a);
	}

	public static double[][] deepClone(final double[][] a) {
		return (double[][]) generalDeepCloneRe(a);
	}

	public static double[] deepClone(final double[] a) {
		return (double[]) generalDeepCloneRe(a);
	}

	static Object generalDeepCloneRe(final Object a) {
		if (a == null) {
			return null;
		}
		final int[] dims = getDims(a);
		final int nDims = dims.length;
		final int dim = dims[0];
		if (nDims == 1) {
			return ((double[]) a).clone();
		} else if (nDims == 2) {
			final double[][] array = (double[][]) a;
			final double[][] returnValue = new double[dim][];
			for (int k = 0; k < dim; ++k) {
				returnValue[k] = (double[]) (generalDeepCloneRe(array[k]));
			}
			return returnValue;
		} else if (nDims == 3) {
			final double[][][] array = (double[][][]) a;
			final double[][][] returnValue = new double[dim][][];
			for (int k = 0; k < dim; ++k) {
				returnValue[k] = (double[][]) (generalDeepCloneRe(array[k]));
			}
			return returnValue;
		} else if (nDims == 4) {
			final double[][][][] array = (double[][][][]) a;
			final double[][][][] returnValue = new double[dim][][][];
			for (int k = 0; k < dim; ++k) {
				returnValue[k] = (double[][][]) (generalDeepCloneRe(array[k]));
			}
			return returnValue;
		} else if (nDims == 5) {
			final double[][][][][] array = (double[][][][][]) a;
			final double[][][][][] returnValue = new double[dim][][][][];
			for (int k = 0; k < dim; ++k) {
				returnValue[k] = (double[][][][]) (generalDeepCloneRe(array[k]));
			}
			return returnValue;
		} else if (nDims == 6) {
			final double[][][][][][] array = (double[][][][][][]) a;
			final double[][][][][][] returnValue = new double[dim][][][][][];
			for (int k = 0; k < dim; ++k) {
				returnValue[k] = (double[][][][][]) (generalDeepCloneRe(array[k]));
			}
			return returnValue;
		}
		return null;
	}

	public static float[][][][][][] deepClone(final float[][][][][][] a) {
		return (float[][][][][][]) generalDeepCloneFl(a);
	}

	public static float[][][][][] deepClone(final float[][][][][] a) {
		return (float[][][][][]) generalDeepCloneFl(a);
	}

	public static float[][][][] deepClone(final float[][][][] a) {
		return (float[][][][]) generalDeepCloneFl(a);
	}

	public static float[][][] deepClone(final float[][][] a) {
		return (float[][][]) generalDeepCloneFl(a);
	}

	public static float[][] deepClone(final float[][] a) {
		return (float[][]) generalDeepCloneFl(a);
	}

	public static float[] deepClone(final float[] a) {
		return (float[]) generalDeepCloneFl(a);
	}

	private static Object generalDeepCloneFl(final Object a) {
		if (a == null) {
			return null;
		}
		final int[] dims = getDimsFl(a);
		final int nDims = dims.length;
		final int dim = dims[0];
		if (nDims == 1) {
			return ((float[]) a).clone();
		} else if (nDims == 2) {
			final float[][] array = (float[][]) a;
			final float[][] returnValue = new float[dim][];
			for (int k = 0; k < dim; ++k) {
				returnValue[k] = (float[]) (generalDeepCloneFl(array[k]));
			}
			return returnValue;
		} else if (nDims == 3) {
			final float[][][] array = (float[][][]) a;
			final float[][][] returnValue = new float[dim][][];
			for (int k = 0; k < dim; ++k) {
				returnValue[k] = (float[][]) (generalDeepCloneFl(array[k]));
			}
			return returnValue;
		} else if (nDims == 4) {
			final float[][][][] array = (float[][][][]) a;
			final float[][][][] returnValue = new float[dim][][][];
			for (int k = 0; k < dim; ++k) {
				returnValue[k] = (float[][][]) (generalDeepCloneFl(array[k]));
			}
			return returnValue;
		} else if (nDims == 5) {
			final float[][][][][] array = (float[][][][][]) a;
			final float[][][][][] returnValue = new float[dim][][][][];
			for (int k = 0; k < dim; ++k) {
				returnValue[k] = (float[][][][]) (generalDeepCloneFl(array[k]));
			}
			return returnValue;
		} else if (nDims == 6) {
			final float[][][][][][] array = (float[][][][][][]) a;
			final float[][][][][][] returnValue = new float[dim][][][][][];
			for (int k = 0; k < dim; ++k) {
				returnValue[k] = (float[][][][][]) (generalDeepCloneFl(array[k]));
			}
			return returnValue;
		}
		return null;
	}

	/** Check for Nans. */
	public static boolean checkForNans(final double[][][][][] a) {
		if (a == null) {
			return false;
		}
		final int n = a.length;
		for (int k = 0; k < n; ++k) {
			if (checkForNans(a[k])) {
				return true;
			}
		}
		return false;
	}

	public static boolean checkForNans(final double[][][][] a) {
		if (a == null) {
			return false;
		}
		final int n = a.length;
		for (int k = 0; k < n; ++k) {
			if (checkForNans(a[k])) {
				return true;
			}
		}
		return false;
	}

	public static boolean checkForNans(final double[][][] a) {
		if (a == null) {
			return false;
		}
		final int n = a.length;
		for (int k = 0; k < n; ++k) {
			if (checkForNans(a[k])) {
				return true;
			}
		}
		return false;
	}

	public static boolean checkForNans(final double[][] a) {
		if (a == null) {
			return false;
		}
		final int n = a.length;
		for (int k = 0; k < n; ++k) {
			if (checkForNans(a[k])) {
				return true;
			}
		}
		return false;
	}

	public static boolean checkForNans(final double[] a) {
		if (a == null) {
			return false;
		}
		final int n = a.length;
		for (int k = 0; k < n; ++k) {
			if (Double.isNaN(a[k])) {
				return true;
			}
		}
		return false;
	}

	/** Check for BigEntry. */
	public static boolean checkForBigEntryx(final double[][][][][] a,
			final double d) {
		if (a == null) {
			return false;
		}
		final int n = a.length;
		for (int k = 0; k < n; ++k) {
			if (checkForBigEntryx(a[k], d)) {
				return true;
			}
		}
		return false;
	}

	public static boolean checkForBigEntryx(final double[][][][] a,
			final double d) {
		if (a == null) {
			return false;
		}
		final int n = a.length;
		for (int k = 0; k < n; ++k) {
			if (checkForBigEntryx(a[k], d)) {
				return true;
			}
		}
		return false;
	}

	public static boolean checkForBigEntryx(final double[][][] a,
			final double d) {
		if (a == null) {
			return false;
		}
		final int n = a.length;
		for (int k = 0; k < n; ++k) {
			if (checkForBigEntryx(a[k], d)) {
				return true;
			}
		}
		return false;
	}

	public static boolean checkForBigEntryx(final double[][] a,
			final double d) {
		if (a == null) {
			return false;
		}
		final int n = a.length;
		for (int k = 0; k < n; ++k) {
			if (checkForBigEntryx(a[k], d)) {
				return true;
			}
		}
		return false;
	}

	public static boolean checkForBigEntryx(final double[] a, final double d) {
		if (a == null) {
			return false;
		}
		final int n = a.length;
		for (int k = 0; k < n; ++k) {
			if (Math.abs(a[k]) > d) {
				return true;
			}
		}
		return false;
	}

	public static boolean checkForBigEntryx(final float[][][][][] a,
			final double d) {
		if (a == null) {
			return false;
		}
		final int n = a.length;
		for (int k = 0; k < n; ++k) {
			if (checkForBigEntryx(a[k], d)) {
				return true;
			}
		}
		return false;
	}

	public static boolean checkForBigEntryx(final float[][][][] a,
			final double d) {
		if (a == null) {
			return false;
		}
		final int n = a.length;
		for (int k = 0; k < n; ++k) {
			if (checkForBigEntryx(a[k], d)) {
				return true;
			}
		}
		return false;
	}

	public static boolean checkForBigEntryx(final float[][][] a,
			final double d) {
		if (a == null) {
			return false;
		}
		final int n = a.length;
		for (int k = 0; k < n; ++k) {
			if (checkForBigEntryx(a[k], d)) {
				return true;
			}
		}
		return false;
	}

	public static boolean checkForBigEntryx(final float[][] a, final double d) {
		if (a == null) {
			return false;
		}
		final int n = a.length;
		for (int k = 0; k < n; ++k) {
			if (checkForBigEntryx(a[k], d)) {
				return true;
			}
		}
		return false;
	}

	public static boolean checkForBigEntryx(final float[] a, final double d) {
		if (a == null) {
			return false;
		}
		final int n = a.length;
		for (int k = 0; k < n; ++k) {
			if (Math.abs(a[k]) > d) {
				return true;
			}
		}
		return false;
	}
}
