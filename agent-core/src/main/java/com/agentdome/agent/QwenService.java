package com.agentdome.agent;

import com.agentdome.common.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class QwenService {

    private static final Logger log = LoggerFactory.getLogger(QwenService.class);

    @Value("${aliyun.dashscope.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String URL =
            "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

    public String solveProblem(String subjectType, String cleanedText) {
        String prompt = buildPrompt(subjectType, cleanedText);
        log.info("Qwen solving with qwen-max, prompt len={}, apiKey={}",
                prompt.length(), apiKey != null ? apiKey.substring(0, 5) : "NULL");

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> body = Map.of(
                "model", "qwen-max",
                "input", Map.of("messages", new Object[]{
                    Map.of("role", "user", "content", prompt)
                }),
                "parameters", Map.of()
            );
            String json = mapper.writeValueAsString(body);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            ResponseEntity<String> resp = restTemplate.postForEntity(
                    URL, new HttpEntity<>(json, headers), String.class);

            log.info("Qwen HTTP {}: {}", resp.getStatusCode(),
                    resp.getBody() != null ? resp.getBody().substring(0, Math.min(200, resp.getBody().length())) : "null");

            JsonNode root = mapper.readTree(resp.getBody());
            if (root.has("output")) {
                return root.get("output").get("text").asText();
            }
            throw new BusinessException("Qwen: " + resp.getBody());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Qwen error", e);
            throw new BusinessException("Qwen: " + e.getMessage());
        }
    }

    private String buildPrompt(String subjectType, String cleanedText) {
        return switch (subjectType) {
            case "ACM" -> """
                    You are an expert competitive programmer. Solve:
                    1. Identify algorithm type
                    2. Explain approach
                    3. Provide C++ code
                    4. Analyze complexity

                    Problem:
                    """ + cleanedText;
            case "MATH" -> """
                    You are a math professor. Solve:
                    1. Identify concepts
                    2. Step-by-step derivation
                    3. Explain reasoning
                    4. Box answer with \\boxed{}

                    Problem:
                    """ + cleanedText;
            default -> "Please solve:\n" + cleanedText;
        };
    }
}
