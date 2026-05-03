package com.agentdome.agent.tools;

import com.agentdome.common.entity.MistakeCollection;
import com.agentdome.common.repository.MistakeCollectionRepository;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeleteMistakeTool {

    private final MistakeCollectionRepository mistakeRepo;

    public DeleteMistakeTool(MistakeCollectionRepository mistakeRepo) {
        this.mistakeRepo = mistakeRepo;
    }

    @Tool("根据序号删除用户的错题。序号从1开始，按创建时间倒序排列")
    public String deleteMistakeByIndex(long userId, int index) {
        List<MistakeCollection> mistakes = mistakeRepo.findByUserIdOrderByCreatedAtDesc(userId);
        if (index < 1 || index > mistakes.size()) {
            return "序号无效，你共有" + mistakes.size() + "道错题，请输入1到" + mistakes.size() + "之间的数字";
        }
        MistakeCollection mistake = mistakes.get(index - 1);
        mistakeRepo.delete(mistake);
        return "已删除第" + index + "道错题（ID=" + mistake.getId() + "）。";
    }
}
