package server;

import dataaccess.MemoryDataAccess;
import handler.RegisterHandler;
import io.javalin.*;
import service.RegisterService;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        var database = new MemoryDataAccess();
        var registerService = new RegisterService(database);
        var registerHandler = new RegisterHandler(registerService);

        javalin.post("/user", registerHandler::register);

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
