package com.agentdome.agent.tools;

import com.agentdome.agent.prompt.PromptTemplateManager;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
public class ExplainConceptTool {

    private final PromptTemplateManager promptManager;

    public ExplainConceptTool(PromptTemplateManager promptManager) {
        this.promptManager = promptManager;
    }

    @Tool("解释指定科目的概念或知识点")
    public String explainConcept(String concept, String subjectType) {
        return String.format("正在准备关于「%s」（科目：%s）的详细解释...\n\n请参考教材中的相关章节。",
                concept, subjectType);
    }
}
