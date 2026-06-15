package com.agentdome.gateway.controller;

import com.agentdome.common.entity.Notice;
import com.agentdome.common.repository.NoticeRepository;
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
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeRepository noticeRepository;

    public NoticeController(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Notice>>> pageNotices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(size, 1),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        if (status != null && !status.isBlank()) {
            return ResponseEntity.ok(ApiResponse.ok(noticeRepository.findByStatus(status.trim().toUpperCase(), pageable)));
        }
        if (type != null && !type.isBlank()) {
            return ResponseEntity.ok(ApiResponse.ok(noticeRepository.findByType(type.trim().toUpperCase(), pageable)));
        }
        return ResponseEntity.ok(ApiResponse.ok(noticeRepository.findAll(pageable)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Notice>> createNotice(@RequestBody Notice form,
                                                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (form.getTitle() == null || form.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "公告标题不能为空"));
        }
        form.setId(null);
        form.setTitle(form.getTitle().trim());
        form.setCreatedBy(userId);
        if (form.getType() != null) form.setType(form.getType().trim().toUpperCase());
        if (form.getStatus() != null) form.setStatus(form.getStatus().trim().toUpperCase());
        return ResponseEntity.ok(ApiResponse.ok(noticeRepository.save(form)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Notice>> getNotice(@PathVariable Long id) {
        return noticeRepository.findById(id)
                .map(n -> ResponseEntity.ok(ApiResponse.ok(n)))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error(404, "公告不存在")));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Notice>> updateNotice(@PathVariable Long id,
                                                            @RequestBody Notice form) {
        return noticeRepository.findById(id)
                .map(n -> {
                    if (form.getTitle() != null && !form.getTitle().trim().isEmpty()) n.setTitle(form.getTitle().trim());
                    if (form.getContent() != null) n.setContent(form.getContent());
                    if (form.getType() != null) n.setType(form.getType().trim().toUpperCase());
                    if (form.getStatus() != null) n.setStatus(form.getStatus().trim().toUpperCase());
                    return ResponseEntity.ok(ApiResponse.ok(noticeRepository.save(n)));
                })
                .orElse(ResponseEntity.status(404).body(ApiResponse.error(404, "公告不存在")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteNotice(@PathVariable Long id) {
        return noticeRepository.findById(id)
                .map(n -> {
                    noticeRepository.delete(n);
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("deleted", true);
                    result.put("id", id);
                    return ResponseEntity.ok(ApiResponse.ok(result));
                })
                .orElse(ResponseEntity.status(404).body(ApiResponse.error(404, "公告不存在")));
    }
}