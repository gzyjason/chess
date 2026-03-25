package client;

import org.junit.jupiter.api.*;
import results.*;
import server.Server;


public class ServerFacadeTests {

    private static Server server;
    private static  ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);

        facade = new ServerFacade("http://localhost:" + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearDatabase() throws FacadeException{
        facade.clear();
    }

    @Test
    public void registerPositive() throws FacadeException {
        RegisterResult result = facade.register("tom", "password123", "abc123@byu.edu");
        Assertions.assertNotNull(result);
        Assertions.assertEquals("tom", result.username());
        Assertions.assertNotNull(result.authToken());
    }

    @Test
    public void registerNegative() throws FacadeException{
        RegisterResult result1 = facade.register("tom", "password123", "abc123@byu.edu");
        FacadeException exception = Assertions.assertThrows(FacadeException.class, () -> facade.register("tom",
                "password456", "xyz456@byu.edu"));
        Assertions.assertEquals(403, exception.getStatusCode());
    }

    @Test
    public void loginPositive() throws FacadeException{
        facade.register("tom", "password123", "abc123@byu.edu");
        LoginResult result = facade.login("tom", "password123");
        Assertions.assertNotNull(result.authToken());
        Assertions.assertEquals("tom", result.username());
    }

    @Test
    public void loginNegative() throws FacadeException {
        facade.register("tom", "password123", "abc123@byu.edu");
        FacadeException exception = Assertions.assertThrows(FacadeException.class,
                () -> facade.login("tom", "password456"));
        Assertions.assertEquals(401, exception.getStatusCode());
    }

    @Test
    public void logoutPositive() throws FacadeException {
        RegisterResult result = facade.register("tom", "password123", "abc123@byu.edu");
        Assertions.assertDoesNotThrow(() -> facade.logout(result.authToken()));

        FacadeException exception = Assertions.assertThrows(FacadeException.class,
                () -> facade.listGame(result.authToken()));
        Assertions.assertEquals(401, exception.getStatusCode());

    }

    @Test
    public void logoutNegative() throws FacadeException {
        FacadeException exception = Assertions.assertThrows(FacadeException.class,
                () -> facade.logout("fakeTokenNotExist"));
        Assertions.assertEquals(401, exception.getStatusCode());
    }

    @Test
    public void createGamePositive() throws FacadeException{
        RegisterResult result = facade.register("tom", "password123", "abc123@byu.edu");
        CreateGameResult createGameResult = facade.createGame(result.authToken(), "game1");
        Assertions.assertNotNull(createGameResult);
        Assertions.assertTrue(createGameResult.gameID() > 0);
    }

    @Test
    public void createGameNegative() throws FacadeException {
        FacadeException exception = Assertions.assertThrows(FacadeException.class,
                () -> facade.createGame("fakeTokenNotExist", "game1"));
        Assertions.assertEquals(401, exception.getStatusCode());
    }

    @Test
    public void listGamePositive() throws FacadeException{
        RegisterResult result = facade.register("tom", "password123", "abc123@byu.edu");
        facade.createGame(result.authToken(), "game1");
        facade.createGame(result.authToken(), "game2");
        ListGamesResult gamesResult = facade.listGame(result.authToken());
        Assertions.assertNotNull(gamesResult);
        Assertions.assertEquals(2, gamesResult.games().size());
    }

    @Test
    public void listGameNegative() throws FacadeException{
        FacadeException exception = Assertions.assertThrows(FacadeException.class,
                () -> facade.listGame("fakeTokenNotExist"));
        Assertions.assertEquals(401, exception.getStatusCode());
    }

    @Test
    public void joinGamePositive() throws FacadeException{
        RegisterResult result = facade.register("tom", "password123", "abc123@byu.edu");
        CreateGameResult createGameResult = facade.createGame(result.authToken(), "game1");
        Assertions.assertDoesNotThrow(() -> facade.joinGame(result.authToken(), "WHITE",
                createGameResult.gameID()));
        ListGamesResult gameList = facade.listGame(result.authToken());
        var games = gameList.games();
        Assertions.assertEquals(1, games.size());
        var specificGame = games.iterator().next();
        Assertions.assertEquals("tom", specificGame.whiteUsername());
    }

    @Test
    public void joinGameNegative() throws FacadeException{
        RegisterResult result = facade.register("tom", "password123", "abc123@byu.edu");
        facade.createGame(result.authToken(), "game1");
        FacadeException exception = Assertions.assertThrows(FacadeException.class,
                () -> facade.joinGame(result.authToken(), "WHITE", 999));
        Assertions.assertEquals(400, exception.getStatusCode());
    }

    @Test
    public void observeGamePositive() throws FacadeException {
        RegisterResult result = facade.register("tom", "password123", "abc123@byu.edu");
        CreateGameResult createGameResult = facade.createGame(result.authToken(), "game1");
        Assertions.assertDoesNotThrow(() -> facade.observeGame(result.authToken(), createGameResult.gameID()));
    }

    @Test
    public void observeGameNegative() throws  FacadeException{
        RegisterResult result = facade.register("tom", "password123", "abc123@byu.edu");
        CreateGameResult createGameResult = facade.createGame(result.authToken(), "game1");
        FacadeException exception = Assertions.assertThrows(FacadeException.class,
                () -> facade.observeGame("invalidToken", createGameResult.gameID()));
        Assertions.assertEquals(401, exception.getStatusCode());
    }

    @Test
    public void clearPositive() throws FacadeException{
        RegisterResult result = facade.register("tom", "password123", "abc123@byu.edu");
        facade.createGame(result.authToken(), "game1");
        facade.createGame(result.authToken(), "game2");
        Assertions.assertDoesNotThrow(() -> facade.clear());
        FacadeException exception = Assertions.assertThrows(FacadeException.class,
                () -> facade.listGame(result.authToken()));
        Assertions.assertEquals(401, exception.getStatusCode());

    }
}
