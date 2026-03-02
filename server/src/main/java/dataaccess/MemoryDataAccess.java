package dataaccess;

import model.*;
import java.util.*;

public class MemoryDataAccess implements DataAccess {

    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthToken> authTokens = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextGameID = 1;

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return users.get(username);
    }

    @Override
    public void insertAuth(AuthToken auth) throws DataAccessException {
        authTokens.put(auth.authToken(), auth);

    }

    @Override
    public AuthToken getAuth(String authToken) throws DataAccessException {
        return authTokens.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        authTokens.remove(authToken);

    }

    @Override
    public void registerUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())) {
            throw new DataAccessException("Username already taken");
        }
        users.put(user.username(), user);

    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        int id = nextGameID++;
        GameData newGame = new GameData(id, null, null, game.gameName(), null);
        games.put(id, newGame);
        return id;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return games.get(gameID);
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return games.values();
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {

    }

    @Override
    public void clear() throws DataAccessException {
        users.clear();
        games.clear();
        authTokens.clear();

    }

}