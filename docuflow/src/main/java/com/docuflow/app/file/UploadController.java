package com.docuflow.app.file;

import com.docuflow.app.storage.MinioStorageService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class UploadController {
    private final FileMetadataRepository repo;
    private final MinioStorageService storage;

    public UploadController(FileMetadataRepository repo, MinioStorageService storage) {
        this.repo = repo;
        this.storage = storage;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status","ok","time", Instant.now().toString());
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @Transactional
    public ResponseEntity<?> upload(@RequestPart("file") @NotNull MultipartFile file) throws Exception {
        String sha = MinioStorageService.sha256(file);
        var existing = repo.findBySha256(sha);
        if (existing.isPresent()) {
            return ResponseEntity.ok(Map.of("id", existing.get().getId(), "duplicate", true));
        }

        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String objectKey = "raw/%s_%s".formatted(sha, original);
        storage.put(objectKey, file.getContentType(), file.getInputStream(), file.getSize());

        var meta = new FileMetadata();
        meta.setOriginalname(original);
        meta.setSha256(sha);
        meta.setObjectKey(objectKey);
        meta.setSizeBytes(file.getSize());
        meta.setMimeType(file.getContentType());
        meta = repo.save(meta);

        return ResponseEntity.ok(Map.of("id", meta.getId(), "duplicate", false));
    }

    @GetMapping("/{id}/url")
    public ResponseEntity<?> presigned(@PathVariable UUID id) throws Exception {
        var meta = repo.findById(id).orElseThrow();
        var url = storage.presignedGet(meta.getObjectKey(), 300);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
