package com.skagit.util.complex;

import com.skagit.util.NumericalRoutines;

class ComplexVector {
	final private double[] _re;
	final private double[] _im;

	ComplexVector(final double[] re, final double[] im) {
		_re = re;
		_im = im;
	}

	ComplexVector(final int m) {
		_re = new double[m];
		_im = new double[m];
	}

	String getString(final String caption) {
		String s = "\n" + caption + ": ";
		final int m = getM();
		for (int k = 0; k < m; ++k) {
			if (k > 0) {
				s += ", ";
			}
			s += String.format("%f + %fi", _re[k], _im[k]);
		}
		return s;
	}

	/** No conjugation here. */
	Complex dotOnRight(final ComplexVector vector) {
		final int m = getM();
		if (vector.getM() != m) {
			return null;
		}
		final double[] hisRe = vector._re;
		final double[] hisIm = vector._im;
		double newRe = 0.0;
		double newIm = 0.0;
		for (int k3 = 0; k3 < m; ++k3) {
			newRe += _re[k3] * hisRe[k3] - _im[k3] * hisIm[k3];
			newIm += _re[k3] * hisIm[k3] + _im[k3] * hisRe[k3];
		}
		return new Complex(newRe, newIm);
	}

	/** No conjugation here. */
	ComplexVector pointwiseOperate(final ComplexVector vector,
			final NumericalRoutines.Operation operation, final boolean inPlace) {
		final int m = getM();
		final int hisM = vector.getM();
		if (m != hisM) {
			return null;
		}
		final double[] re = inPlace ? _re : new double[m];
		final double[] im = inPlace ? _im : new double[m];
		final double[] hisRe = vector._re;
		final double[] hisIm = vector._im;
		for (int k1 = 0; k1 < m; ++k1) {
			final double re1 = _re[k1];
			final double im1 = _im[k1];
			final double re2 = hisRe[k1];
			final double im2 = hisIm[k1];
			double resultRe;
			double resultIm;
			switch (operation) {
			case ADD:
				resultRe = re1 + re2;
				resultIm = im1 + im2;
				break;
			case SUBTRACT:
				resultRe = re1 - re2;
				resultIm = im1 - im2;
				break;
			case MULTIPLY:
				resultRe = re1 * re2 - im1 * im2;
				resultIm = re1 * im2 + re2 * im1;
				break;
			case DIVIDE:
				final double numRe = re1 * re2 + im1 * im2;
				final double numIm = -re1 * im2 + im1 * re2;
				final double den = re2 * re2 + im2 * im2;
				resultRe = numRe / den;
				resultIm = numIm / den;
				break;
			default:
				resultRe = resultIm = Double.NaN;
			}
			re[k1] = resultRe;
			im[k1] = resultIm;
		}
		return inPlace ? this : new ComplexVector(re, im);
	}

	ComplexVector scalarOperate(final Complex complex,
			final NumericalRoutines.Operation operation, final boolean inPlace) {
		final int m = getM();
		final ComplexVector complexVector = inPlace ? this : new ComplexVector(m);
		final double re2 = complex.getRe();
		final double im2 = complex.getIm();
		for (int k = 0; k < m; ++k) {
			final double re1 = _re[k];
			final double im1 = _im[k];
			double resultRe;
			double resultIm;
			switch (operation) {
			case ADD:
				resultRe = re1 + re2;
				resultIm = im1 + im2;
				break;
			case SUBTRACT:
				resultRe = re1 - re2;
				resultIm = im1 - im2;
				break;
			case MULTIPLY:
				resultRe = re1 * re2 - im1 * im2;
				resultIm = re1 * im2 + re2 * im1;
				break;
			case DIVIDE:
				final double numRe = re1 * re2 + im1 * im2;
				final double numIm = -re1 * im2 + im1 * re2;
				final double den = re2 * re2 + im2 * im2;
				resultRe = numRe / den;
				resultIm = numIm / den;
				break;
			default:
				resultRe = resultIm = Double.NaN;
			}
			complexVector._re[k] = resultRe;
			complexVector._im[k] = resultIm;
		}
		return complexVector;
	}

	int getM() {
		return _re.length;
	}

	double getRe(final int i) {
		return _re[i];
	}

	double getIm(final int i) {
		return _im[i];
	}

	void set(final int i, final Complex complex) {
		_re[i] = complex.getRe();
		_im[i] = complex.getIm();
	}

	double[] getReals() {
		return _re;
	}

	double[] getImags() {
		return _im;
	}
}
