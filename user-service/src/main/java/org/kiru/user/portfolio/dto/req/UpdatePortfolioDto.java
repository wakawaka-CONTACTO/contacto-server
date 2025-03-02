package org.kiru.user.portfolio.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
public class UpdatePortfolioDto {
  private Integer key;
  private MultipartFile portfolio;
  private String resource;

  public boolean isNew(){
    if(resource == null) return true;
    return false;
  }
}
