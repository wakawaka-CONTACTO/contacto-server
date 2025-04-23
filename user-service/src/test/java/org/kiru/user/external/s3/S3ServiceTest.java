package org.kiru.user.external.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiru.core.exception.InvalidValueException;
//import org.kiru.user.portfolio.service.PhotoOptimizer;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private AWSConfig awsConfig;

    private S3Service s3Service;

    private final String TEST_BUCKET_NAME = "test-bucket";
    private final String TEST_BASE_PATH   = "https://test-cdn.example.com/";

    @BeforeEach
    void setUp() {
        when(awsConfig.getS3Client()).thenReturn(s3Client);
        s3Service = new S3Service(TEST_BUCKET_NAME, awsConfig, TEST_BASE_PATH);
    }

    @Test
    @DisplayName("이미지 업로드 - 성공")
    void uploadFile_Success() throws IOException {
        // Given
        String directoryPath = "users/1/";
        byte[] content         = "test image content".getBytes(StandardCharsets.UTF_8);
        byte[] optimizedContent = "optimized.jpg".getBytes(StandardCharsets.UTF_8);
        MultipartFile file = new MockMultipartFile(
            "test.jpg",
            "test.jpg",
            "image/jpeg",
            content
        );

        // Mock static PhotoOptimizer
        try (MockedStatic<PhotoOptimizer> photoMock = mockStatic(PhotoOptimizer.class)) {
            photoMock.when(() -> PhotoOptimizer.getOptimizedImageBytes(any(MultipartFile.class)))
                .thenReturn(optimizedContent);

            // When
            String result = s3Service.uploadImage(directoryPath, file);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).startsWith(directoryPath);
            assertThat(result).endsWith(".jpg");
            verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }
    }

    @Test
    @DisplayName("이미지 업로드 - 잘못된 파일 형식")
    void uploadFile_InvalidFileFormat() {
        // Given
        String directoryPath = "users/1/";
        byte[] content = "test content".getBytes(StandardCharsets.UTF_8);
        MultipartFile file = new MockMultipartFile(
            "test.txt",
            "test.txt",
            "text/plain",
            content
        );

        // When & Then
        assertThrows(InvalidValueException.class, () -> s3Service.uploadImage(directoryPath, file));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("이미지 업로드 - 파일 크기 초과")
    void uploadFile_FileSizeExceeded() {
        // Given
        String directoryPath = "users/1/";
        byte[] content = new byte[11 * 1024 * 1024]; // 11MB
        MultipartFile file = new MockMultipartFile(
            "test.jpg",
            "test.jpg",
            "image/jpeg",
            content
        );

        // When & Then
        assertThrows(InvalidValueException.class, () -> s3Service.uploadImage(directoryPath, file));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
}
