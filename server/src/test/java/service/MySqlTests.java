package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import chess.ChessGame;

import static org.junit.jupiter.api.Assertions.*;

public class MySqlTests {

    private DataAccess dataAccess;
    private JoinGameService joinGameService;
    private CreateGameService createGameService;
    private RegisterService registerService;
    private LoginService loginService;
    private LogoutService logoutService;
    private ListGamesService listGamesService;

    @BeforeEach
    void setup() throws DataAccessException {
        dataAccess = new MySqlDataAccess();
        dataAccess.clear();

        joinGameService = new JoinGameService(dataAccess);
        createGameService = new CreateGameService(dataAccess);
        registerService = new RegisterService(dataAccess);
        loginService = new LoginService(dataAccess);
        logoutService = new LogoutService(dataAccess);
        listGamesService = new ListGamesService(dataAccess);
    }

    @Test
    @DisplayName("MySqlClearTest")
    void clearDatabase() throws DataAccessException {

        // insert data
        dataAccess.registerUser(new UserData("nate","password","email@gmail.com"));

        String token = "321";
        dataAccess.insertAuth(new AuthToken(token,"nate"));

        ChessGame game = new ChessGame();
        dataAccess.createGame(new GameData(0,null,null,"game",game));

        // clear
        dataAccess.clear();

        assertNull(dataAccess.getUser("nate"));
        assertNull(dataAccess.getAuth(token));
        assertEquals(0, dataAccess.listGames().size());
    }

    @Test
    @DisplayName("MySqlRegisterSuccess")
    void registerSuccess() throws DataAccessException {

        RegisterRequest request = new RegisterRequest("han","password","email@gmail.com");

        RegisterResponse response = registerService.register(request);

        assertNotNull(response);
        assertEquals("han", response.username());
        assertNotNull(response.authToken());

        UserData user = dataAccess.getUser("han");
        assertNotNull(user);
    }

    @Test
    @DisplayName("MySqlRegisterDuplicate")
    void registerDuplicate() throws DataAccessException {

        RegisterRequest request = new RegisterRequest("han","password","email@gmail.com");

        registerService.register(request);

        // duplicate
        assertThrows(DataAccessException.class, () -> {registerService.register(request);});
    }

    @Test
    @DisplayName("MySqlLoginSuccess")
    void loginSuccess() throws DataAccessException {

        RegisterRequest register = new RegisterRequest("person","password","email@gmail.com");

        registerService.register(register);

        LoginRequest login = new LoginRequest("person","password");

        LoginResponse response = loginService.login(login);

        assertNotNull(response);
        assertEquals("person", response.username());
        assertNotNull(response.authToken());
    }

    @Test
    @DisplayName("MySqlLoginWrongPassword")
    void wrongPasswordLogin() throws DataAccessException {

        RegisterRequest register = new RegisterRequest("person","password","email@mail.com");

        registerService.register(register);


        LoginRequest login = new LoginRequest("person","p@ssw0rd");

        assertThrows(DataAccessException.class, () -> {loginService.login(login);});
    }

    @Test
    @DisplayName("MySqlLogoutSuccess")
    void logoutSuccess() throws DataAccessException {


        RegisterRequest register = new RegisterRequest("lilo","password","email@gmail.com");

        registerService.register(register);

        LoginRequest login = new LoginRequest("lilo","password");

        LoginResponse response = loginService.login(login);

        String token = response.authToken();



        logoutService.logout(token);


        AuthToken auth = dataAccess.getAuth(token);

        assertNull(auth);
    }

    @Test
    @DisplayName("MySqlLogoutWrongToken")
    void logoutWithWrongToken() {

        assertThrows(DataAccessException.class, () -> {logoutService.logout("tokenformnowhere");});
    }

    @Test
    @DisplayName("MySqlCreateGameSuccess")
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
    @DisplayName("MySqlCreateGameWrongAuth")
    void createGameWrongAuth() {

        CreateGameRequest request = new CreateGameRequest("anothergame");

        assertThrows(DataAccessException.class, () -> {createGameService.createGame("WrongToken", request);});
    }

    @Test
    @DisplayName("MySqlListGameSuccess")
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
    @DisplayName("MySqlListFameWrongAuth")
    void listGamesWrongAuth() {

        assertThrows(DataAccessException.class, () -> {listGamesService.listGames("WrongToken");});
    }

    @Test
    @DisplayName("MySqlJoinGameSuccess")
    void joinGameSuccess() throws DataAccessException {


        registerService.register(new RegisterRequest("crox","password","email@gmail.com"));


        LoginResponse login = loginService.login(new LoginRequest("crox","password"));

        String token = login.authToken();


        CreateGameResponse game = createGameService.createGame(token, new CreateGameRequest("FirstEverGame"));

        int gameID = game.gameID();

        JoinGameRequest join = new JoinGameRequest("WHITE", gameID);

        joinGameService.joinGame(token, join);

        GameData GameInProgress = dataAccess.getGame(gameID);

        assertEquals("crox", GameInProgress.whiteUsername());
        assertNotNull(GameInProgress);
    }

    @Test
    @DisplayName("MySqlJoinGameNoID")
    void joinGameNoID() {

        JoinGameRequest join = new JoinGameRequest("WHITE", 369);

        try {
            joinGameService.joinGame("someToken", join);

            fail("Expected DataAccessException");

        } catch (DataAccessException error) {
            assertNotNull(error);
        }
    }


}