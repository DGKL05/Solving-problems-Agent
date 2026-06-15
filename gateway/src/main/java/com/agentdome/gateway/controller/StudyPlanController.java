package com.agentdome.gateway.controller;

import com.agentdome.common.entity.StudyPlan;
import com.agentdome.gateway.dto.ApiResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/study-plans")
public class StudyPlanController {

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> pageStudyPlans(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        int pageIndex = Math.max(page - 1, 0);
        int pageSize = Math.max(size, 1);
        String base = "from StudyPlan p where p.userId = :userId" +
                (status == null || status.isBlank() ? "" : " and p.status = :status");
        var query = entityManager.createQuery("select p " + base + " order by p.createdAt desc", StudyPlan.class)
                .setParameter("userId", userId)
                .setFirstResult(pageIndex * pageSize)
                .setMaxResults(pageSize);
        var countQuery = entityManager.createQuery("select count(p) " + base, Long.class)
                .setParameter("userId", userId);
        if (status != null && !status.isBlank()) {
            query.setParameter("status", status.trim().toUpperCase());
            countQuery.setParameter("status", status.trim().toUpperCase());
        }
        List<StudyPlan> records = query.getResultList();
        Long total = countQuery.getSingleResult();
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "content", records,
                "totalElements", total,
                "page", page,
                "size", size
        )));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<StudyPlan>> createStudyPlan(@RequestBody StudyPlan form,
                                                                  HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (form.getTitle() == null || form.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "学习计划标题不能为空"));
        }
        form.setId(null);
        form.setUserId(userId);
        form.setTitle(form.getTitle().trim());
        if (form.getSubjectType() != null) form.setSubjectType(form.getSubjectType().trim().toUpperCase());
        if (form.getStatus() != null) form.setStatus(form.getStatus().trim().toUpperCase());
        entityManager.persist(form);
        return ResponseEntity.ok(ApiResponse.ok(form));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudyPlan>> getStudyPlan(@PathVariable Long id,
                                                               HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        StudyPlan plan = entityManager.find(StudyPlan.class, id);
        if (plan == null || !plan.getUserId().equals(userId)) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "学习计划不存在"));
        }
        return ResponseEntity.ok(ApiResponse.ok(plan));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<StudyPlan>> updateStudyPlan(@PathVariable Long id,
                                                                  @RequestBody StudyPlan form,
                                                                  HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        StudyPlan plan = entityManager.find(StudyPlan.class, id);
        if (plan == null || !plan.getUserId().equals(userId)) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "学习计划不存在"));
        }
        if (form.getTitle() != null && !form.getTitle().trim().isEmpty()) plan.setTitle(form.getTitle().trim());
        if (form.getSubjectType() != null) plan.setSubjectType(form.getSubjectType().trim().toUpperCase());
        if (form.getContent() != null) plan.setContent(form.getContent());
        if (form.getTargetCount() != null) plan.setTargetCount(form.getTargetCount());
        if (form.getFinishedCount() != null) plan.setFinishedCount(form.getFinishedCount());
        if (form.getStatus() != null) plan.setStatus(form.getStatus().trim().toUpperCase());
        if (form.getStartDate() != null) plan.setStartDate(form.getStartDate());
        if (form.getEndDate() != null) plan.setEndDate(form.getEndDate());
        return ResponseEntity.ok(ApiResponse.ok(entityManager.merge(plan)));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteStudyPlan(@PathVariable Long id,
                                                                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        StudyPlan plan = entityManager.find(StudyPlan.class, id);
        if (plan == null || !plan.getUserId().equals(userId)) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "学习计划不存在"));
        }
        entityManager.remove(plan);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("deleted", true, "id", id)));
    }
}