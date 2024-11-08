package org.kiru.user.userlike.dto.req;

import org.kiru.core.userlike.domain.LikeStatus;

public record LikeRequest(
        Long likedUserId, LikeStatus status
) {
}