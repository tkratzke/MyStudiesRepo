package com.skagit.util.complex;

import com.skagit.util.NumericalRoutines;

public class ComplexMatrix {
    final private double[][] _re;
    final private double[][] _im;

    public ComplexMatrix(final double[][] re, final double[][] im) {
	_re = re;
	_im = im;
    }

    public ComplexMatrix(final int m, final int n) {
	_re = new double[m][n];
	_im = new double[m][n];
    }

    /** Right now, we can only do sqrts for 2 x 2's. */
    public ComplexMatrix sqrt() {
	final int m = getM();
	final int n = getN();
	if (m != n || m != 2) {
	    return null;
	}
	for (int k1 = 0; k1 < m; ++k1) {
	    if (_im[k1][k1] != 0) {
		return null;
	    }
	    for (int k2 = 0; k2 < k1; ++k2) {
		if ((_re[k1][k2] != _re[k2][k1]) || (_im[k1][k2] != -_im[k2][k1])) {
		    return null;
		}
	    }
	}
	double[][] reX = _re;
	double[][] imX = _im;
	final double expand = 1.0 + 1.0e-7;
	for (;;) {
	    final double d0 = reX[0][0];
	    final double d1 = reX[1][1];
	    final double re01 = reX[0][1];
	    final double im01 = imX[0][1];
	    final double delSq = d0 * d1 - (re01 * re01 + im01 * im01);
	    if (delSq < 0) {
		reX = NumericalRoutines.deepClone(reX);
		imX = NumericalRoutines.deepClone(imX);
		for (int k1 = 0; k1 < m; ++k1) {
		    reX[k1][k1] *= expand;
		}
		continue;
	    }
	    final double del = Math.sqrt(delSq);
	    final double scale = Math.sqrt(d0 + d1 + 2 * del);
	    final double[][] returnValueRe = new double[m][];
	    final double[][] returnValueIm = new double[m][];
	    boolean zeroOut = false;
	    for (int k1 = 0; k1 < m; ++k1) {
		returnValueRe[k1] = reX[k1].clone();
		returnValueIm[k1] = imX[k1].clone();
		returnValueRe[k1][k1] += del;
		for (int k2 = 0; k2 < m; ++k2) {
		    returnValueRe[k1][k2] /= scale;
		    returnValueIm[k1][k2] /= scale;
		    if (Double.isNaN(returnValueRe[k1][k2])) {
			zeroOut = true;
		    }
		    if (Double.isNaN(returnValueIm[k1][k2])) {
			zeroOut = true;
		    }
		}
	    }
	    if (zeroOut) {
		for (int k1 = 0; k1 < m; ++k1) {
		    returnValueRe[k1][k1] += del;
		    for (int k2 = 0; k2 < m; ++k2) {
			returnValueRe[k1][k2] = returnValueIm[k1][k2] = 0;
		    }
		}
	    }
	    final ComplexMatrix returnValue = new ComplexMatrix(returnValueRe, returnValueIm);
	    returnValue.hermitianize();
	    return returnValue;
	}
    }

    private void hermitianize() {
	final int n = getM();
	for (int k1 = 0; k1 < n; ++k1) {
	    for (int k2 = 0; k2 < k1; ++k2) {
		final double re = (_re[k1][k2] + _re[k2][k1]) / 2.0;
		_re[k1][k2] = _re[k2][k1] = re;
		final double im = (_im[k1][k2] - _im[k2][k1]) / 2.0;
		_im[k1][k2] = im;
		_im[k2][k1] = -im;
	    }
	}
    }

    public String getString(final String caption) {
	String s = "";
	final int m = _re.length;
	final int n = _re[0].length;
	for (int k1 = 0; k1 < m; ++k1) {
	    if (k1 == 0) {
		s += "\n" + caption;
	    }
	    s += "\n";
	    for (int k2 = 0; k2 < n; ++k2) {
		if (k2 > 0) {
		    s += ", ";
		}
		s += String.format("%f + %fi", _re[k1][k2], _im[k1][k2]);
	    }
	}
	return s;
    }

    /** No conjugates here. */
    public ComplexMatrix multiplyOnRight(final ComplexMatrix complexMatrix) {
	final double[][][] product = multiplyOnRight(complexMatrix._re, complexMatrix._im);
	return new ComplexMatrix(product[DimensionSpec._Re], product[DimensionSpec._Im]);
    }

    /** No conjugates here. */
    public double[][] multiplyOnRight(final double[] reIn, final double[] imIn) {
	final int m = getM();
	final int n = getN();
	if (reIn.length != n) {
	    return null;
	}
	final double[] re = new double[m];
	final double[] im = new double[m];
	for (int i = 0; i < m; ++i) {
	    re[i] = im[i] = 0;
	    for (int j = 0; j < n; ++j) {
		re[i] += _re[i][j] * reIn[j] - _im[i][j] * imIn[j];
		im[i] += _re[i][j] * imIn[j] + _im[i][j] * reIn[j];
	    }
	}
	return new double[][] { re, im };
    }

    /** No conjugates here. */
    public double[][][] multiplyOnRight(final double[][] reIn, final double[][] imIn) {
	final int m = getM();
	final int n1 = getN();
	if (reIn.length != n1) {
	    return null;
	}
	final int n = reIn[0].length;
	final double[][] re = new double[m][n];
	final double[][] im = new double[m][n];
	for (int i = 0; i < m; ++i) {
	    for (int j = 0; j < n; ++j) {
		re[i][j] = im[i][j] = 0;
		for (int k = 0; k < n1; ++k) {
		    re[i][j] += _re[i][k] * reIn[k][j] - _im[i][k] * imIn[k][j];
		    im[i][j] += _re[i][k] * imIn[k][j] + _im[i][k] * reIn[k][j];
		}
	    }
	}
	return new double[][][] { re, im };
    }

    public ComplexMatrix pointwiseOperate(final ComplexMatrix matrix, final NumericalRoutines.Operation operation,
	    final boolean inPlace) {
	final int m = getM();
	final int n = getN();
	final int hisM = matrix._re.length;
	final int hisN = matrix._re[0].length;
	if (m != hisM || n != hisN) {
	    return null;
	}
	final double[][] re = inPlace ? _re : new double[m][];
	final double[][] im = inPlace ? _im : new double[m][];
	final double[][] hisRe = matrix._re;
	final double[][] hisIm = matrix._im;
	for (int k1 = 0; k1 < m; ++k1) {
	    final ComplexVector myRow = new ComplexVector(_re[k1], _im[k1]);
	    final ComplexVector hisRow = new ComplexVector(hisRe[k1], hisIm[k1]);
	    final ComplexVector vector = myRow.pointwiseOperate(hisRow, operation, inPlace);
	    if (!inPlace) {
		re[k1] = vector.getReals();
		im[k1] = vector.getImags();
	    }
	}
	return inPlace ? this : new ComplexMatrix(re, im);
    }

    public ComplexMatrix scalarOperate(final Complex complex, final NumericalRoutines.Operation operation,
	    final boolean inPlace) {
	final int m = getM();
	final double[][] re = inPlace ? _re : new double[m][];
	final double[][] im = inPlace ? _im : new double[m][];
	for (int k1 = 0; k1 < m; ++k1) {
	    final ComplexVector myRow = new ComplexVector(_re[k1], _im[k1]);
	    final ComplexVector vector = myRow.scalarOperate(complex, operation, inPlace);
	    if (!inPlace) {
		re[k1] = vector.getReals();
		im[k1] = vector.getImags();
	    }
	}
	return inPlace ? this : new ComplexMatrix(re, im);
    }

    public ComplexMatrix conjugateTranspose() {
	final int m = _re.length;
	final int n = _re[0].length;
	final double[][] re = new double[n][m];
	final double[][] im = new double[n][m];
	for (int k1 = 0; k1 < m; ++k1) {
	    for (int k2 = 0; k2 < n; ++k2) {
		re[k1][k2] = _re[k2][k1];
		im[k1][k2] = -_im[k2][k1];
	    }
	}
	return new ComplexMatrix(re, im);
    }

    private static ComplexMatrix getHermitianMatrix(final int n) {
	final double[][] re = new double[n][n];
	final double[][] im = new double[n][n];
	for (int k1 = 0; k1 < n; ++k1) {
	    for (int k2 = 0; k2 < n; ++k2) {
		re[k1][k2] = (1 + k1) * (1 + k2);
		im[k1][k2] = k1 + k2 + 2 + (k1 > k2 ? 3 : 1);
	    }
	}
	final ComplexMatrix complexMatrix = new ComplexMatrix(re, im);
	final ComplexMatrix conjugateTranspose = complexMatrix.conjugateTranspose();
	final ComplexMatrix hermitian = complexMatrix.multiplyOnRight(conjugateTranspose);
	return hermitian;
    }

    public int getM() {
	return _re.length;
    }

    public int getN() {
	return _re[0].length;
    }

    public double getRe(final int i, final int j) {
	return _re[i][j];
    }

    public double getIm(final int i, final int j) {
	return _im[i][j];
    }

    double[][] getReals() {
	return _re;
    }

    double[][] getImags() {
	return _im;
    }

    public static void main(final String[] args) {
	final ComplexMatrix hermitianMatrix = getHermitianMatrix(2);
	System.out.println(hermitianMatrix.getString("Hermitian"));
	final ComplexMatrix sqrt = hermitianMatrix.sqrt();
	System.out.println(sqrt.getString("Sqrt"));
	final ComplexMatrix product = sqrt.multiplyOnRight(sqrt.conjugateTranspose());
	System.out.println(product.getString("Product"));
    }

    public void set(final int i, final int j, final Complex complex) {
	_re[i][j] = complex.getRe();
	_im[i][j] = complex.getIm();
    }
    /**
     * <pre>
     * Hermitian 30.000000 + 0.000000i, 48.000000 + 6.000000i 48.000000 +
     * -6.000000i, 81.000000 + 0.000000i
     *
     * Sqrt 3.463575 + 0.000000i, 4.210305 + 0.526288i 4.210305 + -0.526288i,
     * 7.937024 + 0.000000i
     *
     * Product 30.000000 + 0.000000i, 48.000000 + 6.000000i 48.000000 + -6.000000i,
     * 81.000000 + 0.000000i
     */
}
