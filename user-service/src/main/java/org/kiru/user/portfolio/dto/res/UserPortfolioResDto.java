package org.kiru.user.portfolio.dto.res;

import com.querydsl.core.annotations.QueryProjection;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class UserPortfolioResDto{
        private Long portfolioId;
        private Long userId;
        private String username;
        private List<String> portfolioImages;

        public static UserPortfolioResDto of(Long portfolioId, Long userId, String username, List<String> portfolioImages) {
                return UserPortfolioResDto.builder()
                        .portfolioImages(portfolioImages)
                        .userId(userId)
                        .portfolioId(portfolioId)
                        .username(username)
                        .build();
        }

        @QueryProjection
        public UserPortfolioResDto(Long userId, String username, Long portfolioId, String portfolioImageUrls) {
                this.userId = userId;
                this.username = username;
                this.portfolioId = portfolioId;
                this.portfolioImages = Arrays.asList(portfolioImageUrls.split(","));
        }

        public UserPortfolioResDto(Long userId, String username, Long portfolioId, List<String> portfolioImageUrls) {
                this.userId = userId;
                this.username = username;
                this.portfolioId = portfolioId;
                this.portfolioImages = portfolioImageUrls;
        }
}
