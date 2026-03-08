package dataaccess;
import model.GameData;

import java.util.Collection;

public class SqlGameDAO implements GameDAO{

    @Override
    public int createGame(GameData game) throws DataAccessException {
        return 0;
    }

    @Override
    public GameData getGame(int gameId) throws DataAccessException {
        return null;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return null;
    }
    @Override
    public void clear() throws DataAccessException {
    }
}

