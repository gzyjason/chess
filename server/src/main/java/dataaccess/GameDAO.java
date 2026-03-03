package dataaccess;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GameDAO {
    private final Map<Integer, GameData> games = new HashMap<>();

    private int newGameId = 1;

    public int createGame(GameData game) {
        int gameId = newGameId++;
        GameData newGame = new GameData(gameId, game.teamAUsername(), game.teamBUsername(), game.gameName(),
                game.game());
        games.put(gameId, newGame);
        return gameId;
    }

    public GameData getGame(int gameId) {
        return games.get(gameId);
    }

    public void updateGame(GameData game) throws DataAccessException {
        if (!games.containsKey(game.gameID())){
            throw new DataAccessException("Error: bad request");
        }
        games.put(game.gameID(), game);
    }

    public Collection<GameData> listGames() {
        return games.values();
    }

    public void clear() {
        games.clear();
        newGameId = 1;
    }
}
