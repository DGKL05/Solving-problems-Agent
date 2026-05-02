package com.agentdome.common.repository;

import com.agentdome.common.entity.ProblemTag;
import com.agentdome.common.entity.ProblemTag.ProblemTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProblemTagRepository extends JpaRepository<ProblemTag, ProblemTagId> {
    List<ProblemTag> findByIdProblemId(Long problemId);
    List<ProblemTag> findByIdTagId(Long tagId);
    void deleteByIdProblemId(Long problemId);
}
