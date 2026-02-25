package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import request.RegisterRequest;
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
}
