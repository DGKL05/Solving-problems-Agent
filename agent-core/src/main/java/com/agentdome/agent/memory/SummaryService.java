package com.agentdome.agent.memory;

import com.agentdome.common.mongo.ChatSessionDocument;
import com.agentdome.common.mongo.ChatSessionRepository;
import com.agentdome.common.mongo.ChatSessionDocument.SessionSummary;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class SummaryService {

    private final ChatSessionRepository sessionRepo;

    public SummaryService(ChatSessionRepository sessionRepo) {
        this.sessionRepo = sessionRepo;
    }

    public String getPreviousSummary(Long userId) {
        return "";
    }

    public void saveSummary(String sessionId, Long userId, int problemsSolved,
                            List<String> weakTopics, int mistakesAdded) {
        sessionRepo.findBySessionId(sessionId).ifPresent(session -> {
            session.setSummary(new SessionSummary(problemsSolved, weakTopics, mistakesAdded));
            sessionRepo.save(session);
        });
    }
}
