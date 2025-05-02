package org.kiru.user.portfolio.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import reactor.core.publisher.Mono;

import java.util.List;

@Data
@AllArgsConstructor
public class UserScrapeImagesResDto {
    private List<String> imageUrls;
    
    public static UserScrapeImagesResDto from(Mono<List<String>> imageUrls) {
        return new UserScrapeImagesResDto(imageUrls.block());
    }
}
