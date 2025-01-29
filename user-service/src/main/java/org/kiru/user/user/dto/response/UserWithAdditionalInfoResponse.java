package org.kiru.user.user.dto.response;

import java.util.List;
import org.kiru.core.user.talent.entity.UserTalent;
import org.kiru.core.user.user.domain.User;
import org.kiru.core.user.userPortfolioItem.domain.UserPortfolio;
import org.kiru.core.user.userPortfolioItem.domain.UserPortfolioItem;


public record UserWithAdditionalInfoResponse(
        Long id,
        String username,
        String email,
        String description,
        String instagramId,
        String webUrl,
        UserPortfolioResponse userPortfolio,
        List<Integer> userPurposes,
        List<UserTalent> userTalents
) {
    public static UserWithAdditionalInfoResponse of(User user) {
        return new UserWithAdditionalInfoResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDescription(),
                user.getInstagramId(),
                user.getWebUrl(),
                UserPortfolioResponse.of(user.getUserPortfolio()),
                user.getUserPurposes(),
                user.getUserTalents().stream().map(
                        i -> UserTalent.builder().id(user.getId()).talentType(i).build()).toList()
        );
    }

    public record UserPortfolioResponse(
            Long portfolioId,
            Long userId,
            List<String> portfolioImages
    ) {
        public static UserPortfolioResponse of(UserPortfolio userPortfolio) {
            if (userPortfolio == null) {
                return null;
            }
            return new UserPortfolioResponse(
                    userPortfolio.getPortfolioId(),
                    userPortfolio.getUserId(),
                    userPortfolio.getPortfolioItems().stream()
                            .map(UserPortfolioItem::getItemUrl)
                            .toList()
            );
        }
    }
}

