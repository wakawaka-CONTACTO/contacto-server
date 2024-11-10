package org.kiru.user.userlike.dto.req;

import org.kiru.core.user.userlike.domain.LikeStatus;

public record LikeRequest(
        Long likedUserId, LikeStatus status
) {
}