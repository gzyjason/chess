package chess.MoveCalculations;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.ArrayList;

public class OverallMoves {
    public ArrayList<ChessPosition> BoardEdge(ChessBoard board, ChessPosition myPosition, int[] row, int[]col){

        ArrayList<ChessPosition> moves = new ArrayList<>();

        ChessPiece movingPiece = board.getPiece(myPosition);
        if (movingPiece == null) {
            return moves;
        }

        ChessGame.TeamColor currentColor = movingPiece.getTeamColor();

        for(int j = 0; j < row.length; j++){
            int k = myPosition.getRow() + row[j];
            int l = myPosition.getColumn() + col[j];
            while (k >= 1 && k <= 8 && l >= 1 && l <=8){
                ChessPosition target = new ChessPosition(k, l);
                ChessPiece reached = board.getPiece(target);
                if(reached == null) {
                    moves.add(target);
                }
                else if (reached.getTeamColor() != currentColor) {
                    moves.add(target);
                    break;
                }
                else {
                    break;
                }

                k += row[j];
                l += col[j];
            }
        }

        return moves;
    }
}

