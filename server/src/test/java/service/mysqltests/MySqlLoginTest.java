package service.mysqltests;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import service.*;

import static org.junit.jupiter.api.Assertions.*;

public class MySqlLoginTest {

    private DataAccess dataAccess;
    private LoginService loginService;
    private RegisterService registerService;

    @BeforeEach
    void setup() throws DataAccessException {
        dataAccess = new MySqlDataAccess();
        dataAccess.clear();

        loginService = new LoginService(dataAccess);
        registerService = new RegisterService(dataAccess);
    }

    @AfterEach
    void clear() throws DataAccessException {
        dataAccess.clear();
    }

    @Test
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
    void wrongPasswordLogin() throws DataAccessException {

        RegisterRequest register = new RegisterRequest("person","password","email@mail.com");

        registerService.register(register);


        LoginRequest login = new LoginRequest("user1","p@ssw0rd");

        assertThrows(DataAccessException.class, () -> {loginService.login(login);});
    }
}