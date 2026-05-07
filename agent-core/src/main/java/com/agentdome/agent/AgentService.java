package com.agentdome.agent;

import com.agentdome.agent.memory.ChatHistoryService;
import com.agentdome.agent.memory.SessionMemoryManager;
import com.agentdome.agent.memory.SummaryService;
import com.agentdome.agent.prompt.PromptTemplateManager;
import com.agentdome.agent.tools.*;
import com.agentdome.common.config.UserProblemTracker;
import com.agentdome.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.function.Consumer;

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
    private final DeleteMistakeTool deleteMistakeTool;
    private final ManageChatHistoryTool manageChatHistoryTool;
    private final UserProblemTracker problemTracker;
    private final QwenService qwenService;
    private final ChatHistoryService chatHistoryService;

    public AgentService(PromptTemplateManager promptManager,
                        @org.springframework.beans.factory.annotation.Autowired(required = false) SessionMemoryManager memoryManager,
                        SummaryService summaryService,
                        SolveProblemTool solveProblemTool,
                        AddToMistakesTool addToMistakesTool,
                        QueryMistakesTool queryMistakesTool,
                        RecommendSimilarTool recommendSimilarTool,
                        ExplainConceptTool explainConceptTool,
                        DeleteMistakeTool deleteMistakeTool,
                        ManageChatHistoryTool manageChatHistoryTool,
                        UserProblemTracker problemTracker,
                        QwenService qwenService,
                        ChatHistoryService chatHistoryService) {
        this.promptManager = promptManager;
        this.memoryManager = memoryManager;
        this.summaryService = summaryService;
        this.solveProblemTool = solveProblemTool;
        this.addToMistakesTool = addToMistakesTool;
        this.queryMistakesTool = queryMistakesTool;
        this.recommendSimilarTool = recommendSimilarTool;
        this.explainConceptTool = explainConceptTool;
        this.deleteMistakeTool = deleteMistakeTool;
        this.manageChatHistoryTool = manageChatHistoryTool;
        this.problemTracker = problemTracker;
        this.qwenService = qwenService;
        this.chatHistoryService = chatHistoryService;
    }

    public String newSession(Long userId) {
        String sessionId = chatHistoryService.createSession(userId);
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
        chatHistoryService.appendMessage(sessionId, userId, "user", userMessage);

        // For MVP: intent-based dispatch matching against tool triggers
        String response;
        if ((userMessage.contains("聊天") || userMessage.contains("对话")) && (userMessage.contains("清空") || userMessage.contains("删除") || userMessage.contains("移除"))) {
            if (userMessage.contains("清空") || userMessage.contains("全部")) {
                int count = chatHistoryService.deleteAllSessions(userId);
                response = "已清空全部" + count + "个聊天记录。";
            } else {
                int idx = extractNumber(userMessage);
                if (idx > 0) {
                    response = manageChatHistoryTool.deleteChatByIndex(userId, idx);
                } else {
                    response = "请告诉我你想删除第几个对话，例如：删除第3个聊天记录";
                }
            }
        } else if (userMessage.contains("错题") || userMessage.contains("mistake")) {
            if (userMessage.contains("删除") || userMessage.contains("移除")) {
                int idx = extractNumber(userMessage);
                if (idx > 0) {
                    response = deleteMistakeTool.deleteMistakeByIndex(userId, idx);
                } else {
                    response = "请告诉我你想删除第几道错题，例如：删除错题本中第3个错题";
                }
            } else if (userMessage.contains("查看") || userMessage.contains("详情") || userMessage.contains("看看")) {
                int idx = extractNumber(userMessage);
                if (idx > 0) {
                    response = queryMistakesTool.queryMistakeByIndex(userId, idx);
                } else {
                    response = "错题集功能：\n" + queryMistakesTool.queryMistakes(userId).toString();
                }
            } else if (userMessage.contains("加入") || userMessage.contains("添加")) {
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
        } else if (userMessage.contains("概念") || userMessage.contains("解释")) {
            response = explainConceptTool.explainConcept(userMessage, "ACM");
        } else {
            // General question - use Qwen to answer
            try {
                response = qwenService.solveSync("ACM", "请用中文简要回答以下问题，要求简洁清晰：\n" + userMessage);
            } catch (Exception e) {
                response = "收到你的消息：「" + userMessage + "」\n\n请告诉我你需要什么帮助？\n- 拍照解题（上传图片即可）\n- 加入错题集\n- 查询错题\n- 删除错题\n- 提问任何知识问题";
            }
        }

        if (memoryManager != null) {
            memoryManager.appendMessage(sessionId, "assistant", response);
        }
        chatHistoryService.appendMessage(sessionId, userId, "assistant", response);
        return response;
    }

    /**
     * Process a user text message with streaming output.
     * Tool responses are sent as a single chunk; Qwen answers stream token-by-token.
     */
    public void processMessageStream(String sessionId, Long userId, String userMessage,
                                     Consumer<String> onToken, Consumer<String> onDone, Consumer<Throwable> onError) {
        if (memoryManager != null) {
            memoryManager.appendMessage(sessionId, "user", userMessage);
        }
        chatHistoryService.appendMessage(sessionId, userId, "user", userMessage);

        if ((userMessage.contains("聊天") || userMessage.contains("对话")) && (userMessage.contains("清空") || userMessage.contains("删除") || userMessage.contains("移除"))) {
            String response;
            if (userMessage.contains("清空") || userMessage.contains("全部")) {
                int count = chatHistoryService.deleteAllSessions(userId);
                response = "已清空全部" + count + "个聊天记录。";
            } else {
                int idx = extractNumber(userMessage);
                if (idx > 0) {
                    response = manageChatHistoryTool.deleteChatByIndex(userId, idx);
                } else {
                    response = "请告诉我你想删除第几个对话，例如：删除第3个聊天记录";
                }
            }
            onToken.accept(response);
            if (memoryManager != null) memoryManager.appendMessage(sessionId, "assistant", response);
            chatHistoryService.appendMessage(sessionId, userId, "assistant", response);
            onDone.accept(response);
        } else if (userMessage.contains("错题") || userMessage.contains("mistake")) {
            String response;
            if (userMessage.contains("清空")) {
                response = queryMistakesTool.clearAllMistakes(userId);
            } else if (userMessage.contains("删除") || userMessage.contains("移除")) {
                int idx = extractNumber(userMessage);
                if (idx > 0) {
                    response = deleteMistakeTool.deleteMistakeByIndex(userId, idx);
                } else {
                    response = "请告诉我你想删除第几道错题，例如：删除错题本中第3个错题";
                }
            } else if (userMessage.contains("查看") || userMessage.contains("详情") || userMessage.contains("看看")) {
                int idx = extractNumber(userMessage);
                if (idx > 0) {
                    response = queryMistakesTool.queryMistakeByIndex(userId, idx);
                } else {
                    response = "错题集功能：\n" + queryMistakesTool.queryMistakes(userId).toString();
                }
            } else if (userMessage.contains("加入") || userMessage.contains("添加")) {
                Long pid = problemTracker.getLastProblem(userId);
                if (pid == null) {
                    response = "请先解答一道题目，然后再加入错题本。";
                } else {
                    response = addToMistakesTool.addToMistakes(userId, pid, sessionId, "manual", userMessage, null);
                }
            } else {
                response = "错题集功能：\n" + queryMistakesTool.queryMistakes(userId).toString();
            }
            onToken.accept(response);
            if (memoryManager != null) memoryManager.appendMessage(sessionId, "assistant", response);
            chatHistoryService.appendMessage(sessionId, userId, "assistant", response);
            onDone.accept(response);
        } else if (userMessage.contains("推荐") || userMessage.contains("类似") || userMessage.contains("相似")) {
            String response = recommendSimilarTool.recommendSimilar(userId, "ACM", 3);
            onToken.accept(response);
            if (memoryManager != null) memoryManager.appendMessage(sessionId, "assistant", response);
            chatHistoryService.appendMessage(sessionId, userId, "assistant", response);
            onDone.accept(response);
        } else if (userMessage.contains("概念") || userMessage.contains("解释")) {
            String response = explainConceptTool.explainConcept(userMessage, "ACM");
            onToken.accept(response);
            if (memoryManager != null) memoryManager.appendMessage(sessionId, "assistant", response);
            chatHistoryService.appendMessage(sessionId, userId, "assistant", response);
            onDone.accept(response);
        } else {
            // General question - stream Qwen response
            qwenService.solveStream("ACM", "请用中文简要回答以下问题，要求简洁清晰：\n" + userMessage,
                    onToken,
                    fullText -> {
                        if (memoryManager != null) memoryManager.appendMessage(sessionId, "assistant", fullText);
                        chatHistoryService.appendMessage(sessionId, userId, "assistant", fullText);
                        onDone.accept(fullText);
                    },
                    onError);
        }
    }

    private int extractNumber(String text) {
        // Extract Chinese or Arabic number: 第3个, 第1题, 第三个, 第一个, etc.
        String[] cnNums = {"一","二","两","三","四","五","六","七","八","九","十"};
        for (int i = 0; i < cnNums.length; i++) {
            if (text.contains("第" + cnNums[i])) return i + 1;
        }
        if (text.contains("第十")) { if(text.contains("十一")) return 11; return 10; }
        // Try Arabic number: 第3个, 删除第3
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("第\\s*(\\d+)\\s*(个|道|题|条)").matcher(text);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); } catch (NumberFormatException e) {}
        }
        return -1;
    }
}
