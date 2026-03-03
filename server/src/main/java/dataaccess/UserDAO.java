package dataaccess;
import model.UserData;
import java.util.HashMap;
import java.util.Map;

public class UserDAO {
    private final Map<String, UserData> users = new HashMap<>();

    public void insertPlayer(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())){
            throw new DataAccessException("Username taken");
        }
        users.put(user.username(),user);
    }

    public UserData retrievePlayer(String username) {
        return users.get(username);
    }

    public void clear( ) {
        users.clear();
    }
}
