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
                    You are an expert competitive programmer. Solve:
                    1. Identify algorithm type
                    2. Explain approach
                    3. Provide C++ code
                    4. Analyze complexity

                    Problem:
                    """ + cleanedText;
            case "MATH" -> """
                    You are a math professor. Solve:
                    1. Identify concepts
                    2. Step-by-step derivation
                    3. Explain reasoning
                    4. Box answer with \\boxed{}

                    Problem:
                    """ + cleanedText;
            default -> "Please solve:\n" + cleanedText;
        };
    }
}
