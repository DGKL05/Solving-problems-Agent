package com.agentdome.common.repository;

import com.agentdome.common.entity.Problem;
import com.agentdome.common.entity.Problem.SubjectType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    List<Problem> findByUserIdAndSubjectType(Long userId, SubjectType subjectType);
    List<Problem> findByUserIdOrderByCreatedAtDesc(Long userId);
    Page<Problem> findByUserId(Long userId, Pageable pageable);
    Page<Problem> findByUserIdAndSubjectType(Long userId, SubjectType subjectType, Pageable pageable);
}