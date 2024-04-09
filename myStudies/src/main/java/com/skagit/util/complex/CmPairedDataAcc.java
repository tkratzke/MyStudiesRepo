package com.skagit.util.complex;

import com.skagit.util.NumericalRoutines;
import com.skagit.util.arrayDumper.ArrayDumper;
import com.skagit.util.randomx.Randomx;

public class CmPairedDataAcc {
    /** Convenience: */
    final private static int _U = 0;
    final private static int _V = 1;
    final private static int _NInPair = 2;
    final private static int _Re = DimensionSpec._Re;
    final private static int _Im = DimensionSpec._Im;
    final private static int _Cmplx = DimensionSpec._Cmplx;
    private int _n;
    private double _sumURe;
    private double _sumUIm;
    private double _sumVRe;
    private double _sumVIm;
    /** Second order. Note: _sumUuIm and _sumVvIm are always 0. */
    private double _sumUuRe;
    private double _sumUvRe;
    private double _sumUvIm;
    private double _sumVvRe;

    public CmPairedDataAcc() {
	_n = 0;
	_sumURe = _sumUIm = _sumVRe = _sumVIm = 0;
	_sumUuRe = _sumUvRe = _sumUvIm = _sumVvRe = 0;
    }

    public void add(final double uRe, final double uIm, final double vRe, final double vIm) {
	_sumURe += uRe;
	_sumUIm += uIm;
	_sumVRe += vRe;
	_sumVIm += vIm;
	_sumUuRe += (uRe * uRe) + (uIm * uIm);
	_sumUvRe += (uRe * vRe) + (uIm * vIm);
	_sumUvIm += (uRe * -vIm) + (uIm * vRe);
	_sumVvRe += (vRe * vRe) + (vIm * vIm);
	++_n;
    }

    public int getN() {
	return _n;
    }

    /** Returns cmplx, {u,v}, {u,v}. */
    public double[][][] getVar() {
	final double uuRe;
	final double uvRe;
	final double uvIm;
	final double vvRe;
	if (_n < 2) {
	    uuRe = uvRe = uvIm = vvRe = 0;
	} else {
	    final double[][] mean = getMean();
	    final double uBarRe = mean[_Re][_U];
	    final double uBarIm = mean[_Im][_U];
	    final double vBarRe = mean[_Re][_V];
	    final double vBarIm = mean[_Im][_V];
	    final double nM1 = _n - 1;
	    uuRe = (_sumUuRe - ((uBarRe * uBarRe) + (uBarIm * uBarIm)) * _n) / nM1;
	    uvRe = (_sumUvRe - ((uBarRe * vBarRe) + (uBarIm * vBarIm)) * _n) / nM1;
	    uvIm = (_sumUvIm - ((uBarRe * -vBarIm) + (uBarIm * vBarRe)) * _n) / nM1;
	    vvRe = (_sumVvRe - ((vBarRe * vBarRe) + (vBarIm * vBarIm)) * _n) / nM1;
	}
	final double[][][] var = new double[_Cmplx][_NInPair][_NInPair];
	var[_Re][_U][_U] = uuRe;
	var[_Re][_U][_V] = uvRe;
	var[_Re][_V][_U] = uvRe;
	var[_Re][_V][_V] = vvRe;
	var[_Im][_U][_U] = 0;
	var[_Im][_U][_V] = uvIm;
	var[_Im][_V][_U] = -uvIm;
	var[_Im][_V][_V] = 0;
	return var;
    }

    public double[][] getMean() {
	if (_n == 0) {
	    return new double[][] { new double[] { 0, 0 }, new double[] { 0, 0 } };
	}
	final double[][] mean = new double[_Cmplx][_NInPair];
	mean[_Re][_U] = _sumURe / _n;
	mean[_Re][_V] = _sumVRe / _n;
	mean[_Im][_U] = _sumUIm / _n;
	mean[_Im][_V] = _sumVIm / _n;
	return mean;
    }

    public static void main(final String[] args) {
	final int nPairs = 25;
	final Randomx r = new Randomx(/* useCurrentTimeMs= */false);
	final double[][] uvPairs = new double[nPairs][];
	final CmPairedDataAcc acc = new CmPairedDataAcc();
	for (int k = 0; k < nPairs; ++k) {
	    final double uRe = r.nextGaussian();
	    final double uIm = r.nextGaussian();
	    final double vRe = r.nextGaussian();
	    final double vIm = r.nextGaussian();
	    acc.add(uRe, uIm, vRe, vIm);
	    uvPairs[k] = new double[] { uRe, uIm, vRe, vIm };
	}
	final double[][] mean = acc.getMean();
	final double[][][] var = acc.getVar();
	final String s = ArrayDumper.getReString(uvPairs, 0);
	System.out.printf("\nData:\n%s", s);
	final String varString = ArrayDumper.getReString(var, 0);
	System.out.printf("\nMeans:\n%s", NumericalRoutines.getString(mean));
	System.out.printf("\nVar:\n%s", varString);
    }
}
