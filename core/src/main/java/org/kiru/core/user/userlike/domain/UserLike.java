package org.kiru.core.user.userlike.domain;

import java.time.LocalDateTime;

public interface UserLike {
    Long getUserId();
    Long getLikedUserId();
    LikeStatus getLikeStatus();
    boolean isMatched();
    LocalDateTime getCreatedAt();
    void likeStatus(LikeStatus likeStatus);
    boolean isMatched(boolean b);
}
