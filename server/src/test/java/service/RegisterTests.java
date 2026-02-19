package service;

import dataaccess.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterTests {

    @Test
    public void registerSuccess() throws Exception {
        DataAccess dao = new MemoryDataAccess();
        RegisterService service = new RegisterService(dao);

        RegisterRequest request = new RegisterRequest("bob", "123", "bob@email.com");

        RegisterResponse result = service.register(request);

        assertEquals("bob", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    public void registerDuplicateUser() throws Exception {
        DataAccess dao = new MemoryDataAccess();
        RegisterService service = new RegisterService(dao);

        RegisterRequest request = new RegisterRequest("bob", "123", "bob@email.com");

        service.register(request);

        assertThrows(DataAccessException.class, () -> {service.register(request);});
    }
}
