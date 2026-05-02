package com.agentdome.image;

import com.agentdome.common.exception.BusinessException;
import com.agentdome.image.dto.OcrResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class AliyunOcrService {

    @Value("${aliyun.ocr.access-key-id:}")
    private String accessKeyId;

    @Value("${aliyun.ocr.access-key-secret:}")
    private String accessKeySecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String OCR_ENDPOINT = "https://ocr-api.cn-hangzhou.aliyuncs.com/";
    private static final SimpleDateFormat ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    static {
        ISO8601.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public OcrResult recognize(byte[] imageBytes) {
        try {
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // Build signed RPC request
            Map<String, String> params = new TreeMap<>();
            params.put("Action", "RecognizeGeneral");
            params.put("Version", "2021-07-07");
            params.put("Format", "JSON");
            params.put("SignatureMethod", "HMAC-SHA1");
            params.put("SignatureNonce", UUID.randomUUID().toString());
            params.put("SignatureVersion", "1.0");
            params.put("Timestamp", ISO8601.format(new Date()));
            params.put("AccessKeyId", accessKeyId);

            String query = buildQuery(params);
            String signature = sign(query);
            params.put("Signature", signature);

            // Build form body with image
            String formBody = "ImageContent=" + URLEncoder.encode(base64Image, StandardCharsets.UTF_8);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String url = OCR_ENDPOINT + "?" + buildQuery(params);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url, new HttpEntity<>(formBody, headers), String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new BusinessException("OCR API returned non-success: " + response.getStatusCode());
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.has("Message")) {
                throw new BusinessException("OCR error: " + root.get("Message").asText());
            }

            StringBuilder rawText = new StringBuilder();
            JsonNode data = root.get("Data");
            if (data != null && data.has("content")) {
                JsonNode content = data.get("content");
                if (content.has("prism_wordsInfo")) {
                    for (JsonNode word : content.get("prism_wordsInfo")) {
                        rawText.append(word.get("word").asText()).append("\n");
                    }
                }
            }

            OcrResult result = new OcrResult();
            result.setRawText(rawText.isEmpty() ? base64Image.substring(0, 100) : rawText.toString());
            result.setConfidence(data != null && data.has("height") ? 0.9 : 0.0);
            return result;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("OCR recognition failed: " + e.getMessage());
        }
    }

    private String buildQuery(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> percentEncode(e.getKey()) + "=" + percentEncode(e.getValue()))
                .reduce((a, b) -> a + "&" + b).orElse("");
    }

    private String sign(String query) throws Exception {
        String stringToSign = "POST&" + percentEncode("/") + "&" + percentEncode(query);
        String key = accessKeySecret + "&";
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signData);
    }

    private String percentEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8)
                    .replace("+", "%20")
                    .replace("*", "%2A")
                    .replace("%7E", "~");
        } catch (Exception e) {
            return value;
        }
    }
}
