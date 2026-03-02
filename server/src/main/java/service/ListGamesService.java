package service;

import dataaccess.*;
import model.*;

import java.util.Collection;

public class ListGamesService {

    private final DataAccess dataAccess;

    public ListGamesService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public ListGamesResponse listGames(String authToken)
            throws DataAccessException {

        if (authToken == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        if (dataAccess.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        Collection<GameData> games = dataAccess.listGames();

        return new ListGamesResponse(games);
    }
}