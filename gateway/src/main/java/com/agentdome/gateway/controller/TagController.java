package com.agentdome.gateway.controller;

import com.agentdome.common.entity.Tag;
import com.agentdome.common.repository.TagRepository;
import com.agentdome.gateway.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagRepository tagRepository;

    public TagController(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    /**
     * 查询当前用户的全部标签。
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Tag>>> listTags(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(ApiResponse.ok(tagRepository.findByUserId(userId)));
    }

    /**
     * 新增标签。
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Tag>> createTag(@RequestBody Tag form,
                                                      HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (form.getName() == null || form.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "标签名称不能为空"));
        }
        Tag tag = tagRepository.findByUserIdAndName(userId, form.getName().trim()).orElseGet(Tag::new);
        tag.setUserId(userId);
        tag.setName(form.getName().trim());
        tag.setColor(form.getColor() == null || form.getColor().isBlank() ? "#999999" : form.getColor());
        return ResponseEntity.ok(ApiResponse.ok(tagRepository.save(tag)));
    }

    /**
     * 查询标签详情。
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Tag>> getTag(@PathVariable Long id,
                                                   HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return tagRepository.findById(id)
                .filter(t -> t.getUserId().equals(userId))
                .map(t -> ResponseEntity.ok(ApiResponse.ok(t)))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error(404, "标签不存在")));
    }

    /**
     * 修改标签名称和颜色。
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Tag>> updateTag(@PathVariable Long id,
                                                      @RequestBody Tag form,
                                                      HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return tagRepository.findById(id)
                .filter(t -> t.getUserId().equals(userId))
                .map(t -> {
                    if (form.getName() != null && !form.getName().trim().isEmpty()) {
                        t.setName(form.getName().trim());
                    }
                    if (form.getColor() != null && !form.getColor().trim().isEmpty()) {
                        t.setColor(form.getColor().trim());
                    }
                    return ResponseEntity.ok(ApiResponse.ok(tagRepository.save(t)));
                })
                .orElse(ResponseEntity.status(404).body(ApiResponse.error(404, "标签不存在")));
    }

    /**
     * 删除标签。
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteTag(@PathVariable Long id,
                                                                      HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return tagRepository.findById(id)
                .filter(t -> t.getUserId().equals(userId))
                .map(t -> {
                    tagRepository.delete(t);
                    return ResponseEntity.ok(ApiResponse.ok(Map.of("deleted", true, "id", id)));
                })
                .orElse(ResponseEntity.status(404).body(ApiResponse.error(404, "标签不存在")));
    }
}