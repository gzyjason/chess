package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;
import ui.EscapeSequences;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.util.List;

public class ChessClient implements ServerMessageObserver {
    ServerFacade serverFacade;
    String authToken;
    private List<GameData> cachedGames;
    private final String serverURL;

    public ChessClient (String serverURL){
        this.serverURL = serverURL;
        this.serverFacade = new ServerFacade(serverURL);
    }

    public enum State { SIGNED_OUT, SIGNED_IN, GAMEPLAY }

    private State state = State.SIGNED_OUT;
    private WebSocketFacade ws;
    private ChessGame.TeamColor playerColor;
    private Integer currentGameID;
    private ChessGame currentGame;

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
        } else if (state == State.GAMEPLAY) {
            evalGameplay(tokens);
        }
    }

    private void evalGameplay(String[] tokens) throws FacadeException {
        switch (tokens[0].toLowerCase()) {
            case "help":
                printHelpGameplay();
                break;
            case "redraw":
                if (currentGame != null && currentGame.getBoard() != null) {
                    drawBoard(playerColor, currentGame);
                } else {
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "No valid game loaded to redraw.");
                }
                break;
            case "leave":
                ws.sendCommand(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, currentGameID));
                ws.close();
                this.state = State.SIGNED_IN;
                this.ws = null;
                break;
            case "resign":
                handleResign();
                break;
            case "make":
                handleMakeMove(tokens);
                break;
            case "highlight":
                handleHighlight(tokens);
                break;
        }
    }

    private chess.ChessPosition parsePosition(String pos) {
        if (pos == null || pos.length() != 2) {
            return null;
        }
        int col = pos.toLowerCase().charAt(0) - 'a' + 1;
        int row = pos.toLowerCase().charAt(1) - '1' + 1;
        if (col < 1 || col > 8 || row < 1 || row > 8) {
            return null;
        }
        return new chess.ChessPosition(row, col);
    }

    private void handleMakeMove(String[] tokens) throws FacadeException {
        if (tokens.length < 3) {
            System.out.println("Expected: make <START_POS> <END_POS> [PROMOTION_PIECE]");
            return;
        }
        chess.ChessPosition start = parsePosition(tokens[1]);
        chess.ChessPosition end = parsePosition(tokens[2]);
        if (start == null || end == null) {
            System.out.println("Invalid positions. Use algebraic notation (e.g., e2 e4).");
            return;
        }
        chess.ChessPiece.PieceType promotion = null;
        if (tokens.length == 4) {
            try {
                promotion = chess.ChessPiece.PieceType.valueOf(tokens[3].toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid promotion piece. Options: QUEEN, ROOK, BISHOP, KNIGHT");
                return;
            }
        }
        chess.ChessMove move = new chess.ChessMove(start, end, promotion);
        ws.sendCommand(new websocket.commands.MakeMoveCommand(authToken, currentGameID, move));
    }

    private void handleHighlight(String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("Expected: highlight <POSITION> (e.g., highlight e2)");
            return;
        }
        chess.ChessPosition pos = parsePosition(tokens[1]);
        if (pos == null) {
            System.out.println("Invalid position. Use algebraic notation.");
            return;
        }
        if (currentGame == null) {
            System.out.println("Game state is not loaded yet.");
            return;
        }
        java.util.Collection<chess.ChessMove> validMoves = currentGame.validMoves(pos);
        java.util.Collection<chess.ChessPosition> highlights = new java.util.ArrayList<>();
        highlights.add(pos);
        if (validMoves != null) {
            for (chess.ChessMove move : validMoves) {
                highlights.add(move.getEndPosition());
            }
        }
        drawBoard(playerColor, currentGame, highlights);
    }

    private void printHelpGameplay() {
        System.out.print("redraw: redraw board\n" +
                "leave: return to lobby\n" +
                "make <START> <END> [PROMOTION]: move\n" +
                "resign: forfeit game\n" +
                "highlight <PIECE>: see legal moves\n" +
                "help: see menu\n");
    }

    private void handleResign() throws FacadeException {
        System.out.print("Are you sure you want to resign? (yes/no): ");
        Scanner s = new Scanner(System.in);
        if (s.nextLine().equalsIgnoreCase("yes")) {
            ws.sendCommand(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, currentGameID));
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
        System.out.print("register <USERNAME> <PASSWORD> <EMAIL> - create account\n" +
                "login <USERNAME> <PASSWORD> - play\n" +
                "quit - exit\n" +
                "help - commands\n");
    }

    private void printHelpIn() {
        System.out.print("create <NAME>: create game\n" +
                "list: list games\n" +
                "join <ID> [WHITE|BLACK]: join game\n" +
                "observe <ID>: watch game\n" +
                "logout: log out\n" +
                "quit: exit\n" +
                "help: menu\n");
    }

    private void handleLogin(String[] tokens) throws FacadeException {
        if (tokens.length != 3) {
            System.out.println("Expected 2 arguments: login <USERNAME> <PASSWORD>");
            return;
        }
        authToken = serverFacade.login(tokens[1], tokens[2]).authToken();
        state = State.SIGNED_IN;
    }

    private void handleRegister(String[] tokens) throws FacadeException {
        if (tokens.length != 4) {
            System.out.println("Expected 3 arguments: register <USERNAME> <PASSWORD> <EMAIL>");
            return;
        }
        authToken = serverFacade.register(tokens[1], tokens[2], tokens[3]).authToken();
        state = State.SIGNED_IN;
    }

    private void handleCreate(String[] tokens) throws FacadeException {
        if (tokens.length != 2) {
            System.out.println("Expected 1 argument: create <NAME>");
            return;
        }
        serverFacade.createGame(authToken, tokens[1]);
        System.out.println("Created " + tokens[1]);
    }

    private void handleList(String[] tokens) throws FacadeException {
        if (tokens.length != 1) {
            System.out.println("No argument accepted for list");
            return;
        }
        this.cachedGames = new ArrayList<>(serverFacade.listGame(authToken).games());
        if (cachedGames.isEmpty()) {
            System.out.println("No games found. Use 'create <NAME>' to start one");
            return;
        }
        System.out.println("Current Games:");
        for (int i = 0; i < cachedGames.size(); i++) {
            var currentGame = cachedGames.get(i);
            String whiteUser = currentGame.whiteUsername() != null ? currentGame.whiteUsername() : "Empty";
            String blackUser = currentGame.blackUsername() != null ? currentGame.blackUsername() : "Empty";
            System.out.println((i + 1) + ". " + currentGame.gameName() +
                    " | White: " + whiteUser +
                    " | Black: " + blackUser);
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
        if (!Objects.equals(tokens[2].toUpperCase(), "WHITE") && !Objects.equals(tokens[2].toUpperCase(), "BLACK")) {
            System.out.println("Second argument must be either 'WHITE' or 'BLACK'");
            return;
        }
        try {
            GameData game = getGameFromToken(tokens[1]);
            this.currentGameID = game.gameID();
            this.playerColor = ChessGame.TeamColor.valueOf(tokens[2].toUpperCase());
            serverFacade.joinGame(authToken, tokens[2].toUpperCase(), currentGameID);
            System.out.println("Successfully joined game: " + game.gameName());
            this.ws = new WebSocketFacade(serverURL, this);
            ws.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, currentGameID));
            this.state = State.GAMEPLAY;
        } catch (NumberFormatException e) {
            System.out.println("ID must be an integer");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            this.state = State.SIGNED_IN;
            this.ws = null;
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
            GameData game = getGameFromToken(tokens[1]);
            this.currentGameID = game.gameID();
            this.playerColor = ChessGame.TeamColor.WHITE;
            this.ws = new WebSocketFacade(serverURL, this);
            ws.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, currentGameID));
            this.state = State.GAMEPLAY;
        } catch (NumberFormatException e) {
            System.out.println("ID must be an integer");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            this.state = State.SIGNED_IN;
            this.ws = null;
        }
    }

    private void handleLogout(String[] tokens) throws FacadeException {
        if (tokens.length != 1) {
            System.out.println("No argument accepted for logout");
            return;
        }
        serverFacade.logout(authToken);
        this.authToken = null;
        this.state = State.SIGNED_OUT;
        System.out.println("Logged out successfully");
    }

    private void drawBoard(ChessGame.TeamColor perspective, ChessGame game) {
        drawBoard(perspective, game, null);
    }

    private void drawBoard(ChessGame.TeamColor perspective, ChessGame game, java.util.Collection<ChessPosition> highlights) {
        ChessBoard board = game.getBoard();
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
            System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY +
                    EscapeSequences.SET_TEXT_COLOR_BLACK + " " + row + " ");

            for (int col = startCol; col != endCol + colDirection; col += colDirection) {
                ChessPosition currentPos = new ChessPosition(row, col);
                boolean isHighlighted = (highlights != null && highlights.contains(currentPos));

                if (isHighlighted) {
                    System.out.print((row + col) % 2 != 0 ? EscapeSequences.SET_BG_COLOR_GREEN :
                            EscapeSequences.SET_BG_COLOR_DARK_GREEN);
                } else {
                    System.out.print((row + col) % 2 != 0 ? EscapeSequences.SET_BG_COLOR_WHITE :
                            EscapeSequences.SET_BG_COLOR_BLACK);
                }
                printPiece(board.getPiece(currentPos));
            }

            System.out.println(EscapeSequences.SET_BG_COLOR_LIGHT_GREY +
                    EscapeSequences.SET_TEXT_COLOR_BLACK + " " + row + " " +
                    EscapeSequences.RESET_BG_COLOR);
        }
        boardSetup(headers);
    }

    private void boardSetup(String[] headers) {
        System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY + EscapeSequences.SET_TEXT_COLOR_BLACK + "   ");
        for (String header : headers) {
            System.out.print(" " + header + " ");
        }
        System.out.println("   " + EscapeSequences.RESET_BG_COLOR);
    }

    private void printPiece(ChessPiece piece) {
        if (piece == null) {
            System.out.print(EscapeSequences.EMPTY);
            return;
        }
        System.out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                EscapeSequences.SET_TEXT_COLOR_RED : EscapeSequences.SET_TEXT_COLOR_BLUE);
        switch (piece.getPieceType()) {
            case KING -> System.out.print(EscapeSequences.WHITE_KING);
            case QUEEN -> System.out.print(EscapeSequences.WHITE_QUEEN);
            case BISHOP -> System.out.print(EscapeSequences.WHITE_BISHOP);
            case KNIGHT -> System.out.print(EscapeSequences.WHITE_KNIGHT);
            case ROOK -> System.out.print(EscapeSequences.WHITE_ROOK);
            case PAWN -> System.out.print(EscapeSequences.WHITE_PAWN);
        }
    }

    private GameData getGameFromToken(String token) throws IllegalArgumentException {
        int index = Integer.parseInt(token) - 1;
        if (index < 0 || index >= cachedGames.size()) {
            throw new IllegalArgumentException("Invalid game number");
        }
        return cachedGames.get(index);
    }

    @Override
    public void notify(ServerMessage message) {
        try {
            switch (message.getServerMessageType()) {
                case LOAD_GAME -> {
                    System.out.println();
                    this.currentGame = ((LoadGameMessage) message).getGame();
                    if (this.currentGame == null) {
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED +
                                "Data Error: The server sent an empty game state.");
                    } else if (this.currentGame.getBoard() == null) {
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED +
                                "Data Error: The ChessBoard inside the game is null.");
                    } else {
                        drawBoard(playerColor, this.currentGame);
                    }
                    printPrompt();
                }
                case NOTIFICATION -> {
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "\n" +
                            ((NotificationMessage) message).getMessage());
                    printPrompt();
                }
                case ERROR -> {
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "\n" +
                            ((ErrorMessage) message).getErrorMessage());
                    printPrompt();
                }
            }
        } catch (Exception exception) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "\nUI Rendering Error: " +
                    exception.getMessage());
            printPrompt();
        }
    }

    private void printPrompt() {
        System.out.print("\n[" + state + "] >>> ");
        System.out.flush();
    }
}