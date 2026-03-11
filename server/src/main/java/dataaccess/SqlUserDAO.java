package dataaccess;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
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
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        String statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        try (var openConnection = DatabaseManager.getConnection( );
             var getReady = openConnection.prepareStatement(statement)){
            getReady.setString(1, user.username());
            getReady.setString(2, hashedPassword);
            getReady.setString(3,user.email());
            getReady.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException(String.format("Error inserting player: %s", exception.getMessage()), exception);
        }

    }
    @Override
    public UserData retrievePlayer(String username) throws DataAccessException {
        String statement = "SELECT username, password, email FROM user WHERE username =?";
        try (var openConnection = DatabaseManager.getConnection();
             var getReady = openConnection.prepareStatement(statement)) {

            getReady.setString(1, username);

            try (var results = getReady.executeQuery()) {
                if (results.next()) {
                    String returnedUsername = results.getString("username");
                    String returnedPassword = results.getString("password");
                    String returnedEmail = results.getString("email");

                    return new UserData(returnedUsername, returnedPassword, returnedEmail);
                }
            }

        } catch (SQLException exception) {
            throw new DataAccessException(String.format("Error retrieving player: %s", exception.getMessage()), exception);
        }
        return null;
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
