package org.kiru.user.portfolio.dto.res;

import java.util.Arrays;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
public class UserPortfolioResDto {
    private Long portfolioId;
    private Long userId;
    private String username;
    private List<String> portfolioImageUrl;

    public static UserPortfolioResDto of(Long portfolioId, Long userId, String username, List<String> portfolioImageUrl) {
        return UserPortfolioResDto.builder()
            .portfolioImageUrl(portfolioImageUrl)
            .userId(userId)
            .portfolioId(portfolioId)
            .username(username)
            .build();
    }

    public static UserPortfolioResDto of(Long portfolioId, Long userId, String username, String portfolioImageUrl) {
        return UserPortfolioResDto.builder()
                .portfolioImageUrl(Arrays.asList(portfolioImageUrl.split(",")))
                .userId(userId)
                .portfolioId(portfolioId)
                .username(username)
                .build();
    }

    public UserPortfolioResDto( Long portfolioId, Long userId, String username, String portfolioImageUrl){
            this.userId = userId;
            this.username = username;
            this.portfolioId = portfolioId;
            this.portfolioImageUrl = Arrays.asList(portfolioImageUrl.split(","));
    }

    public UserPortfolioResDto(Long portfolioId, Long userId, String username, List<String> portfolioImageUrl) {
            this.userId = userId;
            this.username = username;
            this.portfolioId = portfolioId;
            this.portfolioImageUrl = portfolioImageUrl;
    }
}