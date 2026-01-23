package chess.MoveCalculations;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMoves {
    public Collection<ChessMove> getMoves(ChessBoard board, ChessPosition pst) {
        ArrayList<ChessMove> moves = new ArrayList<>();
        ArrayList<ChessPosition> target = new ArrayList<>();

        ChessPiece currentPiece = board.getPiece(pst);
        int direction = (currentPiece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (currentPiece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int fwdRow = pst.getRow() + direction;
        if(fwdRow >= 1 && fwdRow <= 8) {
            ChessPosition fwd = new ChessPosition(fwdRow, pst.getColumn());
            if(board.getPiece(fwd) == null){
                PawnPromo(pst, fwd, moves);

                if(pst.getRow() == startRow){
                    int doubleFwdRow = pst.getRow() + (2 * direction);
                    ChessPosition doubleFwd = new ChessPosition(doubleFwdRow, pst.getColumn());
                    if(board.getPiece(doubleFwd) == null){
                        PawnPromo(pst, doubleFwd, moves);
                    }
                }
            }
        }
        PawnCapture(board, pst, pst.getRow() + direction, pst.getColumn() - 1, moves);
        PawnCapture(board, pst, pst.getRow() + direction, pst.getColumn() + 1, moves);
        return moves;
    }

    private void PawnCapture(ChessBoard board, ChessPosition start, int targetRow, int targetCol, Collection<ChessMove> moves) {
        if (targetRow >= 1 && targetRow <= 8 && targetCol >= 1 && targetCol <= 8){
            ChessPosition targetPst = new ChessPosition(targetRow, targetCol);
            ChessPiece targetPiece = board.getPiece(targetPst);
            ChessPiece movingPiece = board.getPiece(start);

            if (targetPiece != null && targetPiece.getTeamColor() != movingPiece.getTeamColor()) {
                PawnPromo(start, targetPst, moves);
            }
        }
    }

    private void PawnPromo(ChessPosition start, ChessPosition end, Collection<ChessMove> moves){
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
