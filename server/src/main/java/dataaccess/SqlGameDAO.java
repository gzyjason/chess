package dataaccess;
import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

public class SqlGameDAO implements GameDAO{
    private final Gson serializer = new Gson();

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
        String statement = "INSERT INTO game (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";
        String serializedGame = serializer.toJson(game.game());
        try (var openConnection = DatabaseManager.getConnection( );
             var getReady = openConnection.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
            getReady.setString(1, game.whiteUsername());
            getReady.setString(2, game.blackUsername());
            getReady.setString(3, game.gameName());
            getReady.setString(4, serializedGame);
            getReady.executeUpdate();

            try(var generatedKeys = getReady.getGeneratedKeys()){
                if (generatedKeys.next()){
                    return generatedKeys.getInt(1);
                } else{
                    throw new DataAccessException("No ID was found");
                }
            }
        } catch (SQLException exception) {
            throw new DataAccessException(String.format("Error creating game: %s",exception.getMessage()), exception);
        }
    }


    @Override
    public GameData getGame(int gameId) throws DataAccessException {
        String statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game WHERE gameID = ?";
        try (var openConnection = DatabaseManager.getConnection();
             var getReady = openConnection.prepareStatement(statement)) {
            getReady.setInt(1, gameId);
            try (var results = getReady.executeQuery()) {
                if (results.next()) {
                    String returnedWhiteUsername = results.getString("whiteUsername");
                    String returnedBlackUsername = results.getString("blackUsername");
                    String returnedGameName = results.getString("gameName");
                    String returnedGameString = results.getString("game");
                    ChessGame parsedGame = serializer.fromJson(returnedGameString, ChessGame.class);

                    return new GameData(gameId, returnedWhiteUsername, returnedBlackUsername, returnedGameName, parsedGame);
                } else {
                    return null;
                }
            }
        } catch (SQLException exception) {
            throw new DataAccessException(String.format("Error getting game: %s",exception.getMessage()), exception);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String statement = "UPDATE game SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ? WHERE gameID = ?";
        String serializedGame = serializer.toJson(game.game());
        try (var openConnection = DatabaseManager.getConnection();
             var getReady = openConnection.prepareStatement(statement)) {
            getReady.setString(1, game.whiteUsername());
            getReady.setString(2, game.blackUsername());
            getReady.setString(3, game.gameName());
            getReady.setString(4, serializedGame);
            getReady.setInt(5, game.gameID());

            int rowsUpdated = getReady.executeUpdate();
            if (rowsUpdated == 0) {
                throw new DataAccessException("Error: Game does not exist.");
            }

            getReady.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException(String.format("Error updating game: %s",exception.getMessage()), exception);
        }
    }


    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        String statement ="SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game";
        Collection<GameData> gamesList = new ArrayList<>();
        try (var openConnection = DatabaseManager.getConnection();
             var getReady = openConnection.prepareStatement(statement)) {
            try (var results = getReady.executeQuery()) {
                while (results.next()) {
                    int returnedGameId = results.getInt("gameID");
                    String returnedWhiteUsername = results.getString("whiteUsername");
                    String returnedBlackUsername = results.getString("blackUsername");
                    String returnedGameName = results.getString("gameName");
                    String returnedGameString = results.getString("game");
                    ChessGame parsedGame = serializer.fromJson(returnedGameString, ChessGame.class);
                    GameData game = new GameData(returnedGameId, returnedWhiteUsername, returnedBlackUsername, returnedGameName,parsedGame);
                    gamesList.add(game);
                }
            }
        } catch (SQLException exception) {
            throw new DataAccessException(String.format("Error listing game: %s",exception.getMessage()), exception);
        }
        return gamesList;

    }
    @Override
    public void clear() throws DataAccessException {
        String statement = "TRUNCATE TABLE game";
        try (var openConnection = DatabaseManager.getConnection();
             var getReady = openConnection.prepareStatement(statement)) {
            getReady.executeUpdate();
        } catch (SQLException exception){
            throw new DataAccessException(String.format("Error clearing game: %s", exception.getMessage()), exception);
        }

    }
}

