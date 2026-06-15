package com.agentdome.gateway.controller;

import com.agentdome.common.entity.Problem;
import com.agentdome.common.repository.ProblemRepository;
import com.agentdome.gateway.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemRepository problemRepo;

    public ProblemController(ProblemRepository problemRepo) {
        this.problemRepo = problemRepo;
    }

    /**
     * 分页查询当前用户的题目记录，支持按题型筛选。
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Problem>>> pageProblems(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String subjectType,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(size, 1),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        if (subjectType != null && !subjectType.isBlank()) {
            Problem.SubjectType type = Problem.SubjectType.valueOf(subjectType.trim().toUpperCase());
            return ResponseEntity.ok(ApiResponse.ok(problemRepo.findByUserIdAndSubjectType(userId, type, pageable)));
        }
        return ResponseEntity.ok(ApiResponse.ok(problemRepo.findByUserId(userId, pageable)));
    }

    /**
     * 新增题目记录，可用于手动录入题目。
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Problem>> createProblem(@RequestBody Problem problem,
                                                              HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        problem.setId(null);
        problem.setUserId(userId);
        if (problem.getSubjectType() == null) {
            problem.setSubjectType(Problem.SubjectType.ACM);
        }
        return ResponseEntity.ok(ApiResponse.ok(problemRepo.save(problem)));
    }

    /**
     * 查询题目详情。
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Problem>> getProblem(@PathVariable Long id,
                                                           HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return problemRepo.findById(id)
                .filter(p -> p.getUserId().equals(userId))
                .map(p -> ResponseEntity.ok(ApiResponse.ok(p)))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error(404, "题目不存在")));
    }

    /**
     * 修改题目内容、解答、错误类型等信息。
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Problem>> updateProblem(@PathVariable Long id,
                                                              @RequestBody Problem form,
                                                              HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return problemRepo.findById(id)
                .filter(p -> p.getUserId().equals(userId))
                .map(p -> {
                    if (form.getSubjectType() != null) p.setSubjectType(form.getSubjectType());
                    if (form.getOcrRawText() != null) p.setOcrRawText(form.getOcrRawText());
                    if (form.getCleanedText() != null) p.setCleanedText(form.getCleanedText());
                    if (form.getOriginalImageId() != null) p.setOriginalImageId(form.getOriginalImageId());
                    if (form.getSolutionText() != null) p.setSolutionText(form.getSolutionText());
                    if (form.getSolutionCode() != null) p.setSolutionCode(form.getSolutionCode());
                    if (form.getErrorType() != null) p.setErrorType(form.getErrorType());
                    return ResponseEntity.ok(ApiResponse.ok(problemRepo.save(p)));
                })
                .orElse(ResponseEntity.status(404).body(ApiResponse.error(404, "题目不存在")));
    }

    /**
     * 删除题目记录。
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteProblem(@PathVariable Long id,
                                                                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return problemRepo.findById(id)
                .filter(p -> p.getUserId().equals(userId))
                .map(p -> {
                    problemRepo.delete(p);
                    return ResponseEntity.ok(ApiResponse.ok(Map.of("deleted", true, "id", id)));
                })
                .orElse(ResponseEntity.status(404).body(ApiResponse.error(404, "题目不存在")));
    }
}