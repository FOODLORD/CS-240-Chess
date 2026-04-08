package service;

import chess.ChessGame;
import dataaccess.*;
import model.*;

public class CreateGameService {

    private final DataAccess dataAccess;

    public CreateGameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public CreateGameResponse createGame(String authToken, model.CreateGameRequest request)
            throws DataAccessException {

        if (authToken == null || dataAccess.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        if (request == null || request.gameName() == null) {
            throw new DataAccessException("Error: bad request");
        }

        GameData game = new GameData(0, null, null, request.gameName(), new ChessGame());

        int gameID = dataAccess.createGame(game);

        return new CreateGameResponse(gameID);
    }
}