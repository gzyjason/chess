package chess.MoveCalculations;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;

public class BishopMoves {
    public Collection<ChessMove> getMoves(ChessBoard board, ChessPosition pst) {
        OverallMoves helper = new OverallMoves();

        int[] rowDir = {1, 1, -1, -1};
        int[] colDir = {1, -1, 1, -1};

        ArrayList<ChessPosition> target = helper.BoardEdge(board, pst, rowDir, colDir);
        ArrayList<ChessMove> legal = new ArrayList<>();
        for (ChessPosition end : target) {
            legal.add(new ChessMove(pst, end, null));
        }
        return legal;
    }
}
