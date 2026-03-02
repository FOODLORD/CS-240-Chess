package service;

import dataaccess.*;
import model.*;

public class CreateGameService {

    private final DataAccess dataAccess;

    public CreateGameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public CreateGameResponse createGame(String authToken, CreateGameRequest request)
            throws DataAccessException {

        if (authToken == null || dataAccess.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        if (request == null || request.gameName() == null) {
            throw new DataAccessException("Error: bad request");
        }

        GameData game = new GameData(0, null, null, request.gameName(), null);

        int gameID = dataAccess.createGame(game);

        return new CreateGameResponse(gameID);
    }
}