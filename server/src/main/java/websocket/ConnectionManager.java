package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class ConnectionManager {
    private final ConcurrentHashMap<Integer, Set<Connection>> connections = new ConcurrentHashMap<>();

    public Set<Integer> getGameIDs() {
        return connections.keySet();
    }

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
        return connections.computeIfAbsent(gameID, k -> ConcurrentHashMap.newKeySet());
    }

    public void broadcast(Integer gameID, Session exclude, Object message) {
        String json = new Gson().toJson(message);

        Set<Connection> connections = get(gameID);

        Iterator<Connection> iterator = connections.iterator();

        while (iterator.hasNext()) {
            Connection c = iterator.next();

            if (c.session == null || !c.session.isOpen()) {
                iterator.remove();
                continue;
            }

            if (c.session != exclude) {
                try {
                    c.session.getRemote().sendString(json);
                }
                catch (Exception e) {
                    iterator.remove();
                }
            }
        }
    }
}
