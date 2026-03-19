package service;

import dataaccess.*;
import model.*;

import java.util.UUID;
import org.mindrot.jbcrypt.BCrypt;

public class RegisterService {

    private final DataAccess dataAccess;

    public RegisterService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public RegisterResponse register(model.RegisterRequest request) throws DataAccessException {

        if (request == null) {
            throw new DataAccessException("Error: bad request");
        }

        // check request
        if (request.username() == null || request.password() == null || request.email() == null) {
            throw new DataAccessException("Error: bad request");
        }

        // check current username
        if (dataAccess.getUser(request.username()) != null) {
            throw new DataAccessException("Error: already taken");
        }


        String hashedPassword = BCrypt.hashpw(request.password(), BCrypt.gensalt());


        UserData user = new UserData(
                request.username(),
                hashedPassword,
                request.email()
        );

        dataAccess.registerUser(user);

        //auth token
        String token = UUID.randomUUID().toString();
        AuthToken auth = new AuthToken(token, request.username());
        dataAccess.insertAuth(auth);

        return new RegisterResponse(request.username(), token);
    }
}
