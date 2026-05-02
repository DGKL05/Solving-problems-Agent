package com.agentdome.mistake;

import com.agentdome.common.entity.Tag;
import com.agentdome.common.repository.TagRepository;
import com.agentdome.common.exception.BusinessException;
import com.agentdome.mistake.dto.TagDTO;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public Tag createTag(Long userId, TagDTO dto) {
        Optional<Tag> existing = tagRepository.findByUserIdAndName(userId, dto.getName());
        if (existing.isPresent()) {
            return existing.get();
        }
        Tag tag = new Tag();
        tag.setUserId(userId);
        tag.setName(dto.getName());
        tag.setColor(dto.getColor() != null ? dto.getColor() : "#999999");
        return tagRepository.save(tag);
    }

    public List<Tag> getUserTags(Long userId) {
        return tagRepository.findByUserId(userId);
    }

    public void deleteTag(Long userId, Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new BusinessException("Tag not found"));
        if (!tag.getUserId().equals(userId)) {
            throw new BusinessException(403, "Forbidden");
        }
        tagRepository.delete(tag);
    }
}
