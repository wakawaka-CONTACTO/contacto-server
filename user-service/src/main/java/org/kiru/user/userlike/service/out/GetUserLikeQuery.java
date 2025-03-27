package org.kiru.user.userlike.service.out;

import java.util.List;

import org.kiru.core.user.userlike.domain.LikeStatus;
import org.kiru.user.userlike.dto.Longs;
import org.springframework.data.domain.Pageable;

public interface GetUserLikeQuery {
    Longs getPopularUserId(Pageable pageable);

    List<Long> findAllMatchedUserIdByUserId(Long userId);

    List<Long> findAllLikedUserIdByUserId(Long userId);

    List<Long> findAllLikedUserIdByUserIdAndLikeStatus(Long userId, LikeStatus likeStatus);

    List<Long> findAllLikeMeUserIdAndNotMatchedByLikedUserId(Long userId, Pageable pageable);
}
