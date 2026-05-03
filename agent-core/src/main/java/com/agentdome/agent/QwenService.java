package com.agentdome.agent;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.ResultCallback;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.agentdome.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.function.Consumer;

@Service
public class QwenService {

    private static final Logger log = LoggerFactory.getLogger(QwenService.class);

    @Value("${aliyun.dashscope.api-key}")
    private String apiKey;

    public String solveSync(String subjectType, String cleanedText) {
        String prompt = buildPrompt(subjectType, cleanedText);
        try {
            Generation gen = new Generation();
            Message userMsg = Message.builder().role(Role.USER.getValue()).content(prompt).build();
            GenerationParam param = GenerationParam.builder()
                    .apiKey(apiKey)
                    .model("qwen-max")
                    .messages(Arrays.asList(userMsg))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .build();
            GenerationResult result = gen.call(param);
            return result.getOutput().getChoices().get(0).getMessage().getContent();
        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            throw new BusinessException("Qwen error: " + e.getMessage());
        }
    }

    public void solveStream(String subjectType, String cleanedText,
                            Consumer<String> onToken, Consumer<String> onDone, Consumer<Throwable> onError) {
        String prompt = buildPrompt(subjectType, cleanedText);
        log.info("Qwen streaming solve, prompt len={}", prompt.length());
        try {
            Generation gen = new Generation();
            Message userMsg = Message.builder().role(Role.USER.getValue()).content(prompt).build();
            GenerationParam param = GenerationParam.builder()
                    .apiKey(apiKey)
                    .model("qwen-max")
                    .messages(Arrays.asList(userMsg))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .incrementalOutput(true)
                    .build();

            StringBuilder full = new StringBuilder();
            gen.streamCall(param, new ResultCallback<GenerationResult>() {
                @Override
                public void onEvent(GenerationResult result) {
                    String text = result.getOutput().getChoices().get(0).getMessage().getContent();
                    if (text != null && !text.isEmpty()) {
                        full.append(text);
                        onToken.accept(text);
                    }
                }

                @Override
                public void onComplete() {
                    log.info("Qwen streaming done, total len={}", full.length());
                    onDone.accept(full.toString());
                }

                @Override
                public void onError(Exception e) {
                    log.error("Qwen streaming error", e);
                    onError.accept(e);
                }
            });
        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            log.error("Qwen streaming error", e);
            onError.accept(e);
        }
    }

    private String buildPrompt(String subjectType, String cleanedText) {
        return switch (subjectType) {
            case "ACM" -> """
                    你是一位算法竞赛专家。请解答以下题目：
                    1. 分析问题，识别算法类型
                    2. 给出解题思路和核心算法描述
                    3. 输出C++代码（带必要注释）
                    4. 分析时间复杂度和空间复杂度

                    题目：
                    """ + cleanedText;
            case "MATH" -> """
                    你是一位数学教授。请解答以下题目：
                    1. 明确问题类型和涉及的数学概念
                    2. 给出详细的分步推导过程
                    3. 每一步都要解释原理
                    4. 最终答案用 \\boxed{...} 标注

                    题目：
                    """ + cleanedText;
            default -> "请解答以下题目：\n" + cleanedText;
        };
    }
}
