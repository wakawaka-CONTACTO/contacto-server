package org.kiru.user.user.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.core.chat.chatroom.domain.ChatRoom;
import org.kiru.core.exception.ContactoException;
import org.kiru.core.exception.EntityNotFoundException;
import org.kiru.core.exception.code.FailureCode;
import org.kiru.core.user.talent.entity.UserTalent;
import org.kiru.core.user.user.domain.User;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.kiru.core.user.userPortfolioImg.domain.UserPortfolio;
import org.kiru.core.user.userPortfolioImg.entity.UserPortfolioImg;
import org.kiru.core.user.userPurpose.domain.PurposeType;
import org.kiru.core.user.userPurpose.entity.UserPurpose;
import org.kiru.user.portfolio.repository.UserPortfolioRepository;
import org.kiru.user.user.api.ChatApiClient;
import org.kiru.user.user.dto.UserIdUsername;
import org.kiru.user.user.dto.request.UserUpdateDto;
import org.kiru.user.user.dto.request.UserUpdatePwdDto;
import org.kiru.user.user.repository.UserPurposeRepository;
import org.kiru.user.user.repository.UserRepository;
import org.kiru.user.user.repository.UserTalentRepository;
import org.kiru.user.user.service.in.GetUserMainPageUseCase;
import org.kiru.user.user.service.out.UserQueryWithCache;
import org.kiru.user.user.service.out.UserUpdateUseCase;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements GetUserMainPageUseCase {
    private final UserQueryWithCache userQueryWithCache;
    private final UserPurposeRepository userPurposeRepository;
    private final UserTalentRepository userTalentRepository;
    private final UserRepository userRepository;
    private final UserPortfolioRepository userPortfolioRepository;
    private final UserUpdateUseCase userUpdateUseCase;
    private final ChatApiClient chatApiClient;

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
            CompletableFuture<List<Long>> allParticipantIdsFuture = chatRoomsFuture.thenApplyAsync(
                    this::getAllParticipantIds, executor);
            CompletableFuture<Map<Long, UserPortfolioImg>> userPortfolioImgMapFuture = allParticipantIdsFuture.thenApplyAsync(
                    this::getUserPortfolioImgMap,
                    executor);
            CompletableFuture<Map<Long, String>> userIdToUsernameMapFuture = allParticipantIdsFuture.thenApplyAsync(
                    this::getUserIdToUsernameMap,
                    executor);
            return chatRoomsFuture.thenCombineAsync(userPortfolioImgMapFuture, (chatRooms, userPortfolioImgMap) -> {
                Map<Long, String> userIdToUsernameMap = userIdToUsernameMapFuture.join();
                for (ChatRoom chatRoom : chatRooms) {
                    setChatRoomThumbnail(chatRoom, userPortfolioImgMap);
                    setChatRoomTitle(chatRoom, userId, userIdToUsernameMap);
                }
                return chatRooms;
            }, executor).join();
        }
    }

    public ChatRoom getUserChatRoom(Long roomId, Long userId) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CompletableFuture<ChatRoom> chatRoomFuture = CompletableFuture.supplyAsync(
                    () -> chatApiClient.getRoom(roomId, userId), executor);
            CompletableFuture<List<Long>> participantIdsFuture = chatRoomFuture.thenApplyAsync(
                    chatRoom -> Objects.requireNonNull(chatRoom.getParticipants()).stream()
                            .filter(participantId -> !participantId.equals(userId)).toList(), executor);
            CompletableFuture<Map<Long, UserPortfolioImg>> userPortfolioImgMapFuture = participantIdsFuture.thenApplyAsync(
                    this::getUserPortfolioImgMap,
                    executor);
            CompletableFuture<Map<Long, String>> userIdToUsernameMapFuture = participantIdsFuture.thenApplyAsync(
                    this::getUserIdToUsernameMap,
                    executor);
            return chatRoomFuture.thenCombineAsync(userPortfolioImgMapFuture, (chatRoom, userPortfolioImgMap) -> {
                Map<Long, String> userIdToUsernameMap = userIdToUsernameMapFuture.join();
                setChatRoomThumbnail(chatRoom, userPortfolioImgMap);
                setChatRoomTitle(chatRoom, userId, userIdToUsernameMap);
                return chatRoom;
            }, executor).join();
        }
    }

    public User getUserDetails(User user) {
        Long userId = user.getId();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CompletableFuture<List<PurposeType>> purposesFuture = CompletableFuture.supplyAsync(
                    () -> getUserPurposes(userId), executor);
            CompletableFuture<List<UserTalent>> talentsFuture = CompletableFuture.supplyAsync(
                    () -> userTalentRepository.findAllByUserId(userId),
                    executor);
            CompletableFuture<UserPortfolio> portfolioImgsFuture = CompletableFuture.supplyAsync(
                    () -> getUserPortfolioByUserId(userId), executor);
            return CompletableFuture.allOf(purposesFuture, talentsFuture, portfolioImgsFuture)
                    .thenApply(v -> {
                        user.userPurposes(purposesFuture.join());
                        user.userTalents(talentsFuture.join());
                        user.userPortfolio(portfolioImgsFuture.join());
                        return user;
                    }).join();
        }
    }

    private List<PurposeType> getUserPurposes(Long userId) {
        return userPurposeRepository.findAllByUserId(userId).stream()
                .map(UserPurpose::getPurposeType).toList();
    }

    private UserPortfolio getUserPortfolioByUserId(Long userId) {
        List<UserPortfolioImg> userPortfolioImgs = userPortfolioRepository.findAllByUserId(userId);
        return getUserPortfolioByPortfolioImg(userPortfolioImgs);
    }

    private UserPortfolio getUserPortfolioByPortfolioImg(List<UserPortfolioImg> userPortfolioImgs) {
        if (userPortfolioImgs.isEmpty()) {
            return null;
        }
        userPortfolioImgs.sort(Comparator.comparing(UserPortfolioImg::getSequence));
        Long portfolioId = userPortfolioImgs.get(0).getId();
        return UserPortfolio.builder()
                .portfolioId(portfolioId)
                .portfolioImages(userPortfolioImgs.stream().map(UserPortfolioImg::getPortfolioImageUrl).toList())
                .userId(userPortfolioImgs.get(0).getUserId())
                .build();
    }

    @Transactional
    @CachePut(value = "userDetail", key = "#userId", unless = "#result == null")
    public User updateUser(Long userId, UserUpdateDto userUpdateDto) {
        UserJpaEntity existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(FailureCode.USER_NOT_FOUND));
        try {
            updateUserDetails(existingUser, userUpdateDto);
            CompletableFuture<List<UserPurpose>> purposesFuture = CompletableFuture.supplyAsync(
                    () -> userUpdateUseCase.updateUserPurposes(userId, userUpdateDto));
            CompletableFuture<List<UserTalent>> talentsFuture = CompletableFuture.supplyAsync(
                    () -> userUpdateUseCase.updateUserTalents(userId, userUpdateDto));
            CompletableFuture<List<UserPortfolioImg>> portfolioImgsFuture = CompletableFuture.supplyAsync(
                    () -> userUpdateUseCase.updateUserPortfolioImages(userId, userUpdateDto));
            CompletableFuture.allOf(purposesFuture, talentsFuture, portfolioImgsFuture).join();
            User updatedUser = userQueryWithCache.saveUser(existingUser);
            updatedUser.userPurposes(
                    userPurposeRepository.saveAll(purposesFuture.join()).stream().map(UserPurpose::getPurposeType)
                            .toList());
            updatedUser.userTalents(userTalentRepository.saveAll(talentsFuture.join()));
            updatedUser.userPortfolio(
                    getUserPortfolioByPortfolioImg(userPortfolioRepository.saveAll(portfolioImgsFuture.join())));
            return updatedUser;
        } catch (Exception e) {
            throw new ContactoException(FailureCode.USER_UPDATE_FAILED);
        }
    }

    public void updateUserDetails(UserJpaEntity existingUser, UserUpdateDto userUpdateDto) {
        existingUser.updateDetails(
                User.builder()
                        .username(userUpdateDto.getUsername())
                        .description(userUpdateDto.getDescription())
                        .email(userUpdateDto.getEmail())
                        .instagramId(userUpdateDto.getInstagramId())
                        .webUrl(userUpdateDto.getWebUrl())
                        .build()
        );
    }

    private List<Long> getAllParticipantIds(List<ChatRoom> chatRooms) {
        return chatRooms.stream()
                .flatMap(chatRoom -> chatRoom.getParticipants().stream())
                .distinct()
                .toList();
    }

    private Map<Long, UserPortfolioImg> getUserPortfolioImgMap(List<Long> allParticipantIds) {
        List<UserPortfolioImg> userPortfolioImgs = userPortfolioRepository.findAllByUserIdInWithMinSequence(
                allParticipantIds);
        return userPortfolioImgs.stream()
                .collect(Collectors.toMap(
                        UserPortfolioImg::getUserId,
                        img -> img
                ));
    }

    private Map<Long, String> getUserIdToUsernameMap(List<Long> allParticipantIds) {
        List<UserIdUsername> userIdAndUsernames = userRepository.findUsernamesByIds(allParticipantIds);
        return userIdAndUsernames.stream()
                .collect(Collectors.toMap(
                        UserIdUsername::id,
                        UserIdUsername::name
                ));
    }

    private void setChatRoomThumbnail(ChatRoom chatRoom, Map<Long, UserPortfolioImg> userPortfolioImgMap) {
        Optional<UserPortfolioImg> thumbnail = chatRoom.getParticipants().stream()
                .map(userPortfolioImgMap::get)
                .filter(Objects::nonNull)
                .min(Comparator.comparingInt(UserPortfolioImg::getSequence));
        thumbnail.ifPresent(userPortfolioImg -> chatRoom.setChatRoomThumbnail(userPortfolioImg.getPortfolioImageUrl()));
    }

    private void setChatRoomTitle(ChatRoom chatRoom, Long userId, Map<Long, String> userIdToUsernameMap) {
        assert chatRoom.getParticipants() != null;
        List<String> otherUsernames = chatRoom.getParticipants().stream()
                .filter(participantId -> !participantId.equals(userId))
                .map(userIdToUsernameMap::get)
                .filter(Objects::nonNull)
                .toList();
        if (!otherUsernames.isEmpty()) {
            chatRoom.setTitle(otherUsernames.getFirst());
        }
    }

    @Transactional
    public Boolean updateUserPwd(UserUpdatePwdDto userUpdatePwdDto) {
        Optional<UserJpaEntity> existingUser = userRepository.findByEmail(userUpdatePwdDto.email());
        if (existingUser.isPresent()) {
            return userUpdateUseCase.updateUserPwd(existingUser.get(), userUpdatePwdDto);
        } else {
            return false;
        }
    }

    @Cacheable(value = "user", key = "#email")
    public User findExistUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::of)
                .orElseThrow(() -> new EntityNotFoundException(FailureCode.USER_NOT_FOUND));
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}