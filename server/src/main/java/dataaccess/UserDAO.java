package dataaccess;
import model.UserData;

public interface UserDAO {
    void insertPlayer(UserData user) throws DataAccessException;
    UserData retrievePlayer(String username) throws DataAccessException;
    void clear( ) throws DataAccessException;

}
