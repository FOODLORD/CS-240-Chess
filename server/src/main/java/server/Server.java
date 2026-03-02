package server;

import dataaccess.MemoryDataAccess;
import handler.*;
import io.javalin.*;
import service.*;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.

        //Register
        var database = new MemoryDataAccess();
        var registerService = new RegisterService(database);
        var registerHandler = new RegisterHandler(registerService);

        javalin.post("/user", registerHandler::register);

        //Clear
        ClearService clearService = new ClearService(database);
        ClearHandler clearHandler = new ClearHandler(clearService);

        javalin.delete("/db", clearHandler::clear);

        //Login
        var loginService = new LoginService(database);
        var loginHandler = new LoginHandler(loginService);
        javalin.post("/session", loginHandler::login);

        //Logout
        var logoutService = new LogoutService(database);
        var logoutHandler = new LogoutHandler(logoutService);

        javalin.delete("/session", logoutHandler::logout);

        //List games

        var listGamesService = new ListGamesService(database);
        var listGamesHandler = new ListGamesHandler(listGamesService);

        javalin.get("/game", listGamesHandler::listGames);

        //Create Game

        var createGameService = new CreateGameService(database);
        var createGameHandler = new CreateGameHandler(createGameService);

        javalin.post("/game", createGameHandler::createGame);

        //Join Game

        var joinGameService = new JoinGameService(database);
        var joinGameHandler = new JoinGameHandler(joinGameService);

        javalin.put("/game", joinGameHandler::joinGame);

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
