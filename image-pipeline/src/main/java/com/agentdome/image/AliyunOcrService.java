package com.agentdome.image;

import com.agentdome.common.exception.BusinessException;
import com.agentdome.image.dto.OcrResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Base64;

@Service
public class AliyunOcrService {

    @Value("${aliyun.dashscope.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String VL_OCR_URL =
            "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    /**
     * Use qwen-vl-ocr-latest model to extract text from image.
     */
    public OcrResult recognize(byte[] imageBytes) {
        try {
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            String requestBody = objectMapper.writeValueAsString(
                    new VlOcrRequest(base64Image));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    VL_OCR_URL, new HttpEntity<>(requestBody, headers), String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new BusinessException("VL OCR API returned non-success: " + response.getStatusCode());
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.has("error")) {
                throw new BusinessException("VL OCR error: " + root.get("error").path("message").asText());
            }

            String text = root.path("choices").get(0).path("message").path("content").asText("");

            OcrResult result = new OcrResult();
            result.setRawText(text);
            result.setConfidence(0.95);
            return result;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("VL OCR recognition failed: " + e.getMessage());
        }
    }

    static class VlOcrRequest {
        public String model = "qwen-vl-ocr-latest";
        public Message[] messages;

        VlOcrRequest(String base64Image) {
            this.messages = new Message[]{
                    new Message(new Content[]{
                            new ImageContent("data:image/png;base64," + base64Image),
                            new TextContent("请提取图片中的所有文字内容")
                    })
            };
        }

        static class Message {
            public String role = "user";
            public Content[] content;
            Message(Content[] content) { this.content = content; }
        }

        static class Content {}
        static class ImageContent extends Content {
            public String type = "image_url";
            public ImageUrl image_url;
            public int min_pixels = 3072;
            public int max_pixels = 8388608;
            ImageContent(String url) { this.image_url = new ImageUrl(url); }
        }
        static class ImageUrl { public String url; ImageUrl(String url) { this.url = url; } }
        static class TextContent extends Content {
            public String type = "text";
            public String text;
            TextContent(String text) { this.text = text; }
        }
    }
}
