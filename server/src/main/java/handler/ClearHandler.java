package handler;

import dataaccess.DataAccessException;
import io.javalin.http.Context;
import service.ClearService;

import java.util.Map;

public class ClearHandler {

    private final ClearService service;

    public ClearHandler(ClearService service) {
        this.service = service;
    }

    public void clear(Context body) {
        try {

            service.clear();

            body.status(200);
            body.json(Map.of());

        } catch (DataAccessException error) {

            body.status(500);
            body.json(Map.of("message", "Error: " + error.getMessage()));
        }
    }
}