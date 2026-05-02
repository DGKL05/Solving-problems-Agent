package com.agentdome.common.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "problems")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", nullable = false, length = 10)
    private SubjectType subjectType;

    @Column(name = "ocr_raw_text", columnDefinition = "TEXT")
    private String ocrRawText;

    @Column(name = "cleaned_text", columnDefinition = "TEXT")
    private String cleanedText;

    @Column(name = "original_image_id", length = 100)
    private String originalImageId;

    @Column(name = "solution_text", columnDefinition = "TEXT")
    private String solutionText;

    @Column(name = "solution_code", columnDefinition = "TEXT")
    private String solutionCode;

    @Column(name = "error_type", length = 50)
    private String errorType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum SubjectType {
        ACM, MATH, CS408
    }
}
