package org.kiru.user.userlike.dto.res;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LikeLimitResponse {
    int likeLimit;
    int likeCount;

    public static LikeLimitResponse of(int likeLimit, int likeCount) {
        return LikeLimitResponse.builder()
                .likeLimit(likeLimit)
                .likeCount(likeCount)
                .build();
    }
}
