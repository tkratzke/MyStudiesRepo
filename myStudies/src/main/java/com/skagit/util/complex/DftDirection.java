package com.skagit.util.complex;

public enum DftDirection {
	PHYSICAL_TO_FOURIER(-1), FOURIER_TO_PHYSICAL(
			-PHYSICAL_TO_FOURIER._multiplier);
	final public int _multiplier;

	private DftDirection(final int multiplier) {
		_multiplier = multiplier;
	}

	public int getMultiplier() {
		return _multiplier;
	}

	public DftDirection getOpposite() {
		return this == PHYSICAL_TO_FOURIER ? FOURIER_TO_PHYSICAL
				: PHYSICAL_TO_FOURIER;
	}
}
