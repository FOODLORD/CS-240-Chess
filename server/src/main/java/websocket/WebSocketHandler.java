package websocket;
import chess.ChessGame;
import dataaccess.DataAccess;
import com.google.gson.Gson;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;

import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.*;


public class WebSocketHandler {
    private final ConnectionManager connectionManager = new ConnectionManager();
    private final DataAccess dataAccess;

    public WebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void onMessage(Session session, String message) throws Exception {

        Gson gson = new Gson();
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

        switch (command.getCommandType()) {
            case CONNECT -> connect(session, command);
            case MAKE_MOVE -> {
                MakeMoveCommand moveCommand = gson.fromJson(message, MakeMoveCommand.class);
                makeMove(session, moveCommand);
            }
            case LEAVE -> leave(session, command);
            case RESIGN -> resign(session, command);
        }
    }

    private void connect(Session session, UserGameCommand command) throws Exception {

        var auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            throw new Exception("Error: unauthorized");
        }
        String username = auth.username();


        connectionManager.add(command.getGameID(), new Connection(username, session));


        var gameData = dataAccess.getGame(command.getGameID());
        if (gameData == null) {
            throw new Exception("Error: bad request");
        }

        var game = gameData.game();
        if (game == null) {
            game = new ChessGame();
        }

        session.getRemote().sendString(new Gson().toJson(new LoadGameMessage(game)));

        String message;

        if (username.equals(gameData.whiteUsername())) {
            message = username + " joined as WHITE";
        } else if (username.equals(gameData.blackUsername())) {
            message = username + " joined as BLACK";
        } else {
            message = username + " joined as an observer";
        }

        connectionManager.broadcast(
                command.getGameID(),
                session,
                new NotificationMessage(message)
        );
    }

    private void makeMove(Session session, MakeMoveCommand command) throws Exception {

        var auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            throw new Exception("Error: unauthorized");
        }
        String username = auth.username();

        int gameID = command.getGameID();


        var gameData = dataAccess.getGame(gameID);
        if (gameData == null || gameData.game() == null) {
            throw new Exception("Error: bad request");
        }

        var game = gameData.game();

        if (game.getGameOver()) {
            throw new Exception("Error: game already over");
        }

        ChessGame.TeamColor playerColor;

        if (username.equals(gameData.whiteUsername())) {
            playerColor = ChessGame.TeamColor.WHITE;
        } else if (username.equals(gameData.blackUsername())) {
            playerColor = ChessGame.TeamColor.BLACK;
        } else {
            throw new Exception("Error: observer cannot make moves");
        }


        if (game.getTeamTurn() != playerColor) {
            throw new Exception("Error: not player turn");
        }

        var move = command.getMove();

        boolean isCheckmate;
        boolean isStalemate;

        try {
            game.makeMove(move);
            ChessGame.TeamColor nextTurn = game.getTeamTurn();
            isCheckmate = game.isInCheckmate(nextTurn);
            isStalemate = game.isInStalemate(nextTurn);
        }
        catch (Exception error) {
            throw new Exception("Error: invalid move");
        }


        GameData updatedGame = new GameData(
                gameID,
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                game
        );

        dataAccess.updateGame(updatedGame);


        connectionManager.broadcast(
                gameID,
                null,
                new LoadGameMessage(game)
        );


        String message = username + " moved from " + move.getStartPosition() + " to " + move.getEndPosition();

        connectionManager.broadcast(
                gameID,
                session,
                new NotificationMessage(message)
        );

        if (isCheckmate) {
            connectionManager.broadcast(
                    gameID,
                    null,
                    new NotificationMessage("Checkmate! Game over.")
            );
        }
        else if (isStalemate) {
            connectionManager.broadcast(
                    gameID,
                    null,
                    new NotificationMessage("Stalemate! Game over.")
            );
        }


    }

    private void leave(Session session, UserGameCommand command) throws Exception {
        var auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            throw new Exception("Error: unauthorized");
        }

        String username = auth.username();
        int gameID = command.getGameID();


        connectionManager.remove(gameID, session);

        var gameData = dataAccess.getGame(gameID);
        if (gameData == null) {
            throw new Exception("Error: bad request");
        }

        String whiteUsername = gameData.whiteUsername();
        String blackUsername = gameData.blackUsername();
        String gameName = gameData.gameName();
        ChessGame game = gameData.game();

        boolean leftGame = false;

        if (username.equals(whiteUsername)) {
            whiteUsername = null;
            leftGame = true;
        }

        else if (username.equals(blackUsername)) {
            blackUsername = null;
            leftGame = true;
        }

        if (leftGame) {
            GameData updatedGame = new GameData(
                    gameID,
                    whiteUsername,
                    blackUsername,
                    gameName,
                    game
            );

            dataAccess.updateGame(updatedGame);
        }

        connectionManager.broadcast(
                gameID,
                session,
                new NotificationMessage(username + " left the game")
        );
    }

    private void resign(Session session, UserGameCommand command) throws Exception {
        var auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            throw new Exception("Error: unauthorized");
        }

        String username = auth.username();
        int gameID = command.getGameID();

        var gameData = dataAccess.getGame(gameID);
        if (gameData == null || gameData.game() == null) {
            throw new Exception("Error: bad request");
        }

        var game = gameData.game();


        boolean isWhite = username.equals(gameData.whiteUsername());
        boolean isBlack = username.equals(gameData.blackUsername());

        if (!isWhite && !isBlack) {
            throw new Exception("Error: observers cannot resign");
        }


        if (game.getGameOver()) {
            throw new Exception("Error: game already over");
        }


        game.setGameOver(true);


        GameData updatedGame = new GameData(
                gameID,
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                game
        );

        dataAccess.updateGame(updatedGame);


        String message = username + " resigned. Game over.";

        connectionManager.broadcast(
                gameID,
                null,
                new NotificationMessage(message)
        );

    }
}
