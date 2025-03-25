package org.kiru.user.portfolio.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.kiru.core.user.userlike.domain.LikeStatus;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;
import org.kiru.user.portfolio.service.out.GetRecommendUserIdsQuery;
import org.kiru.user.portfolio.service.out.GetUserPortfoliosQuery;
import org.kiru.user.userBlock.service.out.GetUserBlockQuery;
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
    private final GetUserBlockQuery getUserBlockQuery;


    public PortfolioService(GetUserPortfoliosQuery getUserPortfoliosQuery,
                            @Qualifier("userLikeJpaAdapter")
                            GetUserLikeQuery getUserLikeQuery,
                            Executor virtualThreadExecutor,
                            GetRecommendUserIdsQuery getRecommendUserIdsQuery,
                            GetUserBlockQuery getUserBlockQuery) {
        this.getUserPortfoliosQuery = getUserPortfoliosQuery;
        this.getUserLikeQuery = getUserLikeQuery;
        this.virtualThreadExecutor = virtualThreadExecutor;
        this.getRecommendUserIdsQuery = getRecommendUserIdsQuery;
        this.getUserBlockQuery = getUserBlockQuery;
    }

    public List<UserPortfolioResDto> getUserPortfolios(Long userId, Pageable pageable) {
        List<Long> distinctUserIds = getDistinctUserIds(userId, pageable);
        log.info("추천한 유저Id: {}", distinctUserIds);
        return getUserPortfoliosQuery.findAllPortfoliosByUserIds(distinctUserIds);
    }

    private List<Long> getDistinctUserIds(Long userId, Pageable pageable) {
        // 이미 좋아요를 눌렀거나 차단한 유저
        CompletableFuture<List<Long>> alreadyLikedOrBlockedUserFuture = getAlreadyLikedUserFuture(userId, virtualThreadExecutor)
                .thenCombine(getBlockedUserFuture(userId, virtualThreadExecutor), (likedUserIds, blockedUserIds) -> {
                    List<Long> combined = new ArrayList<>(likedUserIds);
                    combined.add(userId);
                    combined.addAll(blockedUserIds);
                    return combined; });
        // 싫어요를 누른 유저 (맨 뒤로 가게 하기 위함)
        List<Long> disLikedUserIds = getUserLikeQuery.findAllLikedUserIdByUserIdAndLikeStatus(userId, LikeStatus.DISLIKE);
        // 추천 로직에 의한 유저
        List<Long> recommendUserIds = getRecommendUserIdsQuery.getRecommendUserIds(userId, pageable);
        CompletableFuture<List<Long>> userPortfolioIds = getUserPortfolioIds(alreadyLikedOrBlockedUserFuture, disLikedUserIds,
                recommendUserIds);
        return userPortfolioIds.join();
    }

    private CompletableFuture<List<Long>> getAlreadyMatchedUserFuture(Long userId, Executor executor) {
        return CompletableFuture.supplyAsync(() -> getUserLikeQuery.findAllMatchedUserIdByUserId(userId), executor);
    }

    private CompletableFuture<List<Long>> getAlreadyLikedUserFuture(Long userId, Executor executor) {
        return CompletableFuture.supplyAsync(() -> getUserLikeQuery.findAllLikedUserIdByUserIdAndLikeStatus(userId, LikeStatus.LIKE), executor);
    }

    private CompletableFuture<List<Long>> getBlockedUserFuture(Long userId, Executor executor) {
        return CompletableFuture.supplyAsync(() -> getUserBlockQuery.findAllBlockedUserIdByUserId(userId), executor);
    }

    private CompletableFuture<List<Long>> getUserPortfolioIds(CompletableFuture<List<Long>> alreadyLikedUserFuture,
                                                              List<Long> disLikedUserIds,
                                                              List<Long> recommendUserIds) {
        log.info("필터 안 된 추천한 유저Id: {}", recommendUserIds);
        log.info("좋아요 눌렀던 유저Id: {}", alreadyLikedUserFuture.join());
        return alreadyLikedUserFuture.thenApplyAsync(likedUserIds -> {
            log.info("좋아요 눌렀던 유저Id: {}", likedUserIds);
            List<Long> resultList = new ArrayList<>(recommendUserIds);
            resultList.removeAll(likedUserIds);
            List<Long> disLikedInRecommend = disLikedUserIds.stream()
                    .filter(resultList::contains)
                    .toList();
            resultList.removeAll(disLikedInRecommend);
            resultList.addAll(disLikedInRecommend);
            return resultList;
        }, virtualThreadExecutor).exceptionally(throwable -> {
            log.error("사용자 포트폴리오 ID 필터링 중 오류 발생", throwable);
            return new ArrayList<>();
        });
    }
}
