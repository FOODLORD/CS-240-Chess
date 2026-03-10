package service.mysqltests;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import service.*;

import static org.junit.jupiter.api.Assertions.*;

public class MySqlCreateGameTest {

    private DataAccess dataAccess;
    private CreateGameService createGameService;
    private RegisterService registerService;
    private LoginService loginService;

    @BeforeEach
    void setup() throws DataAccessException {
        dataAccess = new MySqlDataAccess();
        dataAccess.clear();

        createGameService = new CreateGameService(dataAccess);
        registerService = new RegisterService(dataAccess);
        loginService = new LoginService(dataAccess);
    }

    @AfterEach
    void clear() throws DataAccessException {
        dataAccess.clear();
    }

    @Test
    void createGameSuccess() throws DataAccessException {

        RegisterRequest register = new RegisterRequest("tailor","password","email@gmail.com");

        registerService.register(register);

        LoginRequest login = new LoginRequest("tailor","password");

        LoginResponse loginResponse = loginService.login(login);

        String token = loginResponse.authToken();


        CreateGameRequest request = new CreateGameRequest("newgame");

        CreateGameResponse response = createGameService.createGame(token, request);

        assertNotNull(response);
        assertTrue(response.gameID() > 0);


        GameData game = dataAccess.getGame(response.gameID());

        assertNotNull(game);
        assertEquals("newgame", game.gameName());
    }

    @Test
    void createGameWrongAuth() {

        CreateGameRequest request = new CreateGameRequest("anothergame");

        assertThrows(DataAccessException.class, () -> {createGameService.createGame("WrongToken", request);});
    }
}