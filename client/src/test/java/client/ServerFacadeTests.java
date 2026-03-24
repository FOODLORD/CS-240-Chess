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


}
