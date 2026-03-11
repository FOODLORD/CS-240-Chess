package dataaccess;

import model.*;
import chess.ChessGame;
import com.google.gson.Gson;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.*;

public class MySqlDataAccess implements DataAccess {

    public MySqlDataAccess() throws DataAccessException {

        configureDatabase();
    }

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();

        try (var conn = DatabaseManager.getConnection()) {

            var statement = conn.createStatement();

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    username VARCHAR(50) PRIMARY KEY,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(100) NOT NULL
                )
            """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS auth (
                    authToken VARCHAR(255) PRIMARY KEY,
                    username VARCHAR(50) NOT NULL
                )
            """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS games (
                    gameID INT AUTO_INCREMENT PRIMARY KEY,
                    whiteUsername VARCHAR(50),
                    blackUsername VARCHAR(50),
                    gameName VARCHAR(100),
                    gameState TEXT
                )
            """);

        } catch (SQLException error) {
            throw new DataAccessException("Error: Database setup failed", error);
        }
    }

    //user

    @Override
    public UserData getUser(String username) throws DataAccessException {

        String sql = "SELECT * FROM users WHERE username=?";

        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(sql)) {

            statement.setString(1, username);
            var rs = statement.executeQuery();

            if (rs.next()) {
                return new UserData(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email")
                );
            }

            return null;

        } catch (SQLException error) {
            throw new DataAccessException("Error: cannot retrieve user", error);
        }
    }

    @Override
    public void registerUser(UserData user) throws DataAccessException {

        String sql = """
            INSERT INTO users (username, password, email)
            VALUES (?, ?, ?)
        """;

        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(sql)) {

            String hashPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());

            statement.setString(1, user.username());
            statement.setString(2, hashPassword);
            statement.setString(3, user.email());

            statement.executeUpdate();

        } catch (SQLException error) {
            throw new DataAccessException("Error: Username already exists", error);
        }
    }

    //login

    @Override
    public void insertAuth(AuthToken auth) throws DataAccessException {

        String sql = "INSERT INTO auth (authToken, username) VALUES (?, ?)";

        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(sql)) {

            statement.setString(1, auth.authToken());
            statement.setString(2, auth.username());

            statement.executeUpdate();

        } catch (SQLException error) {
            throw new DataAccessException("Error: cannot insert auth token", error);
        }
    }

    @Override
    public AuthToken getAuth(String authToken) throws DataAccessException {

        String sql = "SELECT * FROM auth WHERE authToken=?";

        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(sql)) {

            statement.setString(1, authToken);
            var rs = statement.executeQuery();

            if (rs.next()) {
                return new AuthToken(
                        rs.getString("authToken"),
                        rs.getString("username")
                );
            }

            return null;

        } catch (SQLException error) {
            throw new DataAccessException("Error: cannot retrieve auth token", error);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {

        String sql = "DELETE FROM auth WHERE authToken=?";

        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(sql)) {

            statement.setString(1, authToken);
            statement.executeUpdate();

        } catch (SQLException error) {
            throw new DataAccessException("Error: cannot delete auth token", error);
        }
    }

    //game

    @Override
    public int createGame(GameData game) throws DataAccessException {

        if (game == null) {
            throw new DataAccessException("Error: empty game");
        }

        String sql = """
            INSERT INTO games (whiteUsername, blackUsername, gameName, gameState)
            VALUES (?, ?, ?, ?)
        """;

        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            Gson gson = new Gson();

            statement.setString(1, game.whiteUsername());
            statement.setString(2, game.blackUsername());
            statement.setString(3, game.gameName());
            statement.setString(4, gson.toJson(game.game()));

            statement.executeUpdate();

            var rs = statement.getGeneratedKeys();
            rs.next();

            return rs.getInt(1);

        } catch (SQLException error) {
            throw new DataAccessException("Error: cannot create game", error);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {

        String sql = "SELECT * FROM games WHERE gameID=?";

        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(sql)) {

            statement.setInt(1, gameID);

            var rs = statement.executeQuery();

            if (rs.next()) {

                String json = rs.getString("gameState");

                ChessGame game = null;

                if (json != null) {
                    try {
                        game = new Gson().fromJson(json, ChessGame.class);
                    } catch (Exception error) {
                        game = new ChessGame();
                    }
                }

                return new GameData(
                        rs.getInt("gameID"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        game
                );
            }

            return null;

        } catch (SQLException error) {
            throw new DataAccessException("Error: cannot retrieve game", error);
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {

        String sql = "SELECT * FROM games";

        List<GameData> games = new ArrayList<>();

        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(sql)) {

            var rs = statement.executeQuery();

            while (rs.next()) {

                games.add(new GameData(
                        rs.getInt("gameID"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        null
                ));
            }

            return games;

        } catch (SQLException error) {
            throw new DataAccessException("Error: cannot list games", error);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {

        String sql = """
            UPDATE games
            SET whiteUsername=?, blackUsername=?, gameName=?, gameState=?
            WHERE gameID=?
        """;

        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(sql)) {

            Gson gson = new Gson();

            statement.setString(1, game.whiteUsername());
            statement.setString(2, game.blackUsername());
            statement.setString(3, game.gameName());
            statement.setString(4, gson.toJson(game.game()));
            statement.setInt(5, game.gameID());

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated == 0) {
                throw new DataAccessException("Error: game does not exist");
            }

        } catch (SQLException error) {
            throw new DataAccessException("Error: cannot update game", error);
        }
    }

    //clear

    @Override
    public void clear() throws DataAccessException {

        try (var conn = DatabaseManager.getConnection()) {

            var statement = conn.createStatement();

            statement.executeUpdate("DELETE FROM users");
            statement.executeUpdate("DELETE FROM auth");
            statement.executeUpdate("DELETE FROM games");

            statement.executeUpdate("ALTER TABLE games AUTO_INCREMENT = 1");

        } catch (SQLException error) {
            throw new DataAccessException("Error: cannot clear database", error);
        }
    }
}