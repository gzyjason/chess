package chess.movecalculations;

import chess.*;

import java.util.Collection;

public class KnightMoves {
    public Collection<ChessMove> getMoves(ChessBoard board, ChessPosition myPosition) {
        int[] rowDirection = {2, 2, 1, 1, -1, -1, -2, -2};
        int[] colDirection = {1, -1, 2, -2, 2, -2, 1, -1};
        return PieceMoveUtils.getLeaperMoves(board, myPosition, rowDirection, colDirection);
    }
}