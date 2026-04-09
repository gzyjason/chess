package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class ChessGame {
    private TeamColor movingPiece;
    private ChessBoard board;
    private boolean isGameOver = false;
    public ChessGame() {
        this.movingPiece = TeamColor.WHITE;
        this.board = new ChessBoard( );
        this.board.resetBoard();
    }
    public TeamColor getTeamTurn() {
        return movingPiece;
    }
    public void setTeamTurn(TeamColor team) {
        this.movingPiece = team;
    }

    public enum TeamColor {
        WHITE,
        BLACK
    }

    public Collection<ChessMove> legal(ChessPosition startPosition) {
        ChessBoard board = getBoard();
        ChessPiece piece = board.getPiece(startPosition);

        if (piece == null) {
            return null;
        }
        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> legalMoves = new ArrayList<>();

        for (ChessMove move : possibleMoves) {
            ChessBoard simulation = board.cloneBoard();
            simulation.addPiece(move.endPosition(), piece);
            simulation.addPiece(move.startPosition(), null);
            ChessBoard realBoard = getBoard();
            setBoard(simulation);

            if (!isInCheck(piece.getTeamColor())) {
                legalMoves.add(move);
            }
            setBoard(realBoard);
        }
        return legalMoves;
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessBoard board = getBoard();
        ChessPiece piece = board.getPiece(move.startPosition());
        Collection<ChessMove> legalMoves = legal(move.startPosition());

        if (piece == null) {
            throw new InvalidMoveException("There is no piece at the selected start position.");
        }
        if (piece.getTeamColor() != getTeamTurn()) {
            throw new InvalidMoveException("You cannot move your opponent's pieces.");
        }
        if (legalMoves == null || !legalMoves.contains(move)) {
            throw new InvalidMoveException("That move violates the rules of chess.");
        }

        if (move.promotionPiece() != null) {
            ChessPiece promotedPiece = new ChessPiece(piece.getTeamColor(), move.promotionPiece());
            board.addPiece(move.endPosition(), promotedPiece);
        } else {
            board.addPiece(move.endPosition(), piece);
        }
        board.addPiece(move.startPosition(), null);

        if (getTeamTurn() == TeamColor.WHITE) {
            setTeamTurn(TeamColor.BLACK);
        } else {
            setTeamTurn(TeamColor.WHITE);
        }
    }

    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPos = getKingPosition(teamColor);

        for (int row = 1; row <= 8; row++) {
            for(int col = 1; col <= 8; col++) {
                ChessPosition position =  new ChessPosition(row,col);
                if(doesSquareThreatenKing(position, teamColor, kingPos)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean doesSquareThreatenKing(ChessPosition position, TeamColor teamColor, ChessPosition kingPos) {
        ChessBoard board = getBoard( );
        ChessPiece piece = board.getPiece(position);

        if (piece != null && piece.getTeamColor() !=teamColor) {
            Collection<ChessMove> opponentMoves = piece.pieceMoves(board, position);
            for (ChessMove move : opponentMoves) {
                if (move.endPosition().equals(kingPos)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        return isMate(teamColor);
    }

    private boolean isMate(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition myPosition = new ChessPosition(row, col);
                ChessPiece piece = getBoard().getPiece(myPosition);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> legalMoves = legal(myPosition);
                    if (legalMoves != null && !legalMoves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        return isMate(teamColor);
    }

    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    public ChessBoard getBoard() {
        return this.board;
    }

    private ChessPosition getKingPosition(TeamColor team) {
        ChessBoard board = getBoard();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == team) {
                    return position;
                }
            }
        }
        return null;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public void setGameOver(boolean gameOver){
        this.isGameOver = gameOver;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return movingPiece == chessGame.movingPiece && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movingPiece, board);
    }
}