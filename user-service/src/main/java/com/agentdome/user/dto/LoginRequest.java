package com.agentdome.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String code;
    private String nickname;
    private String avatarUrl;
}
