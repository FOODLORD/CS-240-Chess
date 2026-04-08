package websocket;
import dataaccess.DataAccess;
import com.google.gson.Gson;
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

        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);

        switch (command.getCommandType()) {
            case CONNECT -> connect(session, command);
            case MAKE_MOVE -> makeMove(session, command);
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

    private void makeMove(Session session, UserGameCommand command) throws Exception {}

    private void leave(Session session, UserGameCommand command) throws Exception {}

    private void resign(Session session, UserGameCommand command) throws Exception {}
}
