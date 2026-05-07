package com.agentdome.agent.tools;

import com.agentdome.agent.TagRecommendationService;
import com.agentdome.common.entity.Problem;
import com.agentdome.common.repository.ProblemRepository;
import com.agentdome.mistake.MistakeService;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class AddToMistakesTool {

    private final MistakeService mistakeService;
    private final ProblemRepository problemRepo;
    private final TagRecommendationService tagService;

    public AddToMistakesTool(MistakeService mistakeService, ProblemRepository problemRepo,
                             TagRecommendationService tagService) {
        this.mistakeService = mistakeService;
        this.problemRepo = problemRepo;
        this.tagService = tagService;
    }

    @Tool("将当前题目加入用户的错题集，AI自动推荐二级标签")
    public String addToMistakes(long userId, long problemId, String sessionId,
                                String errorType, String memo, List<String> tags) {
        // Look up problem to get content and subject type
        Problem problem = problemRepo.findById(problemId).orElse(null);
        String primaryTag = null;
        String secondaryTag = null;

        if (problem != null) {
            // Primary tag: map subject type
            primaryTag = switch (problem.getSubjectType()) {
                case ACM -> "算法";
                case MATH -> "高数";
                case CS408 -> "计算机408";
            };

            // Secondary tag: AI recommendation based on problem content
            if (problem.getCleanedText() != null && !problem.getCleanedText().isEmpty()) {
                secondaryTag = tagService.recommendTag(
                        problem.getSubjectType().name(), problem.getCleanedText());
            }
        }

        // Compose tags
        List<String> allTags = new ArrayList<>();
        if (primaryTag != null) allTags.add(primaryTag);
        if (secondaryTag != null && !secondaryTag.isEmpty()) allTags.add(secondaryTag);

        mistakeService.addToMistakes(userId, problemId, sessionId, errorType, memo, allTags);

        String tagInfo = "";
        if (primaryTag != null) tagInfo += "一级: " + primaryTag;
        if (secondaryTag != null) tagInfo += (tagInfo.isEmpty() ? "" : ", ") + "二级: " + secondaryTag;
        return "已加入错题集！" + (tagInfo.isEmpty() ? "" : "标签 → " + tagInfo);
    }
}
