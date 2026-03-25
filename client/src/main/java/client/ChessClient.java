package client;
import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;
import ui.EscapeSequences;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.util.List;

public class ChessClient {
    ServerFacade serverFacade;
    String authToken;
    private List<GameData> cachedGames;

    public ChessClient (String serverURL){
        this.serverFacade = new ServerFacade(serverURL);
    }

    public enum State {
        SIGNED_OUT,
        SIGNED_IN
    }

    private State state = State.SIGNED_OUT;

    public void run() {
        Scanner scanner = new Scanner(System.in);
        String input = "";
        while (!input.equalsIgnoreCase("quit")) {
            System.out.print("[" + state + "] >>> ");
            input = scanner.nextLine();
            String[] splitInput = input.split("\\s+");
            try {
                eval(splitInput);
            } catch(FacadeException exception) {
                System.out.println("Error: " + exception.getMessage());
            } catch (Exception exception){
                System.out.println("An unexpected error occurred: " + exception.getMessage());
            }
        }

        scanner.close();
    }

    public void eval(String[] tokens) throws FacadeException {
        if (tokens.length == 0 || tokens[0].isEmpty()) {
            return;
        }

        if (state == State.SIGNED_OUT) {
            evalSignedOut(tokens);
        } else if (state == State.SIGNED_IN) {
            evalSignedIn(tokens);
        }
    }

    private void evalSignedOut(String[] tokens) throws FacadeException {
        switch (tokens[0].toLowerCase()) {
            case "help":
                printHelpOut();
                break;
            case "login":
                handleLogin(tokens);
                break;
            case "register":
                handleRegister(tokens);
                break;
        }
    }

    private void evalSignedIn(String[] tokens) throws FacadeException {
        switch (tokens[0].toLowerCase()) {
            case "help":
                printHelpIn();
                break;
            case "create":
                handleCreate(tokens);
                break;
            case "list":
                handleList(tokens);
                break;
            case "join":
                handleJoin(tokens);
                break;
            case "observe":
                handleObserve(tokens);
                break;
            case "logout":
                handleLogout(tokens);
                break;
        }
    }

    private void printHelpOut() {
        String helpMenuOut = """
                register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                login <USERNAME> <PASSWORD> - to play chess
                quit - playing chess
                help - with possible commands
                """;
        System.out.println(helpMenuOut);
    }

    private void printHelpIn() {
        String helpMenuIn = """
                create <NAME>: to create a game.
                list: to list games.
                join <ID> [WHITE|BLACK]: to join a game.
                observe <ID>: to watch a game.
                logout: to log out.
                quit: to exit the program.
                help: to see this menu again.
                """;
        System.out.println(helpMenuIn);
    }

    private void handleLogin(String[] tokens) throws FacadeException {
        if (tokens.length != 3) {
            System.out.println("Expected 2 arguments: login <USERNAME> <PASSWORD>");
            return;
        }
        var retrievedLogin = serverFacade.login(tokens[1], tokens[2]);
        authToken = retrievedLogin.authToken();
        state = State.SIGNED_IN;
    }

    private void handleRegister(String[] tokens) throws FacadeException {
        if (tokens.length != 4) {
            System.out.println("Expected 3 arguments: register <USERNAME> <PASSWORD> <EMAIL>");
            return;
        }
        var retrievedRegister = serverFacade.register(tokens[1], tokens[2], tokens[3]);
        authToken = retrievedRegister.authToken();
        state = State.SIGNED_IN;
    }

    private void handleCreate(String[] tokens) throws FacadeException {
        if (tokens.length != 2){
            System.out.println("Expected 1 argument: create <NAME>");
            return;
        }
        serverFacade.createGame(authToken, tokens[1]);
        System.out.println("Created " + tokens[1]);
    }

    private void handleList(String[] tokens) throws FacadeException {
        if (tokens.length != 1){
            System.out.println("No argument accepted for list");
            return;
        }
        var gameList = serverFacade.listGame(authToken);
        this.cachedGames = new ArrayList<>(gameList.games());
        if (cachedGames.isEmpty()) {
            System.out.println("No games found. Use 'create <NAME>' to start one");
            return;
        }

        System.out.println("Current Games:");
        for (int i = 0; i < cachedGames.size(); i++){
            var currentGame = cachedGames.get(i);
            int displayIndex = i + 1;
            String whitePlayer = currentGame.whiteUsername() != null ? currentGame.whiteUsername() : "Empty";
            String blackPlayer = currentGame.blackUsername() != null ? currentGame.blackUsername() : "Empty";
            System.out.println(displayIndex + ". " + currentGame.gameName() +
                    " | White: " + whitePlayer +
                    " | Black: " + blackPlayer);
        }
    }

    private void handleJoin(String[] tokens) {
        if (tokens.length != 3) {
            System.out.println("Expected 2 arguments: join <ID> [WHITE|BLACK]");
            return;
        }
        if (cachedGames == null || cachedGames.isEmpty()) {
            System.out.println("Please 'list' games before attempting to join or observe");
            return;
        }
        if (!Objects.equals(tokens[2], "WHITE") && !Objects.equals(tokens[2], "BLACK")) {
            System.out.println("Second argument must be either 'WHITE' or 'BLACK'");
            return;
        }
        try {
            int listNumber = Integer.parseInt(tokens[1]);
            int index = listNumber - 1;
            if (index < 0 || index >= cachedGames.size()){
                System.out.println("Invalid game number");
                return;
            }
            int retrievedGameID = cachedGames.get(index).gameID();

            ChessGame.TeamColor color = ChessGame.TeamColor.valueOf(tokens[2].toUpperCase());
            serverFacade.joinGame(authToken, tokens[2].toUpperCase(), retrievedGameID);
            System.out.println("Successfully joined game: " + cachedGames.get(index));
            drawBoard(color);
        } catch (NumberFormatException exception) {
            System.out.println("ID must be an integer");
        } catch (FacadeException exception){
            System.out.println("Error: " + exception.getMessage());
        }
    }

    private void handleObserve(String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("Expected 1 argument: observe <ID>");
            return;
        }
        if (cachedGames == null || cachedGames.isEmpty()) {
            System.out.println("Please 'list' games before attempting to join or observe");
            return;
        }

        try {
            int listNumber = Integer.parseInt(tokens[1]);
            int index = listNumber - 1;
            if (index < 0 || index >= cachedGames.size()){
                System.out.println("Invalid game number");
                return;
            }
            int retrievedGameID = cachedGames.get(index).gameID();
            serverFacade.observeGame(authToken, retrievedGameID);
            drawBoard(ChessGame.TeamColor.WHITE);
        } catch (NumberFormatException exception) {
            System.out.println("ID must be an integer");
        } catch (FacadeException exception){
            System.out.println("Error: " + exception.getMessage());
        }
    }

    private void handleLogout(String[] tokens) throws FacadeException {
        if (tokens.length != 1){
            System.out.println("No argument accepted for logout");
            return;
        }
        serverFacade.logout(authToken);
        this.authToken = null;
        this.state = State.SIGNED_OUT;
        System.out.println("Logged out successfully");
    }

    private void drawBoard(ChessGame.TeamColor perspective) {
        ChessBoard board = new ChessBoard();
        board.resetBoard();

        int startRow = (perspective == ChessGame.TeamColor.BLACK) ? 1 : 8;
        int endRow = (perspective == ChessGame.TeamColor.BLACK) ? 8 : 1;
        int rowDirection = (perspective == ChessGame.TeamColor.BLACK) ? 1 : -1;

        int startCol = (perspective == ChessGame.TeamColor.BLACK) ? 8 : 1;
        int endCol = (perspective == ChessGame.TeamColor.BLACK) ? 1 : 8;
        int colDirection = (perspective == ChessGame.TeamColor.BLACK) ? -1 : 1;

        String[] headers = (perspective == ChessGame.TeamColor.BLACK)
                ? new String[]{"h", "g", "f", "e", "d", "c", "b", "a"}
                : new String[]{"a", "b", "c", "d", "e", "f", "g", "h"};

        boardSetup(headers);

        for (int row = startRow; row != endRow + rowDirection; row += rowDirection) {
            System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
            System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
            System.out.print(" " + row + " ");

            for (int col = startCol; col != endCol + colDirection; col += colDirection) {
                if ((row + col) % 2 != 0) {
                    System.out.print(EscapeSequences.SET_BG_COLOR_WHITE);
                } else {
                    System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
                }

                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                printPiece(piece);
            }

            System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
            System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
            System.out.print(" " + row + " ");
            System.out.println(EscapeSequences.RESET_BG_COLOR);
        }

        boardSetup(headers);
    }

    private void boardSetup(String[] headers) {
        System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
        System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
        System.out.print("   ");
        for (String header : headers) {
            System.out.print(" " + header + " ");
        }
        System.out.print("   ");
        System.out.println(EscapeSequences.RESET_BG_COLOR);
    }

    private void printPiece(ChessPiece piece) {
        if (piece == null) {
            System.out.print(EscapeSequences.EMPTY);
            return;
        }

        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            System.out.print(EscapeSequences.SET_TEXT_COLOR_RED);
        } else {
            System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE);
        }

        switch (piece.getPieceType()) {
            case KING -> System.out.print(EscapeSequences.WHITE_KING);
            case QUEEN -> System.out.print(EscapeSequences.WHITE_QUEEN);
            case BISHOP -> System.out.print(EscapeSequences.WHITE_BISHOP);
            case KNIGHT -> System.out.print(EscapeSequences.WHITE_KNIGHT);
            case ROOK -> System.out.print(EscapeSequences.WHITE_ROOK);
            case PAWN -> System.out.print(EscapeSequences.WHITE_PAWN);
        }
    }
}