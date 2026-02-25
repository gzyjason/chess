package service;
import dataaccess.*;
import model.AuthData;
import model.UserData;
import request.RegisterRequest;
import results.RegisterResult;
import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO) {
        this.userDAO = userDAO;
    }

    public RegisterResult register(RegisterRequest request) throws DataAccessException {
        if (request.username() == null || request.password() == null || request.email() == null){
            throw new DataAccessException("Error: bad request");
        }
        if (userDAO.getUser(request.username()) != null) {
            throw new DataAccessException("Error: already taken");
        }

        UserData newUser = new UserData(request.username(), request.password(), request.email());
        userDAO.insertUser(newUser);

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, request.username());

        return new RegisterResult(request.username(), authToken);
    }
}
