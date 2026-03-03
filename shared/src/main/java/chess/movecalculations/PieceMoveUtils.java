package chess.movecalculations;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public class PieceMoveUtils {

    public static Collection<ChessMove> getLeaperMoves(ChessBoard board, ChessPosition myPosition, int[] rowOffsets, int[] colOffsets){
        ArrayList<ChessMove> moves = new ArrayList<>();
        ChessPiece movingPiece = board.getPiece(myPosition);

        for (int j = 0; j < rowOffsets.length; j++){
            int targetRow = myPosition.getRow() + rowOffsets[j];
            int targetCol = myPosition.getColumn() + colOffsets[j];

            if (targetRow >= 1 && targetRow <= 8 && targetCol >= 1 && targetCol <= 8) {
                ChessPosition target = new ChessPosition(targetRow, targetCol);
                ChessPiece reached = board.getPiece(target);
                if (reached == null || reached.getTeamColor() != movingPiece.getTeamColor()){
                    moves.add(new ChessMove(myPosition, target, null));
                }
            }
        }
        return moves;
    }
}
