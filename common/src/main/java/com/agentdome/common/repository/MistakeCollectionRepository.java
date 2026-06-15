package com.agentdome.common.repository;

import com.agentdome.common.entity.MistakeCollection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface MistakeCollectionRepository extends JpaRepository<MistakeCollection, Long> {
    List<MistakeCollection> findByUserIdOrderByCreatedAtDesc(Long userId);
    Page<MistakeCollection> findByUserId(Long userId, Pageable pageable);
    List<MistakeCollection> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
    void deleteByUserId(Long userId);
}