package com.agentdome.common.repository;

import com.agentdome.common.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Page<Notice> findByStatus(String status, Pageable pageable);
    Page<Notice> findByType(String type, Pageable pageable);
}