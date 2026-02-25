package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import request.LoginRequest;
import request.RegisterRequest;
import results.LoginResult;
import results.RegisterResult;

public class UserServiceTests {
    private UserDAO userDAO;
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private UserService userService;

    @BeforeEach
    public void setup(){
        userDAO = new UserDAO();
        authDAO = new AuthDAO();
        gameDAO = new GameDAO();
        userService = new UserService(userDAO, authDAO, gameDAO);
    }

    @Test
    public void registerSuccessTest() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("Tom", "password123", "abc123@byu.edu");
        RegisterResult result = userService.register(request);
        Assertions.assertNotNull(result.authToken(), "Service should return an authToken");
        Assertions.assertEquals("Tom", result.username());
        Assertions.assertNotNull(userDAO.getUser("Tom"), "User should be saved in UserDAO");
    }

    @Test
    public void registerUnavailableTest() throws DataAccessException {
        RegisterRequest request1 = new RegisterRequest("Tom", "password123", "abc123@byu.edu");
        userService.register(request1);

        RegisterRequest request2 = new RegisterRequest("Tom", "password123", "abc123@byu.edu");

        DataAccessException exception = Assertions.assertThrows(DataAccessException.class, () -> {
            userService.register(request2);
        });

        Assertions.assertEquals("Error: unavailable", exception.getMessage());
    }

    @Test
    public void loginSuccessTest() throws DataAccessException {
        RegisterRequest registerRequest = new RegisterRequest("Tom", "password123",
                "abc123@byu.edu");
        RegisterResult registerResult = userService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest("Tom", "password123");
        LoginResult loginResult = userService.login(loginRequest);

        Assertions.assertNotNull(loginResult.authToken(), "authToken should not be empty");
        Assertions.assertEquals("Tom", loginResult.username(), "username doesn't match");

    }

    @Test
    public void loginWrongPasswordTest() throws DataAccessException {
        userService.register(new RegisterRequest("Tom", "password123", "abc123@byu.edu"));
        LoginRequest loginRequest = new LoginRequest("Tom", "321drowssap");

        DataAccessException exception = Assertions.assertThrows(DataAccessException.class, () -> {
            userService.login(loginRequest);
        });

        Assertions.assertEquals("Error: unauthorized", exception.getMessage());
    }
}
