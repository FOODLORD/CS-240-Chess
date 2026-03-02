package handler;

import io.javalin.http.Context;
import service.*;

import dataaccess.DataAccessException;
import java.util.Map;

public class ListGamesHandler {

    private final ListGamesService service;

    public ListGamesHandler(ListGamesService service) {
        this.service = service;
    }

    public void listGames(Context body) {
        try {

            String authToken = body.header("authorization");

            ListGamesResponse response =
                    service.listGames(authToken);

            body.status(200);
            body.json(response);

        }

        catch (DataAccessException error) {

            String message = error.getMessage();

            if (message != null && message.contains("unauthorized")) {
                body.status(401);
            }

            else {
                body.status(500);
            }

            body.json(Map.of("message", error.getMessage()));
        }
    }
}