package client;

import exception.ResponseException;
import model.*;
import org.junit.jupiter.api.*;
import server.Server;

import java.util.ArrayList;
import java.util.List;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        facade = new ServerFacade("http://localhost:" + port);
        System.out.println("Started test HTTP server on " + port);
    }

    @BeforeEach
    public void clearDatabase() throws Exception {
        facade.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }


    @Test
    @DisplayName("registerSuccess")
    public void registerSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest("manny", "password", "email@gmail.com");

        RegisterResponse response = facade.register(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("manny", response.username());
        Assertions.assertNotNull(response.authToken());
    }

    @Test
    @DisplayName("registerEmptyName")
    public void registerBadRequest() {
        RegisterRequest request = new RegisterRequest(null, "password", "email@gmail.com.com"
        );

        Assertions.assertThrows(ResponseException.class, () -> {facade.register(request);});
    }

    @Test
    @DisplayName("loginSuccess")
    public void loginSuccess() throws Exception {

        facade.register(new RegisterRequest("someguy", "password", "email@gmail.com"));


        LoginResponse response = facade.login(new LoginRequest("someguy", "password"));

        Assertions.assertNotNull(response);
        Assertions.assertEquals("someguy", response.username());
        Assertions.assertNotNull(response.authToken());
    }

    @Test
    @DisplayName("wrongPass")
    public void loginWrongPassword() throws Exception {
        facade.register(new RegisterRequest("someguy", "password", "email@gmail.com"));

        Assertions.assertThrows(ResponseException.class, () -> {
            facade.login(new LoginRequest("someguy", "blehhh"));
        });
    }

    @Test
    @DisplayName("haventregister")
    public void loginNoRegister() {
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.login(new LoginRequest("blank", "password"));
        });
    }

    @Test
    @DisplayName("noUsername")
    public void loginNoUser() {
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.login(new LoginRequest(null, "pass"));
        });
    }

    @Test
    @DisplayName("logoutSuccess")
    public void logoutSuccess() throws Exception {

        facade.register(new RegisterRequest("logoutUser", "pass", "email@test.com"));
        LoginResponse login = facade.login(new LoginRequest("logoutUser", "pass"));

        String token = login.authToken();


        facade.logout(token);

        Assertions.assertTrue(true);
    }

    @Test
    @DisplayName("logoutFakeToken")
    public void logoutFakeToken() {
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.logout("testtoken");
        });
    }

    @Test
    @DisplayName("logoutTwice")
    public void logoutTwice() throws Exception {
        facade.register(new RegisterRequest("lilo", "password", "email@gmail.com"));
        LoginResponse login = facade.login(new LoginRequest("lilo", "password"));

        String token = login.authToken();

        facade.logout(token);

        Assertions.assertThrows(ResponseException.class, () -> {facade.logout(token);});
    }

    @Test
    @DisplayName("logoutNoToken")
    public void logoutNoToken() {
        Assertions.assertThrows(ResponseException.class, () -> {facade.logout(null);});
    }

    @Test
    @DisplayName("createGameSuccess")
    public void createGameSuccess() throws Exception {

        facade.register(new RegisterRequest("lily", "password", "email@gmail.com"));
        LoginResponse login = facade.login(new LoginRequest("lily", "password"));

        String token = login.authToken();

        CreateGameResponse response = facade.createGame(token, new CreateGameRequest("Newgame"));

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.gameID() > 0);
    }

    @Test
    @DisplayName("createGameBadToken")
    public void createGameBadToken() {

        Assertions.assertThrows(ResponseException.class, () -> {
            facade.createGame("notatoken", new CreateGameRequest("Anothergame"));
        });
    }

    @Test
    @DisplayName("createGameNoName")
    public void createGameNoName() throws Exception {

        facade.register(new RegisterRequest("noname", "noname", "email@gmail.com"));
        LoginResponse login = facade.login(new LoginRequest("noname", "noname"));

        String token = login.authToken();

        Assertions.assertThrows(ResponseException.class, () -> {
            facade.createGame(token, new CreateGameRequest(null));
        });
    }

    @Test
    @DisplayName("createGameNoToken")
    public void createGameNoToken() {

        Assertions.assertThrows(ResponseException.class, () -> {
            facade.createGame(null, new CreateGameRequest("notoken"));
        });
    }

    @Test
    @DisplayName("listGamesEmpty")
    public void listGamesEmpty() throws Exception {

        facade.register(new RegisterRequest("lisa", "password", "email@gmail.com"));
        LoginResponse login = facade.login(new LoginRequest("lisa", "password"));

        String token = login.authToken();

        ListGamesResponse response = facade.listGames(token);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(0, response.games().size());
    }

    @Test
    @DisplayName("listGamesSuccess")
    public void listGamesSuccess() throws Exception {

        facade.register(new RegisterRequest("lisa", "password", "email@gmail.com"));
        LoginResponse login = facade.login(new LoginRequest("lisa", "password"));

        String token = login.authToken();

        facade.createGame(token, new CreateGameRequest("Gaming"));
        facade.createGame(token, new CreateGameRequest("Gamed"));

        ListGamesResponse response = facade.listGames(token);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(2, response.games().size());
    }

    @Test
    @DisplayName("listGamesNoToken")
    public void listGamesNoToken() {

        Assertions.assertThrows(ResponseException.class, () -> {
            facade.listGames(null);
        });
    }

    @Test
    @DisplayName("listGamesCheckName")
    public void listGamesCheckName() throws Exception {

        facade.register(new RegisterRequest("listUser3", "pass", "email@test.com"));
        LoginResponse login = facade.login(new LoginRequest("listUser3", "pass"));

        String token = login.authToken();

        facade.createGame(token, new CreateGameRequest("CoolGame"));

        ListGamesResponse response = facade.listGames(token);
        List<GameData> games = new ArrayList<>(response.games());

        Assertions.assertEquals("CoolGame", games.getFirst().gameName());
    }

    @Test
    @DisplayName("joinGameWhite")
    public void joinGameWhite() throws Exception {

        facade.register(new RegisterRequest("nemo", "password", "email@gmail.com"));
        LoginResponse login = facade.login(new LoginRequest("nemo", "password"));

        String token = login.authToken();

        CreateGameResponse game = facade.createGame(token, new CreateGameRequest("First"));

        Assertions.assertDoesNotThrow(() -> {
            facade.joinGame(token, new JoinGameRequest("WHITE", game.gameID()));
        });
    }

    @Test
    @DisplayName("joinGameBlack")
    public void joinGameBlack() throws Exception {

        facade.register(new RegisterRequest("dora", "password", "email@gmail.com"));
        LoginResponse login = facade.login(new LoginRequest("dora", "password"));

        String token = login.authToken();

        CreateGameResponse game = facade.createGame(token, new CreateGameRequest("Second"));

        Assertions.assertDoesNotThrow(() -> {
            facade.joinGame(token, new JoinGameRequest("BLACK", game.gameID()));
        });
    }

    @Test
    @DisplayName("joinGameInvalidGameID")
    public void joinGameInvalidGameID() throws Exception {

        facade.register(new RegisterRequest("stitch", "password", "email@gmail.com"));
        LoginResponse login = facade.login(new LoginRequest("stitch", "password"));

        String token = login.authToken();

        Assertions.assertThrows(ResponseException.class, () -> {
            facade.joinGame(token, new JoinGameRequest("WHITE", 369));
        });
    }

    @Test
    @DisplayName("joinGameSameTeam")
    public void joinGameSameTeam() throws Exception {


        facade.register(new RegisterRequest("sam", "password", "email@gmail.com"));
        LoginResponse login1 = facade.login(new LoginRequest("sam", "password"));
        String token1 = login1.authToken();

        CreateGameResponse game = facade.createGame(token1, new CreateGameRequest("okay"));

        facade.joinGame(token1, new JoinGameRequest("WHITE", game.gameID()));


        facade.register(new RegisterRequest("tam", "password", "email@gmail.com"));
        LoginResponse login2 = facade.login(new LoginRequest("tam", "password"));
        String token2 = login2.authToken();

        Assertions.assertThrows(ResponseException.class, () -> {
            facade.joinGame(token2, new JoinGameRequest("WHITE", game.gameID()));
        });
    }




}
