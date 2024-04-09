package com.skagit.fun;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Stack;
import java.util.StringTokenizer;

import com.skagit.util.StringUtilities;
import com.skagit.util.TimeUtilities;

public class RpnWithTape {

    final private static int _CaptionLength = 45;

    private static void printStack(final Stack<Double> stack) {
	final int nInStack = stack.size();
	for (int k = 0; k < nInStack; ++k) {
	    System.out.printf(k == 0 ? "\nStack, from top to bottom: " : ", ");
	    System.out.printf("%.3f", stack.get(nInStack - 1 - k));
	}
    }

    public static void main(final String[] args) {
	int iArg = 0;
	final String filePath = args[iArg++];
	final File file = new File(filePath);
	final Stack<Double> stack = new Stack<>();
	boolean havePrintLine = false;
	try (Scanner scanner = new Scanner(file)) {
	    while (scanner.hasNextLine()) {
		final String line = scanner.nextLine();
		if (line.startsWith("!!")) {
		    continue;
		}
		final String lineLc = line.toLowerCase();
		if (lineLc.equals("stop")) {
		    break;
		}
		final StringTokenizer st = new StringTokenizer(line, "\t ");
		if (lineLc.startsWith("print")) {
		    final String token0Lc = st.nextToken().toLowerCase();
		    final boolean printPop = token0Lc.contains("pop");
		    final boolean printSum = token0Lc.contains("sum");
		    final double stackSum;
		    if (printSum) {
			/** Compute the sum of the stack. */
			final int nInStack = stack.size();
			final Iterator<Double> it = stack.iterator();
			double stackSum0 = 0d;
			for (int k = 0; k < nInStack; ++k) {
			    stackSum0 += it.next();
			}
			stackSum = stackSum0;
		    } else {
			stackSum = Double.NaN;
		    }
		    final double topOfStack;
		    if (printPop) {
			topOfStack = stack.pop();
		    } else {
			topOfStack = stack.peek();
		    }
		    /** Get the caption from line. */
		    final String caption0;
		    if (st.hasMoreTokens()) {
			final int printDirectiveLength = token0Lc.length();
			String caption1 = line.substring(printDirectiveLength).trim();
			final int bangBangIndex = caption1.indexOf("!!");
			if (bangBangIndex >= 0) {
			    caption1 = caption1.substring(0, bangBangIndex);
			}
			caption0 = caption1.trim();
		    } else {
			caption0 = "";
		    }
		    final String caption;
		    if (caption0.length() > _CaptionLength) {
			caption = caption0.substring(0, _CaptionLength);
		    } else {
			caption = StringUtilities.padRight(caption0, _CaptionLength);
		    }
		    /**
		     * We print this out assuming that topOfStack is really a numberOfMinutes, and
		     * we're interested in the mm:ss format. This is just an example.
		     */
		    final int nSeconds = (int) Math.round(topOfStack * 60d);
		    System.out.printf("%s%s %s", havePrintLine ? "\n" : "", caption,
			    TimeUtilities.durationInSecondsToMinsSecsString(nSeconds, /* nDigitsForMinutes= */2));
		    havePrintLine = true;
		    if (printSum) {
			final int stackSumSeconds = (int) Math.round(stackSum * 60d);
			System.out.printf(", Cum[%s].", TimeUtilities.durationInSecondsToMinsSecsString(stackSumSeconds,
				/* nDigitsForMinutes= */3));
		    } else {
			System.out.printf(".");
		    }
		    continue;
		}
		/** Not a print. */
		while (st.hasMoreTokens()) {
		    final String token = st.nextToken();
		    if (token.startsWith("!!")) {
			break;
		    }
		    try {
			stack.add(Double.parseDouble(token));
			continue;
		    } catch (final NumberFormatException e) {
		    }
		    /**
		     * <pre>
		     *  Not a number. It must be one of:
		     *  1. +, -, *, or /
		     *  2. U-, U*, or U/
		     *  Otherwise, we ignore it.
		     * </pre>
		     */
		    final int len = token.length();
		    if (len == 0) {
			continue;
		    }
		    final char char0 = Character.toUpperCase(token.charAt(0));
		    if (char0 == 'U') {
			if (len != 2) {
			    continue;
			}
			final char op = token.charAt(1);
			final double topOfStack = stack.pop();
			if (op == '-') {
			    stack.push(-topOfStack);
			} else if (op == '*') {
			    stack.push(topOfStack * topOfStack);
			} else if (op == '/' && topOfStack != 0d) {
			    stack.push(1d / topOfStack);
			} else {
			    stack.push(topOfStack);
			}
		    } else if (len != 1 || stack.size() < 2) {
			continue;
		    } else {
			final char op = char0;
			final double topOfStack = stack.pop();
			final double nextOneDown = stack.pop();
			if (op == '+') {
			    stack.push(nextOneDown + topOfStack);
			} else if (op == '-') {
			    stack.push(nextOneDown - topOfStack);
			} else if (op == '*') {
			    stack.push(nextOneDown * topOfStack);
			} else if (op == '/' && topOfStack != 0d) {
			    stack.push(nextOneDown / topOfStack);
			} else {
			    stack.push(nextOneDown);
			    stack.push(topOfStack);
			}
		    }
		}
	    }
	} catch (final IOException e) {
	}
	System.out.printf("\n");
	printStack(stack);
    }
}
