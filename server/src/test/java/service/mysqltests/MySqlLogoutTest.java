package service.mysqltests;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import service.*;

import static org.junit.jupiter.api.Assertions.*;

public class MySqlLogoutTest {

    private DataAccess dataAccess;
    private LogoutService logoutService;
    private RegisterService registerService;
    private LoginService loginService;

    @BeforeEach
    void setup() throws DataAccessException {
        dataAccess = new MySqlDataAccess();
        dataAccess.clear();

        logoutService = new LogoutService(dataAccess);
        registerService = new RegisterService(dataAccess);
        loginService = new LoginService(dataAccess);
    }

    @AfterEach
    void clear() throws DataAccessException {
        dataAccess.clear();
    }

    @Test
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
    void logoutWithWrongToken() {

        assertThrows(DataAccessException.class, () -> {logoutService.logout("tokenformnowhere");});
    }
}