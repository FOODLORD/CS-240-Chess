package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CreateGameTests {

    @Test
    public void createGame() throws Exception {

        DataAccess dao = new MemoryDataAccess();

        RegisterService registerService = new RegisterService(dao);
        RegisterRequest request = new RegisterRequest("tom", "123", "tom@email.com");
        RegisterResponse registerResult = registerService.register(request);

        CreateGameService service = new CreateGameService(dao);

        CreateGameRequest createRequest = new CreateGameRequest("New Game");

        CreateGameResponse result = service.createGame(registerResult.authToken(), createRequest);

        assertTrue(result.gameID() > 0);

        GameData storedGame = dao.getGame(result.gameID());
        assertEquals("New Game", storedGame.gameName());
    }

    @Test
    public void createGameUnauthorized() {

        DataAccess dao = new MemoryDataAccess();
        CreateGameService service = new CreateGameService(dao);

        CreateGameRequest request = new CreateGameRequest("Another Game");

        assertThrows(DataAccessException.class, () -> service.createGame("randomToken", request));
    }

    @Test
    public void createGameBadRequest() throws Exception {

        DataAccess dao = new MemoryDataAccess();

        RegisterService registerService = new RegisterService(dao);
        RegisterResponse registerResult =
                registerService.register(new RegisterRequest("tom", "123", "tom@email.com"));

        CreateGameService service = new CreateGameService(dao);

        CreateGameRequest badRequest = new CreateGameRequest(null);

        assertThrows(DataAccessException.class, () -> service.createGame(registerResult.authToken(), badRequest));
    }
}