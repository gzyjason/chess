package dataaccess;
import model.GameData;
import java.util.Collection;

public interface GameDAO {
    int createGame(GameData game) throws DataAccessException;
    GameData getGame(int gameId) throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;
    Collection<GameData> listGames() throws DataAccessException;
    void clear() throws DataAccessException;
}
