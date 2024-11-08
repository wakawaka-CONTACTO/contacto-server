package org.kiru.core.userPortfolioImg.domain;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@Builder
public class UserPortfolio {
    private Long portfolioId;
    private Long userId;
    private List<String> portfolioImages;

    public UserPortfolio(Long portfolioId, Long userId, List<String> portfolioImages) {
        this.portfolioId = portfolioId;
        this.userId = userId;
        this.portfolioImages = portfolioImages;
    }
    public void addPortfolioImage(String portfolioImageUrl){
        this.portfolioImages.add(portfolioImageUrl);
    }

    public void addPortfolioImages(List<String> portfolioImageUrls){
        this.portfolioImages.addAll(portfolioImageUrls);
    }
}
