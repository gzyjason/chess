package dataaccess;
import model.AuthData;

public interface AuthDAO {
    void createToken(AuthData auth) throws DataAccessException;
    AuthData getToken(String authToken) throws DataAccessException;
    void deleteToken(String authToken) throws DataAccessException;
    void clear() throws DataAccessException;
}
