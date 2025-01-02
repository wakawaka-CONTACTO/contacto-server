package org.kiru.user.portfolio.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.kiru.core.user.userPurpose.entity.UserPurpose;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;
import org.kiru.user.portfolio.service.out.GetUserPortfoliosQuery;
import org.kiru.user.user.repository.UserPurposeRepository;
import org.kiru.user.userlike.service.out.GetUserLikeQuery;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PortfolioService {
    private final UserPurposeRepository userPurposeRepository;
    private final GetUserPortfoliosQuery getUserPortfoliosQuery;
    private final GetUserLikeQuery getUserLikeQuery;
    private final Executor virtualThreadExecutor;


    public PortfolioService(UserPurposeRepository userPurposeRepository, GetUserPortfoliosQuery getUserPortfoliosQuery,
                             @Qualifier("userLikeJpaAdapter")
                             GetUserLikeQuery getUserLikeQuery,
                             Executor virtualThreadExecutor) {
        this.userPurposeRepository = userPurposeRepository;
        this.getUserPortfoliosQuery = getUserPortfoliosQuery;
        this.getUserLikeQuery = getUserLikeQuery;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    public List<UserPortfolioResDto> getUserPortfolios(Long userId, Pageable pageable) {
        List<Long> distinctUserIds = getDistinctUserIds(userId, pageable);
        return getUserPortfoliosQuery.findAllPortfoliosByUserIds(distinctUserIds);
    }

    private List<Long> getDistinctUserIds(Long userId, Pageable pageable) {
//         이미 매칭된 유저
            CompletableFuture<List<Long>> alreadyMatchedUserFuture = getAlreadyMatchedUserFuture(userId, virtualThreadExecutor);
//            목적에 따른 매칭된 유저
            CompletableFuture<List<Long>> matchingUserIdsByPurposeFuture = getMatchingUserIdsByPurposeFuture(userId, pageable, virtualThreadExecutor);
//           나를 좋아요를 누른 유저
            CompletableFuture<List<Long>> likedUserIdsFuture = getLikedUserIdsFuture(userId, pageable, virtualThreadExecutor);
//            인기있는 유저
            CompletableFuture<List<Long>> popularIdsFuture = getPopularIdsFuture(pageable, virtualThreadExecutor);
//            모든 유저 아이디를 가져옴
            CompletableFuture<List<Long>> userPortfolioIds = getUserPortfolioIds(alreadyMatchedUserFuture,matchingUserIdsByPurposeFuture, likedUserIdsFuture,
                    popularIdsFuture);
            return userPortfolioIds.join();
    }

    private CompletableFuture<List<Long>> getAlreadyMatchedUserFuture(Long userId, Executor executor) {
        return CompletableFuture.supplyAsync(() -> getUserLikeQuery.findAllMatchedUserIdByUserId(userId), executor);
    }

    private CompletableFuture<List<Long>> getMatchingUserIdsByPurposeFuture(Long userId, Pageable pageable, Executor executor) {
        return CompletableFuture.supplyAsync(() -> getMatchingUserIdsByPurposeFuture(userId, pageable), executor);
    }

    private CompletableFuture<List<Long>> getLikedUserIdsFuture(Long userId, Pageable pageable, Executor executor) {
        return CompletableFuture.supplyAsync(() -> getUserLikeQuery.findAllLikeMeUserIdAndNotMatchedByLikedUserId(userId, pageable), executor);
    }

    private CompletableFuture<List<Long>> getPopularIdsFuture(Pageable pageable, Executor executor) {
        return CompletableFuture.supplyAsync(()-> getUserLikeQuery.getPopularUserId(pageable).longs(), executor);
    }

    private CompletableFuture<List<Long>> getUserPortfolioIds(CompletableFuture<List<Long>> alreadyMatchedUserFuture,
                                                              CompletableFuture<List<Long>> matchingUserIdsByPurposeFuture,
                                                              CompletableFuture<List<Long>> likedUserIdsFuture,
                                                              CompletableFuture<List<Long>> popularIdsFuture) {
        return CompletableFuture.allOf(alreadyMatchedUserFuture, matchingUserIdsByPurposeFuture, likedUserIdsFuture, popularIdsFuture)
                .thenApply(v -> {
                    List<Long> combinedList = new ArrayList<>(Stream.of(
                                    matchingUserIdsByPurposeFuture.join(),
                                    likedUserIdsFuture.join(),
                                    popularIdsFuture.join())
                            .flatMap(List::stream)
                            .distinct()
                            .toList());
                    combinedList.removeAll(alreadyMatchedUserFuture.join());
                    return combinedList;
                });
    }

    private List<Long> getMatchingUserIdsByPurposeFuture(Long userId, Pageable pageable) {
        return userPurposeRepository.findUserIdsByPurposeTypesOrderByCount(
                        userPurposeRepository.findAllByUserId(userId).stream()
                                .map(UserPurpose::getPurposeType).toList(), pageable)
                .getContent();
    }
}