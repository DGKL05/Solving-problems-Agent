package com.agentdome.gateway.controller;

import com.agentdome.agent.AgentService;
import com.agentdome.common.repository.ProblemRepository;
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
    private final AgentService agentService;
    private final ProblemRepository problemRepo;

    public ChatController(ImagePipelineService pipelineService,
                          AgentService agentService,
                          ProblemRepository problemRepo) {
        this.pipelineService = pipelineService;
        this.agentService = agentService;
        this.problemRepo = problemRepo;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("subjectType") String subjectType,
            @RequestParam("sessionId") String sessionId,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");

        PipelineResult result = pipelineService.process(file, userId);

        Map<String, Object> data = Map.of(
                "imageId", result.imageId(),
                "rawText", result.rawText(),
                "cleanedText", result.cleanedText()
        );

        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}
