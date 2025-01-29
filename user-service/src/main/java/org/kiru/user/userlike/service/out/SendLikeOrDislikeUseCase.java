package org.kiru.user.userlike.service.out;

import org.kiru.core.user.userlike.domain.LikeStatus;
import org.kiru.core.user.userlike.domain.UserLike;

public interface SendLikeOrDislikeUseCase {
    UserLike sendLikeOrDislike(Long userId, Long likedUserID, LikeStatus status);
}
