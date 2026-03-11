package dataaccess;
import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Collection;


public class SqlGameDAOTests {
    private SqlGameDAO gameDAO;

    @BeforeEach
    public void setUp() throws DataAccessException{
        gameDAO = new SqlGameDAO();
        gameDAO.clear();
    }

    @Test
    public void createGamePositive() throws DataAccessException{
        GameData testGame = new GameData(0, "Tom", "Jack", "game1",
                new ChessGame());
        int returnedGameId = gameDAO.createGame(testGame);
        assertTrue(returnedGameId > 0, "The returned game ID should be greater than 0");

        GameData fetchedGame = gameDAO.getGame(returnedGameId);
        assertNotNull(fetchedGame, "The game should exist in the database");
        assertEquals("game1", fetchedGame.gameName(), "The game names should match");
    }

    @Test
    public void createGameNegative(){
        GameData testGame = new GameData(0, "Tom", "Jack", null,
                new ChessGame());
        assertThrows(DataAccessException.class, () -> gameDAO.createGame(testGame),
                "Null game name should throw DataAccessException");
    }

    @Test
    public void getGamePositive() throws DataAccessException {
        ChessGame originalBoard = new ChessGame();
        GameData testGame = new GameData(0, "Tom", "Jack", "game1", originalBoard);
        int id = gameDAO.createGame(testGame);
        GameData fetchedGame = gameDAO.getGame(id);

        assertNotNull(fetchedGame, "Fetched game should not be null");
        assertEquals(id, fetchedGame.gameID(), "IDs should match");
        assertEquals("Tom", fetchedGame.whiteUsername(), "White username should match");
        assertEquals("Jack", fetchedGame.blackUsername(), "Black username should match");
        assertEquals("game1", fetchedGame.gameName(), "Game name should match");
        assertEquals(originalBoard, fetchedGame.game(), "The ChessGame board state should be identical");
    }

    @Test
    public void getGameNegative() throws DataAccessException {
        GameData result = gameDAO.getGame(9999);
        assertNull(result,  "getGame should return null for a non-existent ID");
    }

    @Test
    public void updateGamePositive() throws DataAccessException{
        GameData testGame1 = new GameData(0, null, null, "game1",
                new ChessGame());
        int id = gameDAO.createGame(testGame1);
        GameData testGame2 = new GameData(id, "Tom", null, "game1",
                new ChessGame());
        gameDAO.updateGame(testGame2);
        assertNotNull(gameDAO.getGame(id).whiteUsername(), "White username shouldn't be null after update");
        assertEquals("game1", gameDAO.getGame(id).gameName(), "Game name should remain identical");

    }

    @Test
    public void updateGameNegative(){
        GameData testGame = new GameData(9999, null, null, "game1",
                new ChessGame());
        assertThrows(DataAccessException.class, () -> gameDAO.updateGame(testGame),
                "Shouldn't be able to update non-existing game");
    }

    @Test
    public void listGamePositive() throws DataAccessException{
        GameData testGame1 = new GameData(0, null, null, "game1",
                new ChessGame());
        GameData testGame2 = new GameData(0, null, null, "game1",
                new ChessGame());
        gameDAO.createGame(testGame1);
        gameDAO.createGame(testGame2);
        Collection<GameData> gamesList = gameDAO.listGames();
        assertNotNull(gamesList, "The list should not be null after games are created");
        assertEquals(2, gamesList.size(), "There should be exactly 2 games in the list");
    }

    @Test
    public void listGameNegative() throws DataAccessException {
        assertTrue(gameDAO.listGames().isEmpty(), "The game list should be empty before games are created");
    }

    @Test
    public void clearPositive() throws DataAccessException{
       GameData testGame = new GameData(0, "Tom", "Jack", "game1",
               new ChessGame());
       gameDAO.createGame(testGame);
       gameDAO.clear();
       Collection<GameData> cleared = gameDAO.listGames();
       assertTrue(cleared.isEmpty(), "The game should not exist once cleared");

    }
}
