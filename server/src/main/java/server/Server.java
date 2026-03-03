package server;

import dataaccess.*;
import io.javalin.*;
import io.javalin.http.Context;
import request.*;
import results.*;
import service.*;

import java.util.Map;

public class Server {

    private final Javalin javalin;
    private final UserService userService;
    private final GameService gameService;
    private final ClearService clearService;

    public Server() {

        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");

            config.jsonMapper(new io.javalin.json.JavalinJackson());
        });

        UserDAO userDao = new UserDAO();
        AuthDAO authDao = new AuthDAO();
        GameDAO gameDao = new GameDAO();

        this.userService = new UserService(userDao, authDao);
        this.gameService = new GameService(gameDao, authDao);
        this.clearService = new ClearService(userDao, authDao, gameDao);

        // Register your endpoints and exception handlers here.
        javalin.delete("/db", this::clear);
        javalin.post("/user", this::register);
        javalin.post("/session", this::login);
        javalin.delete("/session", this::logout);
        javalin.get("/game", this::listGames);
        javalin.post("/game", this::createGame);
        javalin.put("/game", this::joinGame);

        javalin.exception(DataAccessException.class, (e, ctx) -> {
            String message = e.getMessage();
            int statusCode = 500;

            if (message.contains("bad request")) {
                statusCode = 400;
            } else if (message.contains("unauthorized")) {
                statusCode = 401;
            } else if (message.contains("already taken") || message.contains("taken")) {
                statusCode = 403;
            }

            ctx.status(statusCode);
            ctx.json(Map.of("message", message));
        });

    }
    private void clear(Context ctx) {
        clearService.clear();
        ctx.status(200);
        ctx.result("{}");
    }

    private void register(Context ctx) throws DataAccessException { // Change: add throws
        RegisterRequest req = ctx.bodyAsClass(RegisterRequest.class);
        RegisterResult res = userService.register(req);
        ctx.status(200);
        ctx.json(res);
    }

    private void login(Context ctx) throws DataAccessException {
        LoginRequest req = ctx.bodyAsClass(LoginRequest.class);
        LoginResult res = userService.login(req);

        ctx.status(200);
        ctx.json(res);
    }

    private void logout(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");

        userService.logout(authToken);
        ctx.status(200);
        ctx.result("{}");
    }

    private void listGames(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        ListGamesResult res = gameService.listGames(authToken);
        ctx.status(200);
        ctx.json(res);
    }

    private void createGame(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        CreateGameRequest req = ctx.bodyAsClass(CreateGameRequest.class);
        CreateGameResult res = gameService.createGame(authToken, req);
        ctx.status(200);
        ctx.json(res);
    }

    private void joinGame(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        JoinGameRequest req = ctx.bodyAsClass(JoinGameRequest.class);

        if(req.gameID() <= 0) {
            throw new DataAccessException("Error: bad request");
        }

        gameService.joinGame(authToken, req);

        ctx.status(200);
        ctx.result("{}" );
    }

    public int run(int desiredPort) {
         javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}