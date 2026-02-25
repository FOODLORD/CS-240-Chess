package service;

import dataaccess.*;
import model.AuthToken;
import model.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LogoutTests {

    @Test
    public void logoutSuccess() throws Exception {
        DataAccess dao = new MemoryDataAccess();

        dao.registerUser(new UserData("bob", "123", "bob@email.com"));

        AuthToken token = new AuthToken("abc", "bob");
        dao.insertAuth(token);

        LogoutService service = new LogoutService(dao);

        service.logout("abc");

        assertNull(dao.getAuth("abc"));
    }

    @Test
    public void logoutInvalidToken() throws Exception {
        DataAccess dao = new MemoryDataAccess();
        LogoutService service = new LogoutService(dao);

        assertThrows(DataAccessException.class, () -> {
            service.logout("nothing");
        });
    }

    @Test
    public void logoutNullToken() throws Exception {
        DataAccess dao = new MemoryDataAccess();
        LogoutService service = new LogoutService(dao);

        assertThrows(DataAccessException.class, () -> {
            service.logout(null);
        });
    }
}