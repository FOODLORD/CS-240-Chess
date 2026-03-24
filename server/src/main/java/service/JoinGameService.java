package service;

import dataaccess.*;
import model.*;

public class JoinGameService {

    private final DataAccess dataAccess;

    public JoinGameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void joinGame(String authToken, model.JoinGameRequest request) throws DataAccessException {

        if (authToken == null || dataAccess.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        if (request == null) {
            throw new DataAccessException("Error: bad request");
        }

        GameData game = dataAccess.getGame(request.gameID());

        if (game == null) {
            throw new DataAccessException("Error: bad request");
        }

        String username = dataAccess.getAuth(authToken).username();

        String color = request.playerColor();


        if (color == null) {
            throw new DataAccessException("Error: bad request");
        }

        if (color.equals("WHITE")) {

            if (game.whiteUsername() != null) {
                throw new DataAccessException("Error: already taken");
            }

            game = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());

        }

        else if (color.equals("BLACK")) {

            if (game.blackUsername() != null) {
                throw new DataAccessException("Error: already taken");
            }

            game = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());

        }

        else {
            throw new DataAccessException("Error: bad request");
        }

        dataAccess.updateGame(game);
    }
}