package com.docuflow.app.storage;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
public class MinioStorageService {
    private final MinioClient client;
    private final String bucket;

    public MinioStorageService(
            @Value("${docuflow.storage.minio.endpoint}") String endpoint,
            @Value("${docuflow.storage.minio.accessKey}") String accessKey,
            @Value("${docuflow.storage.minio.secretKey}") String secretKey,
            @Value("${docuflow.storage.minio.bucket}") String bucket
    ) {
        this.client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        this.bucket = bucket;
    }

    public void put(String objectKey, String contentType, InputStream is, long size) throws Exception {
        PutObjectArgs args = PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectKey)
                .contentType(contentType != null ? contentType : "application/octet-stream")
                .stream(is, size, -1)
                .build();
        client.putObject(args);
    }

    public String presignedGet(String objectKey, int expiresSeconds) throws Exception {
        return client.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .method(Method.GET)
                        .expiry(expiresSeconds)
                        .build()
        );
    }

    public static String sha256(MultipartFile file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (InputStream in = file.getInputStream()) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = in.read(buf)) != -1) md.update(buf, 0, r);
        }
        return HexFormat.of().formatHex(md.digest());
    }
}
