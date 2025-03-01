package org.kiru.user.user.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.core.chat.chatroom.domain.ChatRoom;
import org.kiru.core.exception.EntityNotFoundException;
import org.kiru.core.exception.code.FailureCode;
import org.kiru.core.user.talent.domain.Talent.TalentType;
import org.kiru.core.user.user.domain.User;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.kiru.core.user.userPortfolioItem.domain.UserPortfolio;
import org.kiru.core.user.userPortfolioItem.domain.UserPortfolioItem;
import org.kiru.core.user.userPurpose.domain.PurposeType;
import org.kiru.user.portfolio.service.out.GetUserPortfoliosQuery;
import org.kiru.user.user.api.ChatApiClient;
import org.kiru.user.user.dto.request.UserUpdateDto;
import org.kiru.user.user.dto.request.UserUpdatePwdDto;
import org.kiru.user.user.dto.response.ChatRoomResponse;
import org.kiru.user.user.dto.response.MessageResponse;
import org.kiru.user.user.repository.UserRepository;
import org.kiru.user.user.service.in.GetUserMainPageUseCase;
import org.kiru.user.user.service.out.GetUserAdditionalInfoQuery;
import org.kiru.user.user.service.out.UserQueryWithCache;
import org.kiru.user.user.service.out.UserUpdatePort;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements GetUserMainPageUseCase {
    private final UserQueryWithCache userQueryWithCache;
    private final UserRepository userRepository;
    private final UserUpdatePort userUpdateport;
    private final ChatApiClient chatApiClient;
    private final GetUserAdditionalInfoQuery getUserAdditionalInfoQuery;
    private final GetUserPortfoliosQuery getUserPortfoliosQuery;

    @Cacheable(value = "userDetail", key = "#userId", unless = "#result == null")
    public User getUserFromIdToMainPage(Long userId) {
        User user = userQueryWithCache.getUser(userId);
        return getUserDetails(user);
    }

    public List<ChatRoom> getUserChatRooms(Long userId, Pageable pageable) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CompletableFuture<List<ChatRoom>> chatRoomsFuture = CompletableFuture.supplyAsync(
                    () -> chatApiClient.getUserChatRooms(userId, pageable),
                    executor);
            CompletableFuture<List<UserPortfolioItem>> allParticipantIdsFuture = chatRoomsFuture.thenApplyAsync(
                    ChatRoom::getAllParticipantIds, executor).thenApplyAsync(
                    getUserPortfoliosQuery::getUserPortfoliosWithMinSequence, executor);
            CompletableFuture<Map<Long, UserPortfolioItem>> userPortfolioImgMapFuture = allParticipantIdsFuture.thenApplyAsync(
                    UserPortfolio::getUserIdAndUserPortfolioItemMap,executor);
            return chatRoomsFuture.thenCombineAsync(userPortfolioImgMapFuture, (chatRooms, userPortfolioImgMap) -> {
                for (ChatRoom chatRoom : chatRooms) {
                    chatRoom.setThumbnailAndRoomTitle(userPortfolioImgMap);
                }
                return chatRooms;
            }, executor).join();
        }
    }

    public ChatRoomResponse getChatMessage(Long roomId, Long userId, int page, int size){
        try( var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CompletableFuture<ChatRoom> chatRoomFuture = CompletableFuture.supplyAsync(
                () -> chatApiClient.getRoom(roomId, userId), executor
            );

            CompletableFuture<List<UserPortfolioItem>> userPortfolioImgMapFuture= chatRoomFuture.thenApplyAsync(
                ChatRoom::getParticipantsIds, executor).thenApplyAsync(
                    getUserPortfoliosQuery::getUserPortfoliosWithMinSequence, executor
            );

            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            CompletableFuture<List<MessageResponse>> messageFuture = CompletableFuture.supplyAsync(() -> {
                return chatApiClient.getMessages(roomId, userId, false, pageable)
                    .stream()
                    .map(MessageResponse::fromMessage)
                    .toList();
            }, executor);

            return chatRoomFuture.thenCombineAsync(userPortfolioImgMapFuture, (chatRoom, userPortfolioImgMap) -> {
                chatRoom.setThumbnailAndRoomTitle(userPortfolioImgMap.getFirst());
                return chatRoom;
            }, executor).thenCombineAsync(messageFuture, ChatRoomResponse::of, executor).join();
        }
    }

    public ChatRoom getUserChatRoom(Long roomId, Long userId) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CompletableFuture<ChatRoom> chatRoomFuture = CompletableFuture.supplyAsync(
                    () -> chatApiClient.getRoom(roomId, userId), executor);
            CompletableFuture<List<UserPortfolioItem>> userPortfolioImgMapFuture = chatRoomFuture.thenApplyAsync(
                    ChatRoom::getParticipantsIds, executor).thenApplyAsync(
                    getUserPortfoliosQuery::getUserPortfoliosWithMinSequence, executor);
            return chatRoomFuture.thenCombineAsync(userPortfolioImgMapFuture, (chatRoom, userPortfolioImgMap) -> {
                chatRoom.setThumbnailAndRoomTitle(userPortfolioImgMap.getFirst());
                return chatRoom;
            }, executor).join();
        }
    }

    public User getUserDetails(User user) {
        Long userId = user.getId();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CompletableFuture<List<PurposeType>> purposesFuture = CompletableFuture.supplyAsync(
                    () -> getUserAdditionalInfoQuery.getUserPurposes(userId), executor);
            CompletableFuture<List<TalentType>> talentsFuture = CompletableFuture.supplyAsync(
                    () -> getUserAdditionalInfoQuery.getUserTalents(userId),
                    executor);
            CompletableFuture<List<UserPortfolioItem>> portfolioImgsFuture = CompletableFuture.supplyAsync(
                    () -> getUserAdditionalInfoQuery.getUserPortfolioByUserId(userId), executor);
            return CompletableFuture.allOf(purposesFuture, talentsFuture, portfolioImgsFuture)
                    .thenApplyAsync(v -> {
                        user.userPurposes(purposesFuture.join());
                        user.userTalents(talentsFuture.join());
                        user.userPortfolio(UserPortfolio.of(portfolioImgsFuture.join()));
                        return user;
                    }).join();
        }
    }

    @Transactional
    @CachePut(value = "userDetail", key = "#userId", unless = "#result == null")
    public User updateUser(final Long userId, final UserUpdateDto userUpdateDto) {
        User user = updateUserDetails(UserJpaEntity.toModel(userRepository.findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException(FailureCode.USER_NOT_FOUND))), userUpdateDto);
        CompletableFuture<List<PurposeType>> purposesFuture = CompletableFuture.supplyAsync(
                () -> userUpdateport.updateUserPurposes(userId, userUpdateDto)).thenApplyAsync(purposeTypeList -> {
            user.userPurposes(purposeTypeList);
            return purposeTypeList;
        });
        CompletableFuture<List<TalentType>> talentsFuture = CompletableFuture.supplyAsync(
                () -> userUpdateport.updateUserTalents(userId, userUpdateDto)).thenApplyAsync(talentTypeList -> {
            user.userTalents(talentTypeList);
            return talentTypeList;
        });
        CompletableFuture<UserPortfolio> userPortfolioFuture = CompletableFuture.supplyAsync(
                () -> userUpdateport.updateUserPortfolioImages(userId, userUpdateDto)).thenApplyAsync(userPortfolio -> {
            user.userPortfolio(userPortfolio);
            return userPortfolio;
        });
        CompletableFuture.allOf(purposesFuture, talentsFuture, userPortfolioFuture).join();
        userQueryWithCache.saveExistUser(user);
        user.getUserPortfolio().sort();
        return user;
    }

    private User updateUserDetails(User user, UserUpdateDto userUpdateDto) {
        return  User.builder()
                .id(user.getId())
                .username(userUpdateDto.getUsername())
                .webUrl(userUpdateDto.getWebUrl())
                .email(userUpdateDto.getEmail())
                .username(userUpdateDto.getUsername())
                .description(userUpdateDto.getDescription())
                .instagramId(userUpdateDto.getInstagramId())
                .build();
    }

    @Transactional
    public Boolean updateUserPwd(UserUpdatePwdDto userUpdatePwdDto) {
        Optional<UserJpaEntity> existingUser = userRepository.findByEmail(userUpdatePwdDto.email());
        if (existingUser.isPresent()) {
            return userUpdateport.updateUserPwd(existingUser.get(), userUpdatePwdDto);
        } else {
            return false;
        }
    }

    @Cacheable(value = "user", key = "#email")
    public User findExistUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserJpaEntity::toModel)
                .orElseThrow(() -> new EntityNotFoundException(FailureCode.USER_NOT_FOUND));
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
