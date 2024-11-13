package org.kiru.user.userlike.repository;

import java.util.List;
import java.util.Optional;
import org.kiru.core.user.userlike.entity.UserLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserLikeRepository extends JpaRepository<UserLike, Long> {
    Optional<UserLike> findByUserIdAndLikedUserId(Long userId, Long likedUserId);

    @Query("SELECT ul.userId FROM UserLike ul WHERE ul.likedUserId = :likedUserId")
    List<Long> findAllByLikedUserId(Long likedUserId);

    @Query("SELECT ul.userId FROM UserLike ul GROUP BY ul.userId ORDER BY COUNT(ul.likedUserId) DESC")
    List<Long> findAllUserIdOrderByLikedUserIdCountDesc();
}