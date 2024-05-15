package com.skagit.util;

abstract public class InterpolatedFunction implements Integrator.IntegrableFunction {
    private double[][] _cache;

    public abstract double coreFunction(double x, Object util);

    private double interpolate(final int tooLow, final double x) {
	final double w = (x - _cache[tooLow][0]) / (_cache[tooLow + 1][0] - _cache[tooLow][0]);
	return w * _cache[tooLow + 1][1] + (1 - w) * _cache[tooLow][1];
    }

    private boolean interpolationIsSufficient(final int tooLow, final double x) {
	final int lengthOfCache = _cache == null ? 0 : _cache.length;
	if (lengthOfCache < 8 || tooLow < 0 || tooLow >= lengthOfCache - 1) {
	    return false;
	} else if (_cache[tooLow][1] >= 1.001 * _cache[tooLow + 1][1]) {
	    return false;
	} else {
	    return true;
	}
    }

    public double functionValue(final double x, final Object util) {
	final int oldLength = _cache == null ? 0 : _cache.length;
	int tooLow = -1;
	int tooHigh = oldLength;
	// Find tooLow or an exact match.
	while (tooLow < tooHigh - 1) {
	    final int pairIndex = (tooLow + tooHigh) / 2;
	    final double[] pair = _cache[pairIndex];
	    if (pair[0] == x) {
		return pair[1];
	    } else if (pair[0] < x) {
		tooLow = pairIndex;
	    } else {
		tooHigh = pairIndex;
	    }
	}
	// No exact match. If interpolation is legal, interpolate.
	if (interpolationIsSufficient(tooLow, x)) {
	    return interpolate(tooLow, x);
	}
	// Compute, ...
	final double value = coreFunction(x, util);
	// ... store the result in the cache, ...
	final double[] cacheEntry = new double[] { x, value };
	if (oldLength == 0) {
	    _cache = new double[][] { cacheEntry };
	} else {
	    final double[][] newCache = new double[oldLength + 1][];
	    boolean havePutAwayNewOne = false;
	    for (int i = oldLength - 1; i >= 0; --i) {
		if (havePutAwayNewOne) {
		    newCache[i] = _cache[i];
		} else {
		    final double[] incumbentCacheEntry = _cache[i];
		    if (incumbentCacheEntry[0] > cacheEntry[0]) {
			newCache[i + 1] = incumbentCacheEntry;
		    } else {
			newCache[i + 1] = cacheEntry;
			newCache[i] = incumbentCacheEntry;
			havePutAwayNewOne = true;
		    }
		}
	    }
	    if (!havePutAwayNewOne) {
		newCache[0] = cacheEntry;
	    }
	    _cache = newCache;
	}
	// and return what you computed.
	return value;
    }

    public static double simpleInterpolateFromPairs(final double[][] pairs, final double x, final Object util) {
	final int n = pairs.length;
	final int glbIndex = MyStudiesNumUtils.getGlbIndex(pairs, new double[] { x }, MyStudiesNumUtils._ByFirstOnly);
	int lowIndex = Math.max(0, glbIndex);
	int highIndex = lowIndex + 1;
	boolean useLow = highIndex == n || ((x - pairs[lowIndex][0]) <= (pairs[highIndex][0] - x));
	double[] pair1 = null;
	while (true) {
	    final double[] pair = pairs[useLow ? lowIndex : highIndex];
	    if (!Double.isNaN(pair[1])) {
		if (pair[0] == x) {
		    return pair[1];
		}
		if (pair1 == null) {
		    pair1 = pair;
		} else {
		    if (pair[0] == pair1[0]) {
			return (pair[1] + pair1[1]) / 2.0;
		    }
		    return pair1[1] + (x - pair1[0]) / (pair[0] - pair1[0]) * (pair[1] - pair1[1]);
		}
	    }
	    /** Look for another one. */
	    if (useLow) {
		--lowIndex;
		/** Use low for the next one only if you can't use high. */
		useLow = highIndex == n;
	    } else {
		++highIndex;
		/** Use low for the next one unless you can't. */
		useLow = lowIndex >= 0;
	    }
	}
    }
}
