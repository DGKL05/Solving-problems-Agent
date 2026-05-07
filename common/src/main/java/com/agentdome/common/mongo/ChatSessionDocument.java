package com.agentdome.common.mongo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.List;

@Document(collection = "chat_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionDocument {

    @Id
    private String id;

    private String sessionId;
    private Long userId;
    private String title;
    private List<ChatMessage> messages;
    private SessionSummary summary;
    private Instant createdAt;
    private Instant endedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        private String role;
        private String content;
        private Instant timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionSummary {
        private int problemsSolved;
        private List<String> weakTopics;
        private int mistakesAdded;
    }
}
