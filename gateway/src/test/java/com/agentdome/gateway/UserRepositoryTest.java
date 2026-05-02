package com.agentdome.gateway;

import com.agentdome.common.entity.User;
import com.agentdome.common.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindByOpenid() {
        User user = new User();
        user.setOpenid("test-openid-123");
        user.setNickname("TestUser");
        userRepository.save(user);

        Optional<User> found = userRepository.findByOpenid("test-openid-123");

        assertThat(found).isPresent();
        assertThat(found.get().getNickname()).isEqualTo("TestUser");
    }
}
