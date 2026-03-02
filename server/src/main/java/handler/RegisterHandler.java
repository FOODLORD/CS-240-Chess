package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import service.*;

import java.util.Map;

public class RegisterHandler {

    private final RegisterService service;
    private final Gson gson = new Gson();

    public RegisterHandler(RegisterService service) {
        this.service = service;
    }

    public void register(Context body) {
        try {
            RegisterRequest request = gson.fromJson(body.body(), RegisterRequest.class);

            if (request == null || request.username() == null || request.password() == null || request.email() == null) {

                body.status(400);
                body.json(Map.of("message", "Error: bad request"));
                return;
            }

            RegisterResponse result = service.register(request);

            body.status(200);
            body.json(result);

        }

        catch (DataAccessException error) {

            String message = error.getMessage();

            if (message != null && message.contains("already taken")) {
                body.status(403);
            }
            else if (message != null && message.contains("bad request")) {
                body.status(400);
            }
            else {
                body.status(500);
            }

            body.json(Map.of("message", error.getMessage()));
        }
    }
}
