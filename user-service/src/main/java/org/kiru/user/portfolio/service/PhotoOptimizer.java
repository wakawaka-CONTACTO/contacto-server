package org.kiru.user.portfolio.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

public class PhotoOptimizer {
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
