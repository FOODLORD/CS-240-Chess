package client;

import exception.ResponseException;
import model.*;
import org.junit.jupiter.api.*;
import server.Server;


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


}
