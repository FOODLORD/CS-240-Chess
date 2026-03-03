package handler;

import io.javalin.http.Context;
import java.util.Map;

public abstract class BaseHandler {

    protected void handleError(Context body, Exception error) {
        String message = error.getMessage();

        if (message == null) {
            message = "Error: message is null";
        }

        if (message.contains("unauthorized")) {
            body.status(401);
        } else if (message.contains("bad request")) {
            body.status(400);
        } else if (message.contains("already taken")) {
            body.status(403);
        } else {
            body.status(500);
        }

        body.json(Map.of("message", message));
    }
}