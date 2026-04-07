package websocket;
import dataaccess.DataAccess;
import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;

import websocket.commands.UserGameCommand;


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

    private void connect(Session session, UserGameCommand command) throws Exception {}

    private void makeMove(Session session, UserGameCommand command) throws Exception {}

    private void leave(Session session, UserGameCommand command) throws Exception {}

    private void resign(Session session, UserGameCommand command) throws Exception {}
}
