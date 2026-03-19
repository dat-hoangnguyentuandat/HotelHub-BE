package com.example.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private static final List<String> ALLOWED_TYPES =
            List.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final long MAX_SIZE = 10L * 1024 * 1024; // 10 MB

    /**
     * POST /api/upload/image?folder=services
     * Nhận file ảnh, lưu vào uploads/{folder}/, trả về { "url": "/uploads/services/xxx.jpg" }
     */
    @PostMapping("/image")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_OWNER')")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "misc") String folder
    ) {
        // Validate
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "File không được rỗng"));
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Chỉ chấp nhận ảnh JPG, PNG, WEBP, GIF"));
        }
        if (file.getSize() > MAX_SIZE) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Ảnh quá lớn, tối đa 10MB"));
        }

        try {
            // Tạo thư mục nếu chưa có
            Path dirPath = Paths.get(uploadDir, folder).toAbsolutePath();
            Files.createDirectories(dirPath);

            // Tạo tên file unique
            String originalName = file.getOriginalFilename() != null
                    ? file.getOriginalFilename() : "image";
            String ext = "";
            int dot = originalName.lastIndexOf('.');
            if (dot >= 0) ext = originalName.substring(dot); // ".jpg"

            String fileName = UUID.randomUUID() + ext;
            Path filePath = dirPath.resolve(fileName);

            // Lưu file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Trả về URL tương đối (frontend ghép với BACKEND_URL)
            String url = "/uploads/" + folder + "/" + fileName;
            return ResponseEntity.ok(Map.of("url", url));

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Lưu file thất bại: " + e.getMessage()));
        }
    }
}
