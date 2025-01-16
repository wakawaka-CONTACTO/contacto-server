package org.kiru.user.userlike.repository;

import java.util.List;
import java.util.Optional;
import org.kiru.core.user.userlike.domain.UserLike;
import org.kiru.core.user.userlike.entity.UserLikeMongoEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface UserLikeMongoRepository extends MongoRepository<UserLikeMongoEntity, String>, UserLikeRepository<UserLikeMongoEntity,UserIdProjection> {
    Optional<UserLikeMongoEntity> findByUserIdAndLikedUserId(Long userId, Long likedUserId);

    @Query(value="{'liked_user_id': ?0, 'is_matched': false,'like_status': 'LIKE'}",
           fields="{'user_id': 1}")
    List<UserIdProjection> findAllLikeMeUserIdAndNotMatchedByLikedUserId(Long likedUserId, Pageable pageable);

    @Query(value="{'user_id': ?0, 'is_matched': true}",
           fields="{'liked_user_id': 1}")
    List<UserIdProjection> findAllMatchedUserIdByUserId(Long userId);

    UserLikeMongoEntity save(UserLike userLike);
}