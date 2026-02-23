package chess.MoveCalculations;


import chess.*;

import java.util.Collection;

public class KingMoves {
    public Collection<ChessMove> getMoves(ChessBoard board, ChessPosition myPosition) {
        int[] rowDirection = {1, 1, 1, 0, 0, -1, -1, -1};
        int[] colDirection = {1, 0, -1, 1, -1, 1, 0, -1};
        return PieceMoveUtils.getLeaperMoves(board, myPosition, rowDirection, colDirection);

    }

}
