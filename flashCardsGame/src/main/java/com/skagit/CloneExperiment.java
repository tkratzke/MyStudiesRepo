package com.skagit;

import java.util.HashMap;

public class CloneExperiment implements Cloneable {

	final int[] _intArray;
	final HashMap<Integer, int[]> _hashMap;
	public CloneExperiment() {
		_intArray = new int[]{0};
		_hashMap = new HashMap<>();
		_hashMap.put(0, new int[]{1});
	}

	/** super.clone() does only a shallow copy. */
	@Override
	protected CloneExperiment clone() {
		try {
			return (CloneExperiment) super.clone();
		} catch (final CloneNotSupportedException e) {
		}
		return null;
	}

	public static void main(final String[] args) {
		final CloneExperiment cloneExperiment0 = new CloneExperiment();
		final CloneExperiment cloneExperiment1 = cloneExperiment0.clone();
		cloneExperiment1._hashMap.get(0)[0] = 4;
		@SuppressWarnings("unused")
		final int x = 0;
	}

}
