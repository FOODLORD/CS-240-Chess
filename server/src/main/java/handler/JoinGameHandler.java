package handler;

import io.javalin.http.Context;
import service.*;
import dataaccess.DataAccessException;

import com.google.gson.Gson;
import java.util.Map;

public class JoinGameHandler {

    private final JoinGameService service;
    private final Gson gson = new Gson();

    public JoinGameHandler(JoinGameService service) {
        this.service = service;
    }

    public void joinGame(Context body) {

        try {

            String authToken = body.header("authorization");

            JoinGameRequest request = gson.fromJson(body.body(), JoinGameRequest.class);

            service.joinGame(authToken, request);

            body.status(200);
            body.json(Map.of());

        } catch (DataAccessException error) {

            if (error.getMessage().contains("unauthorized")) {
                body.status(401);
            }

            else if (error.getMessage().contains("already taken")) {
                body.status(403);
            }

            else if (error.getMessage().contains("bad request")) {
                body.status(400);
            }

            else {
                body.status(500);
            }

            body.json(Map.of("message", error.getMessage()));
        }
    }
}