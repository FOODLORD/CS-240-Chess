package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class LoginService {

    private final DataAccess dataAccess;

    public LoginService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public model.LoginResponse login(LoginRequest request) throws DataAccessException {

        if (request == null) {
            throw new DataAccessException("Error: bad request");
        }

        if (request.username() == null || request.password() == null) {
            throw new DataAccessException("Error: bad request");
        }

        UserData user = dataAccess.getUser(request.username());

        try {
            if (user == null || !BCrypt.checkpw(request.password(), user.password())) {
                throw new DataAccessException("Error: unauthorized");
            }
        } catch (IllegalArgumentException error) {

            throw new DataAccessException("Error: unauthorized");
        }

        String tokenString = UUID.randomUUID().toString();
        AuthToken token = new AuthToken(tokenString, user.username());

        dataAccess.insertAuth(token);

        return new model.LoginResponse(user.username(), tokenString);
    }
}