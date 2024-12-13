package org.kiru.user.userlike.repository;

import java.util.List;
import java.util.Optional;
import org.kiru.core.user.userlike.entity.UserLike;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserLikeRepository extends JpaRepository<UserLike, Long> {
    Optional<UserLike> findByUserIdAndLikedUserId(Long userId, Long likedUserId);

    @Query("SELECT ul.userId FROM UserLike ul WHERE ul.likedUserId = :likedUserId AND ul.isMatched = false GROUP BY ul.userId ORDER BY COUNT(ul.likedUserId) DESC")
    Slice<Long> findAllLikeMeUserIdAndNotMatchedByLikedUserId(@Param("likedUserId") Long likedUserId, Pageable pageable);

    @Query("SELECT ul.userId FROM UserLike ul WHERE ul.isMatched = false GROUP BY ul.userId ORDER BY COUNT(ul.likedUserId) DESC")
    Slice<Long> findAllUserIdOrderByLikedUserIdCountDesc(Pageable pageable);

    @Query("SELECT ul.likedUserId FROM UserLike ul WHERE ul.userId = :userId AND ul.isMatched = true")
    List<Long> findAllMatchedUserIdByUserId(@Param("userId") Long userId);
}