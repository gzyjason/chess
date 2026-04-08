package server;

import chess.*;
import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.websocket.*;
import model.AuthData;
import model.GameData;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebSocketHandler {
    private final Gson gson = new Gson();
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private static final Logger LOGGER = Logger.getLogger(WebSocketHandler.class.getName());
    ConcurrentHashMap<Integer, Set<WsContext>> idMap = new ConcurrentHashMap<>();

    public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public void onConnect(WsConnectContext ctx) {
        ctx.session.setIdleTimeout(Duration.ofSeconds(30));
        System.out.println("New WebSocket connection established: " + ctx.sessionId());
    }

    public void onMessage(WsMessageContext ctx) {
        String rawJson = ctx.message();
        try {
            UserGameCommand baseCommand = gson.fromJson(rawJson, UserGameCommand.class);
            switch (baseCommand.getCommandType()) {
                case MAKE_MOVE -> handleMakeMove(ctx, rawJson, baseCommand);
                case CONNECT -> handleConnect(ctx, baseCommand);
                case LEAVE -> handleLeave(ctx, baseCommand);
                case RESIGN -> handleResign(ctx, baseCommand);
            }
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "WebSocket error occurred", exception);
            try {
                if (ctx.session.isOpen()) {
                    ctx.send(gson.toJson(new ErrorMessage("Error: " + exception.getMessage())));
                }
            } catch (Exception sendException) {
                LOGGER.log(Level.WARNING, "Failed to dispatch error response", sendException);
            }
        }
    }

    private void handleMakeMove(WsMessageContext ctx, String rawJson, UserGameCommand base) throws DataAccessException {
        AuthData authData = validateAuth(ctx, base.getAuthToken());
        if (authData == null) { return; }
        GameData gameData = validateGame(ctx, base.getGameID());
        if (gameData == null) { return; }

        MakeMoveCommand moveCommand = gson.fromJson(rawJson, MakeMoveCommand.class);
        ChessGame game = gameData.game();
        if (game.isGameOver()) {
            sendSafe(ctx, gson.toJson(new ErrorMessage("Error: the game is already over")));
            return;
        }

        ChessGame.TeamColor playerColor = getPlayerColor(authData.username(), gameData);
        if (playerColor == null) {
            sendSafe(ctx, gson.toJson(new ErrorMessage("Error: observers cannot make moves")));
            return;
        }
        if (game.getTeamTurn() != playerColor) {
            sendSafe(ctx, gson.toJson(new ErrorMessage("Error: it is not your turn")));
            return;
        }

        try {
            game.makeMove(moveCommand.getMove());
            checkGameStatus(game);
            gameDAO.updateGame(new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game));

            String desc = String.format("%s moved from %s to %s", authData.username(),
                    moveCommand.getMove().getStartPosition(), moveCommand.getMove().getEndPosition());

            broadcastToAll(base.getGameID(), gson.toJson(new LoadGameMessage(game)));
            broadcastToOthers(base.getGameID(), ctx.sessionId(), new NotificationMessage(desc));
            broadcastMoveResults(base.getGameID(), game);
        } catch (InvalidMoveException e) {
            sendSafe(ctx, gson.toJson(new ErrorMessage("Error: invalid move")));
        }
    }

    private void broadcastMoveResults(Integer gameId, ChessGame game) {
        if (game.isGameOver()) {
            String msg = "The game is over.";
            if (game.isInCheckmate(ChessGame.TeamColor.WHITE) || game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
                msg = "Checkmate! The game is over.";
            } else if (game.isInStalemate(ChessGame.TeamColor.WHITE) || game.isInStalemate(ChessGame.TeamColor.BLACK)) {
                msg = "Stalemate! The game is over.";
            }
            broadcastToAll(gameId, gson.toJson(new NotificationMessage(msg)));
        } else if (game.isInCheck(game.getTeamTurn())) {
            broadcastToAll(gameId, gson.toJson(new NotificationMessage(game.getTeamTurn() + " is in check!")));
        }
    }

    private void handleConnect(WsMessageContext ctx, UserGameCommand base) throws DataAccessException {
        AuthData authData = validateAuth(ctx, base.getAuthToken());
        if (authData == null) { return; }
        GameData gameData = validateGame(ctx, base.getGameID());
        if (gameData == null) { return; }

        String role = "an observer";
        if (Objects.equals(authData.username(), gameData.whiteUsername())) { role = "White"; }
        else if (Objects.equals(authData.username(), gameData.blackUsername())) { role = "Black"; }

        idMap.computeIfAbsent(base.getGameID(), ignored -> ConcurrentHashMap.newKeySet()).add(ctx);
        sendSafe(ctx, gson.toJson(new LoadGameMessage(gameData.game())));

        NotificationMessage notif = new NotificationMessage(authData.username() + " has joined the game as " + role);
        broadcastToOthers(base.getGameID(), ctx.sessionId(), notif);
    }

    private void handleLeave(WsMessageContext ctx, UserGameCommand base) throws DataAccessException {
        AuthData authData = validateAuth(ctx, base.getAuthToken());
        if (authData == null) { return; }
        GameData gameData = validateGame(ctx, base.getGameID());
        if (gameData == null) { return; }

        try {
            if (Objects.equals(authData.username(), gameData.whiteUsername())) {
                gameDAO.updateGame(new GameData(gameData.gameID(), null, gameData.blackUsername(), gameData.gameName(), gameData.game()));
            } else if (Objects.equals(authData.username(), gameData.blackUsername())) {
                gameDAO.updateGame(new GameData(gameData.gameID(), gameData.whiteUsername(), null, gameData.gameName(), gameData.game()));
            }
        } catch (Exception exception) {
            LOGGER.log(Level.WARNING, "Failed to release player slot during leave sequence", exception);
        }

        Set<WsContext> sessions = idMap.get(base.getGameID());
        if (sessions != null) {
            sessions.removeIf(s -> s.sessionId().equals(ctx.sessionId()));
            broadcastToAll(base.getGameID(), gson.toJson(new NotificationMessage(authData.username() + " has left.")));
        }
    }

    private void handleResign(WsMessageContext ctx, UserGameCommand base) throws DataAccessException {
        AuthData authData = validateAuth(ctx, base.getAuthToken());
        if (authData == null) { return; }
        GameData gameData = validateGame(ctx, base.getGameID());
        if (gameData == null) { return; }

        if (!Objects.equals(authData.username(), gameData.whiteUsername()) && !Objects.equals(authData.username(), gameData.blackUsername())) {
            sendSafe(ctx, gson.toJson(new ErrorMessage("Error: observers cannot resign")));
            return;
        }
        if (gameData.game().isGameOver()) {
            sendSafe(ctx, gson.toJson(new ErrorMessage("Error: the game is already over")));
            return;
        }

        gameData.game().setGameOver(true);
        gameDAO.updateGame(new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), gameData.game()));
        broadcastToAll(base.getGameID(), gson.toJson(new NotificationMessage(authData.username() + " resigned. Game over.")));
    }

    private ChessGame.TeamColor getPlayerColor(String user, GameData data) {
        if (Objects.equals(user, data.whiteUsername())) { return ChessGame.TeamColor.WHITE; }
        if (Objects.equals(user, data.blackUsername())) { return ChessGame.TeamColor.BLACK; }
        return null;
    }

    private void checkGameStatus(ChessGame game) {
        if (game.isInCheckmate(ChessGame.TeamColor.WHITE) || game.isInCheckmate(ChessGame.TeamColor.BLACK) ||
                game.isInStalemate(ChessGame.TeamColor.WHITE) || game.isInStalemate(ChessGame.TeamColor.BLACK)) {
            game.setGameOver(true);
        }
    }

    public void onClose(WsCloseContext ctx) {
        String closeId = ctx.sessionId();
        idMap.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(storedCtx -> storedCtx.sessionId().equals(closeId));
            return entry.getValue().isEmpty();
        });
    }

    private void broadcastToAll(Integer gameId, String messageJson) {
        Set<WsContext> sessions = idMap.get(gameId);
        if (sessions != null) {
            for (WsContext s : sessions) {
                sendSafe(s, messageJson);
            }
        }
    }

    private void broadcastToOthers(Integer gameId, String rootId, Object msg) {
        String json = gson.toJson(msg);
        Set<WsContext> sessions = idMap.get(gameId);
        if (sessions != null) {
            for (WsContext s : sessions) {
                if (!s.sessionId().equals(rootId)) {
                    sendSafe(s, json);
                }
            }
        }
    }

    private void sendSafe(WsContext ctx, String message) {
        try {
            if (ctx.session.isOpen()) {
                ctx.send(message);
            }
        } catch (Exception exception) {
            LOGGER.log(Level.WARNING, "Failed to transmit message to client", exception);
        }
    }

    private AuthData validateAuth(WsMessageContext ctx, String token) throws DataAccessException {
        AuthData auth = authDAO.getToken(token);
        if (auth == null) {
            sendSafe(ctx, gson.toJson(new ErrorMessage("Error: unauthorized")));
        }
        return auth;
    }

    private GameData validateGame(WsMessageContext ctx, Integer id) throws DataAccessException {
        GameData game = gameDAO.getGame(id);
        if (game == null) {
            sendSafe(ctx, gson.toJson(new ErrorMessage("Error: bad game ID")));
        }
        return game;
    }
}