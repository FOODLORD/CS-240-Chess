package service;

import dataaccess.*;
import model.*;

import java.util.UUID;

public class RegisterService {

    private final DataAccess dataAccess;

    public RegisterService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public RegisterResponse register(RegisterRequest request) throws DataAccessException {

        // check request
        if (request.username() == null || request.password() == null || request.email() == null) {
            throw new DataAccessException("An error occurred");
        }

        // check current username
        if (dataAccess.getUser(request.username()) != null) {
            throw new DataAccessException("Error username already taken");
        }

        //register user
        UserData user = new UserData(request.username(), request.password(), request.email());

        dataAccess.registerUser(user);

        //auth token
        String token = UUID.randomUUID().toString();
        AuthToken auth = new AuthToken(token, request.username());
        dataAccess.insertAuth(auth);

        return new RegisterResponse(request.username(), token);
    }
}
