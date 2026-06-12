package com.agentdome.image;

import com.agentdome.common.exception.BusinessException;
import com.agentdome.image.dto.OcrResult;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Set;

@Service
public class ImagePipelineService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/bmp", "image/tiff"
    );
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20 MB

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
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException("不支持的文件类型，仅支持 JPEG、PNG、WebP、BMP、TIFF");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件过大，最大支持 20MB");
        }

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
