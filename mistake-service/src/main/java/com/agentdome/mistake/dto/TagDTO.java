package com.agentdome.mistake.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class TagDTO {
    private Long id;

    @NotBlank
    private String name;
    private String color;
}
