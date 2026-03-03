package chess;

import org.jetbrains.annotations.NotNull;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public record ChessMove(ChessPosition startPosition, ChessPosition endPosition, ChessPiece.PieceType promotionPiece) {

    public ChessPosition getStartPosition() {
        return startPosition;
    }

    public ChessPosition getEndPosition() {
        return endPosition;
    }

    public ChessPiece.PieceType getPromotionPiece() {
        return promotionPiece;
    }

    /**
     * @return ChessPosition of starting location
     */

    @Override
    @NotNull
    public ChessPosition startPosition() {

        return startPosition;
    }

    /**
     * @return ChessPosition of ending location
     */
    @Override
    @NotNull
    public ChessPosition endPosition() {
        return endPosition;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    @Override
    public ChessPiece.PieceType promotionPiece( ) {
        return promotionPiece;
    }

    @NotNull
    @Override
    public String toString( ) {
        return String.format( "%s%s", startPosition, endPosition);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ChessMove chessMove = (ChessMove) o;

        if (!startPosition.equals(chessMove.startPosition)) {
            return false;
        }
        if (!endPosition.equals(chessMove.endPosition)) {
            return false;
        }
        return promotionPiece == chessMove.promotionPiece;   // null-safe because enum or null
    }

}
