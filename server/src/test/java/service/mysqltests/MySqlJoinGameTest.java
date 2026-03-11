package service.mysqltests;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import service.*;

import static org.junit.jupiter.api.Assertions.*;

public class MySqlJoinGameTest {

    private DataAccess dataAccess;
    private JoinGameService joinGameService;
    private CreateGameService createGameService;
    private RegisterService registerService;
    private LoginService loginService;

    @BeforeEach
    void setup() throws DataAccessException {
        dataAccess = new MySqlDataAccess();
        dataAccess.clear();

        joinGameService = new JoinGameService(dataAccess);
        createGameService = new CreateGameService(dataAccess);
        registerService = new RegisterService(dataAccess);
        loginService = new LoginService(dataAccess);
    }

    @AfterEach
    void cleanup() throws DataAccessException {
        dataAccess.clear();
    }

    @Test
    void joinGameSuccess() throws DataAccessException {


        registerService.register(new RegisterRequest("user1","password","email@test.com"));


        LoginResponse login = loginService.login(new LoginRequest("user1","password"));

        String token = login.authToken();


        CreateGameResponse game = createGameService.createGame(token, new CreateGameRequest("Game1"));

        int gameID = game.gameID();

        JoinGameRequest join = new JoinGameRequest("WHITE", gameID);

        joinGameService.joinGame(token, join);

        GameData Game_In_Progress = dataAccess.getGame(gameID);

        assertEquals("user1", Game_In_Progress.whiteUsername());
    }

    @Test
    void joinGameNoID() {

        JoinGameRequest join = new JoinGameRequest("WHITE", 999);

        try {
            joinGameService.joinGame("someToken", join);

            fail("Expected DataAccessException");

        } catch (DataAccessException error) {
            assertNotNull(error);
        }
    }
}