package com.agentdome.image.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcrResult {
    private String rawText;
    private double confidence;
    private List<TextBlock> blocks;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextBlock {
        private String text;
        private int x;
        private int y;
        private int width;
        private int height;
    }
}
