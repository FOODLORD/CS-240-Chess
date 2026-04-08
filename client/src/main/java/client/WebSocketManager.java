package client;
import com.google.gson.Gson;
import websocket.messages.*;


import jakarta.websocket.*;
import java.net.URI;
import java.util.concurrent.CountDownLatch;

@ClientEndpoint
public class WebSocketManager {
    private Session session;
    private MessageHandler handler;

    private final CountDownLatch connectLatch = new CountDownLatch(1);

    public interface MessageHandler {
        void handle(ServerMessage message);
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("WebSocket opened");
        connectLatch.countDown();
    }

    public void connect(String url, MessageHandler handler) throws Exception {
        this.handler = handler;

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, URI.create(url));
        connectLatch.await();
    }

    @OnMessage
    public void onMessage(String message) {
        Gson gson = new Gson();

        ServerMessage base = gson.fromJson(message, ServerMessage.class);

        ServerMessage msg;

        switch (base.getServerMessageType()) {
            case LOAD_GAME -> msg = gson.fromJson(message, LoadGameMessage.class);
            case NOTIFICATION -> msg = gson.fromJson(message, NotificationMessage.class);
            case ERROR -> msg = gson.fromJson(message, ErrorMessage.class);
            default -> msg = base;
        }

        handler.handle(msg);
    }



    public void send(Object command) throws Exception {
        if (session == null) {
            throw new Exception("WebSocket not connected");
        }

        String json = new Gson().toJson(command);
        session.getBasicRemote().sendText(json);
    }

    public void close() throws Exception {
        if (session != null) {
            session.close();
        }
    }
}
