package org.kiru.user.portfolio.service;

import java.util.ArrayList;
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

    private List<Long> getDistinctUserIds(Long userId, Pageable pageable) {
//         이미 매칭된 유저
        CompletableFuture<List<Long>> alreadyMatchedUserFuture = getAlreadyMatchedUserFuture(userId,
                virtualThreadExecutor).thenApply(matchedUserIds ->
        {
            matchedUserIds.add(userId);
            return matchedUserIds;
        });
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
        return alreadyMatchedUserFuture.thenApplyAsync(matchedUserIds -> {
                    List<Long> recommendUserIdList = new ArrayList<>(recommendUserIds);
                    recommendUserIdList.removeAll(matchedUserIds);
                    return recommendUserIdList;
                }, virtualThreadExecutor)
                .exceptionally(throwable -> {
                    log.error("사용자 포트폴리오 ID 필터링 중 오류 발생", throwable);
                    return new ArrayList<>();
                });
    }
}
