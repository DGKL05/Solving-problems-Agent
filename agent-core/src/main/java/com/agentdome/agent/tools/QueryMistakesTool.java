package com.agentdome.agent.tools;

import com.agentdome.mistake.MistakeService;
import com.agentdome.mistake.dto.MistakeDTO;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class QueryMistakesTool {

    private final MistakeService mistakeService;

    public QueryMistakesTool(MistakeService mistakeService) {
        this.mistakeService = mistakeService;
    }

    @Tool("查询用户的错题集，返回错题列表")
    public List<MistakeDTO> queryMistakes(long userId) {
        return mistakeService.getUserMistakes(userId);
    }
}
