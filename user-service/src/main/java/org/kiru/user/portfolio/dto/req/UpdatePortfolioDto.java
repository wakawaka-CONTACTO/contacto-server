package org.kiru.user.portfolio.dto.req;

import org.springframework.web.multipart.MultipartFile;

public class UpdatePortfolioDto {
  private Integer key;
  private MultipartFile portfolio;
  private String resource;
}
