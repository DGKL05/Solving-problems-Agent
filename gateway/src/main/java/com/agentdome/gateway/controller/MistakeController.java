package com.agentdome.gateway.controller;

import com.agentdome.gateway.dto.ApiResponse;
import com.agentdome.mistake.MistakeService;
import com.agentdome.mistake.dto.MistakeDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MistakeController {

    private final MistakeService mistakeService;

    public MistakeController(MistakeService mistakeService) {
        this.mistakeService = mistakeService;
    }

    @GetMapping("/mistakes")
    public ResponseEntity<ApiResponse<List<MistakeDTO>>> listMistakes(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(ApiResponse.ok(mistakeService.getUserMistakes(userId)));
    }

    @DeleteMapping("/mistakes/{id}")
    public ResponseEntity<ApiResponse<String>> deleteMistake(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        mistakeService.deleteMistake(userId, id);
        return ResponseEntity.ok(ApiResponse.ok("已删除"));
    }
}
