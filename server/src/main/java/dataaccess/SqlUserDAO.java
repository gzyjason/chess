package dataaccess;
import model.UserData;

public class SqlUserDAO implements UserDAO{
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
