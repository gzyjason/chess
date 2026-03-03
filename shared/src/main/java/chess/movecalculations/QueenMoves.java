package chess.movecalculations;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;

import static chess.movecalculations.BishopMoves.getChessMoves;

public class QueenMoves {
    public Collection<ChessMove> getMoves(ChessBoard board, ChessPosition myPosition) {
        OverallMoves helper = new OverallMoves();
        int[] rowDirection = {1, 1, 1, -1, -1, -1, 0, 0};
        int[] colDirection = {-1, 0, 1, -1, 0, 1, 1, -1};
        return getChessMoves(board, myPosition, helper, rowDirection, colDirection);
    }
}
