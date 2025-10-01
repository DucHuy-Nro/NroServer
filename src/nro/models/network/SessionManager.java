package nro.models.network;

import java.util.ArrayList;
import nro.models.interfaces.ISession;
import java.util.List;

public class SessionManager {

    private static SessionManager instance;
    private final List<ISession> sessions;

    public static SessionManager gI() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public SessionManager() {
        this.sessions = new ArrayList<>();
    }

    public void putSession(ISession session) {
        this.sessions.add(session);
    }

    public void removeSession(ISession session) {
        this.sessions.remove(session);
    }

    public List<ISession> getSessions() {
        return this.sessions;
    }

    public ISession findByID(long id) throws Exception {
        if (this.sessions.isEmpty()) {
            throw new Exception("Session " + id + " does not exist");
        }
        for (ISession session : this.sessions) {
            if (session.getID() > id) {
                throw new Exception("Session " + id + " does not exist");
            }
            if (session.getID() == id) {
                return session;
            }
        }
        throw new Exception("Session " + id + " does not exist");
    }

    public int getNumSession() {
        return this.sessions.size();
    }
}
