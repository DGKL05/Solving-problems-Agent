package com.agentdome.gateway.controller;

import com.agentdome.common.entity.User;
import com.agentdome.common.repository.UserRepository;
import com.agentdome.common.util.JwtUtil;
import com.agentdome.user.UserService;
import com.agentdome.user.dto.LoginRequest;
import com.agentdome.user.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

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
        // Create or find guest user
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
}
