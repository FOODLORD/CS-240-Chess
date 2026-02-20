package service;

import dataaccess.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClearTests {

    @Test
    public void clearSuccess() throws Exception {

        DataAccess dao = new MemoryDataAccess();

        RegisterService registerService = new RegisterService(dao);
        ClearService clearService = new ClearService(dao);

        // Add user
        RegisterRequest request = new RegisterRequest("bob", "123", "bob@email.com");
        registerService.register(request);

        // Clear database
        clearService.clear();

        assertNull(dao.getUser("bob"));
    }

    @Test
    public void clearAllowsRegisterAgain() throws Exception {

        DataAccess dao = new MemoryDataAccess();

        RegisterService registerService = new RegisterService(dao);
        ClearService clearService = new ClearService(dao);

        RegisterRequest request = new RegisterRequest("bob", "123", "bob@email.com");

        registerService.register(request);
        clearService.clear();

        //can register again
        RegisterResponse result = registerService.register(request);

        assertEquals("bob", result.username());
        assertNotNull(result.authToken());
    }
}