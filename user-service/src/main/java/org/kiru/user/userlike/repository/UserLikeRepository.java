package org.kiru.user.userlike.repository;

import java.util.Optional;
import org.kiru.core.userlike.entity.UserLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLikeRepository extends JpaRepository<UserLike, Long> {
    Optional<UserLike> findByUserIdAndLikedUserId(Long userId, Long likedUserId);
}
