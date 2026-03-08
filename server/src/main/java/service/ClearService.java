package service;

import dataaccess.*;

//basic class used for clearing all stored information before starting new game
//all other services and DAOs rely on this class
public class ClearService {
    private final MemoryUserDAO userDao;
    private final MemoryAuthDAO myAuthDAO;
    private final MemoryGameDAO myGameDAO;

    public ClearService(MemoryUserDAO userDao, MemoryAuthDAO myAuthDAO, MemoryGameDAO myGameDAO){
        this.userDao = userDao;
        this.myAuthDAO = myAuthDAO;
        this.myGameDAO = myGameDAO;
    }

    public void clear( ) {
        try {
            userDao.clear( );
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        myAuthDAO.clear();
        myGameDAO.clear();

    }
}
