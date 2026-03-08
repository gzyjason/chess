package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.DataAccessException;
import dataaccess.MemoryUserDAO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import request.LoginRequest;
import request.RegisterRequest;
import results.LoginResult;
import results.RegisterResult;

public class UserServiceTests {
    private MemoryUserDAO userDAO;
    private MemoryAuthDAO authDAO;
    private UserService userService;

    @BeforeEach
    public void setup(){
        this.userDAO = new MemoryUserDAO();
        this.authDAO = new MemoryAuthDAO();
        this.userService = new UserService(userDAO, authDAO);
    }


    @Test
    public void registerSuccessTest() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("Tom", "password123", "abc123@byu.edu");
        RegisterResult result = userService.register(request);
        Assertions.assertNotNull(result.authToken(), "Service should return an authToken");
        Assertions.assertEquals("Tom", result.username());
        Assertions.assertNotNull(userDAO.retrievePlayer("Tom"), "User should be saved in UserDAO");
    }

    @Test
    public void registerUnavailableTest() throws DataAccessException {
        RegisterRequest request1 = new RegisterRequest("Tom", "password123", "abc123@byu.edu");
        userService.register(request1);

        RegisterRequest request2 = new RegisterRequest("Tom", "password123", "abc123@byu.edu");

        DataAccessException exception = Assertions.assertThrows(DataAccessException.class,
                () -> userService.register(request2));

        Assertions.assertEquals("Error: already taken", exception.getMessage());
    }

    @Test
    public void loginSuccessTest() throws DataAccessException {
        userService.register(new RegisterRequest("Tom", "password123", "abc123@byu.edu"));

        LoginRequest loginRequest = new LoginRequest("Tom", "password123");
        LoginResult loginResult = userService.login(loginRequest);

        Assertions.assertNotNull(loginResult.authToken(), "authToken should not be empty");
        Assertions.assertEquals("Tom", loginResult.username(), "username doesn't match");

    }

    @Test
    public void registerBadRequestTest() {
        RegisterRequest badRequest = new RegisterRequest("Tom", "password123", null);

        DataAccessException exception = Assertions.assertThrows(DataAccessException.class,
                () -> userService.register(badRequest));
        Assertions.assertEquals("Error: bad request", exception.getMessage());
    }

    @Test
    public void loginWrongPasswordTest() throws DataAccessException {
        userService.register(new RegisterRequest("Tom", "password123", "abc123@byu.edu"));
        LoginRequest loginRequest = new LoginRequest("Tom", "321drowssap");

        DataAccessException exception = Assertions.assertThrows(DataAccessException.class,
                () -> userService.login(loginRequest));

        Assertions.assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    public  void logoutSuccessTest() throws DataAccessException {
        RegisterResult registerResult = userService.register(new RegisterRequest("Tom", "password123",
                "abc123@byu.edu"));

        userService.logout(registerResult.authToken());

        Assertions.assertNull(authDAO.getToken(registerResult.authToken()));
    }

    @Test
    public void logoutInvalidTest() {
        String tempToken = "fake-token123";
        DataAccessException exception = Assertions.assertThrows(DataAccessException.class,
                () -> userService.logout(tempToken));

        Assertions.assertEquals("Error: unauthorized", exception.getMessage());
    }
}
