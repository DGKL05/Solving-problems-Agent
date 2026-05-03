package com.agentdome.agent;

import com.agentdome.agent.memory.SessionMemoryManager;
import com.agentdome.agent.memory.SummaryService;
import com.agentdome.agent.prompt.PromptTemplateManager;
import com.agentdome.agent.tools.*;
import com.agentdome.common.config.UserProblemTracker;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class AgentService {

    private final PromptTemplateManager promptManager;
    private final SessionMemoryManager memoryManager;
    private final SummaryService summaryService;

    private final SolveProblemTool solveProblemTool;
    private final AddToMistakesTool addToMistakesTool;
    private final QueryMistakesTool queryMistakesTool;
    private final RecommendSimilarTool recommendSimilarTool;
    private final ExplainConceptTool explainConceptTool;
    private final UserProblemTracker problemTracker;

    public AgentService(PromptTemplateManager promptManager,
                        @org.springframework.beans.factory.annotation.Autowired(required = false) SessionMemoryManager memoryManager,
                        SummaryService summaryService,
                        SolveProblemTool solveProblemTool,
                        AddToMistakesTool addToMistakesTool,
                        QueryMistakesTool queryMistakesTool,
                        RecommendSimilarTool recommendSimilarTool,
                        ExplainConceptTool explainConceptTool,
                        UserProblemTracker problemTracker) {
        this.promptManager = promptManager;
        this.memoryManager = memoryManager;
        this.summaryService = summaryService;
        this.solveProblemTool = solveProblemTool;
        this.addToMistakesTool = addToMistakesTool;
        this.queryMistakesTool = queryMistakesTool;
        this.recommendSimilarTool = recommendSimilarTool;
        this.explainConceptTool = explainConceptTool;
        this.problemTracker = problemTracker;
    }

    public String newSession(Long userId) {
        String sessionId = UUID.randomUUID().toString();
        String prevSummary = summaryService.getPreviousSummary(userId);
        if (memoryManager != null) {
            memoryManager.appendMessage(sessionId, "system",
                    promptManager.getSystemPrompt() + "\n历史学习摘要：\n" + prevSummary);
        }
        return sessionId;
    }

    /**
     * Process a user text message. Agent determines intent and delegates to tools.
     */
    public String processMessage(String sessionId, Long userId, String userMessage) {
        if (memoryManager != null) {
            memoryManager.appendMessage(sessionId, "user", userMessage);
        }

        // For MVP: intent-based dispatch matching against tool triggers
        String response;
        if (userMessage.contains("错题") || userMessage.contains("mistake")) {
            if (userMessage.contains("加入") || userMessage.contains("添加")) {
                Long pid = problemTracker.getLastProblem(userId);
                if (pid == null) {
                    response = "请先解答一道题目，然后再加入错题本。";
                } else {
                    response = addToMistakesTool.addToMistakes(userId, pid, sessionId, "manual", userMessage, null);
                }
            } else {
                response = "错题集功能：\n" + queryMistakesTool.queryMistakes(userId).toString();
            }
        } else if (userMessage.contains("推荐") || userMessage.contains("类似") || userMessage.contains("相似")) {
            response = recommendSimilarTool.recommendSimilar(userId, "ACM", 3);
        } else if (userMessage.contains("概念") || userMessage.contains("解释") || userMessage.contains("是什么")) {
            response = explainConceptTool.explainConcept(userMessage, "ACM");
        } else {
            response = "收到你的消息：「" + userMessage + "」\n\n请告诉我你需要什么帮助？\n- 拍照解题（上传图片即可）\n- 加入错题集\n- 查询错题\n- 推荐相似题目\n- 解释某个概念";
        }

        if (memoryManager != null) {
            memoryManager.appendMessage(sessionId, "assistant", response);
        }
        return response;
    }
}
