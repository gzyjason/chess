package service;

import dataaccess.DataAccessException;
import model.GameData;
import org.junit.jupiter.api.*;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import request.CreateGameRequest;
import request.RegisterRequest;
import results.CreateGameResult;
import results.RegisterResult;

public class GameServiceTests {
    private UserDAO userDAO;
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private GameService gameService;
    private UserService userService;

    @BeforeEach
    public void setup() {
        userDAO = new UserDAO();
        authDAO = new AuthDAO();
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
}
