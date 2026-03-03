package service;
import dataaccess.*;
import model.AuthData;
import model.UserData;
import request.LoginRequest;
import request.RegisterRequest;
import results.LoginResult;
import results.RegisterResult;
import java.util.UUID;


public class UserService {
    private final UserDAO userDao;
    private final AuthDAO myAuthDAO;

    public UserService(UserDAO userDao, AuthDAO myAuthDAO) {
        this.userDao = userDao;
        this.myAuthDAO = myAuthDAO;
    }

    public RegisterResult register(RegisterRequest request) throws DataAccessException {
        //cannot register if any required fields are missing
        //the requirements for the course specify that bad request be thrown as exception in this situation, in format used
        if (request.username() == null || request.password( ) == null || request.email() == null){
            throw new DataAccessException("Error: bad request");
        }

        //I don't believe that a specific exception is required for this purpose
        if (userDao.retrievePlayer(request.username()) != null) {
            throw new DataAccessException("Username taken");
        }

        UserData user = new UserData(request.username(), request.password(), request.email());
        userDao.insertPlayer(user);

        String authToken = UUID.randomUUID().toString();
        myAuthDAO.createToken(new AuthData(authToken, request.username()));

        return new RegisterResult(request.username(), authToken);
    }

    public LoginResult login(LoginRequest request) throws DataAccessException {
        UserData user = userDao.retrievePlayer(request.username());

        if (user == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        if (!user.password().equals(request.password())) {
            throw new DataAccessException("Error: unauthorized");
        }

        String authToken = UUID.randomUUID().toString();
        myAuthDAO.createToken(new AuthData(authToken, request.username()));

        return new LoginResult(request.username(), authToken);
    }

    public void logout(String authToken) throws DataAccessException {
        if (myAuthDAO.getToken(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        myAuthDAO.deleteToken(authToken);
    }
}
