package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class ConnectionManager {
    private final ConcurrentHashMap<Integer, Set<Connection>> connections = new ConcurrentHashMap<>();

    public void add(Integer gameID, Connection connection) {
        Set<Connection> set = connections.computeIfAbsent(gameID, k -> ConcurrentHashMap.newKeySet());

        set.add(connection);
    }

    public void remove(Integer gameID, Session session) {
        Set<Connection> connect = connections.get(gameID);
        if (connect != null) {
            connect.removeIf(c -> c.session == session);
        }
    }

    public Set<Connection> get(Integer gameID) {
        return connections.getOrDefault(gameID, ConcurrentHashMap.newKeySet());
    }

    public void broadcast(Integer gameID, Session exclude, Object message) throws Exception {
        String json = new Gson().toJson(message);

        for (Connection c : get(gameID)) {
            if (c.session.isOpen() && c.session != exclude) {
                c.session.getRemote().sendString(json);
            }
        }
    }

    public void broadcastAll(Integer gameID, Object message) throws Exception {
        String json = new Gson().toJson(message);

        for (Connection c : get(gameID)) {
            if (c.session.isOpen()) {
                c.session.getRemote().sendString(json);
            }
        }
    }
}
