package com.agentdome.mistake;

import com.agentdome.common.entity.MistakeCollection;
import com.agentdome.common.entity.Problem;
import com.agentdome.common.entity.ProblemTag;
import com.agentdome.common.entity.Tag;
import com.agentdome.common.entity.ProblemTag.ProblemTagId;
import com.agentdome.common.repository.*;
import com.agentdome.common.exception.BusinessException;
import com.agentdome.mistake.dto.MistakeDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MistakeService {

    private final MistakeCollectionRepository mistakeRepo;
    private final ProblemRepository problemRepo;
    private final TagService tagService;
    private final ProblemTagRepository problemTagRepo;
    private final TagRepository tagRepo;

    public MistakeService(MistakeCollectionRepository mistakeRepo,
                          ProblemRepository problemRepo,
                          TagService tagService,
                          ProblemTagRepository problemTagRepo,
                          TagRepository tagRepo) {
        this.mistakeRepo = mistakeRepo;
        this.problemRepo = problemRepo;
        this.tagService = tagService;
        this.problemTagRepo = problemTagRepo;
        this.tagRepo = tagRepo;
    }

    @Transactional
    public MistakeCollection addToMistakes(Long userId, Long problemId, String sessionId,
                                           String errorType, String memo, List<String> tagNames) {
        Problem problem = problemRepo.findById(problemId)
                .orElseThrow(() -> new BusinessException("Problem not found"));

        MistakeCollection mistake = new MistakeCollection();
        mistake.setUserId(userId);
        mistake.setProblemId(problemId);
        mistake.setSessionId(sessionId);
        mistake.setMemo(memo);
        mistakeRepo.save(mistake);

        problem.setErrorType(errorType);
        problemRepo.save(problem);

        if (tagNames != null) {
            for (String name : tagNames) {
                com.agentdome.mistake.dto.TagDTO tagDTO = new com.agentdome.mistake.dto.TagDTO();
                tagDTO.setName(name);
                Tag tag = tagService.createTag(userId, tagDTO);
                ProblemTag pt = new ProblemTag();
                pt.setId(new ProblemTagId(problemId, tag.getId()));
                problemTagRepo.save(pt);
            }
        }

        return mistake;
    }

    public List<MistakeDTO> getUserMistakes(Long userId) {
        List<MistakeCollection> mistakes = mistakeRepo.findByUserIdOrderByCreatedAtDesc(userId);
        return mistakes.stream().map(m -> {
            Problem p = problemRepo.findById(m.getProblemId()).orElse(null);
            MistakeDTO dto = new MistakeDTO();
            dto.setId(m.getId());
            dto.setProblemId(m.getProblemId());
            dto.setSessionId(m.getSessionId());
            dto.setCreatedAt(m.getCreatedAt());
            dto.setMemo(m.getMemo());
            if (p != null) {
                dto.setSubjectType(p.getSubjectType().name());
                dto.setCleanedText(p.getCleanedText());
                dto.setSolutionText(p.getSolutionText());
                dto.setErrorType(p.getErrorType());
                // Load tags
                List<String> tags = problemTagRepo.findByIdProblemId(p.getId())
                        .stream()
                        .map(pt -> tagRepo.findById(pt.getId().getTagId()).map(Tag::getName).orElse(null))
                        .filter(t -> t != null)
                        .collect(Collectors.toList());
                dto.setTags(tags);
            }
            return dto;
        }).collect(Collectors.toList());
    }

    public List<MistakeDTO> queryMistakes(Long userId, String tag, LocalDateTime start, LocalDateTime end) {
        return getUserMistakes(userId);
    }

    public void deleteMistake(Long userId, Long mistakeId) {
        MistakeCollection mistake = mistakeRepo.findById(mistakeId)
                .orElseThrow(() -> new BusinessException("错题不存在"));
        if (!mistake.getUserId().equals(userId)) {
            throw new BusinessException("无权删除");
        }
        mistakeRepo.delete(mistake);
    }

    @Transactional
    public int clearAllMistakes(Long userId) {
        List<MistakeCollection> all = mistakeRepo.findByUserIdOrderByCreatedAtDesc(userId);
        int count = all.size();
        mistakeRepo.deleteByUserId(userId);
        return count;
    }
}
