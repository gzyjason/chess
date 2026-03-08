package dataaccess;
import model.AuthData;
import java.util.HashMap;
import java.util.Map;

public class MemoryAuthDAO implements AuthDAO{
    private final Map<String, AuthData> authTokens = new HashMap<> ();

    @Override
    public void createToken(AuthData auth)  {
        authTokens.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getToken(String authToken) {
        return authTokens.get(authToken);
    }

    @Override
    public void deleteToken(String authToken) {
        authTokens.remove(authToken);
    }


    @Override
    public void  clear() {
        authTokens.clear();
    }

}
