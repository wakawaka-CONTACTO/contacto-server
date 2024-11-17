package org.kiru.user.userlike.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import org.kiru.core.chat.chatroom.domain.ChatRoomType;
import org.kiru.core.user.userlike.domain.LikeStatus;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;
import org.kiru.user.user.api.ChatApiClient;
import org.kiru.user.userlike.api.CreateChatRoomRequest;
import org.kiru.user.userlike.api.CreateChatRoomResponse;
import org.kiru.user.userlike.dto.res.LikeResponse;
import org.kiru.user.userlike.service.out.GetMatchedUserPortfolioQuery;
import org.kiru.user.userlike.service.out.SendLikeOrDislikeUseCase;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserLikeService {
    private final SendLikeOrDislikeUseCase sendLikeOrDislikeUseCase;
    private final GetMatchedUserPortfolioQuery getMatchedUserPortfolioQuery;
    private final ChatApiClient chatRoomCreateApiClient;

    public LikeResponse sendLikeOrDislike(Long userId, Long likedUserId, LikeStatus status) {
        boolean isMatched = sendLikeOrDislikeUseCase.sendOrDislike(userId, likedUserId, status).isMatched();
        if (isMatched) {
            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                CompletableFuture<List<UserPortfolioResDto>> portfolioFuture = CompletableFuture.supplyAsync(
                        () -> getMatchedUserPortfolioQuery.findByUserIds(List.of(userId, likedUserId)), executor);
                CompletableFuture<CreateChatRoomResponse> chatRoomIdFuture = CompletableFuture.supplyAsync(() ->
                                chatRoomCreateApiClient.createRoom(userId,
                                        CreateChatRoomRequest.of("CONTACTO MANAGER", ChatRoomType.PRIVATE, userId, likedUserId)),
                        executor);
                CompletableFuture<Void> allFutures = CompletableFuture.allOf(portfolioFuture, chatRoomIdFuture);
                allFutures.join();
                return LikeResponse.of(
                        isMatched, portfolioFuture.join(), chatRoomIdFuture.join().getChatRoomId()
                );
            }
        }
        return LikeResponse.of(isMatched, null, null);
    }
}
