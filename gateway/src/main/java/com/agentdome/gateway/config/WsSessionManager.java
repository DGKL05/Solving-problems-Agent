package com.agentdome.gateway.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WsSessionManager {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void register(String sessionId, WebSocketSession session) {
        WebSocketSession old = sessions.put(sessionId, session);
        if (old != null && old.isOpen()) {
            try { old.close(); } catch (IOException ignored) {}
        }
    }

    public void remove(String sessionId) {
        sessions.remove(sessionId);
    }

    public void sendMessage(String sessionId, String json) {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                synchronized (session) {
                    session.sendMessage(new TextMessage(json));
                }
            } catch (IOException e) {
                sessions.remove(sessionId);
            }
        }
    }

    public boolean hasSession(String sessionId) {
        return sessions.containsKey(sessionId);
    }
}
