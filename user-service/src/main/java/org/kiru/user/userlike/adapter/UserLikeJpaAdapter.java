package org.kiru.user.userlike.adapter;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.kiru.core.user.user.entity.QUserJpaEntity;
import org.kiru.core.user.userPortfolioItem.entity.QUserPortfolioImg;
import org.kiru.core.user.userlike.domain.LikeStatus;
import org.kiru.core.user.userlike.domain.UserLike;
import org.kiru.core.user.userlike.entity.QUserLikeJpaEntity;
import org.kiru.core.user.userlike.entity.UserLikeJpaEntity;
import org.kiru.user.admin.dto.AdminLikeUserResponse.AdminLikeUserDto;
import org.kiru.user.admin.dto.MatchedUserResponse;
import org.kiru.user.admin.service.out.UserLikeAdminUseCase;
import org.kiru.user.userlike.dto.Longs;
import org.kiru.user.userlike.repository.UserLikeJpaRepository;
import org.kiru.user.userlike.service.out.GetUserLikeQuery;
import org.kiru.user.userlike.service.out.SendLikeOrDislikeUseCase;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Repository
@Transactional(readOnly = true)
public class UserLikeJpaAdapter implements SendLikeOrDislikeUseCase, GetUserLikeQuery, UserLikeAdminUseCase {

    private final UserLikeJpaRepository userLikeRepository;
    private final JPAQueryFactory queryFactory;

    @Transactional
    @Override
    public UserLike sendLikeOrDislike(Long userId, Long likedUserId, LikeStatus status) {
        // 현재 `userId -> likedUserId` 관계 조회 또는 생성
        UserLike userLike = userLikeRepository.findByUserIdAndLikedUserId(userId, likedUserId)
                .orElseGet(() -> UserLikeJpaEntity.of(userId, likedUserId, status, false));
        if (userLike.getLikeStatus() == status) {
            userLike.touch();
        } else {
            userLike.likeStatus(status);
        }
        // 1. 상대방이 나를 좋아요(`LIKE`)한 기록이 있는지 먼저 조회
        if (userLike.isMatched() && status == LikeStatus.LIKE) {
            return userLike;
        }
        UserLike oppositeLike = userLikeRepository.findOppositeLike(likedUserId, userId, LikeStatus.LIKE);
        // 3. 상대방이 나를 이미 `LIKE`했다면 상호 매칭으로 처리
        if (oppositeLike != null && status == LikeStatus.LIKE) {
            userLike.setMatched(true);
            oppositeLike.setMatched(true);
            userLikeRepository.save(oppositeLike); // 상대방 기록도 업데이트
        }
        // 4. 최종적으로 현재 관계 저장 후 반환
        userLikeRepository.save(userLike);
        return userLike;
    }

    @Override
    @Cacheable(value = "popularIds", key = "#pageable.pageNumber", unless = "#result==null")
    public Longs getPopularUserId(Pageable pageable) {
        return new Longs(userLikeRepository.findPopularUserId(pageable));
    }

    @Override
    public List<Long> findAllMatchedUserIdByUserId(Long userId) {
        return userLikeRepository.findAllMatchedUserIdByUserId(userId);
    }

    @Override
    public List<Long> findAllLikedUserIdByUserId(Long userId) {
        List<Long> likedUserIds = userLikeRepository.findAllLikedUserIdByUserId(userId);
        return likedUserIds;
    }

    @Override
    public List<Long> findAllLikedUserIdByUserIdAndLikeStatus(Long userId, LikeStatus likeStatus) {
        return userLikeRepository.findAllLikedUserIdByUserIdAndLikeStatus(userId, likeStatus);
    }

    @Override
    @Cacheable(value = "like", key = "#userId+'-'+#pageable.pageNumber", unless = "#result == null")
    public List<Long> findAllLikeMeUserIdAndNotMatchedByLikedUserId(Long userId, Pageable pageable) {
        return userLikeRepository.findAllLikeMeUserIdAndNotMatchedByLikedUserId(userId, pageable);
    }

    @Override
    public List<AdminLikeUserDto> findUserLikesInternal(Pageable pageable, Long userId, String name, boolean isLiked) {
        QUserJpaEntity qUserJpaEntity = QUserJpaEntity.userJpaEntity;
        QUserLikeJpaEntity qUserLike = QUserLikeJpaEntity.userLikeJpaEntity;
        QUserPortfolioImg qUserPortfolioImg = QUserPortfolioImg.userPortfolioImg;
        List<AdminLikeUserDto> likes = queryFactory.select(Projections.constructor(AdminLikeUserDto.class,
                        qUserJpaEntity.id,
                        qUserJpaEntity.username,
                        qUserPortfolioImg.portfolioImageUrl,
                        qUserLike.createdAt))
                .from(qUserLike)
                .innerJoin(qUserJpaEntity)
                .on(isLiked ? qUserLike.userId.eq(qUserJpaEntity.id) : qUserLike.likedUserId.eq(qUserJpaEntity.id))
                .leftJoin(qUserPortfolioImg)
                .on(qUserJpaEntity.id.eq(qUserPortfolioImg.userId).and(qUserPortfolioImg.sequence.eq(1)))
                .where((isLiked ? qUserLike.likedUserId.eq(userId) : qUserLike.userId.eq(userId))
                        .and(name != null ? qUserJpaEntity.username.containsIgnoreCase(name) : null))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        return likes;
    }

    @Override
    public Map<Long, MatchedUserResponse> findAllMatchedUserIdWithMatchedTime(Long userId, Pageable pageable) {
        return userLikeRepository.findAllMatchedUsersWithMatchedTime(userId, pageable).stream()
                .map(MatchedUserResponse::of)
                .collect(Collectors.toMap(MatchedUserResponse::userId, matchedUser -> matchedUser));
    }
}
