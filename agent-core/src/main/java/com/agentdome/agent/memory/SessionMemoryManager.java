package com.agentdome.agent.memory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnBean(RedisTemplate.class)
public class SessionMemoryManager {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PREFIX = "session:";
    private static final long TTL_HOURS = 24;

    public SessionMemoryManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, String>> getMessages(String sessionId) {
        Object data = redisTemplate.opsForValue().get(PREFIX + sessionId + ":messages");
        if (data instanceof List) {
            return (List<Map<String, String>>) data;
        }
        return new ArrayList<>();
    }

    public void appendMessage(String sessionId, String role, String content) {
        List<Map<String, String>> messages = getMessages(sessionId);
        messages.add(Map.of("role", role, "content", content));
        redisTemplate.opsForValue().set(PREFIX + sessionId + ":messages", messages, TTL_HOURS, TimeUnit.HOURS);
    }

    public void clearSession(String sessionId) {
        redisTemplate.delete(PREFIX + sessionId + ":messages");
    }
}
