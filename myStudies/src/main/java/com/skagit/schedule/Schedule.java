package com.skagit.schedule;

public class Schedule {

	public static int ComputeN(final double targetRatio, final int currentSum,
			final int currentRow, final int baseRow) {
		/**
		 * <pre>
		 * Computing streak-needed, starting today:
		 * targetRatio = (oldSum + n) / (currentRow - baseRow + n)
		 * targetRatio(currentRow - baseRow + n) = oldSum + n
		 * n * targetRatio - n = oldSum - targetRatio(currentRow - baseRow)
		 * n * (targetRatio - 1) = oldSum - targetRatio(currentRow - baseRow)
		 * n = (oldSum - targetRatio(currentRow - baseRow)) / (targetRatio - 1)
		 * </pre>
		 */

		/**
		 * <pre>
		 * Computing ratioNeeded, starting from today:
		 * r = (targetN - oldSum) / (ttlRows - nOldRows)
		 * </pre>
		 */

		return 0;
	}

}
