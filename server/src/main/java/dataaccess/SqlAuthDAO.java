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
        String statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        try (var openConnection = DatabaseManager.getConnection( );
             var getReady = openConnection.prepareStatement(statement)){
            getReady.setString(1, auth.authToken());
            getReady.setString(2, auth.username());

            getReady.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException(String.format("Error creating token: %s", exception.getMessage()), exception);
        }
    }

    @Override
    public AuthData getToken(String authToken) throws DataAccessException {
        String statement = "SELECT authToken, username FROM auth WHERE authToken =?";
        try (var openConnection = DatabaseManager.getConnection();
             var getReady = openConnection.prepareStatement(statement)) {

            getReady.setString(1, authToken);

            try (var results = getReady.executeQuery()) {
                if (results.next()) {
                    String returnedAuthToken = results.getString("authToken");
                    String returnedUsername = results.getString("username");

                    return new AuthData(returnedAuthToken, returnedUsername);
                }
            }

        } catch (SQLException exception) {
            throw new DataAccessException(String.format("Error retrieving token: %s", exception.getMessage()), exception);
        }
        return null;
    }

    @Override
    public void deleteToken(String authToken) throws DataAccessException{
        String statement ="DELETE FROM auth WHERE authToken = ?";
        try (var openConnection = DatabaseManager.getConnection();
             var getReady = openConnection.prepareStatement(statement)) {
            getReady.setString(1, authToken);
            getReady.executeUpdate();
        } catch (SQLException exception){
            throw new DataAccessException(String.format("Error deleting token: %s", exception.getMessage()), exception);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String statement = "TRUNCATE TABLE auth";
        try (var openConnection = DatabaseManager.getConnection();
             var getReady = openConnection.prepareStatement(statement)) {
            getReady.executeUpdate();
        } catch (SQLException exception){
            throw new DataAccessException(String.format("Error clearing token: %s", exception.getMessage()), exception);
        }

    }
}
