package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor movingPiece;
    private ChessBoard board;
    public ChessGame() {
        this.movingPiece = TeamColor.WHITE;
        this.board = new ChessBoard();
        this.board.resetBoard();
    }


    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return movingPiece;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.movingPiece = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessBoard board = getBoard();
        ChessPiece piece = board.getPiece(startPosition);

        if (piece == null){
            return null;
        }
        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> legalMoves = new ArrayList<>();

        for (ChessMove move : possibleMoves){
            ChessBoard simulation = board.cloneBoard();
            simulation.addPiece(move.getEndPosition(), piece);
            simulation.addPiece(move.getStartPosition(), null);
            ChessBoard realBoard = getBoard();
            setBoard(simulation);

            if (!isInCheck(piece.getTeamColor())){
                legalMoves.add(move);
            }
            setBoard(realBoard);
        }
        return legalMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessBoard board = getBoard();
        ChessPiece piece = board.getPiece(move.getStartPosition());
        Collection<ChessMove> legalMoves = validMoves(move.getStartPosition());
        if (piece == null || piece.getTeamColor() != getTeamTurn()){
            throw new InvalidMoveException();
        }
        if (legalMoves == null || !legalMoves.contains(move)) {
            throw new InvalidMoveException();
        }
        getBoard().addPiece(move.getEndPosition(), piece);
        getBoard().addPiece(move.getStartPosition(), null);

        if (getTeamTurn() == TeamColor.WHITE){
            setTeamTurn(TeamColor.BLACK);
        } else {
            setTeamTurn(TeamColor.WHITE);
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessBoard board = getBoard();
        ChessPiece opponentPiece;
        for (int r = 1; r <= 8; r++ ) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition position = new ChessPosition(r, c);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() != teamColor){
                    opponentPiece = piece;
                    Collection<ChessMove> opponentMoves = opponentPiece.pieceMoves(board, position);
                    for (ChessMove move : opponentMoves){
                        ChessPosition endPST = move.getEndPosition();
                        if (endPST.equals(getKingPosition(teamColor))){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)){
            return false;
        }
        for (int r = 1; r <= 8; r++){
            for (int c = 1; c <= 8; c++){
                ChessPosition pst = new ChessPosition(r, c);
                ChessPiece piece = getBoard().getPiece(pst);
                if (piece != null && piece.getTeamColor() == teamColor){
                    Collection<ChessMove> legalMoves = validMoves(pst);
                    if (legalMoves != null && !legalMoves.isEmpty()){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)){
            return false;
        }
        for (int r = 1; r <= 8; r++){
            for (int c = 1; c <= 8; c++){
                ChessPosition pst = new ChessPosition(r, c);
                ChessPiece piece = getBoard().getPiece(pst);
                if (piece != null && piece.getTeamColor() == teamColor){
                    Collection<ChessMove> legalMoves = validMoves(pst);
                    if (legalMoves != null && !legalMoves.isEmpty()){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }

    private ChessPosition getKingPosition(TeamColor team){
        ChessBoard board = getBoard();
        for (int r = 1; r <= 8; r++ ){
            for (int c = 1; c <=8; c++){
                ChessPosition position = new ChessPosition(r, c);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == team){
                    return position;
                }
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return movingPiece == chessGame.movingPiece;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(movingPiece);
    }
}
