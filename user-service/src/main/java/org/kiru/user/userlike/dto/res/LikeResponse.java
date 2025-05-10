package org.kiru.user.userlike.dto.res;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;

@Builder
@Getter
public class LikeResponse {
    boolean isMatched;
    List<UserPortfolioResDto> userPortfolios;
    Long chatRoomId;
    int likeCount;

    public static LikeResponse of(boolean isMatched, List<UserPortfolioResDto> userPortfolios, Long chatRoomId, int likeCount) {
        return LikeResponse.builder()
                .isMatched(isMatched)
                .userPortfolios(userPortfolios)
                .chatRoomId(chatRoomId)
                .likeCount(likeCount)
                .build();
    }
}
