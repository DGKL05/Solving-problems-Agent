package com.agentdome.common.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "problem_tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemTag {

    @EmbeddedId
    private ProblemTagId id;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProblemTagId implements java.io.Serializable {
        @Column(name = "problem_id")
        private Long problemId;

        @Column(name = "tag_id")
        private Long tagId;
    }
}
