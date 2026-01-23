package chess.MoveCalculations;


import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public class KingMoves {
    public Collection<ChessMove> getMoves(ChessBoard board, ChessPosition pst) {
        int[] rowDir = {1, 1, 1, 0, 0, -1, -1, -1};
        int[] colDir = {1, 0, -1, 1, -1, 1, 0, -1};
        ArrayList<ChessPosition> target = OneStep(board, pst, rowDir, colDir);
        ArrayList<ChessMove> legal = new ArrayList<>();
        for (ChessPosition end : target) {
            legal.add(new ChessMove(pst, end, null));
        }
        return legal;

    }
    public ArrayList<ChessPosition> OneStep(ChessBoard board, ChessPosition myPosition, int[] row, int[] col) {
        ArrayList<ChessPosition> moves = new ArrayList<>();
        ChessPiece movingPiece = board.getPiece(myPosition);
        ChessGame.TeamColor currentColor = movingPiece.getTeamColor();

        for (int j = 0; j < row.length; j++){
            int k = myPosition.getRow() + row[j];
            int l = myPosition.getColumn() + col[j];

            if (k >= 1 && k <= 8 && l >= 1 && l <= 8) {
                ChessPosition target = new ChessPosition(k, l);
                ChessPiece reached = board.getPiece(target);
                if (reached == null || reached.getTeamColor() != currentColor){
                    moves.add(target);
                }
            }
        }
        return moves;
    }

}
