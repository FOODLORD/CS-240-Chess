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
}
