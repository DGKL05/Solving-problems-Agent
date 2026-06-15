package com.agentdome.common.repository;

import com.agentdome.common.entity.KnowledgePoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgePointRepository extends JpaRepository<KnowledgePoint, Long> {
    Page<KnowledgePoint> findByUserId(Long userId, Pageable pageable);
    Page<KnowledgePoint> findByUserIdAndSubjectType(Long userId, String subjectType, Pageable pageable);
    List<KnowledgePoint> findByUserIdOrderByCreatedAtDesc(Long userId);
}