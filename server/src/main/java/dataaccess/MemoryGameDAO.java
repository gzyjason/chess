package dataaccess;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MemoryGameDAO implements GameDAO{
    private final Map<Integer,GameData> games = new HashMap<>();

    private int newGameId = 1;

    @Override
    public int createGame(GameData game) {
        int gameId = newGameId++;
        GameData newGame = new GameData(gameId, game.whiteUsername(), game.blackUsername(), game.gameName(),
                game.game());
        games.put(gameId, newGame);
        return gameId;
    }

    @Override
    public GameData getGame(int gameId) {
        return games.get(gameId);
    }
    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (!games.containsKey(game.gameID())){
            throw new DataAccessException( "Error: bad request");
        }
        games.put(game.gameID(),  game);
    }

    @Override
    public Collection<GameData> listGames() {
        return games.values();
    }

    @Override
    public void clear() {
        games.clear();
        newGameId =1;
    }
}
