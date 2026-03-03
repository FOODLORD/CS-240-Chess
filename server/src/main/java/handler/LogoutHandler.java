package handler;

import dataaccess.DataAccessException;
import io.javalin.http.Context;
import service.LogoutService;

import java.util.Map;

public class LogoutHandler extends BaseHandler{

    private final LogoutService service;

    public LogoutHandler(LogoutService service) {
        this.service = service;
    }

    public void logout(Context body) {

        try {
            String authToken = body.header("Authorization");

            service.logout(authToken);

            body.status(200);
            body.json(Map.of());

        } catch (DataAccessException error) {

            handleError(body, error);
        }
    }
}