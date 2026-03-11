package service;

import dataaccess.*;

//basic class used for clearing all stored information before starting new game
//all other services and DAOs rely on this class
public class ClearService {
    private final UserDAO userDao;
    private final AuthDAO myAuthDAO;
    private final GameDAO myGameDAO;

    public ClearService(UserDAO userDao, AuthDAO myAuthDAO, GameDAO myGameDAO){
        this.userDao = userDao;
        this.myAuthDAO = myAuthDAO;
        this.myGameDAO = myGameDAO;
    }

    public void clear( ) throws DataAccessException {
        try {
            userDao.clear( );
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        myAuthDAO.clear();
        myGameDAO.clear();

    }
}
