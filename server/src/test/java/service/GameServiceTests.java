package service;

import dataaccess.DataAccessException;
import model.GameData;
import org.junit.jupiter.api.*;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import request.CreateGameRequest;
import request.JoinGameRequest;
import request.RegisterRequest;
import results.CreateGameResult;
import results.RegisterResult;

public class GameServiceTests {
    private GameDAO gameDAO;
    private GameService gameService;
    private UserService userService;

    @BeforeEach
    public void setup() {
        UserDAO userDAO = new UserDAO();
        AuthDAO authDAO = new AuthDAO();
        gameDAO = new GameDAO();
        gameService = new GameService(gameDAO, authDAO);
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    public void createGameSuccess() throws DataAccessException {
        RegisterRequest registerRequest = new RegisterRequest("Tom", "password123",
                "abc123@byu.edu");
        RegisterResult registerResult = userService.register(registerRequest);
        String validToken = registerResult.authToken();

        CreateGameRequest createGameRequest = new CreateGameRequest("Match of the era");

        CreateGameResult createGameResult = gameService.createGame(validToken, createGameRequest);

        Assertions.assertNotNull(createGameResult);
        Assertions.assertTrue(createGameResult.gameID() > 0, "GameID should be a positive int");

        GameData savedGame = gameDAO.getGame(createGameResult.gameID());
        Assertions.assertNotNull(savedGame, "Game should be in DAO");
        Assertions.assertEquals("Match of the era", savedGame.gameName());
    }

    @Test
    public void createGameBadName() throws DataAccessException{
        RegisterResult registerResult = userService.register(new RegisterRequest("Tom", "password123",
                "abc123@byu.edu"));
        String validToken = registerResult.authToken();

        CreateGameRequest badRequest = new CreateGameRequest(null);

        DataAccessException exception = Assertions.assertThrows(DataAccessException.class, () ->
                gameService.createGame(validToken, badRequest));

        Assertions.assertEquals("Error: bad request", exception.getMessage());
        Assertions.assertTrue(gameDAO.listGames().isEmpty(), "No game creation when name is null");
    }

    @Test
    public void createGameUnauthorized(){
        CreateGameRequest request = new CreateGameRequest("New Game");

        DataAccessException exception = Assertions.assertThrows(DataAccessException.class,
                () -> gameService.createGame("invalid-token", request));

        Assertions.assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    public void listGamesSuccess() throws DataAccessException {
        RegisterResult reg = userService.register(new RegisterRequest("Tom", "pass", "t@b.edu"));
        gameService.createGame(reg.authToken(), new CreateGameRequest("Game 1"));
        gameService.createGame(reg.authToken(), new CreateGameRequest("Game 2"));

        var result = gameService.listGames(reg.authToken());

        Assertions.assertNotNull(result.games());
        Assertions.assertEquals(2, result.games().size(), "Should return exactly 2 games");
    }

    @Test
    public void listGamesUnauthorized() {
        DataAccessException exception = Assertions.assertThrows(DataAccessException.class, () ->
                gameService.listGames("fake-token"));

        Assertions.assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    public void joinGameSuccess() throws DataAccessException {
        RegisterResult registerResult = userService.register(new RegisterRequest("Tom", "password123", "abc123@byu.edu"));
        CreateGameResult game = gameService.createGame(registerResult.authToken(), new CreateGameRequest("Test Game"));

        gameService.joinGame(registerResult.authToken(), new JoinGameRequest("WHITE", game.gameID()));

        GameData savedGame = gameDAO.getGame(game.gameID());
        Assertions.assertEquals("Tom", savedGame.whiteUsername(), "Tom expected for teamAUsername");
    }

    @Test
    @DisplayName("Negative: Join color that is already taken")
    public void joinGameAlreadyTaken() throws DataAccessException {

        RegisterResult player1Result = userService.register(new RegisterRequest("P1", "pass",
                "1@b.edu"));
        RegisterResult player2Result = userService.register(new RegisterRequest("P2", "pass",
                "2@b.edu"));
        CreateGameResult game = gameService.createGame(player1Result.authToken(),
                new CreateGameRequest("Test Game"));

        gameService.joinGame(player1Result.authToken(), new JoinGameRequest("BLACK", game.gameID()));

        DataAccessException exception = Assertions.assertThrows(DataAccessException.class, () ->
                gameService.joinGame(player2Result.authToken(), new JoinGameRequest("BLACK", game.gameID())));

        Assertions.assertEquals("Error: already taken", exception.getMessage()); //
    }

}
