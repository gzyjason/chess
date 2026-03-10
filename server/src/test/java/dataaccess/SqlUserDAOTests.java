package dataaccess;
import model.UserData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;


public class SqlUserDAOTests {
    private SqlUserDAO userDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        userDAO= new SqlUserDAO();
        userDAO.clear();
    }

    @Test
    public void insertPlayerPositive() throws DataAccessException {
        UserData testUser = new UserData("Tom", "password123", "abc123@byu.edu");
        userDAO.insertPlayer(testUser);
        UserData returnedUser = userDAO.retrievePlayer("Tom");
        assertNotNull(returnedUser, "The returned user should not be null");
        assertEquals(testUser, returnedUser, "The returned user should match the inserted user");
    }

    @Test
    public void insertPlayerNegative() throws DataAccessException{
        UserData testUser = new UserData("Tom", "password123", "abc123@byu.edu");
        userDAO.insertPlayer(testUser);
        UserData testUserAgain = new UserData("Tom", "password456", "xyz456@byu.edu");
        assertThrows(DataAccessException.class, () -> {
            userDAO.insertPlayer(testUserAgain);
        }, "Should not be able to insert duplicate username");
    }


    @Test
    public void retrievePlayerPositive() throws DataAccessException{
        UserData testUser = new UserData("Tom", "password123", "abc123@byu.edu");
        userDAO.insertPlayer(testUser);

        UserData returnedUser =userDAO.retrievePlayer("Tom");

        assertNotNull(returnedUser, "The returned user should not be null");
        assertEquals(testUser, returnedUser, "The returned user should match the inserted user");
    }

    @Test
    public void retrievePlayerNegative() throws DataAccessException {
        UserData testUser = new UserData("Tom", "password123", "abc123@byu.edu");
        userDAO.insertPlayer(testUser);
        UserData returnedUser =userDAO.retrievePlayer("Jack");

        assertNull(returnedUser, "Should not be able to retrieve nonexisting user");
    }

    @Test
    public void clearPositive() throws DataAccessException{
        UserData testUser = new UserData("Tom", "password123", "abc123@byu.edu");
        userDAO.insertPlayer(testUser);
        userDAO.clear();
        UserData cleared = userDAO.retrievePlayer("Tom");
        assertNull(cleared, "The user shouldn't exist after clear is called");
    }
}
