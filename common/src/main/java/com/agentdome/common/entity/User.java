package com.agentdome.common.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 100)
    private String openid;

    @Column(unique = true, length = 50)
    private String username;

    @Column(length = 255)
    private String password;

    @Column(length = 100)
    private String nickname;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    /**
     * 用户角色：ADMIN 管理员，USER 普通用户。
     */
    @Column(length = 20, nullable = false)
    private String role = "USER";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @PrePersist
    protected void onCreate() {
        if (role == null || role.isBlank()) {
            role = "USER";
        }
        createdAt = LocalDateTime.now();
        lastActiveAt = LocalDateTime.now();
    }
}