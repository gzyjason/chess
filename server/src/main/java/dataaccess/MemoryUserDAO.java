package dataaccess;
import model.UserData;
import java.util.HashMap;
import java.util.Map;

public class MemoryUserDAO implements UserDAO {

    private final Map<String, UserData> users = new HashMap<>();

    @Override
    public void insertPlayer(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())){
            throw new DataAccessException("Username taken");
        }
        users.put(user.username(),user);
    }

    @Override
    public UserData retrievePlayer(String username) {
        return users.get(username);
    }

    @Override
    public void clear( ) throws DataAccessException {
        users.clear();
    }
}
