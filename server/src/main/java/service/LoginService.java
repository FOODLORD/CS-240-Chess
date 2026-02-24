package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthToken;
import model.UserData;

import java.util.UUID;

public class LoginService {

    private final DataAccess dataAccess;

    public LoginService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public LoginResponse login(LoginRequest request) throws DataAccessException {

        if (request.username() == null || request.password() == null) {
            throw new DataAccessException("Error: bad request");
        }

        UserData user = dataAccess.getUser(request.username());

        if (user == null || !user.password().equals(request.password())) {
            throw new DataAccessException("Error: unauthorized");
        }

        String tokenString = UUID.randomUUID().toString();
        AuthToken token = new AuthToken(tokenString, user.username());

        dataAccess.insertAuth(token);

        return new LoginResponse(user.username(), tokenString);
    }
}