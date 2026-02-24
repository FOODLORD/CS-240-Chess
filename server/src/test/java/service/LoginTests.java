package service;

import dataaccess.*;
import model.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoginTests {

    @Test
    public void loginSuccess() throws Exception {
        DataAccess dao = new MemoryDataAccess();

        dao.registerUser(new UserData("bob", "123", "bob@email.com"));

        LoginService service = new LoginService(dao);

        LoginRequest request = new LoginRequest("bob", "123");

        LoginResponse result = service.login(request);

        assertEquals("bob", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    public void loginWrongPassword() throws Exception {
        DataAccess dao = new MemoryDataAccess();
        dao.registerUser(new UserData("bob", "123", "bob@email.com"));

        LoginService service = new LoginService(dao);

        LoginRequest request = new LoginRequest("bob", "wrong");

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
        dao.registerUser(new UserData("bob", "123", "bob@email.com"));

        LoginService service = new LoginService(dao);

        LoginResponse result = service.login(new LoginRequest("bob", "123"));

        assertNotNull(dao.getAuth(result.authToken()));
    }
}