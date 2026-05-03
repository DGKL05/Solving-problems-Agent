package com.agentdome.common.config;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class UserProblemTracker {
    private final Map<Long, Long> lastProblem = new ConcurrentHashMap<>();

    public void setLastProblem(Long userId, Long problemId) {
        lastProblem.put(userId, problemId);
    }

    public Long getLastProblem(Long userId) {
        return lastProblem.getOrDefault(userId, null);
    }
}
