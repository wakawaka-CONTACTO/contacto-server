package org.kiru.user.userlike.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.kiru.core.chat.chatroom.domain.ChatRoomType;
import org.kiru.core.user.userlike.domain.LikeStatus;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;
import org.kiru.user.user.api.AlarmApiClient;
import org.kiru.user.user.api.ChatApiClient;
import org.kiru.user.user.repository.UserRepository;
import org.kiru.user.userlike.api.CreateChatRoomRequest;
import org.kiru.user.userlike.api.CreateChatRoomResponse;
import org.kiru.user.userlike.dto.res.LikeResponse;
import org.kiru.user.userlike.service.out.GetMatchedUserPortfolioQuery;
import org.kiru.user.userlike.service.out.SendLikeOrDislikeUseCase;
import org.springframework.beans.factory.annotation.Qualifier;
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

    public UserLikeService(
            @Qualifier("userLikeJpaAdapter") SendLikeOrDislikeUseCase sendLikeOrDislikeUseCase,
            GetMatchedUserPortfolioQuery getMatchedUserPortfolioQuery,
            ChatApiClient chatRoomCreateApiClient,
            MatchNotificationService matchNotificationService,
            Executor virtualThreadExecutor) {
        this.sendLikeOrDislikeUseCase = sendLikeOrDislikeUseCase;
        this.getMatchedUserPortfolioQuery = getMatchedUserPortfolioQuery;
        this.chatRoomCreateApiClient = chatRoomCreateApiClient;
        this.matchNotificationService = matchNotificationService;
        this.virtualThreadExecutor = virtualThreadExecutor;

    }

    public LikeResponse sendLikeOrDislike(Long userId, Long likedUserId, LikeStatus status) {
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
            
            // 매칭 성공 시 양쪽 모두에게 푸시 알림 전송
            CompletableFuture.runAsync(() -> 
                matchNotificationService.sendMatchNotifications(userId, likedUserId),
                virtualThreadExecutor
            );

            CompletableFuture<List<UserPortfolioResDto>> portfolioFuture = CompletableFuture.supplyAsync(
                    () -> getMatchedUserPortfolioQuery.findByUserIds(List.of(userId, likedUserId)),
                    virtualThreadExecutor);
            CompletableFuture<CreateChatRoomResponse> chatRoomIdFuture = CompletableFuture.supplyAsync(() ->
                            chatRoomCreateApiClient.createRoom(userId,
                                    CreateChatRoomRequest.of("CONTACTO MANAGER", ChatRoomType.PRIVATE, userId,
                                            likedUserId)),
                    virtualThreadExecutor);
            return CompletableFuture.allOf(portfolioFuture, chatRoomIdFuture).thenApplyAsync(v -> LikeResponse.of(
                    true, portfolioFuture.join(), chatRoomIdFuture.join().getChatRoomId()
            )).join();
        }
        return LikeResponse.of(false, null, null);
    }
}
