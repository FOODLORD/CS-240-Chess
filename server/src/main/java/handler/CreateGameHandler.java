package handler;

import io.javalin.http.Context;
import service.*;
import dataaccess.DataAccessException;

import com.google.gson.Gson;

public class CreateGameHandler extends BaseHandler{

    private final CreateGameService service;
    private final Gson gson = new Gson();

    public CreateGameHandler(CreateGameService service) {
        this.service = service;
    }

    public void createGame(Context body) {

        try {

            String authToken = body.header("authorization");

            model.CreateGameRequest request = gson.fromJson(body.body(), model.CreateGameRequest.class);

            model.CreateGameResponse response = service.createGame(authToken, request);

            body.status(200);
            body.json(response);

        }

        catch (DataAccessException error) {

            handleError(body, error);
        }
    }
}