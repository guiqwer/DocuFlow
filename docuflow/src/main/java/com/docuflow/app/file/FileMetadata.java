package com.docuflow.app.file;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "file_metadata", indexes = {
        @Index(name = "idx_file_sha256", columnList = "sha256", unique = true)
})
@Getter
@Setter
public class FileMetadata {

    @Id @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String originalname;

    @Column(nullable = false, unique = true, length = 64)
    private String sha256;

    @Column(nullable = false)
    private String objectKey;

    @Column(nullable = false)
    private long sizeBytes;

    private String mimeType;

    @Column(nullable = false)
    private Instant uploadedAt = Instant.now();
}
