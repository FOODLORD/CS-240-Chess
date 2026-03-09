package service.mysqltests;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import service.RegisterRequest;
import service.RegisterResponse;
import service.RegisterService;

import static org.junit.jupiter.api.Assertions.*;

public class MySqlRegisterTest {

    private DataAccess dataAccess;
    private RegisterService registerService;

    @BeforeEach
    void setup() throws DataAccessException {
        dataAccess = new MySqlDataAccess();
        dataAccess.clear();
        registerService = new RegisterService(dataAccess);
    }

    @AfterEach
    void clear() throws DataAccessException {
        dataAccess.clear();
    }

    @Test
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
    void registerDuplicate() throws DataAccessException {

        RegisterRequest request = new RegisterRequest("han","password","email@gmail.com");

        registerService.register(request);

        // duplicate
        assertThrows(DataAccessException.class, () -> {registerService.register(request);});
    }
}