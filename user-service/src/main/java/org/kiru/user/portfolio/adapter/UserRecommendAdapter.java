package org.kiru.user.portfolio.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import org.kiru.core.user.userPurpose.entity.UserPurpose;
import org.kiru.user.portfolio.service.out.GetRecommendUserIdsQuery;

import org.kiru.user.user.repository.UserPurposeRepository;
import org.kiru.user.userlike.service.out.GetUserLikeQuery;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class UserRecommendAdapter implements GetRecommendUserIdsQuery {
    private final Executor virtualThreadExecutor;
    private final GetUserLikeQuery getUserLikeQuery;
    private final UserPurposeRepository userPurposeRepository;

    public UserRecommendAdapter(Executor virtualThreadExecutor,
                                @Qualifier("userLikeJpaAdapter")
                                GetUserLikeQuery getUserLikeQuery, UserPurposeRepository userPurposeRepository) {
        this.virtualThreadExecutor = virtualThreadExecutor;
        this.getUserLikeQuery = getUserLikeQuery;
        this.userPurposeRepository = userPurposeRepository;
    }

    @Override
    @Cacheable(value = "recommendUserIds", key = "#userId" +'-'+ "#pageable.pageNumber")
    public List<Long> getRecommendUserIds(Long userId, Pageable pageable) {
        //            목적에 따른 매칭된 유저
        CompletableFuture<List<Long>> matchingUserIdsByPurposeFuture = getMatchingUserIdsByPurposeFuture(userId,
                pageable, virtualThreadExecutor);
        //           나를 좋아요를 누른 유저
        CompletableFuture<List<Long>> likedUserIdsFuture = getLikedUserIdsFuture(userId, pageable,
                virtualThreadExecutor);
        //            인기있는 유저
        CompletableFuture<List<Long>> popularIdsFuture = getPopularIdsFuture(pageable, virtualThreadExecutor);
        return new ArrayList<>(Stream.of(
                        matchingUserIdsByPurposeFuture.join(),
                        likedUserIdsFuture.join(),
                        popularIdsFuture.join())
                .flatMap(List::stream)
                .distinct()
                .toList());
    }

    private CompletableFuture<List<Long>> getMatchingUserIdsByPurposeFuture(Long userId, Pageable pageable, Executor executor) {
        return CompletableFuture.supplyAsync(() -> getMatchingUserIdsByPurpose(userId, pageable), executor);
    }

    private CompletableFuture<List<Long>> getLikedUserIdsFuture(Long userId, Pageable pageable, Executor executor) {
        return CompletableFuture.supplyAsync(
                () -> getUserLikeQuery.findAllLikeMeUserIdAndNotMatchedByLikedUserId(userId, pageable), executor);
    }

    private CompletableFuture<List<Long>> getPopularIdsFuture(Pageable pageable, Executor executor) {
        return CompletableFuture.supplyAsync(() -> getUserLikeQuery.getPopularUserId(pageable).longs(), executor);
    }

    private List<Long> getMatchingUserIdsByPurpose(Long userId, Pageable pageable) {
        return userPurposeRepository.findUserIdsByPurposeTypesOrderByCount(
                userPurposeRepository.findAllByUserId(userId).stream()
                        .map(UserPurpose::getPurposeType).toList(), pageable).getContent();
    }
}
