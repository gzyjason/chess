package dataaccess;
import model.GameData;

import java.sql.SQLException;
import java.util.Collection;

public class SqlGameDAO implements GameDAO{

    public SqlGameDAO() throws DataAccessException{
        String createTable = """
                CREATE TABLE IF NOT EXISTS game (
                    gameID INT NOT NULL AUTO_INCREMENT,
                    whiteUsername VARCHAR(255),
                    blackUsername VARCHAR(255),
                    gameName VARCHAR(255) NOT NULL,
                    game TEXT NOT NULL,
                    PRIMARY KEY (gameID)
                )
                """;
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(createTable)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to configure database: %s", e.getMessage()), e);
        }
    }
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

