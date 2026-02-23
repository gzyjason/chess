package chess.MoveCalculations;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;

import static chess.MoveCalculations.BishopMoves.getChessMoves;

public class RookMoves {
    public Collection<ChessMove> getMoves(ChessBoard board, ChessPosition myPosition) {
        OverallMoves helper = new OverallMoves();
        int[] rowDirection = {-1, 1, 0, 0};
        int[] colDirection = {0, 0, -1, 1};

        return getChessMoves(board, myPosition, helper, rowDirection, colDirection);
    }

}
