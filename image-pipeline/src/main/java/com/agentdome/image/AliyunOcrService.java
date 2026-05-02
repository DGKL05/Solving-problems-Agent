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

    @Value("${aliyun.ocr.access-key-id:}")
    private String accessKeyId;

    @Value("${aliyun.ocr.access-key-secret:}")
    private String accessKeySecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String OCR_ENDPOINT = "https://ocr-api.cn-hangzhou.aliyuncs.com";

    /**
     * Call Alibaba Cloud General OCR API via HTTP.
     * Image data is provided as base64-encoded string.
     */
    public OcrResult recognize(byte[] imageBytes) {
        try {
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Note: Production requires proper Alibaba Cloud API signature.
            // This simplified version uses API key auth where supported.

            String body = String.format("{\"ImageBase64\":\"%s\"}", base64Image);
            HttpEntity<String> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    OCR_ENDPOINT + "/api/ocr/general", request, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new BusinessException("OCR API returned non-success response");
            }

            JsonNode root = objectMapper.readTree(response.getBody());

            StringBuilder rawText = new StringBuilder();
            if (root.has("content") && root.get("content").has("prism_wordsInfo")) {
                for (JsonNode block : root.get("content").get("prism_wordsInfo")) {
                    rawText.append(block.get("word").asText()).append("\n");
                }
            }

            OcrResult result = new OcrResult();
            result.setRawText(rawText.toString());
            result.setConfidence(root.has("confidence") ? root.get("confidence").asDouble() : 0.0);
            return result;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("OCR recognition failed: " + e.getMessage());
        }
    }
}
