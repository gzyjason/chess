package dataaccess;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GameDAO {
    private final Map<Integer, GameData> gameDataMap = new HashMap<>();

    private int nextId = 1;

    public int createGame(GameData game) throws DataAccessException {
        GameData newGame = new GameData(nextId, game.whiteUsername(), game.blackUsername(), game.gameName(),
                game.game());
        gameDataMap.put(nextId, newGame);
        nextId++;
        return newGame.gameID();
    }

    public GameData getGame(int id) throws DataAccessException{
        return gameDataMap.get(id);
    }

    public void updateGame(GameData game) throws DataAccessException {
        if (!gameDataMap.containsKey(game.gameID())){
            throw new DataAccessException("Error: game doesn't exist");
        }
        gameDataMap.put(game.gameID(), game);
    }

    public Collection<GameData> listGames() throws DataAccessException {
        return gameDataMap.values();
    }

    public void clear() {
        gameDataMap.clear();
    }
}
