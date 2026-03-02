package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class ListGamesTests {

    @Test
    public void listGames() throws Exception {

        DataAccess dao = new MemoryDataAccess();

        RegisterService registerService = new RegisterService(dao);
        RegisterRequest request = new RegisterRequest("bob", "123", "bob@email.com");
        RegisterResponse registerResult = registerService.register(request);

        dao.createGame(new GameData(1, null, null, "Game 1", null));
        dao.createGame(new GameData(2, null, null, "Game 2", null));

        ListGamesService service = new ListGamesService(dao);

        ListGamesResponse result = service.listGames(registerResult.authToken());

        Collection<GameData> games = result.games();

        assertEquals(2, games.size());
    }

    @Test
    public void listGamesUnauthorized() {

        DataAccess dao = new MemoryDataAccess();
        ListGamesService service = new ListGamesService(dao);

        assertThrows(DataAccessException.class, () -> service.listGames("randomtoken"));
    }

    @Test
    public void listGamesEmptyList() throws Exception {

        DataAccess dao = new MemoryDataAccess();

        RegisterService registerService = new RegisterService(dao);
        RegisterRequest request = new RegisterRequest("bob", "123", "bob@email.com");
        RegisterResponse registerResult = registerService.register(request);

        ListGamesService service = new ListGamesService(dao);

        ListGamesResponse result = service.listGames(registerResult.authToken());

        assertTrue(result.games().isEmpty());
    }
}