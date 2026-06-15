package com.agentdome.agent.memory;

import com.agentdome.common.mongo.ChatSessionDocument;
import com.agentdome.common.mongo.ChatSessionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class ChatHistoryService {

    private final ChatSessionRepository sessionRepo;

    public ChatHistoryService(ChatSessionRepository sessionRepo) {
        this.sessionRepo = sessionRepo;
    }

    public String createSession(Long userId) {
        String sessionId = UUID.randomUUID().toString();
        ChatSessionDocument doc = new ChatSessionDocument();
        doc.setSessionId(sessionId);
        doc.setUserId(userId);
        doc.setTitle("新对话");
        doc.setMessages(new ArrayList<>());
        doc.setCreatedAt(Instant.now());
        sessionRepo.save(doc);
        return sessionId;
    }

    public void appendMessage(String sessionId, Long userId, String role, String content) {
        ChatSessionDocument doc = sessionRepo.findBySessionId(sessionId)
                .orElseGet(() -> {
                    ChatSessionDocument newDoc = new ChatSessionDocument();
                    newDoc.setSessionId(sessionId);
                    newDoc.setUserId(userId);
                    newDoc.setTitle("新对话");
                    newDoc.setMessages(new ArrayList<>());
                    newDoc.setCreatedAt(Instant.now());
                    return newDoc;
                });
        if (doc.getMessages() == null) doc.setMessages(new ArrayList<>());
        ChatSessionDocument.ChatMessage msg = new ChatSessionDocument.ChatMessage();
        msg.setRole(role);
        msg.setContent(content);
        msg.setTimestamp(Instant.now());
        doc.getMessages().add(msg);

        // Set title from first user message
        if ("user".equals(role) && "新对话".equals(doc.getTitle())) {
            String title = content.length() > 30 ? content.substring(0, 30) + "…" : content;
            doc.setTitle(title.replace("\n", " "));
        }

        sessionRepo.save(doc);
    }

    public List<ChatSessionDocument> getSessions(Long userId) {
        return sessionRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public ChatSessionDocument getSession(String sessionId) {
        return sessionRepo.findBySessionId(sessionId).orElse(null);
    }

    public ChatSessionDocument updateSessionTitle(String sessionId, Long userId, String title) {
        ChatSessionDocument doc = sessionRepo.findBySessionId(sessionId).orElse(null);
        if (doc == null || !doc.getUserId().equals(userId)) {
            return null;
        }
        if (title != null && !title.trim().isEmpty()) {
            doc.setTitle(title.trim());
        }
        return sessionRepo.save(doc);
    }

    public void endSession(String sessionId) {
        sessionRepo.findBySessionId(sessionId).ifPresent(doc -> {
            doc.setEndedAt(Instant.now());
            sessionRepo.save(doc);
        });
    }

    public void deleteSession(String sessionId, Long userId) {
        sessionRepo.findBySessionId(sessionId).ifPresent(doc -> {
            if (doc.getUserId().equals(userId)) {
                sessionRepo.delete(doc);
            }
        });
    }

    public int deleteAllSessions(Long userId) {
        List<ChatSessionDocument> sessions = sessionRepo.findByUserIdOrderByCreatedAtDesc(userId);
        int count = sessions.size();
        sessionRepo.deleteAll(sessions);
        return count;
    }

    public String deleteSessionByIndex(Long userId, int index) {
        List<ChatSessionDocument> sessions = sessionRepo.findByUserIdOrderByCreatedAtDesc(userId);
        if (index < 1 || index > sessions.size()) {
            return "序号无效，你共有" + sessions.size() + "个对话，请输入1到" + sessions.size() + "之间的数字";
        }
        ChatSessionDocument session = sessions.get(index - 1);
        String title = session.getTitle() != null ? session.getTitle() : "新对话";
        sessionRepo.delete(session);
        return "已删除第" + index + "个对话（" + title + "）。";
    }
}