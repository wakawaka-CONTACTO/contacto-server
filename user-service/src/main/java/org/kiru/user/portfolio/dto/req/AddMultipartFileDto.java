package org.kiru.user.portfolio.dto.req;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AddMultipartFileDto {
  Integer key;
  MultipartFile portfolioImage;
//  Integer size;

//  public int size(){
//    size = (key != null) ? key.size() : 0; // key가 null이면 size = 0
//    return size;
//  }
}