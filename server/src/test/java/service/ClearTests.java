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
        model.RegisterRequest request = new model.RegisterRequest("anna", "123", "anna@email.com");
        registerService.register(request);

        // Clear database
        clearService.clear();

        assertNull(dao.getUser("anna"));
    }

    @Test
    public void clearAllowsRegisterAgain() throws Exception {

        DataAccess dao = new MemoryDataAccess();

        RegisterService registerService = new RegisterService(dao);
        ClearService clearService = new ClearService(dao);

        model.RegisterRequest request = new model.RegisterRequest("anna", "123", "anna@email.com");

        registerService.register(request);
        clearService.clear();

        //can register again
        model.RegisterResponse result = registerService.register(request);

        assertEquals("anna", result.username());
        assertNotNull(result.authToken());
    }
}