package com.agentdome.image;

import com.agentdome.common.exception.BusinessException;
import com.agentdome.image.dto.OcrResult;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class ImagePipelineService {

    private final GridFSBucket gridFSBucket;
    private final AliyunOcrService ocrService;
    private final TextCleaningService cleaningService;

    public ImagePipelineService(GridFSBucket gridFSBucket,
                                AliyunOcrService ocrService,
                                TextCleaningService cleaningService) {
        this.gridFSBucket = gridFSBucket;
        this.ocrService = ocrService;
        this.cleaningService = cleaningService;
    }

    /**
     * Full pipeline: upload → gridfs → OCR → clean → return cleaned text + imageId.
     */
    public PipelineResult process(MultipartFile file, Long userId) {
        try {
            byte[] bytes = file.getBytes();
            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.jpg";

            GridFSUploadOptions options = new GridFSUploadOptions()
                    .metadata(new org.bson.Document("user_id", userId)
                            .append("filename", filename));
            ObjectId fileId = gridFSBucket.uploadFromStream(filename,
                    new java.io.ByteArrayInputStream(bytes), options);
            String imageId = fileId.toHexString();

            OcrResult ocrResult = ocrService.recognize(bytes);

            String cleanedText = cleaningService.clean(ocrResult);

            return new PipelineResult(imageId, ocrResult.getRawText(), cleanedText);
        } catch (IOException e) {
            throw new BusinessException("Image upload failed: " + e.getMessage());
        }
    }

    public record PipelineResult(String imageId, String rawText, String cleanedText) {}
}
