package com.agentdome.gateway;

import com.agentdome.common.entity.*;
import com.agentdome.common.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MistakeRepositoryTest {

    @Autowired private TagRepository tagRepository;
    @Autowired private MistakeCollectionRepository mistakeRepository;

    @Test
    void shouldCreateAndFindTags() {
        Tag tag = new Tag();
        tag.setUserId(1L);
        tag.setName("动态规划");
        tag.setColor("#4A90D9");
        tagRepository.save(tag);

        List<Tag> tags = tagRepository.findByUserId(1L);

        assertThat(tags).hasSize(1);
        assertThat(tags.get(0).getName()).isEqualTo("动态规划");
    }

    @Test
    void shouldCreateAndFindMistakes() {
        MistakeCollection m = new MistakeCollection();
        m.setUserId(1L);
        m.setProblemId(10L);
        m.setSessionId("session-abc");
        m.setMemo("忘了状态转移方程");
        mistakeRepository.save(m);

        List<MistakeCollection> mistakes = mistakeRepository.findByUserIdOrderByCreatedAtDesc(1L);

        assertThat(mistakes).hasSize(1);
        assertThat(mistakes.get(0).getMemo()).isEqualTo("忘了状态转移方程");
    }
}
