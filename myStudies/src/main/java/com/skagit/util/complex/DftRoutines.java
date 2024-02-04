package com.skagit.util.complex;

import java.io.File;

import com.skagit.util.MathX;
import com.skagit.util.NumericalRoutines;
import com.skagit.util.StringUtilities;
import com.skagit.util.arrayDumper.ArrayDumper;
import com.skagit.util.randomx.Randomx;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_3D;

public class DftRoutines {
	/** Convenience: */
	private static int _Re = DimensionSpec._Re;
	private static int _Im = DimensionSpec._Im;
	private static int _Cmplx = DimensionSpec._Cmplx;
	final private static double _TwoPi = 2.0 * Math.PI;
	final private static boolean _DoManualCheck = false;
	final private static boolean _DoInverseCheck = false;

	public static double[][][][] doCompleteDftWithChecks(final double[][][][] a, final DftDirection direction) {
		final int nT = a[_Re].length;
		final int nX = a[_Re][0].length;
		final int nY = a[_Re][0][0].length;
		final double[][][][] aOut = ComplexUtils.deepClone(a);
		doDft(aOut, direction);
		if (_DoInverseCheck) {
			final double[][][][] aCopy = ComplexUtils.deepClone(aOut);
			doDft(aCopy, direction.getOpposite());
			/** aCopy should be the same as a. */
			final double[] compareValues = ComplexUtils.getCompareValuesRe(aCopy, a, ComplexUtils._ThresholdRe);
			System.out.printf("\nInverse Check CompareValues%s", NumericalRoutines.getString(compareValues));
		}
		if (_DoManualCheck) {
			final double[][][][] aOut2 = ComplexUtils.deepClone(a);
			doManualDft(aOut2, direction);
			for (int iT = 0; iT < nT; ++iT) {
				for (int iX = 0; iX < nX; ++iX) {
					for (int iY = 0; iY < nY; ++iY) {
						final double re = aOut[_Re][iT][iX][iY];
						final double im = aOut[_Im][iT][iX][iY];
						final double re2 = aOut2[_Re][iT][iX][iY];
						final double im2 = aOut2[_Im][iT][iX][iY];
						final double absDiff = (re - re2) * (re - re2) + (im - im2) * (im - im2);
						if (absDiff > 1.0e-10) {
							System.exit(88);
						}
					}
				}
			}
		}
		return aOut;
	}

	/** Indices are Re/Im, nT, nX, nY. Replaces a by its dft. */
	private static void doDft(final double[][][][] a, final DftDirection direction) {
		final int nT = a[_Re].length;
		final int nX = a[_Re][0].length;
		final int nY = a[_Re][0][0].length;
		final double[][][] dftArray;
		final double[][] dftArrayForNYEquals1;
		if (nY > 1) {
			dftArray = new double[nT][nX][2 * nY];
			dftArrayForNYEquals1 = null;
			for (int iT = 0; iT < nT; ++iT) {
				for (int iX = 0; iX < nX; ++iX) {
					for (int iY = 0; iY < nY; ++iY) {
						for (int cmplxIndex = 0; cmplxIndex < _Cmplx; ++cmplxIndex) {
							final int ii = (iY * _Cmplx) + cmplxIndex;
							dftArray[iT][iX][ii] = a[cmplxIndex][iT][iX][iY];
						}
					}
				}
			}
			/** The forward for DoubleFFT_3D corresponds to multiplier = -1. */
			if (direction.getMultiplier() == -1) {
				final DoubleFFT_3D dft = new DoubleFFT_3D(nT, nX, nY);
				dft.complexForward(dftArray);
			} else {
				final boolean scale = false;
				final DoubleFFT_3D dftInverse = new DoubleFFT_3D(nT, nX, nY);
				dftInverse.complexInverse(dftArray, scale);
			}
		} else {
			dftArray = null;
			dftArrayForNYEquals1 = new double[nT][2 * nX];
			for (int iT = 0; iT < nT; ++iT) {
				for (int iX = 0; iX < nX; ++iX) {
					for (int cmplxIndex = 0; cmplxIndex < _Cmplx; ++cmplxIndex) {
						final int ii = (iX * _Cmplx) + cmplxIndex;
						dftArrayForNYEquals1[iT][ii] = a[cmplxIndex][iT][iX][0];
					}
				}
			}
			/** The forward for DoubleFFT_3D corresponds to multiplier = -1. */
			if (direction.getMultiplier() == -1) {
				final DoubleFFT_2D dft = new DoubleFFT_2D(nT, nX);
				dft.complexForward(dftArrayForNYEquals1);
			} else {
				final boolean scale = false;
				final DoubleFFT_2D dftInverse = new DoubleFFT_2D(nT, nX);
				dftInverse.complexInverse(dftArrayForNYEquals1, scale);
			}
		}
		for (int iT = 0; iT < nT; ++iT) {
			for (int iX = 0; iX < nX; ++iX) {
				for (int iY = 0; iY < nY; ++iY) {
					for (int cmplxIndex = 0; cmplxIndex < _Cmplx; ++cmplxIndex) {
						if (nY > 1) {
							final int ii = (iY * _Cmplx) + cmplxIndex;
							a[cmplxIndex][iT][iX][iY] = dftArray[iT][iX][ii];
						} else {
							final int ii = (iX * _Cmplx) + cmplxIndex;
							a[cmplxIndex][iT][iX][0] = dftArrayForNYEquals1[iT][ii];
						}
					}
				}
			}
		}
		final double scale = 1.0 / Math.sqrt(nT * nX * nY);
		ComplexUtils.scalarOperate(a, scale, NumericalRoutines.Operation.MULTIPLY);
	}

	/** Indices are Re/Im, nT, nX, nY. Replaces a by its dft. */
	private static void doManualDft(final double[][][][] a, final DftDirection direction) {
		final int multiplier = direction.getMultiplier();
		final int nT = a[_Re].length;
		final int nX = a[_Re][0].length;
		final int nY = a[_Re][0][0].length;
		/**
		 * Accumulate the results into a scratch array. Then scale the results and copy
		 * back to the input array.
		 */
		final double[][][][] result;
		result = new double[_Cmplx][nT][nX][nY];
		for (int iT = 0; iT < nT; ++iT) {
			for (int iX = 0; iX < nX; ++iX) {
				for (int iY = 0; iY < nY; ++iY) {
					double resultRe = 0;
					double resultIm = 0;
					for (int iT1 = 0; iT1 < nT; ++iT1) {
						for (int iX1 = 0; iX1 < nX; ++iX1) {
							for (int iY1 = 0; iY1 < nY; ++iY1) {
								final double re1 = a[_Re][iT1][iX1][iY1];
								final double im1 = a[_Im][iT1][iX1][iY1];
								double expPart = 0.0;
								expPart += (((double) iT1) * iT) / nT;
								expPart += (((double) iX1) * iX) / nX;
								expPart += (((double) iY1) * iY) / nY;
								expPart *= _TwoPi * multiplier;
								final double re2 = MathX.cosX(expPart);
								final double im2 = MathX.sinX(expPart);
								resultRe += re1 * re2 - im1 * im2;
								resultIm += re1 * im2 + re2 * im1;
							}
						}
					}
					result[_Re][iT][iX][iY] = resultRe;
					result[_Im][iT][iX][iY] = resultIm;
				}
			}
		}
		/** No matter which way we're going, we scale the same way. */
		final double scale = 1.0 / Math.sqrt(nT * nX * nY);
		ComplexUtils.scalarOperate(result, scale, NumericalRoutines.Operation.MULTIPLY);
		ComplexUtils.pointwiseOperate(result, a, NumericalRoutines.Operation.COPY_TO);
	}

	/** Rotating (used for some Dfts). */
	private static void rotateLeft(final Object a, final int[] shifts) {
		final int[] dims = ComplexUtils.getDims(a);
		final int nDims = dims.length;
		if (nDims != shifts.length) {
			System.exit(18);
		}
		final int dim = dims[0];
		int shift = shifts[0];
		final int[] newShifts = new int[nDims - 1];
		for (int k = 0; k < nDims - 1; ++k) {
			newShifts[k] = shifts[k + 1];
		}
		while (shift < 0) {
			shift += dim;
		}
		if (nDims == 1) {
			final double[] array = (double[]) a;
			final double[] arrayX = array.clone();
			for (int k = 0; k < dim; ++k) {
				array[k] = arrayX[(k + shift) % dim];
			}
		} else if (nDims == 2) {
			final double[][] array = (double[][]) a;
			final double[][] arrayX = array.clone();
			for (int k = 0; k < dim; ++k) {
				array[k] = arrayX[(k + shift) % dim];
				rotateLeft(array[k], newShifts);
			}
		} else if (nDims == 3) {
			final double[][][] array = (double[][][]) a;
			final double[][][] arrayX = array.clone();
			for (int k = 0; k < dim; ++k) {
				array[k] = arrayX[(k + shift) % dim];
				rotateLeft(array[k], newShifts);
			}
		} else if (nDims == 4) {
			final double[][][][] array = (double[][][][]) a;
			final double[][][][] arrayX = array.clone();
			for (int k = 0; k < dim; ++k) {
				array[k] = arrayX[(k + shift) % dim];
				rotateLeft(array[k], newShifts);
			}
		} else if (nDims == 5) {
			final double[][][][][] array = (double[][][][][]) a;
			final double[][][][][] arrayX = array.clone();
			for (int k = 0; k < dim; ++k) {
				array[k] = arrayX[(k + shift) % dim];
				rotateLeft(array[k], newShifts);
			}
		} else if (nDims == 6) {
			final double[][][][][][] array = (double[][][][][][]) a;
			final double[][][][][][] arrayX = array.clone();
			for (int k = 0; k < dim; ++k) {
				array[k] = arrayX[(k + shift) % dim];
				rotateLeft(array[k], newShifts);
			}
		}
	}

	public static void rotateRight(final Object a, final int[] shifts) {
		final int nDims = shifts.length;
		final int[] rShifts = new int[nDims];
		for (int k = 0; k < nDims; ++k) {
			rShifts[k] = -shifts[k];
		}
		rotateLeft(a, rShifts);
	}

	public static void oldMain(final String[] args) {
		final Randomx r = new Randomx(/* useCurrentTimeMs= */false);
		final int size0 = 4;
		final int size1 = 8;
		final int size2 = 16;
		final double[][][][] origA = new double[2][size0][size1][size2];
		for (int i1 = 0; i1 < size0; ++i1) {
			for (int i2 = 0; i2 < size1; ++i2) {
				for (int i3 = 0; i3 < size2; ++i3) {
					origA[_Re][i1][i2][i3] = r.nextGaussian();
					origA[_Im][i1][i2][i3] = 0;
				}
			}
		}
	}

	public static void main(final String[] args) {
		final String outputPathX = "foo/foo";
		final String outputPath = StringUtilities.cleanUpFilePath(outputPathX);
		final File outputDir = new File(outputPath);
		final boolean clearOutDir = true;
		final ArrayDumper arrayDumper = new ArrayDumper(outputDir, clearOutDir);
		final Randomx r = new Randomx(/* useCurrentTimeMs= */false);
		final int size0 = 8;
		final int size1 = 12;
		final int size2 = 16;
		final double[][][][] origA = new double[2][size0][size1][size2];
		for (int i0 = 0; i0 < size0; ++i0) {
			for (int i1 = 0; i1 < size1; ++i1) {
				for (int i2 = 0; i2 < size2; ++i2) {
					origA[_Re][i0][i1][i2] = r.nextGaussian();
					origA[_Im][i0][i1][i2] = r.nextGaussian();
				}
			}
		}
		arrayDumper.createReFile("origA", origA);
		final double[][][][] a = ComplexUtils.deepClone(origA);
		if (true) {
		} else {
			arrayDumper.createReFile("a", a);
			doDft(a, DftDirection.PHYSICAL_TO_FOURIER);
			arrayDumper.createReFile("physicalToFourier", a);
			doDft(a, DftDirection.PHYSICAL_TO_FOURIER.getOpposite());
			arrayDumper.createReFile("backToPhysical", a);
			double maxDiff = 0;
			for (int i0 = 0; i0 < size0; ++i0) {
				for (int i1 = 0; i1 < size1; ++i1) {
					for (int i2 = 0; i2 < size2; ++i2) {
						maxDiff = Math.max(maxDiff, Math.abs(a[_Re][i0][i1][i2] - origA[_Re][i0][i1][i2]));
						maxDiff = Math.max(maxDiff, Math.abs(a[_Im][i0][i1][i2] - origA[_Im][i0][i1][i2]));
					}
				}
			}
			System.out.println(maxDiff);
		}
		if (true) {
			/** Check against the manual. */
			final double[][][][] a1 = ComplexUtils.deepClone(origA);
			final double[][][][] a2 = ComplexUtils.deepClone(origA);
			doDft(a1, DftDirection.PHYSICAL_TO_FOURIER);
			arrayDumper.createReFile("automagic", a1);
			doManualDft(a2, DftDirection.PHYSICAL_TO_FOURIER);
			arrayDumper.createReFile("manual", a2);
			double maxDiff2 = 0;
			for (int i0 = 0; i0 < size0; ++i0) {
				for (int i1 = 0; i1 < size1; ++i1) {
					for (int i2 = 0; i2 < size2; ++i2) {
						maxDiff2 = Math.max(maxDiff2, Math.abs(a1[_Re][i0][i1][i2] - a2[_Re][i0][i1][i2]));
						maxDiff2 = Math.max(maxDiff2, Math.abs(a1[_Im][i0][i1][i2] - a2[_Im][i0][i1][i2]));
					}
				}
			}
			final double[][][][] ratio = ComplexUtils.deepClone(a1);
			ComplexUtils.pointwiseOperate(ratio, a2, NumericalRoutines.Operation.DIVIDE);
			System.out.println(NumericalRoutines.getString(ratio[0][0]));
			System.out.println(maxDiff2);
		}
	}

	public static boolean isGeneralizedHermitian(final double[][][][] dft) {
		final double[][][] dftRe = dft[_Re];
		final double[][][] dftIm = dft[_Im];
		final int nT = dftRe.length;
		final int nX = dftRe[0].length;
		final int nY = dftRe[0][0].length;
		for (int w = 0; w < nT; ++w) {
			for (int k = 0; k < nX; ++k) {
				for (int ell = 0; ell < nY; ++ell) {
					final int wC = w == 0 ? 0 : (nT - w);
					final int kC = k == 0 ? 0 : (nX - k);
					final int ellC = ell == 0 ? 0 : (nY - ell);
					final double re = dftRe[w][k][ell];
					final double im = dftIm[w][k][ell];
					final double reC = dftRe[wC][kC][ellC];
					final double imC = dftIm[wC][kC][ellC];
					if ((re != reC) || (im != -imC)) {
						return false;
					}
				}
			}
		}
		return true;
	}
}
