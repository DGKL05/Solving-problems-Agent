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

    /**
     * 查询会话列表。
     */
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

    /**
     * 查询会话详情。
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<ChatSessionDocument>> getSession(
            @PathVariable String sessionId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        ChatSessionDocument doc = chatHistoryService.getSession(sessionId);
        if (doc == null || !doc.getUserId().equals(userId)) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "会话不存在"));
        }
        return ResponseEntity.ok(ApiResponse.ok(doc));
    }

    /**
     * 新增会话。
     */
    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<Map<String, String>>> newSession(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String sessionId = agentService.newSession(userId);
        Map<String, String> result = new LinkedHashMap<>();
        result.put("sessionId", sessionId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * 修改会话标题。
     */
    @PutMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<ChatSessionDocument>> updateSession(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        ChatSessionDocument doc = chatHistoryService.updateSessionTitle(sessionId, userId, body.get("title"));
        if (doc == null) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "会话不存在"));
        }
        return ResponseEntity.ok(ApiResponse.ok(doc));
    }

    /**
     * 删除指定会话。
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<String>> deleteSession(
            @PathVariable String sessionId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        chatHistoryService.deleteSession(sessionId, userId);
        return ResponseEntity.ok(ApiResponse.ok("已删除"));
    }

    /**
     * 清空当前用户全部会话。
     */
    @DeleteMapping("/sessions")
    public ResponseEntity<ApiResponse<Map<String, Object>>> clearSessions(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        int count = chatHistoryService.deleteAllSessions(userId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("deleted", count);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}