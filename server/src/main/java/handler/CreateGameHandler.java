package handler;

import io.javalin.http.Context;
import service.*;
import dataaccess.DataAccessException;

import com.google.gson.Gson;
import java.util.Map;

public class CreateGameHandler {

    private final CreateGameService service;
    private final Gson gson = new Gson();

    public CreateGameHandler(CreateGameService service) {
        this.service = service;
    }

    public void createGame(Context body) {

        try {

            String authToken = body.header("authorization");

            CreateGameRequest request =
                    gson.fromJson(body.body(), CreateGameRequest.class);

            CreateGameResponse response =
                    service.createGame(authToken, request);

            body.status(200);
            body.json(response);

        }

        catch (DataAccessException e) {

            if (e.getMessage().contains("unauthorized")) {
                body.status(401);
            } else if (e.getMessage().contains("bad request")) {
                body.status(400);
            } else {
                body.status(500);
            }

            body.json(Map.of("message", e.getMessage()));
        }
    }
}