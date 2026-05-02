package com.agentdome.agent;

import com.agentdome.common.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class QwenService {

    @Value("${aliyun.dashscope.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String DASHSCOPE_URL =
            "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

    /**
     * Call Qwen text model to solve a problem.
     */
    public String solveProblem(String subjectType, String cleanedText) {
        String prompt = buildPrompt(subjectType, cleanedText);

        try {
            String body = objectMapper.writeValueAsString(
                    new QwenRequest("qwen-plus", prompt));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    DASHSCOPE_URL, new HttpEntity<>(body, headers), String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new BusinessException("Qwen API call failed");
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("output").path("text").asText("No response from model");

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Qwen service error: " + e.getMessage());
        }
    }

    private String buildPrompt(String subjectType, String cleanedText) {
        return switch (subjectType) {
            case "ACM" -> """
                    你是一位算法竞赛专家。请解答以下题目：
                    1. 分析问题，识别算法类型
                    2. 给出解题思路和核心算法描述
                    3. 输出C++代码（带必要注释）
                    4. 分析时间复杂度和空间复杂度

                    题目：
                    """ + cleanedText;
            case "MATH" -> """
                    你是一位数学教授。请解答以下题目：
                    1. 明确问题类型和涉及的数学概念
                    2. 给出详细的分步推导过程
                    3. 每一步都要解释原理
                    4. 最终答案用 \\boxed{...} 标注

                    题目：
                    """ + cleanedText;
            case "CS408" -> """
                    你是一位考研408辅导专家。请解答以下题目：
                    1. 明确考点（数据结构/计组/操作系统/计网）
                    2. 给出解题步骤和推理过程
                    3. 对于计算题，写出公式和计算过程
                    4. 对于选择题，逐选项分析正误原因

                    题目：
                    """ + cleanedText;
            default -> "请解答以下题目：\n" + cleanedText;
        };
    }

    static class QwenRequest {
        public String model;
        public Input input;
        public Parameters parameters;

        QwenRequest(String model, String prompt) {
            this.model = model;
            this.input = new Input(prompt);
            this.parameters = new Parameters();
        }

        static class Input {
            public Message[] messages;
            Input(String prompt) {
                this.messages = new Message[]{new Message(prompt)};
            }
        }

        static class Message {
            public String role = "user";
            public String content;
            Message(String content) { this.content = content; }
        }

        static class Parameters {}
    }
}
