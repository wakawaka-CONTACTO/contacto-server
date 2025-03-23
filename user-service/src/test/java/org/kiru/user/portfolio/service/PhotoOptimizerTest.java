package org.kiru.user.portfolio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kiru.user.portfolio.constants.PhotoUsage;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class PhotoOptimizerTest {

  private PhotoOptimizer photoOptimizer;
  private MultipartFile originalFile;
  private final int originalWidth = 2000;
  private final int originalHeight = 1500;

  @BeforeEach
  void setUp() throws Exception {
    BufferedImage originalImage = new BufferedImage(originalWidth, originalHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = originalImage.createGraphics();
    g2d.setPaint(Color.BLUE);
    g2d.fillRect(0, 0, originalWidth, originalHeight);
    g2d.dispose();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(originalImage, "jpg", baos);
    byte[] imageBytes = baos.toByteArray();
    originalFile = new MockMultipartFile("file", "original.jpg", "image/jpeg", imageBytes);

    photoOptimizer = new PhotoOptimizer();
  }

  @Test
  @DisplayName("채팅 프로필 용으로 리사이징: 최대 100 x 100")
  void testOptimizeChatProfile() throws Exception {
    // Given
    PhotoUsage usage = PhotoUsage.CHAT_PROFILE;
    // When
    InputStream optimizedStream = photoOptimizer.optimize(originalFile, usage);
    BufferedImage optimizedImage = ImageIO.read(optimizedStream);
    // Then
    assertThat(optimizedImage.getWidth()).isLessThanOrEqualTo(100);
    assertThat(optimizedImage.getHeight()).isLessThanOrEqualTo(100);
    double originalRatio = (double) originalWidth / originalHeight;
    double optimizedRatio = (double) optimizedImage.getWidth() / optimizedImage.getHeight();
    assertThat(optimizedRatio).isCloseTo(originalRatio, within(0.01));
  }

  @Test
  @DisplayName("포트폴리오 스크롤 용으로 리사이징: 최대 198 x 260")
  void testOptimizePortfolioScroll() throws Exception {
    // Given
    PhotoUsage usage = PhotoUsage.PORTFOLIO_SCROLL;
    // When
    InputStream optimizedStream = photoOptimizer.optimize(originalFile, usage);
    BufferedImage optimizedImage = ImageIO.read(optimizedStream);
    // Then
    assertThat(optimizedImage.getWidth()).isLessThanOrEqualTo(198);
    assertThat(optimizedImage.getHeight()).isLessThanOrEqualTo(260);
    double originalRatio = (double) originalWidth / originalHeight;
    double optimizedRatio = (double) optimizedImage.getWidth() / optimizedImage.getHeight();
    assertThat(optimizedRatio).isCloseTo(originalRatio, within(0.01));
  }

  @Test
  @DisplayName("전체 화면 포트폴리오 조회 용으로 리사이징: 최대 1080 x 1920")
  void testOptimizeFullScreen() throws Exception {
    // Given
    PhotoUsage usage = PhotoUsage.FULL_SCREEN;
    // When
    InputStream optimizedStream = photoOptimizer.optimize(originalFile, usage);
    BufferedImage optimizedImage = ImageIO.read(optimizedStream);
    // Then
    assertThat(optimizedImage.getWidth()).isLessThanOrEqualTo(1080);
    assertThat(optimizedImage.getHeight()).isLessThanOrEqualTo(1920);
    double originalRatio = (double) originalWidth / originalHeight;
    double optimizedRatio = (double) optimizedImage.getWidth() / optimizedImage.getHeight();
    assertThat(optimizedRatio).isCloseTo(originalRatio, within(0.01));
  }
}