package service.mysqltests;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import chess.ChessGame;

import static org.junit.jupiter.api.Assertions.*;

public class MySqlClearTest {

    private DataAccess dataAccess;

    @BeforeEach
    void setup() throws DataAccessException {
        dataAccess = new MySqlDataAccess();
        dataAccess.clear();
    }

    @Test
    void clearDatabase() throws DataAccessException {

        // insert data
        dataAccess.registerUser(new UserData("nate","password","email@gmail.com"));

        String token = "321";
        dataAccess.insertAuth(new AuthToken(token,"nate"));

        ChessGame game = new ChessGame();
        dataAccess.createGame(new GameData(0,null,null,"game",game));

        // clear
        dataAccess.clear();

        assertNull(dataAccess.getUser("nate"));
        assertNull(dataAccess.getAuth(token));
        assertEquals(0, dataAccess.listGames().size());
    }
}