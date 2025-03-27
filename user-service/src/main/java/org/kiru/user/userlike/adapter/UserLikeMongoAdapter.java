package org.kiru.user.userlike.adapter;

import static org.kiru.core.user.userlike.domain.LikeStatus.LIKE;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.kiru.core.user.userlike.domain.LikeStatus;
import org.kiru.core.user.userlike.domain.UserLike;
import org.kiru.core.user.userlike.entity.UserLikeMongoEntity;
import org.kiru.user.admin.dto.AdminLikeUserResponse.AdminLikeUserDto;
import org.kiru.user.admin.dto.MatchedUserResponse;
import org.kiru.user.admin.service.out.UserLikeAdminUseCase;
import org.kiru.user.user.repository.UserRepository;
import org.kiru.user.userlike.dto.Longs;
import org.kiru.user.userlike.repository.UserIdProjection;
import org.kiru.user.userlike.repository.UserLikeMongoRepository;
import org.kiru.user.userlike.service.out.GetUserLikeQuery;
import org.kiru.user.userlike.service.out.SendLikeOrDislikeUseCase;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Repository
@Transactional(readOnly = true)
public class UserLikeMongoAdapter implements SendLikeOrDislikeUseCase, GetUserLikeQuery, UserLikeAdminUseCase {

    private final MongoTemplate mongoTemplate;
    private final UserLikeMongoRepository userLikeRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserLike sendLikeOrDislike(Long userId, Long likedUserId, LikeStatus status) {
        // 1. Check if opposite like exists
        Query oppositeLikeQuery = new Query(
                Criteria.where("userId").is(likedUserId).and("likedUserId").is(userId).and("likeStatus")
                        .is(LIKE));
        UserLike oppositeLike = mongoTemplate.findOne(oppositeLikeQuery, UserLikeMongoEntity.class);
        // 2. Find or create current like record
        UserLike userLike = userLikeRepository.findByUserIdAndLikedUserId(userId, likedUserId)
                .orElseGet(() -> UserLikeMongoEntity.of(userId, likedUserId, status, false));
        userLike.likeStatus(status);
        // 3. Check for mutual match
        if (oppositeLike != null && status == LIKE) {
            userLike.setMatched(true);
            // Update opposite like
            Query updateQuery = new Query(Criteria.where("userId").is(likedUserId).and("likedUserId").is(userId));
            Update update = new Update().set("isMatched", true);
            mongoTemplate.updateFirst(updateQuery, update, UserLikeMongoEntity.class);
        }
        return userLikeRepository.save(userLike);
    }


    @Override
    @Cacheable(value = "popular", key = "#pageable.pageNumber + '_' + #pageable.pageSize", unless = "#result == null && result.isEmpty()")
    public Longs getPopularUserId(Pageable pageable) {
        int limit = pageable.getPageSize() + 1;
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("like_status").is(LIKE)),
                Aggregation.group("user_id").count().as("likedUserIdCount"),
                Aggregation.sort(Direction.DESC, "likedUserIdCount"),
                Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                Aggregation.limit(limit),
                Aggregation.project("_id").and("_id").as("userId")
        );
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "user_likes", Document.class);
        return new Longs(results.getMappedResults().stream()
                .map(doc -> {
                    return switch (doc.get("_id").getClass().getSimpleName()) {
                        case "Integer" -> (long) doc.getInteger("userId");
                        case "Long" -> doc.getLong("userId");
                        default -> throw new IllegalArgumentException(
                                "Unexpected type: " + doc.get("_id").getClass().getSimpleName());
                    };
                }).toList());
    }

    @Override
    public List<Long> findAllMatchedUserIdByUserId(Long userId) {
        return userLikeRepository.findAllMatchedUserIdByUserId(userId).stream().map(UserIdProjection::getUserId).toList();
    }

    @Override
    public List<Long> findAllLikedUserIdByUserId(Long userId) {
        return userLikeRepository.findAllLikedUserIdByUserId(userId).stream().map(UserIdProjection::getUserId).toList();
    }

    @Override
    public List<Long> findAllLikedUserIdByUserIdAndLikeStatus(Long userId, LikeStatus likeStatus) {
        // 구현 전
        throw new UnsupportedOperationException("아직 구현되지 않음");
    }

    @Override
    public List<Long> findAllLikeMeUserIdAndNotMatchedByLikedUserId(Long likedUserId, Pageable pageable) {
        return userLikeRepository.findAllLikeMeUserIdAndNotMatchedByLikedUserId(likedUserId, pageable)
                .stream().map(UserIdProjection::getUserId).toList();
    }

    @Override
    public List<AdminLikeUserDto> findUserLikesInternal(Pageable pageable, Long userId, String name, boolean isLiked) {
        // 좋아요 쿼리 생성
        Criteria criteria = new Criteria();
        if (isLiked) {
            // 내가 받은 좋아요 찾기
            criteria.and("like_user_id").is(userId);
        } else {
            // 내가 한 좋아요 찾기
            criteria.and("user_id").is(userId);
        }
        // 이름 필터링 추가 (선택적)
        if (name != null && !name.isEmpty()) {
            criteria.and("user_name").regex(name, "i");
        }
        Query query = new Query(criteria);
        query.with(pageable);
        // 좋아요 목록 조회
        List<UserLike> likes = mongoTemplate.find(query, UserLike.class);
        // 좋아요 총 개수 조회
        long total = mongoTemplate.count(query, UserLikeMongoEntity.class);
        // DTO 변환
        // 사용자 정보 조회 로직 추가 필요
        return likes.stream()
                .map(like -> {
                    // 사용자 정보 조회 로직 추가 필요
                    UserJpaEntity user = userRepository.findById(
                            isLiked ? like.getUserId() : like.getLikedUserId()
                    ).orElse(null);
                    return new AdminLikeUserDto(
                            user.getId(),
                            user.getUsername(),
                            null,
                            like.getCreatedAt()
                    );
                })
                .toList();
    }

    @Override
    public Map<Long, MatchedUserResponse> findAllMatchedUserIdWithMatchedTime(Long userId, Pageable pageable) {
        return Map.of();
    }
}
