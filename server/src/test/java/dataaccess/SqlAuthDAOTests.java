package dataaccess;
import model.AuthData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class SqlAuthDAOTests {
    private SqlAuthDAO authDAO;
    @BeforeEach
    public void setUp() throws DataAccessException {
        authDAO = new SqlAuthDAO();
        authDAO.clear();
    }

    @Test
    public void createTokenPositive() throws DataAccessException{
        AuthData testToken = new AuthData("randomToken", "Tom");
        authDAO.createToken(testToken);
        AuthData returnedToken = authDAO.getToken("randomToken");

        assertNotNull(returnedToken, "The returned token shouldn't be null");
        assertEquals(testToken, returnedToken, "The returned token should match the created token");
    }

    @Test
    public void createTokenNegative() throws DataAccessException{
        AuthData testToken = new AuthData("randomToken", "Tom");
        authDAO.createToken(testToken);
        AuthData testTokenAgain = new AuthData("randomToken", "Jack");
        assertThrows(DataAccessException.class, () -> {
            authDAO.createToken(testTokenAgain);
        }, "Shouldn't be able to create duplicate tokens");
    }

    @Test
    public void getTokenPositive() throws DataAccessException {
        AuthData testToken = new AuthData("randomToken", "Tom");
        authDAO.createToken(testToken);
        AuthData returnedToken = authDAO.getToken("randomToken");

        assertNotNull(returnedToken, "The returned token shouldn't be null");
        assertEquals(testToken, returnedToken, "The returned token should match the created token");
    }

    @Test
    public void getTokenNegative() throws DataAccessException{
        AuthData testToken = new AuthData("randomToken", "Tom");
        authDAO.createToken(testToken);
        AuthData returnedToken = authDAO.getToken("fakeToken");
        assertNull(returnedToken, "Should not be able to retrieve nonexisting token");
    }

    @Test
    public void deleteTokenPositive() throws DataAccessException{
        AuthData testToken = new AuthData("randomToken", "Tom");
        authDAO.createToken(testToken);
        authDAO.deleteToken("randomToken");
        AuthData returnedToken = authDAO.getToken("randomToken");
        assertNull(returnedToken, "Getting deleted token should result in null");
    }

    @Test
    public void deleteTokenNegative() throws DataAccessException {
        AuthData testToken = new AuthData("randomToken", "Tom");
        authDAO.createToken(testToken);

        assertDoesNotThrow(() -> {
            authDAO.deleteToken("fakeToken");
        }, "Deleting a non-existent token should not throw an exception");

        assertNotNull(authDAO.getToken("randomToken"), "The existing token should still exist");
    }

    @Test
    public void clearPositive() throws DataAccessException{
        AuthData testToken = new AuthData("randomToken", "Tom");
        authDAO.createToken(testToken);
        authDAO.clear();
        AuthData cleared = authDAO.getToken("randomToken");
        assertNull(cleared, "The token shouldn't exist after clear is called");
    }
}
