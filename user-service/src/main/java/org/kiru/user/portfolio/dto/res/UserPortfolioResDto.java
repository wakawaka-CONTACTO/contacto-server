package org.kiru.user.portfolio.dto.res;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.kiru.core.userPortfolioImg.domain.UserPortfolio;

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
}
