package chess.MoveCalculations;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;

public class BishopMoves {
    public Collection<ChessMove> getMoves(ChessBoard board, ChessPosition myPosition) {
        OverallMoves helper = new OverallMoves();

        int[] rowDirection = {1, 1, -1, -1};
        int[] colDirection = {1, -1, 1, -1};

        return getChessMoves(board, myPosition, helper, rowDirection, colDirection);
    }

    static Collection<ChessMove> getChessMoves(ChessBoard board, ChessPosition pst, OverallMoves helper, int[] rowDir, int[] colDir) {
        ArrayList<ChessPosition> target = helper.BoardEdge(board, pst, rowDir, colDir);
        ArrayList<ChessMove> legal = new ArrayList<>();
        for (ChessPosition end : target) {
            legal.add(new ChessMove(pst, end, null));
        }
        return legal;
    }
}
