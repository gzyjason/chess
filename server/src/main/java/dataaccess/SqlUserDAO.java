package dataaccess;
import model.UserData;

import java.sql.SQLException;

public class SqlUserDAO implements UserDAO{

    public SqlUserDAO() throws DataAccessException{
        String createTable = """
            CREATE TABLE IF NOT EXISTS user (
                username VARCHAR(255) NOT NULL,
                password VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL,
                PRIMARY KEY (username)
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
    public void insertPlayer(UserData user) throws DataAccessException{

    }
    @Override
    public UserData retrievePlayer(String username) throws DataAccessException {
        return null;
    }

    @Override
    public void clear() throws DataAccessException {

    }

}
