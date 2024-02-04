package com.skagit.util.complex;

import com.skagit.util.randomx.Randomx;

public class ComplexVectorGenerator {
	final public static double _SmallForRandomVectorGenerator = 1.0e-8;
	final private static double _Sqrt2 = Math.sqrt(2.0);
	final private static int _Re = DimensionSpec._Re;
	final private static int _Im = DimensionSpec._Im;
	final private Randomx _r;
	final private double[] _muRe;
	final private double[] _muIm;
	final private double[][] _varRe;
	final private double[][] _varIm;
	final private ComplexMatrix _sqrtVar;

	public ComplexVectorGenerator(final double[] muRe, final double[] muIm, final double[][] varRe,
			final double[][] varIm, final Randomx r) {
		_r = r;
		_muRe = muRe;
		_muIm = muIm;
		_varRe = varRe;
		_varIm = varIm;
		final ComplexMatrix complexVar = new ComplexMatrix(_varRe, _varIm);
		_sqrtVar = complexVar.sqrt();
	}

	public double[][] getVarRe() {
		return _varRe;
	}

	public double[][] getVarIm() {
		return _varIm;
	}

	public double[][] generateComplexGaussianVector() {
		final int m = _muRe.length;
		final double[][] reImPair;
		final double[] re = new double[m];
		final double[] im = new double[m];
		for (int i = 0; i < m; ++i) {
			re[i] = _r.nextGaussian() / _Sqrt2;
			im[i] = _r.nextGaussian() / _Sqrt2;
		}
		reImPair = _sqrtVar.multiplyOnRight(re, im);
		for (int k = 0; k < m; ++k) {
			reImPair[_Re][k] += _muRe[k];
			reImPair[_Im][k] += _muIm[k];
		}
		return reImPair;
	}
}
