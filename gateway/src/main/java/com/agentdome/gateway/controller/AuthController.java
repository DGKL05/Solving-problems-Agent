package com.agentdome.gateway.controller;

import com.agentdome.common.entity.User;
import com.agentdome.common.repository.UserRepository;
import com.agentdome.common.util.JwtUtil;
import com.agentdome.user.UserService;
import com.agentdome.user.dto.LoginRequest;
import com.agentdome.user.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserService userService, UserRepository userRepository, JwtUtil jwtUtil) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping("/guest")
    public ResponseEntity<LoginResponse> guestLogin() {
        String guestOpenid = "guest_" + UUID.randomUUID().toString().substring(0, 8);
        User user = userRepository.findByOpenid(guestOpenid).orElseGet(() -> {
            User newUser = new User();
            newUser.setOpenid(guestOpenid);
            newUser.setNickname("游客" + guestOpenid.substring(6));
            return userRepository.save(newUser);
        });
        String token = jwtUtil.generateToken(user.getId());
        return ResponseEntity.ok(new LoginResponse(token, user.getId(), user.getNickname(), null));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String nickname = body.getOrDefault("nickname", username);

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "用户名和密码不能为空"));
        }
        username = username.trim();
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "用户名已存在"));
        }

        User user = new User();
        user.setOpenid("web_" + UUID.randomUUID().toString().substring(0, 12));
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId());
        return ResponseEntity.ok(new LoginResponse(token, user.getId(), user.getNickname(), null));
    }

    @PostMapping("/web-login")
    public ResponseEntity<?> webLogin(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "用户名和密码不能为空"));
        }

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || user.getPassword() == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("code", 401, "message", "用户名或密码错误"));
        }

        String token = jwtUtil.generateToken(user.getId());
        return ResponseEntity.ok(new LoginResponse(token, user.getId(), user.getNickname(), null));
    }
}
