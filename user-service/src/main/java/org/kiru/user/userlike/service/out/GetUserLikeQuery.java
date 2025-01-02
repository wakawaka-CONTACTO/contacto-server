package org.kiru.user.userlike.service.out;

import java.util.List;
import org.kiru.user.userlike.dto.Longs;
import org.springframework.data.domain.Pageable;

public interface GetUserLikeQuery {
    Longs getPopularUserId(Pageable pageable);

    List<Long> findAllMatchedUserIdByUserId(Long userId);

    List<Long> findAllLikeMeUserIdAndNotMatchedByLikedUserId(Long likedUserId, Pageable pageable);
}
