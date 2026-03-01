package service;

import chess.ChessGame;
import dataaccess.*;
import model.GameData;
import request.CreateGameRequest;
import results.CreateGameResult;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO){
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }
    public CreateGameResult createGame (String authToken, CreateGameRequest request) throws DataAccessException {
        if (authDAO.getAuth(authToken) == null){
            throw new DataAccessException("Error: unauthorized");
        }

        if (request.gameName() == null || request.gameName().isEmpty()) {
            throw  new DataAccessException("Error: bad request");
        }

        int newGameID = gameDAO.createGame(new GameData(0, null, null, request.gameName(), new ChessGame()));

        return new CreateGameResult(newGameID);
    }
}
