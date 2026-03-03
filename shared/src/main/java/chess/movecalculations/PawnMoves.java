package chess.movecalculations;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMoves {
    public Collection<ChessMove> getMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> moves = new ArrayList<>();

        ChessPiece currentPiece = board.getPiece(myPosition);
        int direction = (currentPiece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (currentPiece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int fwdRow = myPosition.getRow() + direction;
        if(fwdRow >= 1 && fwdRow <= 8) {
            ChessPosition fwd = new ChessPosition(fwdRow, myPosition.getColumn());
            if(board.getPiece(fwd) == null){
                pawnPromo(myPosition, fwd, moves);

                if(myPosition.getRow() == startRow){
                    int doubleFwdRow = myPosition.getRow() + (2 * direction);
                    ChessPosition doubleFwd = new ChessPosition(doubleFwdRow, myPosition.getColumn());
                    if(board.getPiece(doubleFwd) == null){
                        pawnPromo(myPosition, doubleFwd, moves);
                    }
                }
            }
        }
        pawnCapture(board, myPosition, myPosition.getRow() + direction, myPosition.getColumn() - 1, moves);
        pawnCapture(board, myPosition, myPosition.getRow() + direction, myPosition.getColumn() + 1, moves);
        return moves;
    }

    private void pawnCapture(ChessBoard board, ChessPosition start, int targetRow, int targetCol, Collection<ChessMove> moves) {
        if (targetRow >= 1 && targetRow <= 8 && targetCol >= 1 && targetCol <= 8){
            ChessPosition targetPst = new ChessPosition(targetRow, targetCol);
            ChessPiece targetPiece = board.getPiece(targetPst);
            ChessPiece movingPiece = board.getPiece(start);

            if (targetPiece != null && targetPiece.getTeamColor() != movingPiece.getTeamColor()) {
                pawnPromo(start, targetPst, moves);
            }
        }
    }

    private void pawnPromo(ChessPosition start, ChessPosition end, Collection<ChessMove> moves){
        int endingRow = end.getRow();
        if (endingRow == 8 || endingRow == 1){
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.QUEEN));
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.ROOK));
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.BISHOP));
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.KNIGHT));
        } else {
            moves.add(new ChessMove(start, end, null));
        }
    }
}
