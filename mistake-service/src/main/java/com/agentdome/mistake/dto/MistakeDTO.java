package com.agentdome.mistake.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MistakeDTO {
    private Long id;
    private Long problemId;
    private String subjectType;
    private String cleanedText;
    private String errorType;
    private String memo;
    private List<String> tags;
    private LocalDateTime createdAt;
}
