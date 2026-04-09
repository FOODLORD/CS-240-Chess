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

    public void onMessage(Session session, String message) {

        Gson gson = new Gson();

        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

            if (command == null || command.getCommandType() == null) {
                ErrorMessage error = new ErrorMessage("Error: invalid command");
                session.getRemote().sendString(gson.toJson(error));
                return;
            }

            switch (command.getCommandType()) {
                case CONNECT -> connect(session, command);
                case MAKE_MOVE -> {
                    MakeMoveCommand moveCommand = gson.fromJson(message, MakeMoveCommand.class);
                    makeMove(session, moveCommand);
                }
                case LEAVE -> leave(session, command);
                case RESIGN -> resign(command);
            }
        }
        catch (Exception e) {
            System.out.println("WebSocket error: " + e.getMessage());

            try {
                if (session != null && session.isOpen()) {
                    ErrorMessage error = new ErrorMessage("Error: " + e.getMessage());
                    session.getRemote().sendString(gson.toJson(error));
                }
            }
            catch (Exception ignored) {

            }
        }
    }

    private void connect(Session session, UserGameCommand command) throws Exception {

        var auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            throw new Exception("Error: unauthorized");
        }

        String username = auth.username();

        var gameData = dataAccess.getGame(command.getGameID());
        if (gameData == null) {
            throw new Exception("Error: bad request");
        }

        var game = gameData.game();
        if (game == null) {
            game = new ChessGame();
        }

        connectionManager.add(command.getGameID(), new Connection(username, session));

        String message;

        if (username.equals(gameData.whiteUsername())) {
            message = "\n" + username + " joined as WHITE";
        } else if (username.equals(gameData.blackUsername())) {
            message = "\n" + username + " joined as BLACK";
        } else {
            message = "\n" + username + " joined as an observer";
        }

        connectionManager.broadcast(
                command.getGameID(),
                session,
                new NotificationMessage(message)
        );

        try {
            session.getRemote().sendString(new Gson().toJson(new LoadGameMessage(game)));
        }
        catch (Exception error) {
            System.out.println("Error sending game to client: " + error.getMessage());
        }
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

        if (move == null) {
            throw new Exception("Error: invalid move");
        }

        boolean isCheckmate;
        boolean isStalemate;

        try {
            game.makeMove(move);
            ChessGame.TeamColor nextTurn = game.getTeamTurn();
            isCheckmate = game.isInCheckmate(nextTurn);
            isStalemate = game.isInStalemate(nextTurn);
        }
        catch (Exception error) {
            try {
                session.getRemote().sendString(
                        new Gson().toJson(new ErrorMessage("Error: invalid move"))
                );
            }
            catch (Exception ignored) {
            }
            return;
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
            try {
                connectionManager.broadcast(
                        gameID,
                        null,
                        new NotificationMessage("Checkmate! Game over.")
                );
            }
            catch (Exception ignored) {}
        }
        else if (isStalemate) {
            try {
                connectionManager.broadcast(
                        gameID,
                        null,
                        new NotificationMessage("Stalemate! Game over.")
                );
            } catch (Exception ignored) {}
        }


    }

    private void leave(Session session, UserGameCommand command) throws Exception {
        var auth = dataAccess.getAuth(command.getAuthToken());
        if (auth == null) {
            throw new Exception("Error: unauthorized");
        }

        String username = auth.username();
        int gameID = command.getGameID();

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

        connectionManager.remove(gameID, session);

    }

    private void resign(UserGameCommand command) throws Exception {
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

        try {
            connectionManager.broadcast(
                    gameID,
                    null,
                    new NotificationMessage(message)
            );
        } catch (Exception ignored) {}

    }


}
