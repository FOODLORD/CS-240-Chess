package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JoinGameTests {

    @Test
    public void joinGameWhite() throws Exception {

        DataAccess dao = new MemoryDataAccess();

        RegisterService registerService = new RegisterService(dao);
        RegisterResponse registerResult = registerService.register(new RegisterRequest("anny", "123", "anny@email.com"));

        CreateGameService createGameService = new CreateGameService(dao);
        CreateGameResponse createResult = createGameService.createGame(registerResult.authToken(), new CreateGameRequest("Game 1"));

        JoinGameService joinService = new JoinGameService(dao);

        joinService.joinGame(registerResult.authToken(), new JoinGameRequest("WHITE", createResult.gameID()));

        GameData updatedGame = dao.getGame(createResult.gameID());

        assertEquals("anny", updatedGame.whiteUsername());
        assertNull(updatedGame.blackUsername());
    }

    @Test
    public void joinGameUnauthorized() {

        DataAccess dao = new MemoryDataAccess();
        JoinGameService joinService = new JoinGameService(dao);

        assertThrows(DataAccessException.class, () -> joinService.joinGame("Token", new JoinGameRequest("WHITE", 1)));
    }

    @Test
    public void joinGameBadGameID() throws Exception {

        DataAccess dao = new MemoryDataAccess();

        RegisterService registerService = new RegisterService(dao);
        RegisterResponse registerResult =
                registerService.register(new RegisterRequest("anny", "123", "anny@email.com"));

        JoinGameService joinService = new JoinGameService(dao);

        assertThrows(DataAccessException.class, () ->
                joinService.joinGame(registerResult.authToken(), new JoinGameRequest("WHITE", 888))
        );
    }

    @Test
    public void joinGameAlreadyTaken() throws Exception {

        DataAccess dao = new MemoryDataAccess();

        RegisterService registerService = new RegisterService(dao);

        RegisterResponse user1 = registerService.register(new RegisterRequest("anny", "123", "anny@email.com"));

        RegisterResponse user2 = registerService.register(new RegisterRequest("lisa", "123", "lisa@email.com"));


        CreateGameService createGameService = new CreateGameService(dao);
        CreateGameResponse createResult = createGameService.createGame(user1.authToken(), new CreateGameRequest("Game 1"));

        JoinGameService joinService = new JoinGameService(dao);


        joinService.joinGame(user1.authToken(), new JoinGameRequest("WHITE", createResult.gameID()));

        assertThrows(DataAccessException.class, () -> joinService.joinGame(user2.authToken(), new JoinGameRequest("WHITE", createResult.gameID()))
        );
    }
}