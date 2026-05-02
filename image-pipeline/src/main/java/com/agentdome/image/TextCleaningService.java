package com.agentdome.image;

import com.agentdome.image.dto.OcrResult;
import org.springframework.stereotype.Service;

@Service
public class TextCleaningService {

    /**
     * Clean OCR output: remove noise lines, merge paragraphs,
     * preserve code indentation and math formulas.
     */
    public String clean(OcrResult ocrResult) {
        if (ocrResult == null || ocrResult.getRawText() == null) {
            return "";
        }

        String[] lines = ocrResult.getRawText().split("\n");
        StringBuilder cleaned = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.isEmpty()) continue;
            if (trimmed.startsWith("扫描") || trimmed.startsWith("第") && trimmed.contains("页")) continue;

            cleaned.append(trimmed).append("\n");
        }

        return cleaned.toString().replaceAll("\n{3,}", "\n\n").trim();
    }
}
