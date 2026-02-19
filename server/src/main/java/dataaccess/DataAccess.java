package dataaccess;

import model.*;
import java.util.Collection;

public interface DataAccess {
    //login
    UserData getUser(String username) throws DataAccessException;

    void insertAuth(AuthToken auth) throws DataAccessException;
    AuthToken getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;

    //register
    void registerUser(UserData user) throws DataAccessException;

    //game
    void insertGame(GameData game) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    Collection<GameData> listGames() throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;


    //clear
    void clear() throws DataAccessException;
}