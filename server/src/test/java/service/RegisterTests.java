package service;

import dataaccess.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterTests {

    @Test
    public void registerSuccess() throws Exception {
        DataAccess dao = new MemoryDataAccess();
        RegisterService service = new RegisterService(dao);

        model.RegisterRequest request = new model.RegisterRequest("willy", "123", "willy@email.com");

        model.RegisterResponse result = service.register(request);

        assertEquals("willy", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    public void registerDuplicateUser() throws Exception {
        DataAccess dao = new MemoryDataAccess();
        RegisterService service = new RegisterService(dao);

        model.RegisterRequest request = new model.RegisterRequest("willy", "123", "willy@email.com");

        service.register(request);

        assertThrows(DataAccessException.class, () -> {service.register(request);});
    }
}
