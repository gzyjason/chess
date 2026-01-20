package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;
    private int[] temp = new int[2];
    private ArrayList<ChessPosition> result = new ArrayList<>();

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
        ChessPiece piece = board.getPiece(myPosition);
        if (piece.getPieceType() == PieceType.BISHOP) {

        }
        return List.of();
    }

    public void BoardEdge(ChessBoard board, ChessPosition myPosition, int[] row, int[]col){
        for(int i = 0; i < row.length; i++){
            temp[1] = myPosition.getColumn() + col[i];
            temp[0] = myPosition.getRow() + row[i];
            while (temp[0] >= 1 && temp[0] <= 8 && temp[1] >= 1 && temp[1] <=8){
                ChessPosition tempPosition = new ChessPosition(temp[0], temp[1]);
                if(board.getPiece(tempPosition).getTeamColor() != board.getPiece(myPosition).getTeamColor()){
                     result.add(tempPosition);
                     break;
                }
                result.add(tempPosition);
                temp[0] = temp[0] + row[i];
                temp[1] = temp[1] + col[i];
            }
        }
    }
}
