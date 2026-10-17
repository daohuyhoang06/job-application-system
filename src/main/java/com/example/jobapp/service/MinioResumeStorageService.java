package com.example.jobapp.service;

import com.example.jobapp.dto.response.StoredResumeResponse;
import com.example.jobapp.exception.InvalidResumeException;
import com.example.jobapp.exception.ResumeStorageException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class MinioResumeStorageService {

    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final long MAX_RESUME_SIZE_BYTES = 5L * 1024 * 1024;
    private static final byte[] PDF_SIGNATURE = {'%', 'P', 'D', 'F', '-'};

    private final MinioClient minioClient;
    private final String bucket;

    public MinioResumeStorageService(
            MinioClient minioClient,
            @Value("${minio.bucket}") String bucket
    ) {
        this.minioClient = minioClient;
        this.bucket = bucket;
    }

    public StoredResumeResponse upload(Integer applicantId, MultipartFile file) {
        validatePdf(file);

        String originalFilename = sanitizeFilename(file.getOriginalFilename());
        String objectKey = "applicants/" + applicantId + "/resumes/" + UUID.randomUUID() + ".pdf";
        LocalDateTime uploadedAt = LocalDateTime.now();

        try {
            ensureBucketExists();
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(PDF_CONTENT_TYPE)
                        .userMetadata(Map.of("original-filename", originalFilename))
                        .build());
            }
        } catch (IOException exception) {
            throw new ResumeStorageException("Unable to read the uploaded resume", exception);
        } catch (Exception exception) {
            throw new ResumeStorageException("Unable to store the resume", exception);
        }

        return new StoredResumeResponse(
                objectKey,
                originalFilename,
                PDF_CONTENT_TYPE,
                file.getSize(),
                uploadedAt
        );
    }

    public StoredResumeResponse getOwnedResume(Integer applicantId, String objectKey) {
        String expectedPrefix = "applicants/" + applicantId + "/resumes/";
        if (objectKey == null || !objectKey.startsWith(expectedPrefix)) {
            throw new InvalidResumeException("Resume does not belong to the authenticated applicant");
        }

        try {
            var object = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());

            if (!PDF_CONTENT_TYPE.equalsIgnoreCase(object.contentType())) {
                throw new InvalidResumeException("Resume must be a PDF file");
            }

            String originalFilename = object.userMetadata().getOrDefault("original-filename", objectKey);
            return new StoredResumeResponse(
                    objectKey,
                    originalFilename,
                    object.contentType(),
                    object.size(),
                    object.lastModified().toLocalDateTime()
            );
        } catch (InvalidResumeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InvalidResumeException("Uploaded resume was not found in storage");
        }
    }

    public void deleteQuietly(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
        } catch (Exception ignored) {
            // The application transaction remains the source of truth; orphan cleanup can retry later.
        }
    }

    private void ensureBucketExists() throws Exception {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    private void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidResumeException("Resume file is required");
        }
        if (file.getSize() > MAX_RESUME_SIZE_BYTES) {
            throw new InvalidResumeException("Resume must not exceed 5 MB");
        }
        if (!PDF_CONTENT_TYPE.equalsIgnoreCase(file.getContentType())) {
            throw new InvalidResumeException("Resume content type must be application/pdf");
        }

        String filename = sanitizeFilename(file.getOriginalFilename());
        if (!filename.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new InvalidResumeException("Resume filename must end with .pdf");
        }

        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = inputStream.readNBytes(PDF_SIGNATURE.length);
            for (int index = 0; index < PDF_SIGNATURE.length; index++) {
                if (header.length != PDF_SIGNATURE.length || header[index] != PDF_SIGNATURE[index]) {
                    throw new InvalidResumeException("Uploaded file is not a valid PDF");
                }
            }
        } catch (IOException exception) {
            throw new InvalidResumeException("Unable to read the uploaded resume");
        }
    }

    private String sanitizeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new InvalidResumeException("Resume filename is required");
        }
        String filename = originalFilename.replace('\\', '/');
        filename = filename.substring(filename.lastIndexOf('/') + 1).trim();
        if (filename.isEmpty()) {
            throw new InvalidResumeException("Resume filename is required");
        }
        return filename;
    }
}
