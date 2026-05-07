package com.agentdome.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(TagRecommendationService.class);
    private final QwenService qwenService;

    public TagRecommendationService(QwenService qwenService) {
        this.qwenService = qwenService;
    }

    /**
     * Recommend a secondary tag based on subject type and problem content.
     * Returns a single tag name (≤5 chars recommended).
     */
    public String recommendTag(String subjectType, String cleanedText) {
        String subjectName = switch (subjectType) {
            case "ACM" -> "算法";
            case "MATH" -> "高数";
            case "CS408" -> "计算机408";
            default -> subjectType;
        };

        String prompt = """
            你是一个题目分类专家。请根据以下信息为题目推荐一个二级标签（知识点/算法名称）。

            一级标签（科目）：%s
            题目内容：%s

            要求：
            - 如果是算法题，推荐具体算法名称（如：动态规划、贪心、DFS、BFS、最短路径、最小生成树、并查集 等）
            - 如果是数学题，推荐具体知识点（如：微分方程、线性代数、概率论、数列极限、多重积分 等）
            - 如果是计算机题，推荐具体知识模块（如：操作系统、计算机网络、数据结构、组成原理 等）
            - 只返回二级标签名称（5个字以内），不要返回其他内容，不要解释。
            """.formatted(subjectName, cleanedText.length() > 500 ? cleanedText.substring(0, 500) : cleanedText);

        try {
            String tag = qwenService.solveSync(subjectType, prompt);
            // Clean up: remove quotes, newlines, extra spaces
            tag = tag.replaceAll("[\"'\\n\\r]", "").trim();
            // Limit to 10 chars
            if (tag.length() > 10) tag = tag.substring(0, 10);
            log.info("Recommended tag: {} for subject {}", tag, subjectType);
            return tag.isEmpty() ? "其他" : tag;
        } catch (Exception e) {
            log.warn("Tag recommendation failed", e);
            return "其他";
        }
    }
}
