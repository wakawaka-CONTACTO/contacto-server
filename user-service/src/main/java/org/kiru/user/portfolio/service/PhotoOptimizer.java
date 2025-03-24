package org.kiru.user.portfolio.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import net.coobird.thumbnailator.Thumbnails;
import org.kiru.user.portfolio.constants.PhotoUsage;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class PhotoOptimizer {

  public InputStream optimize(MultipartFile multipartFile, PhotoUsage usage) throws IOException {
    int width, height;
    switch (usage) {
      case CHAT_PROFILE:
        width = 100;
        height = 100;
        break;
      case PORTFOLIO_SCROLL:
        width = 198;
        height = 260;
        break;
      case FULL_SCREEN:
        width = 1080;
        height = 1920;
        break;
      default:
        throw new IllegalArgumentException("Unknown PhotoUsage: " + usage);
    }
    return resize(multipartFile, width, height);
  }

  public InputStream resize(MultipartFile multipartFile, int width, int height) throws IOException {
    try (InputStream originalInputStream = multipartFile.getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      Thumbnails.of(originalInputStream)
          .size(width, height)
          .outputFormat("jpg")
          .toOutputStream(outputStream);
      byte[] resizedImageBytes = outputStream.toByteArray();
      return new ByteArrayInputStream(resizedImageBytes);
    }
  }
}
