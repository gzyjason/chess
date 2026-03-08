package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import java.util.Collection;

public class ClearServiceTests {
    private MemoryUserDAO userDAO;
    private MemoryAuthDAO authDAO;
    private MemoryGameDAO gameDAO;
    private ClearService service;

    @BeforeEach
    public void setup(){
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        service = new ClearService(userDAO, authDAO, gameDAO);
    }

    @Test
    public void clearDataTest() throws DataAccessException {
        userDAO.insertPlayer(new UserData("Tom", "password123", "abc123@byu.edu"));
        authDAO.createToken(new AuthData("token123", "Tom"));
        service.clear();

        Assertions.assertNull(userDAO.retrievePlayer("Tom"), "User should be gone");
        Assertions.assertNull(authDAO.getToken("token123"), "Auth token should be gone");
        Collection<GameData> games = gameDAO.listGames();
        Assertions.assertTrue(games.isEmpty(), "Game list should be empty");

        int idCleared = gameDAO.createGame(new GameData(0,null, null,
                "New Game", new ChessGame()));
        Assertions.assertEquals(1, idCleared, "Game ID should reset to 1 after being cleared");
    }
}
