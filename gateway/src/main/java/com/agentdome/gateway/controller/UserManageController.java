package com.agentdome.gateway.controller;

import com.agentdome.common.entity.User;
import com.agentdome.common.repository.UserRepository;
import com.agentdome.gateway.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserManageController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserManageController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 分页查询用户列表。课程设计演示用，实际生产可进一步限制为 ADMIN 角色。
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<User>>> pageUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(size, 1),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.ok(userRepository.findAll(pageable)));
    }

    /**
     * 查询当前登录用户信息。
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> me(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userRepository.findById(userId)
                .map(user -> ResponseEntity.ok(ApiResponse.ok(user)))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error(404, "用户不存在")));
    }

    /**
     * 新增用户。
     */
    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody Map<String, String> body) {
        String username = trim(body.get("username"));
        String password = trim(body.get("password"));
        String nickname = trim(body.get("nickname"));
        String role = trim(body.get("role"));

        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "用户名不能为空"));
        }
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "用户名已存在"));
        }

        User user = new User();
        user.setOpenid("web_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        user.setUsername(username);
        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        user.setNickname(nickname == null || nickname.isEmpty() ? username : nickname);
        user.setRole(role == null || role.isEmpty() ? "USER" : role.toUpperCase());
        user.setCreatedAt(LocalDateTime.now());
        user.setLastActiveAt(LocalDateTime.now());
        return ResponseEntity.ok(ApiResponse.ok(userRepository.save(user)));
    }

    /**
     * 查询用户详情。
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(ApiResponse.ok(user)))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error(404, "用户不存在")));
    }

    /**
     * 修改用户昵称、头像、角色、密码等信息。
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long id,
                                                        @RequestBody Map<String, String> body) {
        return userRepository.findById(id)
                .map(user -> {
                    String nickname = trim(body.get("nickname"));
                    String avatarUrl = trim(body.get("avatarUrl"));
                    String role = trim(body.get("role"));
                    String password = trim(body.get("password"));
                    if (nickname != null && !nickname.isEmpty()) user.setNickname(nickname);
                    if (avatarUrl != null && !avatarUrl.isEmpty()) user.setAvatarUrl(avatarUrl);
                    if (role != null && !role.isEmpty()) user.setRole(role.toUpperCase());
                    if (password != null && !password.isEmpty()) user.setPassword(passwordEncoder.encode(password));
                    user.setLastActiveAt(LocalDateTime.now());
                    return ResponseEntity.ok(ApiResponse.ok(userRepository.save(user)));
                })
                .orElse(ResponseEntity.status(404).body(ApiResponse.error(404, "用户不存在")));
    }

    /**
     * 删除用户。
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok(ApiResponse.ok(Map.of("deleted", true, "id", id)));
                })
                .orElse(ResponseEntity.status(404).body(ApiResponse.error(404, "用户不存在")));
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}