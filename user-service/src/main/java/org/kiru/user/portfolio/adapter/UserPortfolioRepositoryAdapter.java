package org.kiru.user.portfolio.adapter;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.core.user.user.entity.QUserJpaEntity;
import org.kiru.core.user.userPortfolioImg.entity.QUserPortfolioImg;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;
import org.kiru.user.portfolio.service.out.GetUserPortfoliosQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Repository
@Slf4j
public class UserPortfolioRepositoryAdapter implements GetUserPortfoliosQuery {
    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public List<UserPortfolioResDto> findAllPortfoliosByUserIds(List<Long> userIds) {
        QUserPortfolioImg userPortfolioImg = QUserPortfolioImg.userPortfolioImg;
        QUserJpaEntity userJpaEntity = QUserJpaEntity.userJpaEntity;
        // 쿼리 실행: 사용자, 포트폴리오, 이미지 URL 데이터를 가져옴
        List<Tuple> results = queryFactory
                .select(
                        userJpaEntity.id,
                        userJpaEntity.username,
                        userPortfolioImg.portfolioId,
                        userPortfolioImg.portfolioImageUrl
                )
                .from(userJpaEntity)
                .leftJoin(userPortfolioImg)
                .on(userPortfolioImg.userId.eq(userJpaEntity.id))
                .where(userJpaEntity.id.in(userIds))
                .orderBy(
                        userJpaEntity.id.asc(),
                        userPortfolioImg.portfolioId.asc(),
                        userPortfolioImg.sequence.asc()
                )
                .fetch();
        Map<Long, Map<Long, UserPortfolioResDto>> groupedData = new HashMap<>();
        results.stream()
                .filter(tuple -> tuple.get(userJpaEntity.id) != null && tuple.get(userPortfolioImg.portfolioId) != null) // 하나의 필터로 합침
                .forEach(tuple -> {
                    Long userId = tuple.get(userJpaEntity.id);
                    Long portfolioId = tuple.get(userPortfolioImg.portfolioId);
                    String username = tuple.get(userJpaEntity.username);
                    String imageUrl = tuple.get(userPortfolioImg.portfolioImageUrl);
                    groupedData
                            .computeIfAbsent(userId, k -> new HashMap<>()) // 사용자 ID에 대한 Map 생성
                            .compute(portfolioId, (key, existingDto) -> {
                                if (existingDto == null) {
                                    // 새 UserPortfolioResDto 생성
                                    List<String> imageUrls = new ArrayList<>();
                                    if (imageUrl != null) imageUrls.add(imageUrl);
                                    return new UserPortfolioResDto(userId, username, portfolioId, imageUrls);
                                } else {
                                    if (imageUrl != null && existingDto.getPortfolioImages().size() < 10) {
                                        existingDto.getPortfolioImages().add(imageUrl);
                                    }
                                    return existingDto;
                                }
                            });
                });
        Map<Long, Integer> userIdOrderMap = userIds.stream()
                .collect(Collectors.toMap(id -> id, userIds::indexOf));
        return groupedData.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> userIdOrderMap.get(entry.getKey()))) // 사용자 ID 순서 정렬
                .flatMap(entry -> entry.getValue().values().stream()) // 각 포트폴리오의 데이터 풀어내기
                .toList();
    }
}