package org.kiru.user.portfolio.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

public class PhotoOptimizer {
  private final static int width = 800;
  private final static int height = 800;
  public InputStream resize(MultipartFile multipartFile) throws IOException {
    try (InputStream originalInputStream = multipartFile.getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      Thumbnails.of(originalInputStream)
          .size(width, height)
          .outputFormat("webp")
          .toOutputStream(outputStream);
      byte[] resizedImageBytes = outputStream.toByteArray();
      return new ByteArrayInputStream(resizedImageBytes);
    }
  }
}
