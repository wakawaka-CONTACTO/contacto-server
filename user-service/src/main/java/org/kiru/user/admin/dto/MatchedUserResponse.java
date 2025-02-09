package org.kiru.user.admin.dto;

import java.time.LocalDateTime;
import org.kiru.core.user.userlike.entity.UserLikeJpaEntity;

public record MatchedUserResponse(
        Long userId,
        LocalDateTime matchedAt
) {
    public static MatchedUserResponse of(UserLikeJpaEntity userLikeJpaEntity) {
        return new MatchedUserResponse(
                userLikeJpaEntity.getLikedUserId(),
                userLikeJpaEntity.getUpdateAt()
        );
    }
}
