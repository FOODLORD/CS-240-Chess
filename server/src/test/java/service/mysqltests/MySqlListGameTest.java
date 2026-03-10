package service.mysqltests;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import service.*;

import static org.junit.jupiter.api.Assertions.*;

public class MySqlListGameTest {

    private DataAccess dataAccess;
    private ListGamesService listGamesService;
    private CreateGameService createGameService;
    private RegisterService registerService;
    private LoginService loginService;

    @BeforeEach
    void setup() throws DataAccessException {
        dataAccess = new MySqlDataAccess();
        dataAccess.clear();

        listGamesService = new ListGamesService(dataAccess);
        createGameService = new CreateGameService(dataAccess);
        registerService = new RegisterService(dataAccess);
        loginService = new LoginService(dataAccess);
    }

    @AfterEach
    void clear() throws DataAccessException {
        dataAccess.clear();
    }

    @Test
    void listGamesSuccess() throws DataAccessException {


        RegisterRequest register = new RegisterRequest("cath","password","email@gmail.com");

        registerService.register(register);


        LoginRequest login = new LoginRequest("cath","password");

        LoginResponse loginResponse = loginService.login(login);
        String token = loginResponse.authToken();


        createGameService.createGame(token, new CreateGameRequest("first"));
        createGameService.createGame(token, new CreateGameRequest("second"));


        ListGamesResponse response = listGamesService.listGames(token);

        assertNotNull(response);
        assertEquals(2, response.games().size());
    }

    @Test
    void listGamesWrongAuth() {

        assertThrows(DataAccessException.class, () -> {listGamesService.listGames("WrongToken");});
    }
}