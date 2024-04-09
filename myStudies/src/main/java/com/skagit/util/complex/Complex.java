package com.skagit.util.complex;

import com.skagit.util.NumericalRoutines;

public class Complex {
    final private double _re;
    final private double _im;

    public Complex(final double r, final double i) {
	_re = r;
	_im = i;
    }

    public double getRe() {
	return _re;
    }

    public double getIm() {
	return _im;
    }

    public Complex operate(final Complex complex, final NumericalRoutines.Operation operation) {
	final double re2 = complex.getRe();
	final double im2 = complex.getIm();
	final double re1 = _re;
	final double im1 = _im;
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
	return new Complex(resultRe, resultIm);
    }
}
