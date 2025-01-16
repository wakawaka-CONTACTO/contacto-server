package org.kiru.user.userlike.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.kiru.core.chat.chatroom.domain.ChatRoomType;
import org.kiru.core.user.userlike.domain.LikeStatus;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;
import org.kiru.user.user.api.ChatApiClient;
import org.kiru.user.userlike.api.CreateChatRoomRequest;
import org.kiru.user.userlike.api.CreateChatRoomResponse;
import org.kiru.user.userlike.dto.res.LikeResponse;
import org.kiru.user.userlike.service.out.GetMatchedUserPortfolioQuery;
import org.kiru.user.userlike.service.out.SendLikeOrDislikeUseCase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class UserLikeService {
    private final SendLikeOrDislikeUseCase sendLikeOrDislikeUseCase;
    private final GetMatchedUserPortfolioQuery getMatchedUserPortfolioQuery;
    private final ChatApiClient chatRoomCreateApiClient;
    private final Executor virtualThreadExecutor;

    public UserLikeService(
            @Qualifier("userLikeJpaAdapter") SendLikeOrDislikeUseCase sendLikeOrDislikeUseCase,
            GetMatchedUserPortfolioQuery getMatchedUserPortfolioQuery,
            ChatApiClient chatRoomCreateApiClient,
            Executor virtualThreadExecutor) {
        this.sendLikeOrDislikeUseCase = sendLikeOrDislikeUseCase;
        this.getMatchedUserPortfolioQuery = getMatchedUserPortfolioQuery;
        this.chatRoomCreateApiClient = chatRoomCreateApiClient;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    public LikeResponse sendLikeOrDislike(Long userId, Long likedUserId, LikeStatus status) {
        boolean isMatched = sendLikeOrDislikeUseCase.sendOrDislike(userId, likedUserId, status).isMatched();
        if (isMatched) {
            CompletableFuture<List<UserPortfolioResDto>> portfolioFuture = CompletableFuture.supplyAsync(
                    () -> getMatchedUserPortfolioQuery.findByUserIds(List.of(userId, likedUserId)),
                    virtualThreadExecutor);
            CompletableFuture<CreateChatRoomResponse> chatRoomIdFuture = CompletableFuture.supplyAsync(() ->
                            chatRoomCreateApiClient.createRoom(userId,
                                    CreateChatRoomRequest.of("CONTACTO MANAGER", ChatRoomType.PRIVATE, userId,
                                            likedUserId)),
                    virtualThreadExecutor);
            return CompletableFuture.allOf(portfolioFuture, chatRoomIdFuture).thenApply(v -> LikeResponse.of(
                    true, portfolioFuture.join(), chatRoomIdFuture.join().getChatRoomId()
            )).join();
        }
        return LikeResponse.of(false, null, null);
    }
}
