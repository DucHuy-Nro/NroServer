package nro.netty;

import io.netty.util.AttributeKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import nro.models.player.Player;
import nro.server.Client;

public class SessionManager {

    private static SessionManager instance;
    private final List<Session> sessions;

    public static final AttributeKey<Session> SESSION_KEY = AttributeKey.valueOf("SESSION_KEY");

    private SessionManager() {
        this.sessions = Collections.synchronizedList(new ArrayList<>());
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void add(Session session) {
        this.sessions.add(session);
    }

    public void remove(Session session) {
        this.sessions.remove(session);
        if (session.player != null) {
            Client.gI().removePlayer(session.player);
        }
    }

    public void kick(String reason) {
        for (Session session : sessions) {
            if(session.player != null) {
                Player player = session.player;
                Client.gI().kickSession(session);
            }
        }
    }
}