package com.agentdome.common.repository;

import com.agentdome.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByOpenid(String openid);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
