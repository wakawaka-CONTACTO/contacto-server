package org.kiru.user.portfolio.adapter;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.core.user.user.entity.QUserJpaEntity;
import org.kiru.core.user.userPortfolioImg.entity.QUserPortfolioImg;
import org.kiru.user.portfolio.dto.res.QUserPortfolioResDto;
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

        List<UserPortfolioResDto> portfolios = queryFactory.select(new QUserPortfolioResDto(
                        userJpaEntity.id,
                        userJpaEntity.username,
                        userPortfolioImg.portfolioId,
                        userPortfolioImg.portfolioImageUrl))
                .from(userPortfolioImg)
                .join(userJpaEntity).on(userPortfolioImg.userId.eq(userJpaEntity.id))
                .where(userPortfolioImg.userId.in(userIds))
                .fetch();

        Map<Long, Integer> userIdOrderMap = userIds.stream()
                .collect(Collectors.toMap(id -> id, userIds::indexOf));

        return portfolios.stream()
                .sorted((p1, p2) -> Integer.compare(userIdOrderMap.get(p1.getUserId()),
                        userIdOrderMap.get(p2.getUserId())))
                .toList();
    }
}