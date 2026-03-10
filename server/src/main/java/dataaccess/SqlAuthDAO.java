package dataaccess;

import model.AuthData;

import java.sql.SQLException;

public class SqlAuthDAO implements AuthDAO{
    public SqlAuthDAO() throws DataAccessException{
        String createTable = """
            CREATE TABLE IF NOT EXISTS auth (
                authToken VARCHAR(255) NOT NULL,
                username VARCHAR(255) NOT NULL,
                PRIMARY KEY (authToken)
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
    public void createToken(AuthData auth) throws DataAccessException {

    }

    @Override
    public AuthData getToken(String authToken) throws DataAccessException {
        return null;
    }
    @Override
    public void deleteToken(String authToken) throws DataAccessException{

    }

    @Override
    public void clear() throws DataAccessException {
        String statement = "TRUNCATE TABLE user";
        try (var openConnection = DatabaseManager.getConnection();
             var getReady = openConnection.prepareStatement(statement)) {
            getReady.executeUpdate();
        } catch (SQLException exception){
            throw new DataAccessException(String.format("Error clearing player: %s", exception.getMessage()), exception);
        }

    }
}
