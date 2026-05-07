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

    @Tool("根据序号查看单道错题的详细信息。序号从1开始，按创建时间倒序排列")
    public String queryMistakeByIndex(long userId, int index) {
        List<MistakeDTO> mistakes = mistakeService.getUserMistakes(userId);
        if (index < 1 || index > mistakes.size()) {
            return "序号无效，你共有" + mistakes.size() + "道错题，请输入1到" + mistakes.size() + "之间的数字";
        }
        MistakeDTO m = mistakes.get(index - 1);
        StringBuilder sb = new StringBuilder();
        sb.append("【错题 #").append(index).append("】\n");
        sb.append("科目：").append(m.getSubjectType() != null ? m.getSubjectType() : "未分类").append("\n");
        if (m.getErrorType() != null) sb.append("错误类型：").append(m.getErrorType()).append("\n");
        sb.append("\n📄 题目：\n").append(m.getCleanedText() != null ? m.getCleanedText() : "无").append("\n");
        sb.append("\n✅ 解答：\n").append(m.getSolutionText() != null ? m.getSolutionText() : "暂无解答").append("\n");
        if (m.getMemo() != null && !m.getMemo().isEmpty()) sb.append("\n📝 备注：").append(m.getMemo());
        return sb.toString();
    }

    @Tool("清空用户的所有错题")
    public String clearAllMistakes(long userId) {
        int count = mistakeService.clearAllMistakes(userId);
        return "已清空全部" + count + "道错题。";
    }
}
