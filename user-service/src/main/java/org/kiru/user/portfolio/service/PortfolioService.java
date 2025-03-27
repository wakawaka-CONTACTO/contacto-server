package org.kiru.user.portfolio.service;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;
import org.kiru.user.portfolio.service.out.GetRecommendUserIdsQuery;
import org.kiru.user.portfolio.service.out.GetUserPortfoliosQuery;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PortfolioService {
    private final GetUserPortfoliosQuery getUserPortfoliosQuery;
    private final GetRecommendUserIdsQuery getRecommendUserIdsQuery;

    public PortfolioService(GetUserPortfoliosQuery getUserPortfoliosQuery,
                            GetRecommendUserIdsQuery getRecommendUserIdsQuery) {
        this.getUserPortfoliosQuery = getUserPortfoliosQuery;
        this.getRecommendUserIdsQuery = getRecommendUserIdsQuery;
    }

    public List<UserPortfolioResDto> getUserPortfolios(Long userId, Pageable pageable) {
        List<Long> recommendedUserIds = getRecommendUserIdsQuery.findRecommendedUserIds(userId, pageable);
        log.info("최종 추천한 유저 Id: {}, 개수: {}", recommendedUserIds, recommendedUserIds.size());
        return getUserPortfoliosQuery.findAllPortfoliosByUserIds(recommendedUserIds);
    }
}
