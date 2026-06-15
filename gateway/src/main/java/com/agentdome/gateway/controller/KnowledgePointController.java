package com.agentdome.gateway.controller;

import com.agentdome.common.entity.KnowledgePoint;
import com.agentdome.common.repository.KnowledgePointRepository;
import com.agentdome.gateway.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge-points")
public class KnowledgePointController {

    private final KnowledgePointRepository knowledgePointRepository;

    public KnowledgePointController(KnowledgePointRepository knowledgePointRepository) {
        this.knowledgePointRepository = knowledgePointRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<KnowledgePoint>>> pageKnowledgePoints(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String subjectType,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(size, 1),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        if (subjectType != null && !subjectType.isBlank()) {
            return ResponseEntity.ok(ApiResponse.ok(
                    knowledgePointRepository.findByUserIdAndSubjectType(userId, subjectType.trim().toUpperCase(), pageable)));
        }
        return ResponseEntity.ok(ApiResponse.ok(knowledgePointRepository.findByUserId(userId, pageable)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<KnowledgePoint>> createKnowledgePoint(@RequestBody KnowledgePoint form,
                                                                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (form.getName() == null || form.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "知识点名称不能为空"));
        }
        form.setId(null);
        form.setUserId(userId);
        form.setName(form.getName().trim());
        if (form.getSubjectType() != null) form.setSubjectType(form.getSubjectType().trim().toUpperCase());
        return ResponseEntity.ok(ApiResponse.ok(knowledgePointRepository.save(form)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KnowledgePoint>> getKnowledgePoint(@PathVariable Long id,
                                                                         HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return knowledgePointRepository.findById(id)
                .filter(k -> k.getUserId().equals(userId))
                .map(k -> ResponseEntity.ok(ApiResponse.ok(k)))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error(404, "知识点不存在")));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<KnowledgePoint>> updateKnowledgePoint(@PathVariable Long id,
                                                                            @RequestBody KnowledgePoint form,
                                                                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return knowledgePointRepository.findById(id)
                .filter(k -> k.getUserId().equals(userId))
                .map(k -> {
                    if (form.getName() != null && !form.getName().trim().isEmpty()) k.setName(form.getName().trim());
                    if (form.getSubjectType() != null) k.setSubjectType(form.getSubjectType().trim().toUpperCase());
                    if (form.getCategory() != null) k.setCategory(form.getCategory());
                    if (form.getDescription() != null) k.setDescription(form.getDescription());
                    if (form.getMasteryLevel() != null) k.setMasteryLevel(form.getMasteryLevel());
                    return ResponseEntity.ok(ApiResponse.ok(knowledgePointRepository.save(k)));
                })
                .orElse(ResponseEntity.status(404).body(ApiResponse.error(404, "知识点不存在")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteKnowledgePoint(@PathVariable Long id,
                                                                                 HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return knowledgePointRepository.findById(id)
                .filter(k -> k.getUserId().equals(userId))
                .map(k -> {
                    knowledgePointRepository.delete(k);
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("deleted", true);
                    result.put("id", id);
                    return ResponseEntity.ok(ApiResponse.ok(result));
                })
                .orElse(ResponseEntity.status(404).body(ApiResponse.error(404, "知识点不存在")));
    }
}