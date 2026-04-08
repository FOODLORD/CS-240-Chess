package websocket;
import chess.ChessGame;
import dataaccess.DataAccess;
import com.google.gson.Gson;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;

import websocket.commands.UserGameCommand;
import websocket.messages.*;


public class WebSocketHandler {
    private final ConnectionManager connectionManager = new ConnectionManager();
    private final DataAccess dataAccess;

    public WebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void onMessage(Session session, String message) throws Exception {

        var command = new Gson().fromJson(message, UserGameCommand.class);

        switch (command.getCommandType()) {
            case CONNECT -> connect(session, command);
            case MAKE_MOVE -> {
                var moveCommand = new Gson().fromJson(message, websocket.commands.MakeMoveCommand.class);
                makeMove(session, command);
            }
            case LEAVE -> leave(session, command);
            case RESIGN -> resign(session, command);
        }
    }

    private void connect(Session session, UserGameCommand command) throws Exception {

        var auth = dataAccess.getAuth(command.getAuthToken());
        String username = auth.username();


        connectionManager.add(command.getGameID(), new Connection(username, session));


        var gameData = dataAccess.getGame(command.getGameID());
        var game = gameData.game();

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

    private void makeMove(Session session, UserGameCommand command) throws Exception {

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


        var moveCommand = (websocket.commands.MakeMoveCommand) command;
        var move = moveCommand.getMove();


        try {
            game.makeMove(move);
        } catch (Exception error) {
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

    }

    private void leave(Session session, UserGameCommand command) throws Exception {}

    private void resign(Session session, UserGameCommand command) throws Exception {}
}
