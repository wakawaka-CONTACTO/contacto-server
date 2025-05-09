package org.kiru.user.external.s3;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.kiru.core.exception.BadRequestException;
import org.kiru.core.exception.InvalidValueException;
import org.kiru.core.exception.code.FailureCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@Component
public class S3Service {
    private final String bucketName;
    private final AWSConfig awsConfig;
    private final String basePath;
    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList("image/jpeg", "image/png", "image/jpg", "image/webp", "image/heic", "image/heif","image/avif");
    private static final Long MAX_FILE_SIZE = 10 * 1024 * 1024L;

    public S3Service(@Value("${aws-property.s3-bucket-name}") final String bucketName, AWSConfig awsConfig, @Value("${cloudfront.domain}") String basePath) {
        this.bucketName = bucketName;
        this.awsConfig = awsConfig;
        this.basePath = basePath;
    }

    public String uploadImage(String directoryPath, MultipartFile image) throws IOException {
        final String key = directoryPath + generateImageFileName(image);  // 이미지 파일 이름 생성
        final S3Client s3Client = awsConfig.getS3Client();
        validateExtension(image);  // 파일 확장자 유효성 검사
        validateFileSize(image);   // 파일 크기 유효성 검사
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(image.getContentType())
                .contentDisposition("inline")
                .build();
        RequestBody requestBody = RequestBody.fromBytes(image.getBytes());  // 파일 바이트 배열로 변환
        try {
            s3Client.putObject(request, requestBody);
        } catch (S3Exception e) {
            throw new IOException("이미지 업로드 중 오류가 발생했습니다.", e);  // 예외 발생 시 IOException 처리
        }
        return key;  // S3에 저장된 이미지 경로 반환
    }

    // S3Service.java
    public void deleteImages(Set<String> imageUrls) {
        final S3Client s3Client = awsConfig.getS3Client();
        imageUrls.forEach(imageUrl -> {
            try {
                String imageKey = extractImageKeyFromImageUrl(imageUrl);
                s3Client.deleteObject((DeleteObjectRequest.Builder builder) ->
                    builder.bucket(bucketName)
                        .key(imageKey)
                        .build()
                );
            } catch (BadRequestException e) {
                System.err.println("잘못된 이미지 URL입니다: " + imageUrl + " - " + e.getMessage());
            }
        });
    }

    private String generateImageFileName(MultipartFile image) {
        String extension = getExtension(Objects.requireNonNull(image.getContentType()));
        if (extension == null) {
            throw new InvalidValueException(FailureCode.INVALID_IMAGE_TYPE);
        }
        return UUID.randomUUID() + extension;
    }

    private String getExtension(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/heic" -> ".heic";
            case "image/heif" -> ".heif";
            default -> ".jpg";
        };
    }

    private void validateExtension(MultipartFile image) {
        String contentType = image.getContentType();
        if (!IMAGE_EXTENSIONS.contains(contentType)) {
            throw new InvalidValueException(FailureCode.INVALID_IMAGE_TYPE);
        }
    }

    private void validateFileSize(MultipartFile image) {
        if (image.getSize() > MAX_FILE_SIZE) {
            throw new InvalidValueException(FailureCode.INVALID_IMAGE_SIZE);
        }
    }

    private String extractImageKeyFromImageUrl(String url) {
        if (url.startsWith(this.basePath)) {
            String key = url.substring(basePath.length());
            if (key.startsWith("/")) {
                key = key.substring(1);
            }
            return key;
        } else {
            throw new BadRequestException(FailureCode.WRONG_IMAGE_URL);
        }
    }


    public List<String> getAllImageKeys(String prefix) {
        final S3Client s3Client = awsConfig.getS3Client();
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix) // 특정 디렉토리에서 가져오고 싶다면 prefix를 설정
                .build();
        ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
        return listResponse.contents().stream()
                .map(S3Object::key)
                .toList();
    }
}
