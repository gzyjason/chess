package dataaccess;

import model.AuthData;

public class SqlAuthDAO implements AuthDAO{
    @Override
    public void createToken(AuthData auth) throws DataAccessException {

    }

    @Override
    public AuthData getToken(String authToken) throws DataAccessException {
        return null;
    }
    @Override
    public void deleteToken(String authToken) throws DataAccessException{

    }

    @Override
    public void clear() {

    }
}
