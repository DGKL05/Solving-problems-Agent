package com.agentdome.gateway.config;

import com.agentdome.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(jwtUtil))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**", "/api/health");
    }

    static class AuthInterceptor implements HandlerInterceptor {
        private final JwtUtil jwtUtil;

        AuthInterceptor(JwtUtil jwtUtil) {
            this.jwtUtil = jwtUtil;
        }

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                                 Object handler) throws Exception {
            String token = null;

            // Try Authorization header first
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }

            // Fallback to request parameter (for wx.uploadFile compatibility)
            if (token == null || token.isEmpty()) {
                token = request.getParameter("token");
            }

            if (token == null || !jwtUtil.validateToken(token)) {
                response.setStatus(401);
                response.getWriter().write("{\"code\":401,\"message\":\"Unauthorized\"}");
                return false;
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            request.setAttribute("userId", userId);
            return true;
        }
    }
}
