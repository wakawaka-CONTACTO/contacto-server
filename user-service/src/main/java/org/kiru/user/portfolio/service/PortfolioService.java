package org.kiru.user.portfolio.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;
import org.kiru.user.portfolio.service.out.GetRecommendUserIdsQuery;
import org.kiru.user.portfolio.service.out.GetUserPortfoliosQuery;
import org.kiru.user.userlike.service.out.GetUserLikeQuery;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PortfolioService {
    private final GetUserPortfoliosQuery getUserPortfoliosQuery;
    private final GetUserLikeQuery getUserLikeQuery;
    private final Executor virtualThreadExecutor;
    private final GetRecommendUserIdsQuery getRecommendUserIdsQuery;


    public PortfolioService(GetUserPortfoliosQuery getUserPortfoliosQuery,
                            @Qualifier("userLikeJpaAdapter")
                            GetUserLikeQuery getUserLikeQuery,
                            Executor virtualThreadExecutor, GetRecommendUserIdsQuery getRecommendUserIdsQuery) {
        this.getUserPortfoliosQuery = getUserPortfoliosQuery;
        this.getUserLikeQuery = getUserLikeQuery;
        this.virtualThreadExecutor = virtualThreadExecutor;
        this.getRecommendUserIdsQuery = getRecommendUserIdsQuery;
    }

    public List<UserPortfolioResDto> getUserPortfolios(Long userId, Pageable pageable) {
        List<Long> distinctUserIds = getDistinctUserIds(userId, pageable);
        return getUserPortfoliosQuery.findAllPortfoliosByUserIds(distinctUserIds);
    }

    public List<Long> getDistinctUserIds(Long userId, Pageable pageable) {
//         이미 매칭된 유저
        CompletableFuture<List<Long>> alreadyMatchedUserFuture = getAlreadyMatchedUserFuture(userId,
                virtualThreadExecutor);
//          추천 로직에 의한 유저
        List<Long> recommendUserIds = getRecommendUserIdsQuery.getRecommendUserIds(userId, pageable);
        CompletableFuture<List<Long>> userPortfolioIds = getUserPortfolioIds(alreadyMatchedUserFuture,
                recommendUserIds);
        return userPortfolioIds.join();
    }

    private CompletableFuture<List<Long>> getAlreadyMatchedUserFuture(Long userId, Executor executor) {
        return CompletableFuture.supplyAsync(() -> getUserLikeQuery.findAllMatchedUserIdByUserId(userId), executor);
    }

    private CompletableFuture<List<Long>> getUserPortfolioIds(CompletableFuture<List<Long>> alreadyMatchedUserFuture,
                                                              List<Long> recommendUserIds) {
        return alreadyMatchedUserFuture.thenApply(matchedUserIds -> {
            recommendUserIds.removeAll(matchedUserIds);
            return recommendUserIds;
        });
    }
}
