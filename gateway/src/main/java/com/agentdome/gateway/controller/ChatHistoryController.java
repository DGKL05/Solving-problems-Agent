package com.agentdome.gateway.controller;

import com.agentdome.agent.AgentService;
import com.agentdome.agent.memory.ChatHistoryService;
import com.agentdome.common.mongo.ChatSessionDocument;
import com.agentdome.gateway.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/chat")
public class ChatHistoryController {

    private final ChatHistoryService chatHistoryService;
    private final AgentService agentService;

    public ChatHistoryController(ChatHistoryService chatHistoryService, AgentService agentService) {
        this.chatHistoryService = chatHistoryService;
        this.agentService = agentService;
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listSessions(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<ChatSessionDocument> sessions = chatHistoryService.getSessions(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ChatSessionDocument s : sessions) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("sessionId", s.getSessionId());
            m.put("title", s.getTitle() != null ? s.getTitle() : "新对话");
            m.put("createdAt", s.getCreatedAt() != null ? s.getCreatedAt().toString() : null);
            m.put("messageCount", s.getMessages() != null ? s.getMessages().size() : 0);
            result.add(m);
        }
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<ChatSessionDocument>> getSession(
            @PathVariable String sessionId, HttpServletRequest request) {
        ChatSessionDocument doc = chatHistoryService.getSession(sessionId);
        if (doc == null) {
            return ResponseEntity.ok(ApiResponse.ok(null));
        }
        return ResponseEntity.ok(ApiResponse.ok(doc));
    }

    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<Map<String, String>>> newSession(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String sessionId = agentService.newSession(userId);
        Map<String, String> result = Map.of("sessionId", sessionId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<String>> deleteSession(
            @PathVariable String sessionId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        chatHistoryService.deleteSession(sessionId, userId);
        return ResponseEntity.ok(ApiResponse.ok("已删除"));
    }
}
