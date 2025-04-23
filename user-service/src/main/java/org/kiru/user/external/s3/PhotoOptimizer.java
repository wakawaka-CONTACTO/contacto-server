package org.kiru.user.external.s3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
public class PhotoOptimizer {

  public static byte[] getOptimizedImageBytes(MultipartFile image) throws IOException {
    try (InputStream optimizedStream = resize(image, 800, 800)){
      return optimizedStream.readAllBytes();
    } catch (S3Exception e) {
      return image.getBytes();
    } catch (IOException e){
      throw new IOException("이미지 업로드 중 오류가 발생했습니다.", e);  // 예외 발생 시 IOException 처리
    }
  }

  public static InputStream resize(MultipartFile multipartFile, int width, int height) throws IOException {
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
