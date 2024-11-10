package org.kiru.user.userlike.repository;

import com.querydsl.core.Tuple;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

import org.kiru.core.user.user.entity.QUserJpaEntity;
import org.kiru.core.user.userPortfolioImg.entity.QUserPortfolioImg;
import org.kiru.core.user.userPortfolioImg.entity.UserPortfolioImg;
import org.kiru.core.user.userlike.domain.LikeStatus;
import org.kiru.core.user.userlike.entity.QUserLike;
import org.kiru.core.user.userlike.entity.UserLike;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;
import org.kiru.user.portfolio.repository.UserPortfolioImgRepository;
import org.kiru.user.userlike.service.out.GetMatchedUserPortfolioQuery;
import org.kiru.user.userlike.service.out.SendLikeOrDislikeUseCase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class UserLikeAdapter implements SendLikeOrDislikeUseCase, GetMatchedUserPortfolioQuery {

    private final JPAQueryFactory queryFactory;
    private final UserLikeRepository userLikeRepository;
    private final UserPortfolioImgRepository userPortfolioImgRepository;

    @Transactional
    public UserLike sendOrDislike(Long userId, Long likedUserId, LikeStatus status) {
        QUserLike qUserLike = QUserLike.userLike;

        // 1. 상대방이 나를 좋아요(`LIKE`)한 기록이 있는지 먼저 조회
        UserLike oppositeLike = queryFactory.selectFrom(qUserLike)
                .where(qUserLike.userId.eq(likedUserId)
                        .and(qUserLike.likedUserId.eq(userId))
                        .and(qUserLike.likeStatus.eq(LikeStatus.LIKE)))
                .fetchOne();

        // 2. 현재 `userId -> likedUserId` 관계 조회 또는 생성
        UserLike userLike = userLikeRepository.findByUserIdAndLikedUserId(userId, likedUserId)
                .orElseGet(() -> UserLike.of(userId, likedUserId, status, false));

        // 3. 상대방이 나를 이미 `LIKE`했다면 상호 매칭으로 설정
        if (oppositeLike != null && status == LikeStatus.LIKE) {
            userLike.isMatched(true);
            oppositeLike.isMatched(true);
            userLikeRepository.save(oppositeLike); // 상대방 기록도 업데이트
        }
        // 4. 최종적으로 현재 관계 저장 후 반환
        return userLikeRepository.save(userLike);
    }

    public List<UserPortfolioResDto> findByUserIds(List<Long> userIds) {
        QUserJpaEntity qUser = QUserJpaEntity.userJpaEntity;
        QUserPortfolioImg qUserPortfolioImg = QUserPortfolioImg.userPortfolioImg;
        // 1. User와 UserPortfolioImg를 fetch join으로 한 번에 조회
        List<Tuple> results = queryFactory
                .select(qUser.id, qUser.username, qUserPortfolioImg)
                .from(qUser)
                .leftJoin(qUserPortfolioImg).on(qUserPortfolioImg.userId.eq(qUser.id))
                .where(qUser.id.in(userIds))
                .fetch();
        // 2. UserId 기준으로 포트폴리오 이미지를 그룹핑
        Map<Long, List<UserPortfolioImg>> portfolioImgMap = results.stream()
                .filter(tuple -> tuple.get(qUserPortfolioImg) != null)
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(qUser.id),
                        Collectors.mapping(tuple -> tuple.get(qUserPortfolioImg), Collectors.toList())
                ));
        // 3. 결과를 담을 리스트 생성
        List<UserPortfolioResDto> userPortfolios = new ArrayList<>();
        Map<Long, String> usernameMap = results.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(qUser.id),
                        tuple -> tuple.get(qUser.username),
                        (username1, username2) -> username1 // 중복 키 처리
                ));
        // 4. UserId별로 UserPortfolioResDto 객체 생성
        for (Long userIdByPortfolio : userIds) {
            String username = usernameMap.get(userIdByPortfolio);
            List<UserPortfolioImg> portfolioImgs = portfolioImgMap.getOrDefault(userIdByPortfolio, new ArrayList<>());
            UserPortfolioResDto userPortfolio = UserPortfolioResDto.builder()
                    .portfolioId(portfolioImgs.isEmpty() ? null : portfolioImgs.get(0).getPortfolioId())
                    .userId(userIdByPortfolio)
                    .username(username)
                    .portfolioImages(portfolioImgs.stream()
                            .sorted(Comparator.comparingInt(UserPortfolioImg::getSequence))
                            .map(UserPortfolioImg::getPortfolioImageUrl)
                            .toList())
                    .build();
            userPortfolios.add(userPortfolio);
        }
        return userPortfolios;
    }
}
