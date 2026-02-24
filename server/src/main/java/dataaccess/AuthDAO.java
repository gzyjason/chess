package dataaccess;
import model.AuthData;
import java.util.HashMap;
import java.util.Map;

public class AuthDAO {
    private final Map<String, AuthData> authDataMap = new HashMap<>();

    public void createAuth(AuthData auth) throws DataAccessException {
        authDataMap.put(auth.authToken(), auth);
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        return authDataMap.get(authToken);
    }

    public void deleteAuth(String authToken) throws DataAccessException {
        authDataMap.remove(authToken);
    }

    public void clear() throws DataAccessException {
        authDataMap.clear();
    }

}
