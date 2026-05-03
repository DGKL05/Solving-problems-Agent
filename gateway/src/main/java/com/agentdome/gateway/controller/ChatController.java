package com.agentdome.gateway.controller;

import com.agentdome.gateway.dto.ApiResponse;
import com.agentdome.image.ImagePipelineService;
import com.agentdome.image.ImagePipelineService.PipelineResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ImagePipelineService pipelineService;

    public ChatController(ImagePipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("subjectType") String subjectType,
            @RequestParam("sessionId") String sessionId,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");

        // OCR extract text
        PipelineResult result = pipelineService.process(file, userId);

        Map<String, Object> data = Map.of(
                "imageId", result.imageId(),
                "cleanedText", result.cleanedText(),
                "subjectType", subjectType
        );

        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}
