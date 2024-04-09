package com.skagit.util;

import java.util.Arrays;
import java.util.Comparator;

public class NumericalUtils {

    /**
     * Examples, and comparison between binarySearch and glbIndex:
     *
     * <pre>
     * int[] array = new int[] { 1, 1, 3, 3, 5, 5 };
     * 0: binarySearch[-1], glbIndex[-1].
     * 1: binarySearch[1], glbIndex[1].
     * 2: binarySearch[-3], glbIndex[1].
     * 3: binarySearch[2], glbIndex[3].
     * 4: binarySearch[-5], glbIndex[3].
     * 5: binarySearch[4], glbIndex[5].
     * 6: binarySearch[-7], glbIndex[5].
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public static <T extends Object> int getGlbIndex(final T[] tArray, final T t, final Comparator<T> comparator) {
	final int j = Arrays.binarySearch(tArray, t, comparator);
	if (j < 0) {
	    return -j - 2;
	}
	/**
	 * We now know that tArray[j] <= t. Increase j until array[j] > t, and return j
	 * - 1.
	 */
	final int n = tArray.length;
	int idx = j;
	for (idx = j + 1; idx < n; ++idx) {
	    int compareValue;
	    if (comparator != null) {
		compareValue = comparator.compare(tArray[idx], t);
	    } else {
		compareValue = ((Comparable<T>) tArray[idx]).compareTo(t);
	    }
	    if (compareValue > 0) {
		return idx - 1;
	    }
	}
	return n - 1;
    }

    public static Comparator<double[]> _ByFirstOnly = new Comparator<>() {
	@Override
	public int compare(final double[] xy0, final double[] xy1) {
	    if ((xy0 == null) && (xy1 == null)) {
		return 0;
	    }
	    if ((xy0 == null) || (xy1 == null)) {
		return xy0 == null ? -1 : 1;
	    }
	    final double compareValue = xy0[0] - xy1[0];
	    return compareValue < 0 ? -1 : (compareValue > 0 ? 1 : 0);
	}
    };

    public static boolean relativeErrorIsSmall(final double d0, final double d1, final double relativeErrorThreshold) {
	final double d0Abs = Math.abs(d0);
	final double d1Abs = Math.abs(d1);
	/** If either is very close to 0, both have to be. */
	if (Math.min(d0Abs, d1Abs) < relativeErrorThreshold) {
	    return Math.max(d0Abs, d1Abs) < relativeErrorThreshold;
	}
	if (d0 > 0d != d1 > 0d) {
	    return false;
	}
	final double relativeError = Math.abs(d0Abs - d1Abs) / Math.max(d0Abs, d1Abs);
	return relativeError <= relativeErrorThreshold;
    }

}
