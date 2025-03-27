package org.kiru.user.userlike.repository;

import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;
import org.kiru.core.user.userlike.domain.LikeStatus;
import org.kiru.core.user.userlike.domain.UserLike;
import org.kiru.core.user.userlike.entity.UserLikeJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLikeJpaRepository extends JpaRepository<UserLikeJpaEntity, Long>,  UserLikeRepository<UserLikeJpaEntity,Long>  {
    Optional<UserLikeJpaEntity> findByUserIdAndLikedUserId(Long userId, Long likedUserId);

    @Query("SELECT ul.userId FROM UserLikeJpaEntity ul WHERE ul.likedUserId = :likedUserId AND ul.isMatched = false GROUP BY ul.userId ORDER BY COUNT(ul.likedUserId) DESC")
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "150"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")

    })
    List<Long> findAllLikeMeUserIdAndNotMatchedByLikedUserId(@Param("likedUserId") Long likedUserId, Pageable pageable);

    @Query("SELECT ul.likedUserId FROM UserLikeJpaEntity ul WHERE ul.userId = :userId AND ul.isMatched = true")
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "150"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")

    })
    List<Long> findAllMatchedUserIdByUserId(@Param("userId") Long userId);

    @Query("SELECT ul.likedUserId FROM UserLikeJpaEntity ul WHERE ul.userId = :userId")
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "150"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")
    })
    List<Long> findAllLikedUserIdByUserId(@Param("userId") Long userId);

    @Query("SELECT ul.likedUserId From UserLikeJpaEntity ul WHERE ul.userId = :userId AND ul.likeStatus = :status ORDER BY ul.updateAt ASC")
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "150"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")
    })
    List<Long> findAllLikedUserIdByUserIdAndLikeStatus(@Param("userId") Long userId, @Param("status") LikeStatus likeStatus);

    @Query("SELECT ul FROM UserLikeJpaEntity ul WHERE ul.userId = :likedUserId AND ul.likedUserId = :userId AND ul.likeStatus = :status")
    UserLikeJpaEntity findOppositeLike(@Param("likedUserId") Long likedUserId, @Param("userId") Long userId, @Param("status") LikeStatus status);

    @Query("SELECT ul.userId FROM UserLikeJpaEntity ul WHERE ul.likeStatus = 'LIKE' GROUP BY ul.userId ORDER BY COUNT(ul.likedUserId) DESC")
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "150"),
            @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")

    })
    List<Long> findPopularUserId(Pageable pageable);

    UserLikeJpaEntity save(UserLike userLike);

    @Query("SELECT ul FROM UserLikeJpaEntity ul WHERE ul.userId = :userId AND ul.isMatched = true")
     List<UserLikeJpaEntity> findAllMatchedUsersWithMatchedTime(Long userId, Pageable pageable);
}