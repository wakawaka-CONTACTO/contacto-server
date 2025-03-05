package org.kiru.user.portfolio.dto.req;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor
public class PortfolioImagesRequest {

  private List<MultipartFile> portfolio;
  private List<Integer> keys;

  public boolean isValid(){
    return portfolio.size() == keys.size() ? true : false;
  }

  public int size(){
    return portfolio.size();
  }
}
