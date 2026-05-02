package com.agentdome.gateway.config;

import com.agentdome.agent.AgentService;
import com.agentdome.common.util.JwtUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.*;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final AgentService agentService;
    private final JwtUtil jwtUtil;

    public WebSocketConfig(AgentService agentService, JwtUtil jwtUtil) {
        this.agentService = agentService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new ChatWebSocketHandler(agentService, jwtUtil), "/ws/chat")
                .setAllowedOrigins("*");
    }

    static class ChatWebSocketHandler extends TextWebSocketHandler {

        private final AgentService agentService;
        private final JwtUtil jwtUtil;
        private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

        ChatWebSocketHandler(AgentService agentService, JwtUtil jwtUtil) {
            this.agentService = agentService;
            this.jwtUtil = jwtUtil;
        }

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            URI uri = session.getUri();
            if (uri == null) { session.close(); return; }

            String query = uri.getQuery();
            String token = null;
            if (query != null) {
                for (String param : query.split("&")) {
                    if (param.startsWith("token=")) {
                        token = param.substring(6);
                    }
                }
            }

            if (token == null || !jwtUtil.validateToken(token)) {
                session.close(CloseStatus.POLICY_VIOLATION);
                return;
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            String sessionId = agentService.newSession(userId);
            session.getAttributes().put("userId", userId);
            session.getAttributes().put("sessionId", sessionId);
            sessions.put(sessionId, session);
            session.sendMessage(new TextMessage("{\"type\":\"connected\",\"sessionId\":\"" + sessionId + "\"}"));
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            Long userId = (Long) session.getAttributes().get("userId");
            String sessionId = (String) session.getAttributes().get("sessionId");
            String response = agentService.processMessage(sessionId, userId, message.getPayload());
            session.sendMessage(new TextMessage("{\"type\":\"message\",\"content\":\"" +
                    escapeJson(response) + "\"}"));
        }

        private String escapeJson(String s) {
            return s.replace("\\", "\\\\").replace("\"", "\\\"")
                    .replace("\n", "\\n").replace("\r", "\\r");
        }
    }
}
