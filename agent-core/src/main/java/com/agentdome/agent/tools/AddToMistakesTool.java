package com.agentdome.agent.tools;

import com.agentdome.mistake.MistakeService;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class AddToMistakesTool {

    private final MistakeService mistakeService;

    public AddToMistakesTool(MistakeService mistakeService) {
        this.mistakeService = mistakeService;
    }

    @Tool("将当前题目加入用户的错题集")
    public String addToMistakes(long userId, long problemId, String sessionId,
                                String errorType, String memo, List<String> tags) {
        mistakeService.addToMistakes(userId, problemId, sessionId, errorType, memo, tags);
        return "已成功加入错题集！建议标签: " + (tags != null ? String.join(", ", tags) : "无");
    }
}
