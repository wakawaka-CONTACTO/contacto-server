package org.kiru.user.userlike.service.out;

import org.kiru.core.user.userlike.domain.LikeStatus;
import org.kiru.core.user.userlike.entity.UserLike;

public interface SendLikeOrDislikeUseCase {
    UserLike sendOrDislike(Long userId, Long likedUserID, LikeStatus status);
}
