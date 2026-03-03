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

    public void clear() {
        userDao.clear();
        myAuthDAO.clear();
        myGameDAO.clear();
    }
}
