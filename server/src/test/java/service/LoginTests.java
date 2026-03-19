package service;

import dataaccess.*;
import model.UserData;
import org.junit.jupiter.api.Test;
import model.*;

import static org.junit.jupiter.api.Assertions.*;

public class LoginTests {

    @Test
    public void loginSuccess() throws Exception {
        DataAccess dao = new MemoryDataAccess();

        RegisterService registerService = new RegisterService(dao);
        registerService.register(new RegisterRequest("bob", "123", "bob@email.com"));

        LoginService service = new LoginService(dao);

        LoginRequest request = new LoginRequest("bob", "123");

        model.LoginResponse result = service.login(request);

        assertEquals("bob", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    public void loginWrongPassword() throws Exception {
        DataAccess dao = new MemoryDataAccess();
        RegisterService registerService = new RegisterService(dao);
        registerService.register(new RegisterRequest("tate", "123", "tate@email.com"));

        LoginService service = new LoginService(dao);

        LoginRequest request = new LoginRequest("tate", "wrong");

        assertThrows(DataAccessException.class, () -> {service.login(request);});
    }

    @Test
    public void loginUserDoesNotExist() throws Exception {
        DataAccess dao = new MemoryDataAccess();
        LoginService service = new LoginService(dao);

        LoginRequest request = new LoginRequest("wowowo", "123");

        assertThrows(DataAccessException.class, () -> {service.login(request);});
    }

    @Test
    public void loginMissingUsername() throws Exception {
        DataAccess dao = new MemoryDataAccess();
        LoginService service = new LoginService(dao);

        LoginRequest request = new LoginRequest(null, "123");

        assertThrows(DataAccessException.class, () -> {service.login(request);});
    }

    @Test
    public void loginStoresAuthToken() throws Exception {
        DataAccess dao = new MemoryDataAccess();
        RegisterService registerService = new RegisterService(dao);
        registerService.register(new RegisterRequest("tate", "123", "tate@email.com"));

        LoginService service = new LoginService(dao);

        model.LoginResponse result = service.login(new LoginRequest("tate", "123"));

        assertNotNull(dao.getAuth(result.authToken()));
    }
}