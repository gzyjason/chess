package server;

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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler {
    private final Gson gson = new Gson();
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    ConcurrentHashMap<Integer, Set<WsContext>> idMap = new ConcurrentHashMap<>();


    public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO){
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;

    }
    public void onConnect(WsConnectContext ctx){
        ctx.session.setIdleTimeout(Duration.ofSeconds(30));
        System.out.println("New WebSocket connection established: " + ctx.sessionId());
    }

    public void onMessage (WsMessageContext ctx){
        String rawJson = ctx.message();


        try {
            UserGameCommand baseCommand = gson.fromJson(rawJson, UserGameCommand.class);

            String authToken = baseCommand.getAuthToken();
            Integer retrievedId = baseCommand.getGameID();
            switch (baseCommand.getCommandType()){
                case MAKE_MOVE: {
                    MakeMoveCommand moveCommand = gson.fromJson(rawJson, MakeMoveCommand.class);
                    break;
                }

                case CONNECT: {
                    AuthData authData = authDAO.getToken(authToken);
                    if (authData == null) {
                        ctx.send(gson.toJson(new ErrorMessage("Error: unauthorized")));
                        break;
                    }

                    GameData gameData = gameDAO.getGame(retrievedId);
                    if (gameData == null) {
                        ctx.send(gson.toJson(new ErrorMessage("Error: bad game ID")));
                        break;
                    }

                    String username = authData.username();
                    String role = "an observer";
                    if (username.equals((gameData.whiteUsername()))) {
                        role = "White";
                    } else if (username.equals((gameData.blackUsername()))) {
                        role = "Black";
                    }

                    Set<WsContext> sessions = idMap.get(retrievedId);
                    if (sessions == null) {
                        sessions = ConcurrentHashMap.newKeySet();
                        idMap.put(retrievedId, sessions);
                    }
                    sessions.add(ctx);

                    LoadGameMessage loadMessage = new LoadGameMessage(gameData);
                    ctx.send(gson.toJson(loadMessage));

                    NotificationMessage notification = new NotificationMessage(username + " has joined the game as " + role);
                    String notificationJson = gson.toJson(notification);

                    for (WsContext sessionContext : sessions) {
                        if (!sessionContext.sessionId().equals(ctx.sessionId())) {
                            sessionContext.send(notificationJson);
                        }
                    }

                    break;
                }

                case LEAVE: {
                    AuthData authData = authDAO.getToken(authToken);
                    if (authData == null) {
                        ctx.send(gson.toJson(new ErrorMessage("Error: unauthorized")));
                        break;
                    }

                    GameData gameData = gameDAO.getGame(retrievedId);
                    if (gameData == null) {
                        ctx.send(gson.toJson(new ErrorMessage("Error: bad game ID")));
                        break;
                    }

                    String username = authData.username();

                    if (username.equals(gameData.whiteUsername())) {
                        gameDAO.updateGame(new GameData(gameData.gameID(), null, gameData.blackUsername(), gameData.gameName(), gameData.game()));
                    } else if (username.equals(gameData.blackUsername())) {
                        gameDAO.updateGame(new GameData(gameData.gameID(), gameData.whiteUsername(), null, gameData.gameName(), gameData.game()));
                    }

                    Set<WsContext> sessions = idMap.get(retrievedId);
                    if (sessions != null) {
                        sessions.removeIf(s -> s.sessionId().equals(ctx.sessionId()));

                        NotificationMessage notification = new NotificationMessage(username + " has left the game.");
                        String notificationJson = gson.toJson(notification);

                        for (WsContext s : sessions) {
                            s.send(notificationJson);
                        }
                    }
                    break;
                }

                case RESIGN: {
                    AuthData authData = authDAO.getToken(authToken);
                    if (authData == null) {
                        ctx.send(gson.toJson(new ErrorMessage("Error: unauthorized")));
                        break;
                    }

                    GameData gameData = gameDAO.getGame(retrievedId);
                    if (gameData == null) {
                        ctx.send(gson.toJson(new ErrorMessage("Error: bad game ID")));
                        break;
                    }

                    String username = authData.username();

                    if (!username.equals(gameData.whiteUsername()) && !username.equals(gameData.blackUsername())) {
                        ctx.send(gson.toJson(new ErrorMessage("Error: observers cannot resign")));
                        break;
                    }

                    if (gameData.game().isGameOver()) {
                        ctx.send(gson.toJson(new ErrorMessage("Error: the game is already over")));
                        break;
                    }

                    gameData.game().setGameOver(true);
                    gameDAO.updateGame(gameData);

                    NotificationMessage notification = new NotificationMessage(username + " has resigned. Game over.");
                    String notificationJson = gson.toJson(notification);

                    Set<WsContext> sessions = idMap.get(retrievedId);
                    if (sessions != null) {
                        for (WsContext s : sessions) {
                            s.send(notificationJson);
                        }
                    }
                    break;
                }
            }
        } catch (Exception exception){
            ErrorMessage networkError = new ErrorMessage("Error: invalid format");
            ctx.send(gson.toJson(networkError));
        }
    }

    public void onClose(WsCloseContext ctx){
        String closeId = ctx.sessionId();

        for (Map.Entry<Integer, Set<WsContext>> entry : idMap.entrySet()) {
            Set<WsContext> gameSessions = entry.getValue();
            Integer gameId = entry.getKey();
            gameSessions.removeIf(storedCtx -> storedCtx.sessionId().equals(closeId));

            if (gameSessions.isEmpty()) {
                idMap.remove(gameId);
            }
        }



    }
}
