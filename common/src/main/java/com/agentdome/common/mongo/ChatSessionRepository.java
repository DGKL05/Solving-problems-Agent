package com.agentdome.common.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ChatSessionRepository extends MongoRepository<ChatSessionDocument, String> {
    Optional<ChatSessionDocument> findBySessionId(String sessionId);
}
