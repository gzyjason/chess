package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import java.util.Collection;

public class ClearServiceTests {
    private UserDAO userDAO;
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private ClearService service;

    @BeforeEach
    public void setup(){
        userDAO = new UserDAO();
        authDAO = new AuthDAO();
        gameDAO = new GameDAO();
        service = new ClearService(userDAO, authDAO, gameDAO);
    }

    @Test
    public void clearDataTest() throws DataAccessException {
        userDAO.insertUser(new UserData("Tom", "password123", "abc123@byu.edu"));
        authDAO.createAuth(new AuthData("token123", "Tom"));
        int firstId = gameDAO.createGame((new GameData(0, null, null,
                "Test Game", new ChessGame())));
        service.clear();

        Assertions.assertNull(userDAO.getUser("Tom"), "User should be gone");
        Assertions.assertNull(authDAO.getAuth("token123"), "Auth token should be gone");
        Collection<GameData> games = gameDAO.listGames();
        Assertions.assertTrue(games.isEmpty(), "Game list should be empty");

        int idCleared = gameDAO.createGame(new GameData(0,null, null,
                "New Game", new ChessGame()));
        Assertions.assertEquals(1, idCleared, "Game ID should reset to 1 after being cleared");
    }
}
