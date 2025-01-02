package org.kiru.user.userlike.repository;

import java.util.List;
import java.util.Optional;
import org.kiru.core.user.userlike.domain.UserLike;
import org.springframework.data.domain.Pageable;

public interface UserLikeRepository<T extends UserLike,A> {
    Optional<T> findByUserIdAndLikedUserId(Long userId, Long likedUserId);
    List<A> findAllLikeMeUserIdAndNotMatchedByLikedUserId(Long likedUserId, Pageable pageable);
    List<A> findAllMatchedUserIdByUserId(Long userId);
}


