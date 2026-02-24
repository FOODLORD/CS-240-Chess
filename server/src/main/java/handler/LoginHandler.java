package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import service.*;

import java.util.Map;

public class LoginHandler {

    private final LoginService service;
    private final Gson gson = new Gson();

    public LoginHandler(LoginService service) {
        this.service = service;
    }

    public void login(Context body) {
        try {
            LoginRequest request = gson.fromJson(body.body(), LoginRequest.class);

            LoginResponse result = service.login(request);

            body.status(200);
            body.json(result);
        }

        catch (DataAccessException error) {

            if (error.getMessage().contains("unauthorized")) {
                body.status(401);
            } else if (error.getMessage().contains("bad request")) {
                body.status(400);
            } else {
                body.status(500);
            }

            body.json(Map.of("Error", error.getMessage()));
        }
    }
}