package com.agentdome.agent.tools;

import com.agentdome.agent.memory.ChatHistoryService;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
public class ManageChatHistoryTool {

    private final ChatHistoryService chatHistoryService;

    public ManageChatHistoryTool(ChatHistoryService chatHistoryService) {
        this.chatHistoryService = chatHistoryService;
    }

    @Tool("清空用户的所有聊天记录")
    public String deleteAllChats(long userId) {
        int count = chatHistoryService.deleteAllSessions(userId);
        return "已清空全部" + count + "个聊天记录。";
    }

    @Tool("根据序号删除指定聊天记录。序号从1开始，按创建时间倒序排列")
    public String deleteChatByIndex(long userId, int index) {
        return chatHistoryService.deleteSessionByIndex(userId, index);
    }
}
