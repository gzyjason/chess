package dataaccess;
import model.UserData;
import java.util.HashMap;
import java.util.Map;

public class UserDAO {
    private final Map<String, UserData> userDataMap = new HashMap<>();

    public void insertUser(UserData user) throws DataAccessException {
        if (userDataMap.containsKey(user.username())){
            throw new DataAccessException("Error: username unavailable");
        }
        userDataMap.put(user.username(), user);
    }

    public UserData getUser(String username) throws DataAccessException {
        return userDataMap.get(username);
    }

    public void clear() {
        userDataMap.clear();
    }
}
