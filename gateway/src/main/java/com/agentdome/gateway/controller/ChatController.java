package com.agentdome.gateway.controller;

import com.agentdome.agent.QwenService;
import com.agentdome.common.entity.Problem;
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
    private final QwenService qwenService;
    private final ProblemRepository problemRepo;

    public ChatController(ImagePipelineService pipelineService,
                          QwenService qwenService,
                          ProblemRepository problemRepo) {
        this.pipelineService = pipelineService;
        this.qwenService = qwenService;
        this.problemRepo = problemRepo;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("subjectType") String subjectType,
            @RequestParam("sessionId") String sessionId,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");

        // Step 1: OCR extract text
        PipelineResult result = pipelineService.process(file, userId);

        // Step 2: Qwen solves
        String solution;
        try {
            solution = qwenService.solveProblem(subjectType, result.cleanedText());
        } catch (Exception e) {
            solution = "AI解题暂时不可用，请稍后重试。\n错误：" + e.getMessage();
        }

        // Step 3: Save problem
        Problem problem = new Problem();
        problem.setUserId(userId);
        problem.setSubjectType(Problem.SubjectType.valueOf(subjectType));
        problem.setOriginalImageId(result.imageId());
        problem.setOcrRawText(result.rawText());
        problem.setCleanedText(result.cleanedText());
        problem.setSolutionText(solution);
        problemRepo.save(problem);

        Map<String, Object> data = Map.of(
                "problemId", problem.getId(),
                "imageId", result.imageId(),
                "cleanedText", result.cleanedText(),
                "solution", solution
        );

        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}
