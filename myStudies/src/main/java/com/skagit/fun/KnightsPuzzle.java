package com.skagit.fun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;

import com.skagit.util.CombinatoricTools;

public class KnightsPuzzle {

    public class Board {
	public final int[] _array;
	public final int _whiteToMove;

	public Board(final int[] array, final int whiteToMove) {
	    _array = array;
	    Arrays.sort(_array);
	    _whiteToMove = whiteToMove;
	}

	@Override
	public String toString() {
	    String s = "[";
	    final int nPieces = _array.length;
	    for (int k = 0; k < nPieces; ++k) {
		final int pieceSquareCombination = _array[k];
		final int square = Math.abs(pieceSquareCombination);
		s += k > 0 ? ", " : "";
		s += pieceSquareCombination < 0 ? _blackSquareNames[square] : _whiteSquareNames[square];
	    }
	    s += "]";
	    s += CombinatoricTools.getString(_array);
	    s += _whiteToMove > 0 ? "WhiteToMove" : (_whiteToMove < 0 ? "BlackToMove" : "");
	    return s;
	}
    }

    public static boolean IsOccupied(final int[] array, final int square) {
	if (Arrays.binarySearch(array, square) >= 0) {
	    return true;
	}
	return Arrays.binarySearch(array, square) >= 0;
    }

    final int[] _initialArray, _targetArray;
    final int[][] _legalMoves;
    final String[] _whiteSquareNames;
    final String[] _blackSquareNames;
    final ArrayList<int[]> _stack;
    final TreeSet<Board> _visited;

    public KnightsPuzzle(final int[] initialArray, final int[][] legalMoves, final String[] squareNames) {
	_initialArray = initialArray;
	_targetArray = _initialArray.clone();
	for (int k = 0; k < _targetArray.length; ++k) {
	    _targetArray[k] = -_targetArray[k];
	}
	Arrays.sort(_initialArray);
	Arrays.sort(_targetArray);
	_legalMoves = legalMoves;
	/** Validate _legalMoves. */
	final int nSquares = _legalMoves.length;
	for (int i = 0; i < nSquares; ++i) {
	    for (int j = 0; j < nSquares; ++j) {
		if (canMove(i, j) != canMove(j, i)) {
		    System.exit(33);
		}
	    }
	}
	_stack = new ArrayList<>();
	_visited = new TreeSet<>(new Comparator<>() {

	    @Override
	    public int compare(final Board b0, final Board b1) {
		final int compareValue = CombinatoricTools._ByAllInOrder.compare(b0._array, b1._array);
		if (compareValue != 0) {
		    return compareValue;
		}
		return b0._whiteToMove > b1._whiteToMove ? -1 : (b0._whiteToMove < b1._whiteToMove ? -1 : 0);
	    }
	});
	_whiteSquareNames = new String[nSquares];
	_blackSquareNames = new String[nSquares];
	for (int k = 0; k < nSquares; ++k) {
	    final String squareName = squareNames[k];
	    _whiteSquareNames[k] = squareName.toUpperCase();
	    _blackSquareNames[k] = squareName.toLowerCase();
	}
    }

    private boolean canMove(final int i, final int j) {
	return Arrays.binarySearch(_legalMoves[i], j) >= 0;
    }

    public Board[] getCanMoveTos(final int whiteToMove) {
	final int nPieces = _initialArray.length;
	final int nInStack = _stack.size();
	final int[] array0 = _stack.get(nInStack - 1);
	final ArrayList<Board> boardList = new ArrayList<>();
	for (int i = 0; i < nPieces; ++i) {
	    final int piece = array0[i];
	    if (whiteToMove > 0 && piece < 0) {
		continue;
	    }
	    if (whiteToMove < 0 && piece > 0) {
		continue;
	    }
	    final int absPiece = Math.abs(piece);
	    final int[] row = _legalMoves[absPiece];
	    final int nInRow = row.length;
	    for (int j = 0; j < nInRow; ++j) {
		final int square = row[j];
		if (!IsOccupied(array0, square)) {
		    final int[] array = array0.clone();
		    array[i] = piece > 0 ? square : -square;
		    final Board board = new Board(array, -whiteToMove);
		    if (!_visited.contains(board)) {
			boardList.add(board);
		    }
		}
	    }
	}
	return boardList.toArray(new Board[boardList.size()]);

    }

    public boolean search(final int whiteToMove) {
	final int nInStack = _stack.size();
	final Board[] canMoveTos = getCanMoveTos(whiteToMove);
	final int nCanMoveTos = canMoveTos.length;
	if (nCanMoveTos > 0) {
	    System.out.printf("\nnInStack=%d, nCanMoveTos=%d, nVisited=%d", nInStack, nCanMoveTos, _visited.size());
	    for (int k = 0; k < nCanMoveTos; ++k) {
		final Board board = canMoveTos[k];
		final int[] array = board._array;
		_stack.add(array);
		_visited.add(board);
		if (Arrays.equals(array, _targetArray)) {
		    return true;
		}
		if (search(-whiteToMove)) {
		    return true;
		}
		_stack.remove(nInStack);
	    }
	}
	return false;
    }

    public boolean search(final boolean mustAlternate) {
	for (int iPass = 0; iPass < 2; ++iPass) {
	    _stack.clear();
	    _visited.clear();
	    final int whiteToMove = iPass == 0 ? (mustAlternate ? 1 : 0) : -1;
	    _stack.add(_initialArray);
	    _visited.add(new Board(_initialArray, whiteToMove));
	    if (search(whiteToMove)) {
		return true;
	    }
	    if (!mustAlternate) {
		break;
	    }
	}
	return false;
    }

    public static void main(final String[] args) {
	{
	    final int[] initialArray = new int[] { 1, 5, -7, -9 };
	    final int[][] legalMoves = new int[][] { new int[] { // 0
		    }, new int[] { // 1
			    5 },
		    new int[] { // 2
			    6, 7, 9 },
		    new int[] { // 3
			    8, 10 },
		    new int[] { // 4
			    10 },
		    new int[] { // 5
			    1, 7 },
		    new int[] { // 6
			    2, 8 },
		    new int[] { // 7
			    2, 5 },
		    new int[] { // 8
			    3, 6 },
		    new int[] { // 9
			    2 },
		    new int[] { // 10
			    3, 4 } };
	    final String[] squareNames = new String[] { "", //
		    "A1", //
		    "B1", "B2", //
		    "C1", "C2", "C3", //
		    "D1", "D2", "D3", "D4" };
	    final KnightsPuzzle knightsPuzzle = new KnightsPuzzle(initialArray, legalMoves, squareNames);
	    if (knightsPuzzle.search(/* mustAlternate= */false)) {
		System.out.printf("Success");
	    }
	}
	if (true) {
	} else {
	    final int[] initialArray = new int[] { 1, -7 };
	    final int[][] legalMoves = new int[][] { new int[] { // 0
		    }, new int[] { // 1
			    2, 6 },
		    new int[] { // 2
			    1, 7 },
		    new int[] { // 3
		    }, new int[] { // 4
		    }, new int[] { // 5
		    }, new int[] { // 6
			    1, 7 },
		    new int[] { // 7
			    2, 6 } };
	    final String[] squareNames = new String[] { "", //
		    "A1", //
		    "B1", "B2", "B3", "B4", "B5", //
		    "C1" };
	    final KnightsPuzzle knightsPuzzle = new KnightsPuzzle(initialArray, legalMoves, squareNames);
	    knightsPuzzle.search(/* mustAlternate= */true);
	    System.exit(33);
	}
    }

}
