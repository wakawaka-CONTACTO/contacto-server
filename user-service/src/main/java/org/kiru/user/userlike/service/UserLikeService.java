package org.kiru.user.userlike.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.kiru.core.chat.chatroom.domain.ChatRoomType;
import org.kiru.core.exception.BadRequestException;
import org.kiru.core.exception.code.FailureCode;
import org.kiru.core.user.userlike.domain.LikeStatus;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;
import org.kiru.user.user.api.ChatApiClient;
import org.kiru.user.userlike.api.CreateChatRoomRequest;
import org.kiru.user.userlike.api.CreateChatRoomResponse;
import org.kiru.user.userlike.dto.res.LikeLimitResponse;
import org.kiru.user.userlike.dto.res.LikeResponse;
import org.kiru.user.userlike.service.out.GetMatchedUserPortfolioQuery;
import org.kiru.user.userlike.service.out.SendLikeOrDislikeUseCase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserLikeService {
    private final SendLikeOrDislikeUseCase sendLikeOrDislikeUseCase;
    private final GetMatchedUserPortfolioQuery getMatchedUserPortfolioQuery;
    private final ChatApiClient chatRoomCreateApiClient;
    private final MatchNotificationService matchNotificationService;
    private final Executor virtualThreadExecutor;
    private final RedisTemplate<String, String> redisTemplateForOne;

    public UserLikeService(
            @Qualifier("userLikeJpaAdapter") SendLikeOrDislikeUseCase sendLikeOrDislikeUseCase,
            GetMatchedUserPortfolioQuery getMatchedUserPortfolioQuery,
            ChatApiClient chatRoomCreateApiClient,
            MatchNotificationService matchNotificationService,
            Executor virtualThreadExecutor,
            RedisTemplate<String, String> redisTemplateForOne) {
        this.sendLikeOrDislikeUseCase = sendLikeOrDislikeUseCase;
        this.getMatchedUserPortfolioQuery = getMatchedUserPortfolioQuery;
        this.chatRoomCreateApiClient = chatRoomCreateApiClient;
        this.matchNotificationService = matchNotificationService;
        this.virtualThreadExecutor = virtualThreadExecutor;
        this.redisTemplateForOne = redisTemplateForOne;
    }

    public LikeLimitResponse getLikeLimit(Long userId) {
        int likeLimit = 50;
        int likeCount = getLikeCount(userId);
        return LikeLimitResponse.of(likeLimit, likeCount);
    }

    public LikeResponse sendLikeOrDislike(Long userId, Long likedUserId, LikeStatus status) {
        // 좋아요 횟수 체크
        int likeCount = increaseLikeCount(userId, status);

        boolean isMatched = sendLikeOrDislikeUseCase.sendLikeOrDislike(userId, likedUserId, status).isMatched();
        
        // 좋아요 생성 시 푸시 알림 전송 (좋아요를 받은 사람에게만)
//        if (status == LikeStatus.LIKE) {
//            CompletableFuture.runAsync(() -> {
//                try {
//                    UserJpaEntity user = userRepository.findById(userId)
//                            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
//                    String title = user.getUsername() + "님이 좋아요를 주셨어요🥰";
//                    String body = "새로운 좋아요가 도착했습니다!";
//                    alarmApiClient.sendMessageToUser(likedUserId,
//                        AlarmMessageRequest.of(title, body));
//                } catch (Exception e) {
//                    log.error("Failed to send like notification to user: {}", likedUserId, e);
//                }
//            }, virtualThreadExecutor);
//        }
        if (isMatched) {
            log.info("User matched with userId: {} and likedUserId: {}", userId, likedUserId);

            CompletableFuture<List<UserPortfolioResDto>> portfolioFuture = CompletableFuture.supplyAsync(
                    () -> getMatchedUserPortfolioQuery.findByUserIds(List.of(userId, likedUserId)),
                    virtualThreadExecutor);
            CompletableFuture<CreateChatRoomResponse> chatRoomIdFuture = CompletableFuture.supplyAsync(() ->
                            chatRoomCreateApiClient.createRoom(userId,
                                    CreateChatRoomRequest.of("CONTACTO MANAGER", ChatRoomType.PRIVATE, userId,
                                            likedUserId)),
                    virtualThreadExecutor);
            // 매칭 성공 시 양쪽 모두에게 푸시 알림 전송
            CompletableFuture.runAsync(() -> 
                matchNotificationService.sendMatchNotifications(userId, likedUserId, chatRoomIdFuture.join().getChatRoomId()),
                virtualThreadExecutor
            );
            return CompletableFuture.allOf(portfolioFuture, chatRoomIdFuture).thenApplyAsync(v -> LikeResponse.of(
                    true, portfolioFuture.join(), chatRoomIdFuture.join().getChatRoomId(), likeCount
            )).join();
        }
        return LikeResponse.of(false, null, null, likeCount);
    }

    private int getLikeCount(Long userId) {
        String key = getRedisKey(userId);
        String likeCountStr = redisTemplateForOne.opsForValue().get(key);
        log.debug("{} = {}", key, likeCountStr);
        return (likeCountStr != null) ? Integer.parseInt(likeCountStr) : 0;
    }

    private int increaseLikeCount(Long userId, LikeStatus status) {
        String key = getRedisKey(userId);
        String likeCountStr = redisTemplateForOne.opsForValue().get(key);
        log.debug("{} = {}", key, likeCountStr);

        int likeCount = (likeCountStr != null) ? Integer.parseInt(likeCountStr) : 0;
        
        if (status == LikeStatus.LIKE) {
            if (likeCount >= 5) {
                log.debug("좋아요 횟수 제한을 초과했음.");
                throw new BadRequestException(FailureCode.LIKE_TOO_MANY_REQUESTS);
            }
            
            if (likeCountStr == null) {
                log.debug("저장된 데이터가 없으므로 새 데이터 생성");
                setRedisExpiration(key);
            }
            
            likeCount += 1;
            redisTemplateForOne.opsForValue().set(key, String.valueOf(likeCount));
        }
        return likeCount;
    }

    private String getRedisKey(Long userId) {
        ZoneId zoneKST = ZoneId.of("Asia/Seoul");
        LocalDateTime now = LocalDateTime.now(zoneKST);
        String today = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "likeCount:" + userId + ":" + today;
    }

    private void setRedisExpiration(String key) {
        ZoneId zoneKST = ZoneId.of("Asia/Seoul");
        LocalDateTime now = LocalDateTime.now(zoneKST);
        long secondsUntilEndOfDay = Duration.between(
                now,
                now.toLocalDate().plusDays(1).atStartOfDay(zoneKST)
        ).getSeconds();
        redisTemplateForOne.expire(key, secondsUntilEndOfDay, TimeUnit.SECONDS);
    }
}
