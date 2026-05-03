package com.agentdome.agent.tools;

import com.agentdome.common.entity.Problem;
import com.agentdome.common.repository.ProblemRepository;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class RecommendSimilarTool {

    private final ProblemRepository problemRepo;

    public RecommendSimilarTool(ProblemRepository problemRepo) {
        this.problemRepo = problemRepo;
    }

    @Tool("根据当前题目的标签和科目类型推荐相似题目")
    public String recommendSimilar(long userId, String subjectType, int count) {
        List<Problem> problems = problemRepo.findByUserIdAndSubjectType(userId,
                Problem.SubjectType.valueOf(subjectType));
        if (problems.isEmpty()) {
            return "暂无相似题目推荐。多做几道题后再来！";
        }
        StringBuilder sb = new StringBuilder("为你推荐以下相似题目：\n");
        int n = Math.min(count, problems.size());
        for (int i = 0; i < n; i++) {
            Problem p = problems.get(i);
            String preview = p.getCleanedText() != null
                    ? p.getCleanedText().substring(0, Math.min(100, p.getCleanedText().length())) + "..."
                    : "(无文本)";
            sb.append(i + 1).append(". ").append(preview).append("\n");
        }
        return sb.toString();
    }
}
