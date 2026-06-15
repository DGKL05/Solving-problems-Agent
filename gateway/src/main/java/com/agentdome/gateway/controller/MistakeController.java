package com.agentdome.gateway.controller;

import com.agentdome.common.entity.MistakeCollection;
import com.agentdome.common.entity.Problem;
import com.agentdome.common.repository.MistakeCollectionRepository;
import com.agentdome.common.repository.ProblemRepository;
import com.agentdome.gateway.dto.ApiResponse;
import com.agentdome.mistake.MistakeService;
import com.agentdome.mistake.dto.MistakeDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MistakeController {

    private final MistakeService mistakeService;
    private final MistakeCollectionRepository mistakeRepository;
    private final ProblemRepository problemRepository;

    public MistakeController(MistakeService mistakeService,
                             MistakeCollectionRepository mistakeRepository,
                             ProblemRepository problemRepository) {
        this.mistakeService = mistakeService;
        this.mistakeRepository = mistakeRepository;
        this.problemRepository = problemRepository;
    }

    /**
     * 查询当前用户全部错题，兼容旧接口。
     */
    @GetMapping("/mistakes")
    public ResponseEntity<ApiResponse<List<MistakeDTO>>> listMistakes(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(ApiResponse.ok(mistakeService.getUserMistakes(userId)));
    }

    /**
     * 分页查询错题收藏记录。
     */
    @GetMapping("/mistakes/page")
    public ResponseEntity<ApiResponse<Page<MistakeCollection>>> pageMistakes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(size, 1),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.ok(mistakeRepository.findByUserId(userId, pageable)));
    }

    /**
     * 手动新增错题收藏。body 至少传 problemId。
     */
    @PostMapping("/mistakes")
    public ResponseEntity<ApiResponse<MistakeCollection>> createMistake(@RequestBody Map<String, Object> body,
                                                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long problemId = toLong(body.get("problemId"));
        if (problemId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "problemId 不能为空"));
        }
        Problem problem = problemRepository.findById(problemId).orElse(null);
        if (problem == null || !problem.getUserId().equals(userId)) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "题目不存在"));
        }
        MistakeCollection mistake = new MistakeCollection();
        mistake.setUserId(userId);
        mistake.setProblemId(problemId);
        mistake.setSessionId(asString(body.get("sessionId")));
        mistake.setMemo(asString(body.get("memo")));
        mistake.setReviewCount(0);
        return ResponseEntity.ok(ApiResponse.ok(mistakeRepository.save(mistake)));
    }

    /**
     * 查询错题详情。
     */
    @GetMapping("/mistakes/{id}")
    public ResponseEntity<ApiResponse<MistakeCollection>> getMistake(@PathVariable Long id,
                                                                     HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return mistakeRepository.findById(id)
                .filter(m -> m.getUserId().equals(userId))
                .map(m -> ResponseEntity.ok(ApiResponse.ok(m)))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error(404, "错题不存在")));
    }

    /**
     * 修改错题备注、复习次数等信息。
     */
    @PutMapping("/mistakes/{id}")
    public ResponseEntity<ApiResponse<MistakeCollection>> updateMistake(@PathVariable Long id,
                                                                        @RequestBody Map<String, Object> body,
                                                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return mistakeRepository.findById(id)
                .filter(m -> m.getUserId().equals(userId))
                .map(m -> {
                    if (body.containsKey("memo")) m.setMemo(asString(body.get("memo")));
                    if (body.containsKey("sessionId")) m.setSessionId(asString(body.get("sessionId")));
                    if (body.containsKey("reviewCount")) m.setReviewCount(toInteger(body.get("reviewCount")));
                    if (Boolean.TRUE.equals(body.get("reviewed"))) {
                        m.setReviewCount((m.getReviewCount() == null ? 0 : m.getReviewCount()) + 1);
                        m.setLastReviewedAt(LocalDateTime.now());
                    }
                    return ResponseEntity.ok(ApiResponse.ok(mistakeRepository.save(m)));
                })
                .orElse(ResponseEntity.status(404).body(ApiResponse.error(404, "错题不存在")));
    }

    /**
     * 删除错题收藏。
     */
    @DeleteMapping("/mistakes/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteMistake(@PathVariable Long id,
                                                                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return mistakeRepository.findById(id)
                .filter(m -> m.getUserId().equals(userId))
                .map(m -> {
                    mistakeRepository.delete(m);
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("deleted", true);
                    result.put("id", id);
                    return ResponseEntity.ok(ApiResponse.ok(result));
                })
                .orElse(ResponseEntity.status(404).body(ApiResponse.error(404, "错题不存在")));
    }

    /**
     * 清空当前用户所有错题。
     */
    @DeleteMapping("/mistakes")
    public ResponseEntity<ApiResponse<Map<String, Object>>> clearMistakes(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        int count = mistakeService.clearAllMistakes(userId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("deleted", count);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer toInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}