package chess;

import chess.MoveCalculations.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;


    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {

        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        if (getPieceType() == PieceType.BISHOP) {
            BishopMoves calc = new BishopMoves();
            moves.addAll(calc.getMoves(board, myPosition));
        }

        if (getPieceType() == PieceType.QUEEN) {
            QueenMoves calc = new QueenMoves();
            moves.addAll(calc.getMoves(board, myPosition));
        }

        if (getPieceType() == PieceType.ROOK) {
            RookMoves calc = new RookMoves();
            moves.addAll(calc.getMoves(board, myPosition));
        }

        if (getPieceType() == PieceType.KING){
            KingMoves calc = new KingMoves();
            moves.addAll(calc.getMoves(board, myPosition));
        }

        if (getPieceType() == PieceType.KNIGHT){
            KnightMoves calc = new KnightMoves();
            moves.addAll(calc.getMoves(board, myPosition));
        }
        return moves;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}
