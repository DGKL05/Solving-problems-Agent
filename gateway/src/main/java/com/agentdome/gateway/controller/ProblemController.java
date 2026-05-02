package com.agentdome.gateway.controller;

import com.agentdome.common.entity.Problem;
import com.agentdome.common.repository.ProblemRepository;
import com.agentdome.gateway.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemRepository problemRepo;

    public ProblemController(ProblemRepository problemRepo) {
        this.problemRepo = problemRepo;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Problem>> getProblem(@PathVariable Long id,
                                                           HttpServletRequest request) {
        return problemRepo.findById(id)
                .map(p -> ResponseEntity.ok(ApiResponse.ok(p)))
                .orElse(ResponseEntity.notFound().build());
    }
}
