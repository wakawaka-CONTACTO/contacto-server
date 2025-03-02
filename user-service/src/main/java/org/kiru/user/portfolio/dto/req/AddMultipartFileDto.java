package org.kiru.user.portfolio.dto.req;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
public class AddMultipartFileDto {
  private Integer key;
  private MultipartFile portfolio;

//  public MultipartFile getPortfolio(){
//    return portfolio.get();
//  }

//  public static boolean isEmpty(){
//    if (portfolio.isEmpty()) return true;
//  }
}
