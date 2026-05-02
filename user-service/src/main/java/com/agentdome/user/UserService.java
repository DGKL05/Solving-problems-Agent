package com.agentdome.user;

import com.agentdome.common.entity.User;
import com.agentdome.common.exception.BusinessException;
import com.agentdome.common.repository.UserRepository;
import com.agentdome.common.util.JwtUtil;
import com.agentdome.user.dto.LoginRequest;
import com.agentdome.user.dto.LoginResponse;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final WeChatAuthService weChatAuthService;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository,
                       WeChatAuthService weChatAuthService,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.weChatAuthService = weChatAuthService;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        String openid = weChatAuthService.codeToOpenid(request.getCode());

        User user = userRepository.findByOpenid(openid)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setOpenid(openid);
                    newUser.setNickname(request.getNickname());
                    newUser.setAvatarUrl(request.getAvatarUrl());
                    return userRepository.save(newUser);
                });

        user.setLastActiveAt(LocalDateTime.now());
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId());
        return new LoginResponse(token, user.getId(), user.getNickname(), user.getAvatarUrl());
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
    }
}
