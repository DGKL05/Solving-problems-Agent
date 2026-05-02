package com.agentdome.agent.tools;

import com.agentdome.agent.prompt.PromptTemplateManager;
import com.agentdome.common.entity.Problem;
import com.agentdome.common.repository.ProblemRepository;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
public class SolveProblemTool {

    private final ProblemRepository problemRepo;
    private final PromptTemplateManager promptManager;

    public SolveProblemTool(ProblemRepository problemRepo,
                            PromptTemplateManager promptManager) {
        this.problemRepo = problemRepo;
        this.promptManager = promptManager;
    }

    @Tool("Solve a problem from an uploaded image. Call when user asks to solve a problem.")
    public String solveProblem(long userId, String subjectType, String cleanedText, String imageId) {
        String prompt = promptManager.buildSolvePrompt(subjectType, cleanedText);

        // Save to DB with placeholder solution (actual LLM call handled by AgentService)
        Problem problem = new Problem();
        problem.setUserId(userId);
        problem.setSubjectType(Problem.SubjectType.valueOf(subjectType));
        problem.setOriginalImageId(imageId);
        problem.setCleanedText(cleanedText);
        problem.setSolutionText("Processing via AgentService...");
        problemRepo.save(problem);

        return String.format("[解题请求已接收] 科目：%s, 题目：%s...", subjectType,
                cleanedText != null && cleanedText.length() > 50 ? cleanedText.substring(0, 50) : cleanedText);
    }
}
