package com.agentdome.gateway.config;

import com.agentdome.agent.AgentService;
import com.agentdome.agent.QwenService;
import com.agentdome.agent.memory.ChatHistoryService;
import com.agentdome.common.mongo.ChatSessionDocument;
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
    private final ChatHistoryService chatHistoryService;

    public WebSocketConfig(AgentService agentService, QwenService qwenService,
                           ProblemRepository problemRepo, JwtUtil jwtUtil,
                           WsSessionManager sessionManager, UserProblemTracker problemTracker,
                           ChatHistoryService chatHistoryService) {
        this.agentService = agentService;
        this.qwenService = qwenService;
        this.problemRepo = problemRepo;
        this.jwtUtil = jwtUtil;
        this.sessionManager = sessionManager;
        this.problemTracker = problemTracker;
        this.chatHistoryService = chatHistoryService;
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
            String reqSessionId = null;
            if (query != null) {
                for (String param : query.split("&")) {
                    if (param.startsWith("token=")) {
                        token = param.substring(6);
                    } else if (param.startsWith("sessionId=")) {
                        reqSessionId = param.substring(10);
                    }
                }
            }

            if (token == null || !jwtUtil.validateToken(token)) {
                session.close(CloseStatus.POLICY_VIOLATION);
                return;
            }

            Long userId = jwtUtil.getUserIdFromToken(token);

            // Reuse existing session if valid, otherwise create new
            String sessionId;
            if (reqSessionId != null) {
                ChatSessionDocument existing = chatHistoryService.getSession(reqSessionId);
                if (existing != null && existing.getUserId().equals(userId)) {
                    sessionId = reqSessionId;
                } else {
                    sessionId = agentService.newSession(userId);
                }
            } else {
                sessionId = agentService.newSession(userId);
            }

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

                if ("ping".equals(msgType)) {
                    sendJson(session, Map.of("type", "pong"));
                    return;
                }

                if ("solve".equals(msgType)) {
                    // Streaming solve request
                    String text = (String) msg.get("text");
                    String subjectType = (String) msg.getOrDefault("subjectType", "ACM");

                    // Persist user's problem text to chat history
                    chatHistoryService.appendMessage(sessionId, userId, "user", "📷 [" + subjectType + "] " + text);

                    sendJson(session, Map.of("type", "solve-start"));

                    qwenService.solveStream(subjectType, text,
                            token -> sendJson(session, Map.of("type", "solve-chunk", "chunk", token)),
                            fullText -> {
                                sendJson(session, Map.of("type", "solve-done"));
                                // Persist assistant solution to chat history
                                chatHistoryService.appendMessage(sessionId, userId, "assistant", fullText);
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
                    // Regular chat message — extract content if present, stream response
                    String userText = msg.containsKey("content") ? (String) msg.get("content") : message.getPayload();
                    agentService.processMessageStream(sessionId, userId, userText,
                            token -> sendJson(session, Map.of("type", "message-chunk", "chunk", token)),
                            fullText -> sendJson(session, Map.of("type", "message-done")),
                            error -> sendJson(session, Map.of("type", "message-error", "message", error.getMessage()))
                    );
                }
            } catch (Exception e) {
                // Not JSON - treat as plain text, stream response
                agentService.processMessageStream(sessionId, userId, message.getPayload(),
                        token -> sendJson(session, Map.of("type", "message-chunk", "chunk", token)),
                        fullText -> sendJson(session, Map.of("type", "message-done")),
                        error -> sendJson(session, Map.of("type", "message-error", "message", error.getMessage()))
                );
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
