package com.agentdome.gateway.config;

import com.agentdome.agent.AgentService;
import com.agentdome.agent.QwenService;
import com.agentdome.common.repository.ProblemRepository;
import com.agentdome.common.entity.Problem;
import com.agentdome.common.config.UserProblemTracker;
import com.agentdome.common.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.*;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.Map;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final AgentService agentService;
    private final QwenService qwenService;
    private final ProblemRepository problemRepo;
    private final JwtUtil jwtUtil;
    private final WsSessionManager sessionManager;
    private final UserProblemTracker problemTracker;

    public WebSocketConfig(AgentService agentService, QwenService qwenService,
                           ProblemRepository problemRepo, JwtUtil jwtUtil,
                           WsSessionManager sessionManager, UserProblemTracker problemTracker) {
        this.agentService = agentService;
        this.qwenService = qwenService;
        this.problemRepo = problemRepo;
        this.jwtUtil = jwtUtil;
        this.sessionManager = sessionManager;
        this.problemTracker = problemTracker;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new ChatWebSocketHandler(), "/ws/chat")
                .setAllowedOrigins("*");
    }

    class ChatWebSocketHandler extends TextWebSocketHandler {

        private final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);
        private final ObjectMapper mapper = new ObjectMapper();

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
            sessionManager.register(sessionId, session);
            sendJson(session, Map.of("type", "connected", "sessionId", sessionId));
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            Long userId = (Long) session.getAttributes().get("userId");
            String sessionId = (String) session.getAttributes().get("sessionId");

            try {
                Map<String, Object> msg = mapper.readValue(message.getPayload(), Map.class);
                String msgType = (String) msg.getOrDefault("type", "text");

                if ("solve".equals(msgType)) {
                    // Streaming solve request
                    String text = (String) msg.get("text");
                    String subjectType = (String) msg.getOrDefault("subjectType", "ACM");

                    sendJson(session, Map.of("type", "solve-start"));

                    qwenService.solveStream(subjectType, text,
                            token -> sendJson(session, Map.of("type", "solve-chunk", "chunk", token)),
                            fullText -> {
                                sendJson(session, Map.of("type", "solve-done"));
                                // Save problem
                                Problem problem = new Problem();
                                problem.setUserId(userId);
                                problem.setSubjectType(Problem.SubjectType.valueOf(subjectType));
                                problem.setCleanedText(text);
                                problem.setSolutionText(fullText);
                                problemRepo.save(problem);
                                problemTracker.setLastProblem(userId, problem.getId());
                                sendJson(session, Map.of("type", "problem-saved", "problemId", problem.getId()));
                            },
                            error -> sendJson(session, Map.of("type", "solve-error", "message", error.getMessage()))
                    );
                } else {
                    // Regular chat message via AgentService
                    String response = agentService.processMessage(sessionId, userId, message.getPayload());
                    sendJson(session, Map.of("type", "message", "content", response));
                }
            } catch (Exception e) {
                // Not JSON - treat as plain text
                String response = agentService.processMessage(sessionId, userId, message.getPayload());
                sendJson(session, Map.of("type", "message", "content", response));
            }
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
            String sessionId = (String) session.getAttributes().get("sessionId");
            if (sessionId != null) {
                sessionManager.remove(sessionId);
            }
        }

        private void sendJson(WebSocketSession session, Object data) {
            try {
                synchronized (session) {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(mapper.writeValueAsString(data)));
                    }
                }
            } catch (Exception e) {
                log.error("WS send error", e);
            }
        }
    }
}
