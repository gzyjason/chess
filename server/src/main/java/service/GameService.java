package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import request.CreateGameRequest;
import request.JoinGameRequest;
import results.CreateGameResult;
import results.ListGamesResult;
import java.util.Collection;

public class GameService {
    private final GameDAO myGameDAO;
    private final AuthDAO myAuthDAO;

    public GameService(GameDAO myGameDAO, AuthDAO myAuthDAO){
        this.myGameDAO = myGameDAO;
        this.myAuthDAO = myAuthDAO;
    }


    public CreateGameResult createGame (String authToken, CreateGameRequest request) throws DataAccessException {
        if (myAuthDAO.getToken(authToken) == null){
            throw new DataAccessException("Error: unauthorized");
        }
        if (request.gameName() == null || request.gameName().isEmpty()) {
            throw  new DataAccessException("Error: bad request");
        }

        int newGame = myGameDAO.createGame(new GameData(0, null, null,
                request.gameName(),new ChessGame()));

        return new CreateGameResult(newGame);
    }

   public ListGamesResult listGames (String authToken) throws DataAccessException {
        if (myAuthDAO.getToken(authToken)  == null){
            throw new DataAccessException("Error: unauthorized");
        }

        Collection<GameData> games = myGameDAO.listGames();
        return new ListGamesResult(games);
    }

    public void joinGame(String authToken, JoinGameRequest request) throws DataAccessException {
        AuthData authorized = myAuthDAO.getToken(authToken);
        if (authorized == null){
            throw new DataAccessException("Error: unauthorized");
        }

        GameData game = myGameDAO.getGame(request.gameID());
        if (game == null) {
            throw new DataAccessException("Error: bad request");
        }

        String username = authorized.username();
        String team = request.playerColor();

        if (team != null) {
            if  (team.equalsIgnoreCase("WHITE")) {
                if(game.teamAUsername() !=  null) {
                    throw new DataAccessException("Error: already taken");
                }
                game= new GameData(game.gameID(), username, game.teamBUsername(), game.gameName(), game.game());
            } else if (team.equalsIgnoreCase("BLACK")) {
                if (game.teamBUsername() != null) {

                    throw new DataAccessException("Error: already taken");
                }

                game =new GameData(game.gameID(), game.teamAUsername(), username, game.gameName(), game.game());
            } else {
                throw new DataAccessException("Error: bad request");

            }

            myGameDAO.updateGame(game);
        }
    }
}
