package dataaccess;
import model.AuthData;
import java.util.HashMap;
import java.util.Map;

public class AuthDAO {
    private final Map<String, AuthData> authTokens = new HashMap<> ();

    public void createToken(AuthData auth)  {
        authTokens.put(auth.authToken(), auth);
    }

    public AuthData getToken(String authToken) {
        return authTokens.get(authToken);
    }
    public void deleteToken(String authToken) {
        authTokens.remove(authToken);
    }


    public void  clear() {
        authTokens.clear();
    }

}
