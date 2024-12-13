package org.kiru.user.portfolio.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.core.user.userPurpose.entity.UserPurpose;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;
import org.kiru.user.portfolio.service.out.GetUserPortfoliosQuery;
import org.kiru.user.user.repository.UserPurposeRepository;
import org.kiru.user.userlike.repository.UserLikeRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PortfolioService {
    private final UserPurposeRepository userPurposeRepository;
    private final GetUserPortfoliosQuery getUserPortfoliosQuery;
    private final UserLikeRepository userLikeRepository;

    public List<UserPortfolioResDto> getUserPortfolios(Long userId, Pageable pageable) {
        List<Long> distinctUserIds = getDistinctUserIds(userId, pageable);
        return getUserPortfoliosQuery.findAllPortfoliosByUserIds(distinctUserIds);
    }

    private List<Long> getDistinctUserIds(Long userId, Pageable pageable) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CompletableFuture<List<Long>> alreadyMatchedUserFuture = getAlreadyMatchedUserFuture(userId, executor);
            CompletableFuture<List<Long>> matchingUserIdsFuture = getMatchingUserIdsFuture(userId, pageable, executor);
            CompletableFuture<List<Long>> combinedFuture = getCombinedFuture(alreadyMatchedUserFuture,
                    matchingUserIdsFuture);
            CompletableFuture<Slice<Long>> likedUserIdsFuture = getLikedUserIdsFuture(userId, pageable, executor);
            CompletableFuture<Slice<Long>> popularIdsFuture = getPopularIdsFuture(pageable, executor);
            CompletableFuture<List<Long>> userPortfolioIds = getUserPortfolioIds(combinedFuture, likedUserIdsFuture,
                    popularIdsFuture);
            return userPortfolioIds.join().stream().distinct().toList();
        }
    }

    private CompletableFuture<List<Long>> getAlreadyMatchedUserFuture(Long userId, Executor executor) {
        return CompletableFuture.supplyAsync(
                () -> userLikeRepository.findAllMatchedUserIdByUserId(userId), executor);
    }

    private CompletableFuture<List<Long>> getMatchingUserIdsFuture(Long userId, Pageable pageable, Executor executor) {
        return CompletableFuture.supplyAsync(
                () -> getMatchingUserIds(userId, pageable), executor);
    }

    private CompletableFuture<List<Long>> getCombinedFuture(CompletableFuture<List<Long>> alreadyMatchedUserFuture,
                                                            CompletableFuture<List<Long>> matchingUserIdsFuture) {
        return alreadyMatchedUserFuture.thenCombine(matchingUserIdsFuture,
                (alreadyMatchedUser, matchingUserIdsResult) -> {
                    List<Long> mutableMatchingUserIds = new ArrayList<>(matchingUserIdsResult);
                    mutableMatchingUserIds.removeAll(alreadyMatchedUser);
                    return mutableMatchingUserIds;
                });
    }

    private CompletableFuture<Slice<Long>> getLikedUserIdsFuture(Long userId, Pageable pageable, Executor executor) {
        return CompletableFuture.supplyAsync(
                () -> userLikeRepository.findAllLikeMeUserIdAndNotMatchedByLikedUserId(userId, pageable), executor);
    }

    private CompletableFuture<Slice<Long>> getPopularIdsFuture(Pageable pageable, Executor executor) {
        return CompletableFuture.supplyAsync(
                () -> userLikeRepository.findAllUserIdOrderByLikedUserIdCountDesc(pageable), executor);
    }

    private CompletableFuture<List<Long>> getUserPortfolioIds(CompletableFuture<List<Long>> combinedFuture,
                                                              CompletableFuture<Slice<Long>> likedUserIdsFuture,
                                                              CompletableFuture<Slice<Long>> popularIdsFuture) {
        return CompletableFuture.allOf(combinedFuture, likedUserIdsFuture, popularIdsFuture)
                .thenApply(v -> {
                    List<Long> combineUserIds = combinedFuture.join();
                    combineUserIds.addAll(likedUserIdsFuture.join().getContent());
                    combineUserIds.addAll(popularIdsFuture.join().getContent());
                    return combineUserIds;
                });
    }

    private List<Long> getMatchingUserIds(Long userId, Pageable pageable) {
        if(pageable == null) {
            pageable = Pageable.unpaged();
        }
        return userPurposeRepository.findUserIdsByPurposeTypesOrderByCount(
                        userPurposeRepository.findAllByUserId(userId).stream()
                                .map(UserPurpose::getPurposeType).toList(), pageable)
                .getContent();
    }
}