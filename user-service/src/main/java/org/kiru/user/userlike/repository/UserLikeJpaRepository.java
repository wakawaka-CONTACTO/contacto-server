package org.kiru.user.userlike.repository;

import java.util.List;
import java.util.Optional;
import org.kiru.core.user.userlike.domain.LikeStatus;
import org.kiru.core.user.userlike.domain.UserLike;
import org.kiru.core.user.userlike.entity.UserLikeJpaEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLikeJpaRepository extends JpaRepository<UserLikeJpaEntity, Long>,  UserLikeRepository<UserLikeJpaEntity,Long>  {
    Optional<UserLikeJpaEntity> findByUserIdAndLikedUserId(Long userId, Long likedUserId);

    @Query("SELECT ul.userId FROM UserLikeJpaEntity ul WHERE ul.likedUserId = :likedUserId AND ul.isMatched = false GROUP BY ul.userId ORDER BY COUNT(ul.likedUserId) DESC")
    List<Long> findAllLikeMeUserIdAndNotMatchedByLikedUserId(@Param("likedUserId") Long likedUserId, Pageable pageable);

    @Query("SELECT ul.likedUserId FROM UserLikeJpaEntity ul WHERE ul.userId = :userId AND ul.isMatched = true")
    List<Long> findAllMatchedUserIdByUserId(@Param("userId") Long userId);

    @Query("SELECT ul FROM UserLikeJpaEntity ul WHERE ul.userId = :likedUserId AND ul.likedUserId = :userId AND ul.likeStatus = :status")
    UserLikeJpaEntity findOppositeLike(@Param("likedUserId") Long likedUserId, @Param("userId") Long userId, @Param("status") LikeStatus status);

    @Query("SELECT ul.userId FROM UserLikeJpaEntity ul WHERE ul.likeStatus = 'LIKE' GROUP BY ul.userId ORDER BY COUNT(ul.likedUserId) DESC")
    @Cacheable(value = "popularIds", key = "#pageable.pageNumber", unless = "#result==null")
    List<Long> findPopularUserId(Pageable pageable);

    UserLike save(UserLike userLike);
}