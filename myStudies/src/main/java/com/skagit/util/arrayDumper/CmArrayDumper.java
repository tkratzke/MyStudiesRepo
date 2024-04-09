package com.skagit.util.arrayDumper;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import com.skagit.util.complex.DimensionSpec;
import com.skagit.util.poiUtils.CmExcelDumper;

public class CmArrayDumper extends ArrayDumper {

    final static String _CmSuffix = "Cm";
    /** Convenience constants. */
    final private static int _Re = DimensionSpec._Re;
    final private static int _Im = DimensionSpec._Im;

    final private CmExcelDumper _cmExcelDumper;

    public CmArrayDumper(final File outputDir, final boolean clearOutDir) {
	super(outputDir, clearOutDir);
	if (_excelDumper != null) {
	    final boolean clearOutDir2 = false;
	    _cmExcelDumper = new CmExcelDumper(new File(_outputDir, "XL"), clearOutDir2);
	    _excelDumper = _cmExcelDumper;
	} else {
	    _cmExcelDumper = null;
	}
    }

    public void createCmFile(final String variableName, final double[][][][][] a) {
	if (isClosed()) {
	    return;
	}
	final double[][][][] aa = suppress(a);
	if (aa != null) {
	    createCmFile(variableName, aa);
	    return;
	}
	final String s = getCmString(a, 0);
	if (s == null) {
	    return;
	}
	final String variableName2 = variableName + _CmSuffix;
	final String fileName2 = variableName2 + _ListSuffix;
	try (final PrintWriter pw = new PrintWriter(new File(_outputDir, fileName2))) {
	    pw.printf("%s =\n%s", variableName2, s);
	} catch (final IOException e) {
	}
	if (_cmExcelDumper != null) {
	    _cmExcelDumper.createSheetCm(null, variableName, a);
	}
    }

    public void createCmFile(final String variableName, final double[][][][] a) {
	if (isClosed()) {
	    return;
	}
	final double[][][] aa = suppress(a);
	if (aa != null) {
	    createCmFile(variableName, aa);
	    return;
	}
	final String s = getCmString(a, 0);
	if (s == null) {
	    return;
	}
	final String variableName2 = variableName + _CmSuffix;
	final String fileName2 = variableName2 + _ListSuffix;
	try (final PrintWriter pw = new PrintWriter(new File(_outputDir, fileName2))) {
	    pw.printf("%s =\n%s", variableName2, s);
	} catch (final IOException e) {
	}
	if (_cmExcelDumper != null) {
	    _cmExcelDumper.createSheetCm(null, variableName, a);
	}
    }

    public void createCmFile(final String variableName, final double[][][] a) {
	if (isClosed()) {
	    return;
	}
	final double[][] aa = suppress(a);
	if (aa != null) {
	    createCmFile(variableName, aa);
	    return;
	}
	final String s = getCmString(a, 0);
	if (s == null) {
	    return;
	}
	final String variableName2 = variableName + _CmSuffix;
	final String fileName2 = variableName2 + _ListSuffix;
	try (final PrintWriter pw = new PrintWriter(new File(_outputDir, fileName2))) {
	    pw.printf("%s =\n%s", variableName2, s);
	} catch (final IOException e) {
	}
	if (_cmExcelDumper != null) {
	    _cmExcelDumper.createSheetCm(null, variableName, a);
	}
    }

    public void createCmFile(final String variableName, final double[][] a) {
	if (isClosed()) {
	    return;
	}
	final double[] aa = suppress(a);
	if (aa != null) {
	    createCmFile(variableName, aa);
	    return;
	}
	final String s = getCmString(a, 0);
	if (s == null) {
	    return;
	}
	final String variableName2 = variableName + _CmSuffix;
	final String fileName2 = variableName2 + _ListSuffix;
	try (final PrintWriter pw = new PrintWriter(new File(_outputDir, fileName2))) {
	    pw.printf("%s =\n%s", variableName2, s);
	} catch (final IOException e) {
	}
	if (_cmExcelDumper != null) {
	    _cmExcelDumper.createSheetCm(null, variableName, a);
	}
    }

    public void createCmFile(final String variableName, final double[] a) {
	if (isClosed()) {
	    return;
	}
	final String s = getCmString(a, 0);
	if (s == null) {
	    return;
	}
	final String variableName2 = variableName + _CmSuffix;
	final String fileName2 = variableName2 + _ListSuffix;
	try (final PrintWriter pw = new PrintWriter(new File(_outputDir, fileName2))) {
	    pw.printf("%s =\n%s", variableName2, s);
	} catch (final IOException e) {
	}
    }

    private static String getCmString(final double[][][][][][] a, final int nTabs) {
	final double capacityD = getReCapacity(a);
	if (capacityD > Integer.MAX_VALUE / 4) {
	    return null;
	}
	final int capacity = (int) capacityD;
	final StringBuilder sb = new StringBuilder(capacity);
	final String tabString = getTabString(nTabs);
	if (nTabs > 0) {
	    sb.append('\n').append(tabString).append('{');
	} else {
	    sb.append('{');
	}
	final int aLength = a == null ? 0 : a[_Re].length;
	for (int k = 0; k < aLength; ++k) {
	    sb.append(getCmString(new double[][][][][] { a[_Re][k], a[_Im][k] }, nTabs + 1));
	    if (k < aLength - 1) {
		sb.append(',');
	    }
	}
	sb.append('\n').append(tabString).append('}');
	sb.trimToSize();
	return new String(sb);
    }

    private static String getCmString(final double[][][][][] a, final int nTabs) {
	final double capacityD = getReCapacity(a);
	if (capacityD > Integer.MAX_VALUE / 4) {
	    return null;
	}
	final int capacity = (int) capacityD;
	final StringBuilder sb = new StringBuilder(capacity);
	final String tabString = getTabString(nTabs);
	if (nTabs > 0) {
	    sb.append('\n').append(tabString).append('{');
	} else {
	    sb.append('{');
	}
	final int aLength = a == null ? 0 : a[_Re].length;
	for (int k = 0; k < aLength; ++k) {
	    sb.append(getCmString(new double[][][][] { a[_Re][k], a[_Im][k] }, nTabs + 1));
	    if (k < aLength - 1) {
		sb.append(',');
	    }
	}
	sb.append('\n').append(tabString).append('}');
	sb.trimToSize();
	return new String(sb);
    }

    private static String getCmString(final double[][][][] a, final int nTabs) {
	final double capacityD = getReCapacity(a);
	if (capacityD > Integer.MAX_VALUE / 4) {
	    return null;
	}
	final int capacity = (int) capacityD;
	final StringBuilder sb = new StringBuilder(capacity);
	final String tabString = getTabString(nTabs);
	if (nTabs > 0) {
	    sb.append('\n').append(tabString).append('{');
	} else {
	    sb.append('{');
	}
	final int aLength = a == null ? 0 : a[_Re].length;
	for (int k = 0; k < aLength; ++k) {
	    sb.append(getCmString(new double[][][] { a[_Re][k], a[_Im][k] }, nTabs + 1));
	    if (k < aLength - 1) {
		sb.append(',');
	    }
	}
	sb.append('\n').append(tabString).append('}');
	sb.trimToSize();
	return new String(sb);
    }

    private static String getCmString(final double[][][] a, final int nTabs) {
	final double capacityD = getReCapacity(a);
	if (capacityD > Integer.MAX_VALUE / 4) {
	    return null;
	}
	final int capacity = (int) capacityD;
	final StringBuilder sb = new StringBuilder(capacity);
	final String tabString = getTabString(nTabs);
	if (nTabs > 0) {
	    sb.append('\n').append(tabString).append('{');
	} else {
	    sb.append('{');
	}
	final int aLength = a == null ? 0 : a[_Re].length;
	for (int k = 0; k < aLength; ++k) {
	    sb.append(getCmString(new double[][] { a[_Re][k], a[_Im][k] }, nTabs + 1));
	    if (k < aLength - 1) {
		sb.append(',');
	    }
	}
	sb.append('\n').append(tabString).append('}');
	sb.trimToSize();
	return new String(sb);
    }

    private static String getCmString(final double[][] a, final int nTabs) {
	final double capacityD = getReCapacity(a);
	if (capacityD > Integer.MAX_VALUE / 4) {
	    return null;
	}
	final int capacity = (int) capacityD;
	final StringBuilder sb = new StringBuilder(capacity);
	final String tabString = getTabString(nTabs);
	if (nTabs > 0) {
	    sb.append('\n').append(tabString).append('{');
	} else {
	    sb.append('{');
	}
	final int aLength = a == null ? 0 : a[_Re].length;
	for (int k = 0; k < aLength; ++k) {
	    sb.append(getCmString(new double[] { a[_Re][k], a[_Im][k] }, nTabs + 1));
	    if (k < aLength - 1) {
		sb.append(',');
	    }
	}
	sb.append('\n').append(tabString).append('}');
	sb.trimToSize();
	return new String(sb);
    }

    private static String getCmString(final double[] cmplx, final int nTabs) {
	final double capacityD = getReCapacity(cmplx);
	if (capacityD > Integer.MAX_VALUE / 4) {
	    return null;
	}
	final int capacity = (int) capacityD;
	final StringBuilder sb = new StringBuilder(capacity);
	final String tabString = getTabString(nTabs);
	if (nTabs > 0) {
	    sb.append('\n').append(tabString).append('{');
	} else {
	    sb.append('{');
	}
	final String re = getReString(cmplx[_Re]);
	final String im = getReString(cmplx[_Im]);
	return re + " + " + im + "I";
    }

    /** For complex float */
    public void createCmFlFile(final String variableName, final float[][][][][][] a) {
	if (isClosed()) {
	    return;
	}
	final float[][][][][] aa = suppress(a);
	if (aa != null) {
	    createCmFlFile(variableName, aa);
	    return;
	}
	final String s = getCmFlString(a, 0);
	if (s == null) {
	    return;
	}
	final String variableName2 = variableName + _CmSuffix;
	final String fileName2 = variableName2 + _ListSuffix;
	try (final PrintWriter pw = new PrintWriter(new File(_outputDir, fileName2))) {
	    pw.printf("%s =\n%s", variableName2, s);
	} catch (final IOException e) {
	}
	/** For a 6-er, excel doesn't work very well. So we don't dump 6es. */
    }

    public void createCmFlFile(final String variableName, final float[][][][][] a) {
	if (isClosed()) {
	    return;
	}
	final float[][][][] aa = suppress(a);
	if (aa != null) {
	    createCmFlFile(variableName, aa);
	    return;
	}
	final String s = getCmFlString(a, 0);
	if (s == null) {
	    return;
	}
	final String variableName2 = variableName + _CmSuffix;
	final String fileName2 = variableName2 + _ListSuffix;
	try (final PrintWriter pw = new PrintWriter(new File(_outputDir, fileName2))) {
	    pw.printf("%s =\n%s", variableName2, s);
	} catch (final IOException e) {
	}
	if (_cmExcelDumper != null) {
	    _cmExcelDumper.createSheetCmFl(null, variableName, a);
	}
    }

    public void createCmFlFile(final String variableName, final float[][][][] a) {
	if (isClosed()) {
	    return;
	}
	final float[][][] aa = suppress(a);
	if (aa != null) {
	    createCmFlFile(variableName, aa);
	    return;
	}
	final String s = getCmFlString(a, 0);
	if (s == null) {
	    return;
	}
	final String variableName2 = variableName + _CmSuffix;
	final String fileName2 = variableName2 + _ListSuffix;
	try (final PrintWriter pw = new PrintWriter(new File(_outputDir, fileName2))) {
	    pw.printf("%s =\n%s", variableName2, s);
	} catch (final IOException e) {
	}
	if (_cmExcelDumper != null) {
	    _cmExcelDumper.createSheetCmFl(null, variableName, a);
	}
    }

    public void createCmFlFile(final String variableName, final float[][][] a) {
	if (isClosed()) {
	    return;
	}
	final float[][] aa = suppress(a);
	if (aa != null) {
	    createCmFlFile(variableName, aa);
	    return;
	}
	final String s = getCmFlString(a, 0);
	if (s == null) {
	    return;
	}
	final String variableName2 = variableName + _CmSuffix;
	final String fileName2 = variableName2 + _ListSuffix;
	try (final PrintWriter pw = new PrintWriter(new File(_outputDir, fileName2))) {
	    pw.printf("%s =\n%s", variableName2, s);
	} catch (final IOException e) {
	}
	if (_cmExcelDumper != null) {
	    _cmExcelDumper.createSheetCmFl(null, variableName, a);
	}
    }

    public void createCmFlFile(final String variableName, final float[][] a) {
	if (isClosed()) {
	    return;
	}
	final float[] aa = suppress(a);
	if (aa != null) {
	    createCmFlFile(variableName, aa);
	    return;
	}
	final String s = getCmFlString(a, 0);
	if (s == null) {
	    return;
	}
	final String variableName2 = variableName + _CmSuffix;
	final String fileName2 = variableName2 + _ListSuffix;
	try (final PrintWriter pw = new PrintWriter(new File(_outputDir, fileName2))) {
	    pw.printf("%s =\n%s", variableName2, s);
	} catch (final IOException e) {
	}
	if (_cmExcelDumper != null) {
	    _cmExcelDumper.createSheetCmFl(null, variableName, a);
	}
    }

    public void createCmFlFile(final String variableName, final float[] a) {
	if (isClosed()) {
	    return;
	}
	final String s = getCmFlString(a, 0);
	if (s == null) {
	    return;
	}
	final String variableName2 = variableName + _CmSuffix;
	final String fileName2 = variableName2 + _ListSuffix;
	try (final PrintWriter pw = new PrintWriter(new File(_outputDir, fileName2))) {
	    pw.printf("%s =\n%s", variableName2, s);
	} catch (final IOException e) {
	}
    }

    private static String getCmFlString(final float[][][][][][] a, final int nTabs) {
	final double capacityFl = getFlCapacity(a);
	if (capacityFl > Integer.MAX_VALUE / 4) {
	    return null;
	}
	final int capacity = (int) capacityFl;
	final StringBuilder sb = new StringBuilder(capacity);
	final String tabString = getTabString(nTabs);
	if (nTabs > 0) {
	    sb.append('\n').append(tabString).append('{');
	} else {
	    sb.append('{');
	}
	final int aLength = a == null ? 0 : a[_Re].length;
	for (int k = 0; k < aLength; ++k) {
	    sb.append(getCmFlString(new float[][][][][] { a[_Re][k], a[_Im][k] }, nTabs + 1));
	    if (k < aLength - 1) {
		sb.append(',');
	    }
	}
	sb.append('\n').append(tabString).append('}');
	sb.trimToSize();
	return new String(sb);
    }

    private static String getCmFlString(final float[][][][][] a, final int nTabs) {
	final double capacityFl = getFlCapacity(a);
	if (capacityFl > Integer.MAX_VALUE / 4) {
	    return null;
	}
	final int capacity = (int) capacityFl;
	final StringBuilder sb = new StringBuilder(capacity);
	final String tabString = getTabString(nTabs);
	if (nTabs > 0) {
	    sb.append('\n').append(tabString).append('{');
	} else {
	    sb.append('{');
	}
	final int aLength = a == null ? 0 : a[_Re].length;
	for (int k = 0; k < aLength; ++k) {
	    sb.append(getCmFlString(new float[][][][] { a[_Re][k], a[_Im][k] }, nTabs + 1));
	    if (k < aLength - 1) {
		sb.append(',');
	    }
	}
	sb.append('\n').append(tabString).append('}');
	sb.trimToSize();
	return new String(sb);
    }

    private static String getCmFlString(final float[][][][] a, final int nTabs) {
	final double capacityFl = getFlCapacity(a);
	if (capacityFl > Integer.MAX_VALUE / 4) {
	    return null;
	}
	final int capacity = (int) capacityFl;
	final StringBuilder sb = new StringBuilder(capacity);
	final String tabString = getTabString(nTabs);
	if (nTabs > 0) {
	    sb.append('\n').append(tabString).append('{');
	} else {
	    sb.append('{');
	}
	final int aLength = a == null ? 0 : a[_Re].length;
	for (int k = 0; k < aLength; ++k) {
	    sb.append(getCmFlString(new float[][][] { a[_Re][k], a[_Im][k] }, nTabs + 1));
	    if (k < aLength - 1) {
		sb.append(',');
	    }
	}
	sb.append('\n').append(tabString).append('}');
	sb.trimToSize();
	return new String(sb);
    }

    private static String getCmFlString(final float[][][] a, final int nTabs) {
	final double capacityFl = getFlCapacity(a);
	if (capacityFl > Integer.MAX_VALUE / 4) {
	    return null;
	}
	final int capacity = (int) capacityFl;
	final StringBuilder sb = new StringBuilder(capacity);
	final String tabString = getTabString(nTabs);
	if (nTabs > 0) {
	    sb.append('\n').append(tabString).append('{');
	} else {
	    sb.append('{');
	}
	final int aLength = a == null ? 0 : a[_Re].length;
	for (int k = 0; k < aLength; ++k) {
	    sb.append(getCmFlString(new float[][] { a[_Re][k], a[_Im][k] }, nTabs + 1));
	    if (k < aLength - 1) {
		sb.append(',');
	    }
	}
	sb.append('\n').append(tabString).append('}');
	sb.trimToSize();
	return new String(sb);
    }

    private static String getCmFlString(final float[][] a, final int nTabs) {
	final double capacityFl = getFlCapacity(a);
	if (capacityFl > Integer.MAX_VALUE / 4) {
	    return null;
	}
	final int capacity = (int) capacityFl;
	final StringBuilder sb = new StringBuilder(capacity);
	final String tabString = getTabString(nTabs);
	if (nTabs > 0) {
	    sb.append('\n').append(tabString).append('{');
	} else {
	    sb.append('{');
	}
	final int aLength = a == null ? 0 : a[_Re].length;
	for (int k = 0; k < aLength; ++k) {
	    sb.append(getCmFlString(new float[] { a[_Re][k], a[_Im][k] }, nTabs + 1));
	    if (k < aLength - 1) {
		sb.append(',');
	    }
	}
	sb.append('\n').append(tabString).append('}');
	sb.trimToSize();
	return new String(sb);
    }

    private static String getCmFlString(final float[] a, final int nTabs) {
	final double capacityFl = getFlCapacity(a);
	if (capacityFl > Integer.MAX_VALUE / 4) {
	    return null;
	}
	final int capacity = (int) capacityFl;
	final StringBuilder sb = new StringBuilder(capacity);
	final String tabString = getTabString(nTabs);
	if (nTabs > 0) {
	    sb.append('\n').append(tabString).append('{');
	} else {
	    sb.append('{');
	}
	final String re = getReString(a[_Re]);
	final String im = getReString(a[_Im]);
	return re + " + " + im + "I";
    }

    /** End of complex float */

    /** For complex double. */
    public void createCmFile(final String variableName, final double[][][][][][] a) {
	if (isClosed()) {
	    return;
	}
	final double[][][][][] aa = suppress(a);
	if (aa != null) {
	    createCmFile(variableName, aa);
	    return;
	}
	final String s = getCmString(a, 0);
	if (s == null) {
	    return;
	}
	final String variableName2 = variableName + _CmSuffix;
	final String fileName2 = variableName2 + _ListSuffix;
	try (final PrintWriter pw = new PrintWriter(new File(_outputDir, fileName2))) {
	    pw.printf("%s =\n%s", variableName2, s);
	} catch (final IOException e) {
	}
	/** Can't do 6es in excel. */
    }

}
