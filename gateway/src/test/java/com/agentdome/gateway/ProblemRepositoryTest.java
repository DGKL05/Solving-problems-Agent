package com.agentdome.gateway;

import com.agentdome.common.entity.Problem;
import com.agentdome.common.entity.Problem.SubjectType;
import com.agentdome.common.repository.ProblemRepository;
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
class ProblemRepositoryTest {

    @Autowired
    private ProblemRepository problemRepository;

    @Test
    void shouldFindByUserIdAndSubjectType() {
        Problem p = new Problem();
        p.setUserId(1L);
        p.setSubjectType(SubjectType.ACM);
        p.setOcrRawText("Given an array, find the max subarray sum");
        p.setCleanedText("Given an array, find the max subarray sum");
        problemRepository.save(p);

        List<Problem> problems = problemRepository.findByUserIdAndSubjectType(1L, SubjectType.ACM);

        assertThat(problems).hasSize(1);
        assertThat(problems.get(0).getSubjectType()).isEqualTo(SubjectType.ACM);
    }
}
