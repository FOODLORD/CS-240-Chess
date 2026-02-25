package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthToken;

public class LogoutService {

    private final DataAccess dataAccess;

    public LogoutService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void logout(String authToken) throws DataAccessException {

        if (authToken == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        AuthToken token = dataAccess.getAuth(authToken);

        if (token == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        dataAccess.deleteAuth(authToken);
    }
}