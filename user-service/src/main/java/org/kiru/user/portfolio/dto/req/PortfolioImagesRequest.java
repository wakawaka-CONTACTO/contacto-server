package org.kiru.user.portfolio.dto.req;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor
public class PortfolioImagesRequest {
  private AddMultipartFileDto[] portfolioImages; // 리스트를 감싸는 DTO
}
